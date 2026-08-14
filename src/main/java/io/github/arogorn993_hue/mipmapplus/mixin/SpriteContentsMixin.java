package io.github.arogorn993_hue.mipmapplus.mixin;

import io.github.arogorn993_hue.mipmapplus.client.SpriteContentsExtension;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin implements SpriteContentsExtension {
    @Unique
    private Optional<AnimationMetadataSection> mipMapPlus$animationMetadata = Optional.empty();

    @Unique
    private Optional<TextureMetadataSection> mipMapPlus$textureMetadata = Optional.empty();

    @Inject(
        method = "<init>(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/metadata/animation/FrameSize;Lcom/mojang/blaze3d/platform/NativeImage;Ljava/util/Optional;Ljava/util/List;Ljava/util/Optional;)V",
        at = @At("TAIL")
    )
    private void mipMapPlus$saveMetadata(
        Identifier identifier,
        FrameSize frameSize,
        NativeImage image,
        Optional<AnimationMetadataSection> animationMetadata,
        List<MetadataSectionType.WithValue<?>> additionalMetadata,
        Optional<TextureMetadataSection> textureMetadata,
        CallbackInfo ci
    ) {
        this.mipMapPlus$animationMetadata = animationMetadata;
        this.mipMapPlus$textureMetadata = textureMetadata;
    }

    @Override
    public Optional<AnimationMetadataSection> mipMapPlus$getAnimationMetadata() {
        return this.mipMapPlus$animationMetadata;
    }

    @Override
    public Optional<TextureMetadataSection> mipMapPlus$getTextureMetadata() {
        return this.mipMapPlus$textureMetadata;
    }
}
