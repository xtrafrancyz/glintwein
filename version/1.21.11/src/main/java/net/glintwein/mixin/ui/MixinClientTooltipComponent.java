package net.glintwein.mixin.ui;

import net.glintwein.impl.GlintTooltipComponent;
import net.glintwein.impl.MCCharSeqReader;
import net.glintwein.ui.MinecraftTooltipRenderer;
import net.glintwein.ui.element.Element;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface MixinClientTooltipComponent {
    @Inject(
        method = "create(Lnet/minecraft/util/FormattedCharSequence;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void create(FormattedCharSequence sequence, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        Element element = MinecraftTooltipRenderer.parseElement(new MCCharSeqReader(sequence));
        if (element != null)
            cir.setReturnValue(new GlintTooltipComponent(element));
    }
}
