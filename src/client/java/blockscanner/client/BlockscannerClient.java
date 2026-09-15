package blockscanner.client;

import blockscanner.BlockScanner;
import blockscanner.client.renderer.BlockPositionRenderer;
import blockscanner.client.renderer.ScannerHudElement;
import blockscanner.client.screens.ScannerScreen;
import blockscanner.items.BlockScannerItems;
import blockscanner.items.ScannerItem;
import blockscanner.menus.BlockScannerMenuTypes;
import blockscanner.networking.ActiveChangedPayload;
import blockscanner.networking.HighlightBlocksPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;

public class BlockscannerClient implements ClientModInitializer {
	private final BlockPositionRenderer positionRenderer = new BlockPositionRenderer();
	private static final KeyMapping.Category KEYS_CATEGORY = KeyMapping.Category.register(BlockScanner.id("keys"));
	private static KeyMapping scannerActiveKeyMapping;

	@Override
	public void onInitializeClient() {
		initializeHighlightBlocks();
		initializeKeyMapping();

		MenuScreens.register(BlockScannerMenuTypes.SCANNER, ScannerScreen::new);
		HudElementRegistry.attachElementAfter(VanillaHudElements.BOSS_BAR, BlockScanner.id("hud"), new ScannerHudElement());
	}

	private void initializeKeyMapping() {
		scannerActiveKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.blockscanner.active",
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_K,
				KEYS_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (scannerActiveKeyMapping.consumeClick()) {
				// If the player has a scanner selected, toggle that one...
				var newActive = false;
				var single = false;
				var scannerStack = client.player.getInventory().getSelectedItem();
				if (scannerStack.getItem() instanceof ScannerItem scannerItem) {
					newActive = !scannerItem.getActive(scannerStack);
					scannerItem.setActive(scannerStack, newActive);
					single = true;
				}
				else {
					// ... else toggle the mode for all scanners
					newActive = !ScannerItem.playerHasActiveScanner(client.player);
					var finalNewActive = newActive;
					ScannerItem.visitScanners(client.player, (stack, item, currentActive) -> {
						item.setActive(stack, finalNewActive);
					});
				}

				ClientPlayNetworking.send(new ActiveChangedPayload(newActive, single));
			}
		});
	}

	private void initializeHighlightBlocks() {
		ClientPlayNetworking.registerGlobalReceiver(HighlightBlocksPayload.ID, (payload, context) -> {
			positionRenderer.setPositions(payload.positions());
		});
	}

}