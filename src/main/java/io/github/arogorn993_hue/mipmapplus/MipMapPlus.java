package io.github.arogorn993_hue.mipmapplus;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Set;

public final class MipMapPlus {
    /** Level 10 supports 1024px source textures; atlas size and VRAM requirements can be extreme. */
    public static final int MAX_MIPMAP_LEVEL = 10;

    /** Only the block atlas uses the extended chain and automatic upscaling. */
    @SuppressWarnings("deprecation")
    public static final Set<Identifier> UPSCALE_WHITELIST = Set.of(TextureAtlas.LOCATION_BLOCKS);

    private MipMapPlus() {
    }

    /**
     * Builds the value-sensitive warning shown by both vanilla and Sodium.
     *
     * <p>The estimate states the atlas-memory formula and common atlas-size examples. Actual
     * resource reloads also use temporary native memory and may contain several atlases.</p>
     */
    public static Component mipmapWarning(int level) {
        int clampedLevel = Math.max(0, Math.min(level, MAX_MIPMAP_LEVEL));
        long textureWidth = 1L << clampedLevel;
        if (clampedLevel >= 10) {
            return Component.translatable(
                "mipmapplus.options.mipmap_levels.tooltip.extreme",
                clampedLevel,
                textureWidth
            );
        }

        if (clampedLevel >= 8) {
            return Component.translatable(
                "mipmapplus.options.mipmap_levels.tooltip.high",
                clampedLevel,
                textureWidth
            );
        }

        return Component.translatable(
            "mipmapplus.options.mipmap_levels.tooltip.normal",
            clampedLevel,
            textureWidth
        );
    }
}
