package blockscanner.client.renderer;

import blockscanner.BlockScanner;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class BlockScannerRenderTypes {
    // Lines render type that always passes depth testing (renders through blocks).
    public static RenderType linesAlwaysPassDepth() {
        if (linesAlwaysPassDepth == null) {
            linesAlwaysPassDepth = createLinesAlwaysPassDepth();
        }
        return linesAlwaysPassDepth;
    }

    private static RenderType linesAlwaysPassDepth;

    private static RenderType createLinesAlwaysPassDepth() {
        try {
            var identifier = Identifier.fromNamespaceAndPath(BlockScanner.MOD_ID, "lines_always_pass_depth");

            var globalsSnippet = RenderPipeline.builder()
                    .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                    .buildSnippet();

            var matricesFogSnippet = RenderPipeline.builder(globalsSnippet)
                    .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                    .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                    .withBindGroupLayout(BindGroupLayouts.FOG)
                    .buildSnippet();

            var pipeline = RenderPipeline.builder(matricesFogSnippet)
                    .withVertexShader("core/rendertype_lines")
                    .withFragmentShader("core/rendertype_lines")
                    .withCull(false)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
                    .withPrimitiveTopology(PrimitiveTopology.LINES)
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withLocation(identifier)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .build();

            var setup = RenderSetup.builder(pipeline).createRenderSetup();
            return RenderType.create(identifier.toString(), setup);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create lines-always-pass-depth render type", e);
        }
    }
}
