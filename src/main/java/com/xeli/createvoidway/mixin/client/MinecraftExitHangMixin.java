package com.xeli.createvoidway.mixin.client;

import com.xeli.createvoidway.client.ExitHangGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(Minecraft.class)
public abstract class MinecraftExitHangMixin {

	@Shadow
	@Nullable
	private IntegratedServer singleplayerServer;

	@Inject(method = "stop", at = @At("HEAD"))
	private void createvoidway$haltServerOnStop(CallbackInfo ci) {
		beginShutdown();
	}

	@Inject(method = "destroy", at = @At("HEAD"))
	private void createvoidway$haltServerOnDestroy(CallbackInfo ci) {
		beginShutdown();
	}

    @ModifyExpressionValue(
        method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/server/IntegratedServer;isShutdown()Z"
        )
    )
    private boolean createvoidway$breakWaitForServer(boolean original) {
        return ExitHangGuard.isShuttingDown() || original;
    }

	private void beginShutdown() {
		ExitHangGuard.markShuttingDown();
		// Never call halt(true) on the render thread — it join()s the server while it saves worlds.
		IntegratedServer server = singleplayerServer;
		if (server != null && server.isRunning()) {
			Thread haltThread = new Thread(() -> server.halt(true), "createvoidway-server-halt");
			haltThread.setDaemon(true);
			haltThread.start();
		}
	}

}
