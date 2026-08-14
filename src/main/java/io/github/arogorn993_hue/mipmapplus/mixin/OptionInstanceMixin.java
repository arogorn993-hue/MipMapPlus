package io.github.arogorn993_hue.mipmapplus.mixin;

import io.github.arogorn993_hue.mipmapplus.MipMapPlus;
import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin {
    @Mutable
    @Shadow
    @Final
    private OptionInstance.ValueSet<?> values;

    @Mutable
    @Shadow
    @Final
    private Codec<?> codec;

    @Mutable
    @Shadow
    @Final
    private OptionInstance.TooltipSupplier<?> tooltip;

    @Inject(
        method = "<init>(Ljava/lang/String;Lnet/minecraft/client/OptionInstance$TooltipSupplier;Lnet/minecraft/client/OptionInstance$CaptionBasedToString;Lnet/minecraft/client/OptionInstance$ValueSet;Lcom/mojang/serialization/Codec;Ljava/lang/Object;Lnet/minecraft/client/OptionInstance$ValueUpdateListener;)V",
        at = @At("RETURN")
    )
    private void mipMapPlus$extendMipmapRange(
        String key,
        OptionInstance.TooltipSupplier<?> tooltipSupplier,
        OptionInstance.CaptionBasedToString<?> captionBasedToString,
        OptionInstance.ValueSet<?> valueSet,
        Codec<?> originalCodec,
        Object initialValue,
        OptionInstance.ValueUpdateListener<?> valueUpdateListener,
        CallbackInfo ci
    ) {
        if ("options.mipmapLevels".equals(key)) {
            OptionInstance.IntRange range = new OptionInstance.IntRange(0, MipMapPlus.MAX_MIPMAP_LEVEL);
            this.values = range;
            this.codec = range.codec();

            OptionInstance.TooltipSupplier<Integer> mipmapTooltip =
                level -> Tooltip.create(MipMapPlus.mipmapWarning(level));
            this.tooltip = mipmapTooltip;
        }
    }
}
