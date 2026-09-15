package blockscanner.client.renderer;

import blockscanner.items.ScannerItem;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;

public class ScannerHudElement implements HudElement {
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        var client = Minecraft.getInstance();
        var player = client.player;

        if (player != null) {
            var stack = ScannerItem.getActiveScanner(player);
            if (!stack.isEmpty()) {
                String name = null;
                var scannedBlockId = stack.get(ScannerItem.BLOCK);
                if (scannedBlockId != null) {
                    var block = BuiltInRegistries.BLOCK.get(scannedBlockId);
                    if (block.isPresent()) {
                        name = block.get().value().getName().getString();
                    }
                }

                graphics.fakeItem(stack, 8, graphics.guiHeight() / 2 - 8);
                graphics.text(client.font, name, 26, graphics.guiHeight() / 2 - client.font.lineHeight + 5, 0xFF03ee41, true);
            }
        }

    }
}
