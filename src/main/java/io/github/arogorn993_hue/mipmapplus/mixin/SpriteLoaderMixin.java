package io.github.arogorn993_hue.mipmapplus.mixin;

import io.github.arogorn993_hue.mipmapplus.MipMapPlus;
import io.github.arogorn993_hue.mipmapplus.client.SpriteContentsExtension;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin {
    @Unique
    private static final Logger MIPMAPPLUS_LOGGER = LogUtils.getLogger();

    @Unique
    private static final SystemToast.SystemToastId MIPMAPPLUS_FALLBACK_TOAST =
        new SystemToast.SystemToastId();

    @Unique
    private int mipMapPlus$requestedLevel;

    @Unique
    private int mipMapPlus$effectiveLevel;

    @Unique
    private boolean mipMapPlus$fallbackPending;

    @Shadow
    @Final
    private Identifier location;

    @Shadow
    @Final
    private int maxSupportedTextureSize;

    /**
     * Chooses a stitchable mip level before any replacement NativeImages are allocated.
     * The dry run uses the exact normalized sprite dimensions, Minecraft's stitcher,
     * the active GPU texture-size limit, and the same padding policy as the real run.
     */
    @ModifyArgs(
        method = "lambda$loadAndStitch$2",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/SpriteLoader;stitch(Ljava/util/List;ILjava/util/concurrent/Executor;)Lnet/minecraft/client/renderer/texture/SpriteLoader$Preparations;"
        )
    )
    private void mipMapPlus$prepareBlockSprites(Args args) {
        List<SpriteContents> sprites = args.get(0);
        int requestedMipLevel = args.get(1);

        this.mipMapPlus$requestedLevel = requestedMipLevel;
        this.mipMapPlus$effectiveLevel = requestedMipLevel;
        this.mipMapPlus$fallbackPending = false;

        if (!MipMapPlus.UPSCALE_WHITELIST.contains(this.location) || requestedMipLevel <= 4) {
            return;
        }

        int anisotropyBit = mipMapPlus$anisotropyBit();
        int effectiveLevel = 4;
        for (int candidate = requestedMipLevel; candidate >= 4; candidate--) {
            if (mipMapPlus$canStitch(sprites, candidate, anisotropyBit)) {
                effectiveLevel = candidate;
                break;
            }
        }

        this.mipMapPlus$effectiveLevel = effectiveLevel;
        this.mipMapPlus$fallbackPending = effectiveLevel < requestedMipLevel;
        if (this.mipMapPlus$fallbackPending) {
            MIPMAPPLUS_LOGGER.warn(
                "MipMapPlus: block atlas cannot fit requested mip level {}; using level {}",
                requestedMipLevel,
                effectiveLevel
            );
        }

        args.set(0, mipMapPlus$upscaleSmallBlockSprites(sprites, effectiveLevel));
        args.set(1, effectiveLevel);
    }

    @Unique
    private List<SpriteContents> mipMapPlus$upscaleSmallBlockSprites(
        List<SpriteContents> sprites,
        int effectiveMipLevel
    ) {
        int targetFrameSize = 1 << effectiveMipLevel;
        int spriteCount = sprites.size();
        List<SpriteContents> result = new ArrayList<>(spriteCount);
        List<SpriteContents> replacedOriginals = new ArrayList<>(spriteCount);
        List<SpriteContents> generatedReplacements = new ArrayList<>(spriteCount);
        List<SpriteContents> committedResult;
        int upscaledCount = 0;

        try {
            for (SpriteContents sprite : sprites) {
                int factor = mipMapPlus$scaleFactor(sprite, targetFrameSize);
                if (factor == 0) {
                    result.add(sprite);
                } else {
                    SpriteContents replacement = mipMapPlus$upscale(sprite, factor);
                    // Track ownership immediately so every later failure can close it.
                    generatedReplacements.add(replacement);
                    result.add(replacement);
                    replacedOriginals.add(sprite);
                    upscaledCount++;
                }
            }
            committedResult = List.copyOf(result);
        } catch (RuntimeException | Error failure) {
            mipMapPlus$closeAfterFailedReplacement(generatedReplacements, failure);
            throw failure;
        }

        // Commit the replacement only after every allocation and SpriteContents construction
        // succeeds, so a failed high-memory reload leaves the original list usable.
        for (SpriteContents original : replacedOriginals) {
            original.close();
        }

        MIPMAPPLUS_LOGGER.info(
            "MipMapPlus: prepared {} block sprites for mip level {} (upscaled {} undersized sprites to at least {}px per frame)",
            sprites.size(), effectiveMipLevel, upscaledCount, targetFrameSize
        );
        return committedResult;
    }

    @Unique
    private static void mipMapPlus$closeAfterFailedReplacement(
        List<SpriteContents> replacements,
        Throwable failure
    ) {
        for (SpriteContents replacement : replacements) {
            try {
                replacement.close();
            } catch (RuntimeException | Error closeFailure) {
                failure.addSuppressed(closeFailure);
            }
        }
    }

    /** Shows one friendly toast only after the fallback atlas and all of its mip images are ready. */
    @Inject(method = "stitch", at = @At("RETURN"))
    private void mipMapPlus$notifySuccessfulFallback(
        List<SpriteContents> sprites,
        int requestedMipLevel,
        Executor executor,
        CallbackInfoReturnable<SpriteLoader.Preparations> cir
    ) {
        if (!this.mipMapPlus$fallbackPending || !MipMapPlus.UPSCALE_WHITELIST.contains(this.location)) {
            return;
        }

        int requested = this.mipMapPlus$requestedLevel;
        int effective = this.mipMapPlus$effectiveLevel;
        this.mipMapPlus$fallbackPending = false;

        cir.getReturnValue().readyForUpload().thenRun(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> SystemToast.addOrUpdate(
                minecraft.gui.toastManager(),
                MIPMAPPLUS_FALLBACK_TOAST,
                Component.translatable("mipmapplus.toast.fallback.title"),
                Component.translatable("mipmapplus.toast.fallback.message", requested, effective)
            ));
        }).exceptionally(error -> {
            MIPMAPPLUS_LOGGER.debug("MipMapPlus fallback atlas did not finish preparing; toast suppressed", error);
            return null;
        });
    }

    @Unique
    private boolean mipMapPlus$canStitch(List<SpriteContents> sprites, int mipLevel, int anisotropyBit) {
        try {
            int targetFrameSize = 1 << mipLevel;
            Stitcher<Stitcher.Entry> stitcher = new Stitcher<>(
                this.maxSupportedTextureSize,
                this.maxSupportedTextureSize,
                mipLevel,
                anisotropyBit
            );

            for (SpriteContents sprite : sprites) {
                int factor = mipMapPlus$scaleFactor(sprite, targetFrameSize);
                int scale = 1 << factor;
                stitcher.registerSprite(new MipMapPlus$ScaledEntry(
                    sprite.name(),
                    Math.multiplyExact(sprite.width(), scale),
                    Math.multiplyExact(sprite.height(), scale)
                ));
            }

            stitcher.stitch();
            return true;
        } catch (RuntimeException failure) {
            return false;
        }
    }

    @Unique
    private static int mipMapPlus$anisotropyBit() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.options.textureFiltering().get() == net.minecraft.client.TextureFilteringMethod.ANISOTROPIC
            ? minecraft.options.maxAnisotropyBit().get()
            : 0;
    }

    @Unique
    private record MipMapPlus$ScaledEntry(Identifier name, int width, int height) implements Stitcher.Entry {
    }

    @Unique
    private static int mipMapPlus$scaleFactor(SpriteContents sprite, int targetFrameSize) {
        int smallestDimension = Math.min(sprite.width(), sprite.height());
        int smallestPowerOfTwo = Math.min(
            Integer.lowestOneBit(sprite.width()),
            Integer.lowestOneBit(sprite.height())
        );
        int factor = 0;
        while ((smallestDimension << factor) < targetFrameSize
            || (smallestPowerOfTwo << factor) < targetFrameSize) {
            factor++;
        }
        return factor;
    }

    @Unique
    private static SpriteContents mipMapPlus$upscale(SpriteContents original, int factor) {
        NativeImage input = ((SpriteContentsAccessor) original).mipMapPlus$getOriginalImage();
        int scale = 1 << factor;
        NativeImage output = new NativeImage(
            input.format(),
            input.getWidth() * scale,
            input.getHeight() * scale,
            false
        );

        try {
            mipMapPlus$nearestNeighborScale(input, output, scale);

            SpriteContentsExtension metadata = (SpriteContentsExtension) original;
            return new SpriteContents(
                original.name(),
                new FrameSize(original.width() * scale, original.height() * scale),
                output,
                metadata.mipMapPlus$getAnimationMetadata(),
                ((SpriteContentsAccessor) original).mipMapPlus$getAdditionalMetadata(),
                metadata.mipMapPlus$getTextureMetadata()
            );
        } catch (RuntimeException | Error failure) {
            try {
                output.close();
            } catch (RuntimeException | Error closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }

    /** Byte-exact nearest-neighbor expansion with bulk row writes. */
    @Unique
    private static void mipMapPlus$nearestNeighborScale(NativeImage input, NativeImage output, int scale) {
        int components = input.format().components();
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int outputWidth = output.getWidth();
        ByteBuffer source = input.getPixelBytes();
        ByteBuffer destination = output.getPixelBytes();
        byte[] expandedRow = new byte[Math.multiplyExact(outputWidth, components)];

        for (int sourceY = 0; sourceY < inputHeight; sourceY++) {
            int rowOffset = sourceY * inputWidth * components;
            int expandedOffset = 0;
            for (int sourceX = 0; sourceX < inputWidth; sourceX++) {
                int pixelOffset = rowOffset + sourceX * components;
                for (int repeatX = 0; repeatX < scale; repeatX++) {
                    for (int component = 0; component < components; component++) {
                        expandedRow[expandedOffset++] = source.get(pixelOffset + component);
                    }
                }
            }

            for (int repeatY = 0; repeatY < scale; repeatY++) {
                int outputY = sourceY * scale + repeatY;
                destination.put(outputY * expandedRow.length, expandedRow, 0, expandedRow.length);
            }
        }
    }
}
