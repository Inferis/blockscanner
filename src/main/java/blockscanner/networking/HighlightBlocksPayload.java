package blockscanner.networking;

import blockscanner.BlockScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record HighlightBlocksPayload(List<BlockPos> positions) implements CustomPacketPayload {
    public static final Type<HighlightBlocksPayload> ID = new CustomPacketPayload.Type<>(Identifier.parse(BlockScanner.MOD_ID + ":highlight_blocks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HighlightBlocksPayload> CODEC = new StreamCodec<>() {
        @Override
        public HighlightBlocksPayload decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            var positions = new ArrayList<BlockPos>(size);
            for (int i = 0; i < size; i++) {
                positions.add(buf.readBlockPos());
            }
            return new HighlightBlocksPayload(positions);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, HighlightBlocksPayload value) {
            buf.writeVarInt(value.positions().size());
            for (var pos : value.positions()) {
                buf.writeBlockPos(pos);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
