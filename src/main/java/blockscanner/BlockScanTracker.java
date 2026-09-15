package blockscanner;

import blockscanner.items.ScannerItem;
import blockscanner.networking.HighlightBlocksPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;

import java.util.*;

public class BlockScanTracker {
    public final static BlockScanTracker TRACKER = new BlockScanTracker();
    private final Map<String, Set<BlockPos>> playerHighlights = new HashMap<>();
    private final Map<String, BlockPos> playerPositions = new HashMap<>();
    private final List<String> playerSendable = new ArrayList<>();

    public BlockScanTracker() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var playerId = handler.player.getStringUUID();
            if (!playerSendable.contains(playerId)) {
                playerSendable.add(playerId);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var playerId = handler.player.getStringUUID();
            playerSendable.remove(playerId);
            removePlayerHighlight(handler.player);
        });

    }

    public void resetPlayerHighlight(ServerPlayer player) {
        var playerId = player.getStringUUID();
        playerPositions.remove(playerId);
        updatePlayerHighlight(player);
    }

    public void updatePlayerHighlight(ServerPlayer player) {
        var playerId = player.getStringUUID();

        if (!playerSendable.contains(playerId)) {
            return;
        }

        if (!ScannerItem.playerHasActiveScanner(player)) {
            sendEmptyPositions(player);
            return;
        }

        var playerPos = player.blockPosition();
        var previousPlayerPos = playerPositions.get(playerId);
        if (previousPlayerPos != null && playerPos == previousPlayerPos) {
            return;
        }


        var world = player.level();
        var range = 20;
        var newPositions = new ArrayList<BlockPos>();
        var scanner = ScannerItem.getActiveScanner(player);
        var scannedBlockId = scanner.get(ScannerItem.BLOCK);
        if (scannedBlockId != null) {
            var block = BuiltInRegistries.BLOCK.get(scannedBlockId);
            if (block.isPresent()) {
                for (var x=playerPos.getX() - range; x<playerPos.getX() + range; ++x) {
                    for (var z=playerPos.getZ() - range; z<playerPos.getZ() + range; ++z) {
                        for (var y=playerPos.getY() - range; y<playerPos.getY() + range; ++y) {
                            var pos = new BlockPos(x, y, z);
                            if (world.getBlockState(pos).is(block.get())) {
                                newPositions.add(pos);
                            }
                        }
                    }
                }
            }
        }

        var lastPositions = playerHighlights.getOrDefault(playerId, Set.of());
        if (!newPositions.equals(lastPositions)) {
            playerHighlights.put(playerId, new HashSet<>(newPositions));
            if (ServerPlayNetworking.canSend(player, HighlightBlocksPayload.ID)) {
                ServerPlayNetworking.send(player, new HighlightBlocksPayload(new ArrayList<>(newPositions)));
            }
        }
        playerPositions.put(playerId, playerPos);
    }

    private void sendEmptyPositions(ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, HighlightBlocksPayload.ID)) {
            ServerPlayNetworking.send(player, new HighlightBlocksPayload(new ArrayList<>()));
        }
    }

    private void removePlayerHighlight(ServerPlayer player) {
        playerHighlights.remove(player.getStringUUID());
        playerPositions.remove(player.getStringUUID());
    }
}
