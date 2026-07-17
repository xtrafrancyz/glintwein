package net.glintwein.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.glintwein.impl.TooltipInterceptor;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.util.GMath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public class MixinScreen {
    @WrapOperation(
        method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/util/FormattedCharSequence;)I"
        )
    )
    private int renderTooltipComputeWidth(Font font, FormattedCharSequence seq, Operation<Integer> original) {
        Element el = TooltipInterceptor.parseElement(seq);
        if (el != null) {
            return GMath.ceil(el.getComputedWidth());
        }
        return original.call(font, seq);
    }

    @Inject(
        method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/Screen;width:I",
            opcode = Opcodes.GETFIELD,
            shift = At.Shift.BY,
            by = -2
        )
    )
    private void renderTooltipCorrectHeight(
        PoseStack pose, List<? extends FormattedCharSequence> tooltips,
        int mouseX, int mouseY,
        CallbackInfo ci,
        @Local(ordinal = 6) LocalIntRef height
    ) {
        int y = 0;
        for (FormattedCharSequence line : tooltips) {
            int lineHeight = 10;
            if (line != null) {
                Element el = TooltipInterceptor.getCachedElement(line);
                if (el != null) {
                    lineHeight = GMath.ceil(el.getComputedHeight());
                    height.set(height.get() - 10 + lineHeight);
                    TooltipInterceptor.setElementY(el, y);
                }
            }
            if (y == 0)
                y += 2;
            y += lineHeight;
        }
    }

    @Inject(
        method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V")
    )
    private void renderTooltipCapturePosition(
        PoseStack pose, List<? extends FormattedCharSequence> tooltips,
        int mouseX, int mouseY,
        CallbackInfo ci,
        @Local(ordinal = 3) int x, @Local(ordinal = 4) int y
    ) {
        TooltipInterceptor.setTooltipBase(x, y);
    }

    @Inject(
        method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;get(I)Ljava/lang/Object;",
            shift = At.Shift.BY,
            by = 3
        )
    )
    private void renderTooltipInterceptDraw(
        PoseStack pose, List<? extends FormattedCharSequence> tooltips,
        int mouseX, int mouseY,
        CallbackInfo ci,
        @Local LocalRef<FormattedCharSequence> line, @Local(ordinal = 4) LocalIntRef y
    ) {
        FormattedCharSequence seq = line.get();
        if (seq == null)
            return;
        Element el = TooltipInterceptor.getCachedElement(seq);
        if (el != null) {
            y.set(y.get() - 10 + GMath.ceil(el.getComputedHeight()));
            line.set(null);
        }
    }
}
