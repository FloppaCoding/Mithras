package floppacoding.mithras.utils.render

import net.minecraft.client.render.LayeringTransform
import net.minecraft.client.render.OutputTarget
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderSetup

object RenderLayers {

    // these builders could use the .translucent() method to produce correct effect.
    // This will enable sorting within the layer. Since the rendering in this mod is so far done immediately for every
    // object this would not do much.

    val LINES: RenderLayer = RenderLayer.of(
        "mithras_lines",
        RenderSetup.builder(MithrasRenderPipelines.LINES)
            .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .outputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .build()
    )

    val LINES_PHASE: RenderLayer = RenderLayer.of(
        "mithras_lines",
        RenderSetup.builder(MithrasRenderPipelines.LINES_PHASE)
            .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .outputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .build()
    )

    val LINE_STRIP: RenderLayer = RenderLayer.of(
        "mithras_line_strip",
        RenderSetup.builder(MithrasRenderPipelines.LINE_STRIP)
            .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .outputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .build()
    )

    val LINE_STRIP_PHASE: RenderLayer = RenderLayer.of(
        "mithras_line_strip",
        RenderSetup.builder(MithrasRenderPipelines.LINE_STRIP_PHASE)
            .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .outputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .build()
    )

    val TRIANGLE_FAN: RenderLayer = RenderLayer.of(
        "mithras_triangle_fan",
        RenderSetup.builder(MithrasRenderPipelines.TRIANGLE_FAN)
            .build()
    )

    val TRIANGLE_FAN_PHASE: RenderLayer = RenderLayer.of(
        "mithras_triangle_fan",
        RenderSetup.builder(MithrasRenderPipelines.TRIANGLE_FAN_PHASE)
            .build()
    )

    val QUADS: RenderLayer = RenderLayer.of(
        "mithras_quads",
        RenderSetup.builder(MithrasRenderPipelines.QUADS)
        .build()
    )

    val QUADS_PHASE: RenderLayer = RenderLayer.of(
        "mithras_quads",
        RenderSetup.builder(MithrasRenderPipelines.QUADS_PHASE)
                .build()
    )
}