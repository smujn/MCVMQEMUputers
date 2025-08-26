package mcvmcomputers.mixins;

import static mcvmcomputers.client.ClientMod.*;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mcvmcomputers.client.ClientMod;
import mcvmcomputers.client.gui.setup.pages.SetupPageUnfocusBinding;
import mcvmcomputers.client.utils.KeyConverter;
import mcvmcomputers.client.utils.QemuKey;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_LOCK_KEY_MODS, GLFW.GLFW_TRUE);
    }

    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKey(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        executor.submit(() -> {
            MinecraftClient mcc = MinecraftClient.getInstance();
            if (window != mcc.getWindow().getHandle()) return;

            boolean shiftPressed = (mods & GLFW.GLFW_MOD_SHIFT) != 0;

            if ((ClientMod.qemu ? ClientMod.isQemuRunning() : ClientMod.vmTurnedOn)) {
                if (isAllowedToRecordKeys) {
                    if (ClientMod.qemu) {
                        synchronized (ClientMod.qemuKeys) {
                            ClientMod.qemuKeys.add(QemuKey.keyboard(
                                KeyConverter.keySymFromGLFW(key, shiftPressed),
                                action == GLFW.GLFW_PRESS
                            ));
                        }
                    } else if (action != GLFW.GLFW_REPEAT) {
                        ClientMod.vmKeyboardScancodes.addAll(KeyConverter.toVBKey(key, action));
                    }
                }
            } else if (SetupPageUnfocusBinding.changeBinding && action == GLFW.GLFW_PRESS) {
                if (getKeyName(key) != null) {
                    switch (SetupPageUnfocusBinding.bindingToBeChangedNum) {
                        case 1:
                            glfwUnfocusKey1 = key;
                            break;
                        case 2:
                            glfwUnfocusKey2 = key;
                            break;
                        case 3:
                            glfwUnfocusKey3 = key;
                            break;
                        case 4:
                            glfwUnfocusKey4 = key;
                            break;
                    }
                    SetupPageUnfocusBinding.bindingJustChanged = true;
                    SetupPageUnfocusBinding.changeBinding = false;
                }
            }
        });
    }
}