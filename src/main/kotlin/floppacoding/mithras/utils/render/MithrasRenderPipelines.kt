package floppacoding.mithras.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.VertexFormats

object MithrasRenderPipelines {

    val LINES: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET).withLocation("pipeline/lines")
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.DrawMode.LINES)
            .build()
    )

    val LINES_PHASE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET).withLocation("pipeline/lines")
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.DrawMode.LINES)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build()
    )

    val LINE_STRIP: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation("pipeline/line_strip")
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.DEBUG_LINE_STRIP)
            .build()
    )

    val LINE_STRIP_PHASE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation("pipeline/line_strip")
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.DEBUG_LINE_STRIP)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    )

    val TRIANGLE_FAN: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation("pipeline/debug_triangle_fan")
            .withCull(false)
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_FAN)
            .withDepthBias(-1f, -1f)
            .build()
    )

    val TRIANGLE_FAN_PHASE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation("pipeline/debug_triangle_fan")
            .withCull(false)
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_FAN)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    )

    val QUADS: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET).withLocation("pipeline/debug_quads")
            .withDepthBias(-1f, -1f)
            .withCull(false).build()
    )

    val QUADS_PHASE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET).withLocation("pipeline/debug_quads")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false).build()
    )
}