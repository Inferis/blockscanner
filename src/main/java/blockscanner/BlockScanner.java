package blockscanner;

import blockscanner.items.BlockScannerItems;
import blockscanner.menus.BlockScannerMenuTypes;
import blockscanner.networking.ActiveChangedPayload;
import blockscanner.networking.HighlightBlocksPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockScanner implements ModInitializer {
	public static final String MOD_ID = "blockscanner";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		BlockScannerItems.registerItems();
		BlockScannerMenuTypes.registerMenuTypes();

		PayloadTypeRegistry.clientboundPlay().register(HighlightBlocksPayload.ID, HighlightBlocksPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ActiveChangedPayload.ID, ActiveChangedPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ActiveChangedPayload.ID, ActiveChangedPayload::serverHandler);

		PlayerBlockBreakEvents.AFTER.register(Event.DEFAULT_PHASE, (level, player, pos, state, blockEntity) -> {
			if (player instanceof ServerPlayer serverPlayer) {
				BlockScanTracker.TRACKER.resetPlayerHighlight(serverPlayer);
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
