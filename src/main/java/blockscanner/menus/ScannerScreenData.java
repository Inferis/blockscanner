package blockscanner.menus;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ScannerScreenData(ItemStack stack) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ScannerScreenData> CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ScannerScreenData::stack,
            ScannerScreenData::new);
}
