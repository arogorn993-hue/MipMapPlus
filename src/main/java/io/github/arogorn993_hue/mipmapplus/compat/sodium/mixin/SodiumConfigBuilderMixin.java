package io.github.arogorn993_hue.mipmapplus.compat.sodium.mixin;

import io.github.arogorn993_hue.mipmapplus.MipMapPlus;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder", remap = false)
public abstract class SodiumConfigBuilderMixin {
    /** Sodium 0.9.1's fourth integer range on the quality page is mipmaps. */
    @ModifyArg(
        method = "buildQualityPage(Lnet/caffeinemc/mods/sodium/api/config/structure/ConfigBuilder;)Lnet/caffeinemc/mods/sodium/api/config/structure/OptionPageBuilder;",
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=sodium:quality.mipmap_levels"),
            to = @At(value = "CONSTANT", args = "stringValue=sodium:quality.filtering_mode")
        ),
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/api/config/structure/IntegerOptionBuilder;setRange(III)Lnet/caffeinemc/mods/sodium/api/config/structure/IntegerOptionBuilder;",
            remap = false
        ),
        index = 1,
        remap = false
    )
    private int mipMapPlus$extendSodiumMipmapRange(int originalMaximum) {
        return MipMapPlus.MAX_MIPMAP_LEVEL;
    }

    @Redirect(
        method = "buildQualityPage(Lnet/caffeinemc/mods/sodium/api/config/structure/ConfigBuilder;)Lnet/caffeinemc/mods/sodium/api/config/structure/OptionPageBuilder;",
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=sodium:quality.mipmap_levels"),
            to = @At(value = "CONSTANT", args = "stringValue=sodium:quality.filtering_mode")
        ),
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/api/config/structure/IntegerOptionBuilder;setTooltip(Lnet/minecraft/network/chat/Component;)Lnet/caffeinemc/mods/sodium/api/config/structure/IntegerOptionBuilder;",
            remap = false
        ),
        remap = false
    )
    private IntegerOptionBuilder mipMapPlus$replaceSodiumMipmapTooltip(
        IntegerOptionBuilder builder,
        Component ignoredTooltip
    ) {
        return builder.setTooltip(MipMapPlus::mipmapWarning);
    }
}
