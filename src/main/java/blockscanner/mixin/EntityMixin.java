package blockscanner.mixin;

import blockscanner.BlockScanTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/world/entity/Entity;setPosRaw(DDD)V")
    public void setPosRaw(final double x, final double y, final double z, CallbackInfo info) {
        if ((Object)this instanceof ServerPlayer player) {
            BlockScanTracker.TRACKER.updatePlayerHighlight(player);
        }
    }
}
