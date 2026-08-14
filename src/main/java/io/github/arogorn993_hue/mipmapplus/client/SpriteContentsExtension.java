package io.github.arogorn993_hue.mipmapplus.client;

import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;

import java.util.Optional;

public interface SpriteContentsExtension {
    Optional<AnimationMetadataSection> mipMapPlus$getAnimationMetadata();

    Optional<TextureMetadataSection> mipMapPlus$getTextureMetadata();
}
