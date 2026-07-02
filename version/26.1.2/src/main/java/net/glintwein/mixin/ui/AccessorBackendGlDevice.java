package net.glintwein.mixin.ui;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com/mojang/blaze3d/opengl/GlDevice")
public interface AccessorBackendGlDevice {
    @Accessor("directStateAccess")
    DirectStateAccess getDirectStateAccess();
}
