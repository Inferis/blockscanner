package blockscanner.networking;

import blockscanner.BlockScanTracker;
import blockscanner.BlockScanner;
import blockscanner.items.ScannerItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ActiveChangedPayload(boolean isActive, boolean isSingle) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ActiveChangedPayload> ID = new CustomPacketPayload.Type<>(Identifier.parse(BlockScanner.MOD_ID + ":active_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActiveChangedPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ActiveChangedPayload::isActive,
            ByteBufCodecs.BOOL, ActiveChangedPayload::isSingle,
            ActiveChangedPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void serverHandler(ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ScannerItem.visitScanners(context.player(), (stack, item, currentMode) -> {
                item.setActive(stack, isActive());
                BlockScanTracker.TRACKER.resetPlayerHighlight(context.player());
            });

            MutableComponent message;
            if (isSingle()) {
                message = Component.translatable("message.blockscanner.scanner.single");
            }
            else {
                message = Component.translatable("message.blockscanner.scanner.multi");
            }
            message.append(" ");
            if (isActive()) {
                message.append(Component.translatable("message.blockscanner.scanner.activated").withStyle(ChatFormatting.GREEN));
            }
            else {
                message.append(Component.translatable("message.blockscanner.scanner.deactivated").withStyle(ChatFormatting.RED));
            }

            context.player().sendSystemMessage(message, true);
        });
    }
}