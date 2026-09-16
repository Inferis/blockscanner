package blockscanner.client.renderer;

import blockscanner.items.ScannerItem;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.List;
import java.util.Scanner;

import static net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE;

public class BlockPositionRenderer {
    private List<BlockPos> positions = List.of();

    public BlockPositionRenderer() {
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(DEFAULT_PHASE,(context, outlineRenderState) -> {
            return positions.isEmpty();
        });

        LevelRenderEvents.END_MAIN.register(DEFAULT_PHASE,context -> {
            if (positions.isEmpty()) return;

            var poseStack = context.poseStack();
            var camera = context.gameRenderer().mainCamera();

            poseStack.pushPose();
            poseStack.translate(-camera.position().x, -camera.position().y, -camera.position().z);

            var collector = context.submitNodeCollector();
            var shape = Shapes.create(-0.001, -0.001, -0.001, 1.001, 1.001, 1.001);
            for (var pos: positions) {
                var outlineShape = shape.move(pos.getX(), pos.getY(), pos.getZ());
                // Ghost outline visible through occluding blocks (ALWAYS_PASS depth, translucent)
                collector.submitShapeOutline(poseStack, outlineShape, BlockScannerRenderTypes.linesAlwaysPassDepth(), 0x6603ee41, 4.0f, true);
            }

            poseStack.popPose();
        });
    }

    public void setPositions(List<BlockPos> positions) {
        this.positions = positions;
    }
}
