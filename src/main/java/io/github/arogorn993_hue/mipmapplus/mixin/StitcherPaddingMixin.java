package io.github.arogorn993_hue.mipmapplus.mixin;

import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Stitcher.class)
public abstract class StitcherPaddingMixin {
    private static final int MIPMAPPLUS_MAX_PADDING = 128;

    @Shadow
    @Final
    private int mipLevel;

    @Mutable
    @Shadow
    @Final
    private int padding;

    /**
     * Minecraft 26.2 makes padding grow as 2^mipLevel (and again with AF).
     * At level 9 that reserves at least a 512px border around every 512px
     * sprite, so the atlas cannot possibly fit. Capping that growth gives an
     * extended atlas a chance to fit; SpriteLoaderMixin preflights the exact
     * padded geometry and safely lowers the requested level when it cannot.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void mipMapPlus$allowExtendedAtlas(
        int maxWidth,
        int maxHeight,
        int mipLevel,
        int anisotropyBit,
        CallbackInfo ci
    ) {
        if (mipLevel > 4) {
            this.padding = Math.min(this.padding, MIPMAPPLUS_MAX_PADDING);
        }
    }

    /**
     * Vanilla rounds (512 + 2*padding) to the next multiple of 512. With any
     * gutter at level 9 that turns every 512px sprite into a wasteful 1024px
     * cell. Keeping the real padded dimensions still separates every sprite;
     * their non-overlapping 512px content intervals remain non-overlapping at
     * every shifted mip coordinate.
     */
    @Redirect(
        method = "registerSprite",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/Stitcher;smallestFittingMinTexel(II)I"
        )
    )
    private int mipMapPlus$avoidWastefulExtendedRounding(int input, int maximumMipLevel) {
        if (this.mipLevel > 4) {
            return input;
        }

        int quantum = 1 << maximumMipLevel;
        return ((input + quantum - 1) / quantum) * quantum;
    }
}
