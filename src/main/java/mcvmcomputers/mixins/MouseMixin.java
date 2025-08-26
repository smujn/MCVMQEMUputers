package mcvmcomputers.mixins;

import mcvmcomputers.entities.EntityMouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow private double eventDeltaWheel;
    @Shadow private boolean leftButtonClicked;
    @Shadow private boolean middleButtonClicked;
    @Shadow private boolean rightButtonClicked;

    @Inject(method = "onMouseScroll", at = @At("TAIL"))
    private void onMouseScroll(CallbackInfo ci) {
        // Optional scroll tracking
    }

    @Inject(method = "onMouseButton", at = @At("TAIL"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (client.crosshairTarget instanceof EntityHitResult) {
                EntityHitResult entityHit = (EntityHitResult) client.crosshairTarget;
                Entity entity = entityHit.getEntity();
                if (entity instanceof EntityMouse) {
                    EntityMouse mouse = (EntityMouse) entity;
                    mouse.onRightClick();
                }
            }
        }
    }
}