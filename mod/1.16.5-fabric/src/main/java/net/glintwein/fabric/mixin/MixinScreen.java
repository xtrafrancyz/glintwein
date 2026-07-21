package net.glintwein.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.glintwein.fabric.impl.MCCharSeqReader;
import net.glintwein.ui.MinecraftTooltipRenderer;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(Screen.class)
public class MixinScreen {
    @WrapOperation(
        method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/util/FormattedCharSequence;)I"
        )
    )
    private int renderTooltipComputeWidth(
        Font font, FormattedCharSequence seq, Operation<Integer> original,
        @Share("elementCache") LocalRef<Map<FormattedCharSequence, Element>> elementCache
    ) {
        if (!MinecraftTooltipRenderer.enabled())
            return original.call(font, seq);
        Element el = MinecraftTooltipRenderer.parseElement(new MCCharSeqReader(seq));
        if (el != null) {
            Map<FormattedCharSequence, Element> cache = elementCache.get();
            if (cache == null)
                elementCache.set(cache = new HashMap<>());
            cache.put(seq, el);
            return GMath.ceil(el.getComputedWidth());
        }
        return original.call(font, seq);
    }

    @Inject(
        method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/Screen;width:I",
            opcode = Opcodes.GETFIELD
        )
    )
    private void renderTooltipCorrectHeight(
        PoseStack pose, List<? extends FormattedCharSequence> tooltips,
        int mouseX, int mouseY,
        CallbackInfo ci,
        @Local(ordinal = 6) LocalIntRef height,
        @Share("elementCache") LocalRef<Map<FormattedCharSequence, Element>> elementCache
    ) {
        Map<FormattedCharSequence, Element> cache = elementCache.get();
        if (cache == null)
            return;
        int y = 0;
        for (FormattedCharSequence line : tooltips) {
            int lineHeight = 10;
            if (line != null) {
                Element el = cache.get(line);
                if (el != null) {
                    lineHeight = GMath.ceil(el.getComputedHeight());
                    height.set(height.get() - 10 + lineHeight);
                }
            }
            if (y == 0)
                y += 2;
            y += lineHeight;
        }
    }

    @ModifyExpressionValue(
        method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;get(I)Ljava/lang/Object;"
        )
    )
    private Object renderTooltipInterceptDraw(
        Object original,
        @Local(ordinal = 3) int x, @Local(ordinal = 4) LocalIntRef y,
        @Share("elementCache") LocalRef<Map<FormattedCharSequence, Element>> elementCache
    ) {
        FormattedCharSequence seq = (FormattedCharSequence) original;
        if (seq == null)
            return null;
        Map<FormattedCharSequence, Element> cache = elementCache.get();
        if (cache == null)
            return seq;
        Element el = cache.get(seq);
        if (el != null) {
            MinecraftTooltipRenderer.setElementPos(el, x, y.get());
            y.set(y.get() - 10 + GMath.ceil(el.getComputedHeight()));
            return null;
        }
        return seq;
    }
}
