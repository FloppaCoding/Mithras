package floppacoding.mithras.utils.render

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayer.MultiPhase
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.RenderPhase.LineWidth
import java.util.*

object RenderLayers {

    val LINES: MultiPhase = RenderLayer.of(
        "Mithras_lines",
        1536,
        false,
        true,
        MithrasRenderPipelines.LINES,
        RenderLayer.MultiPhaseParameters.builder()
            .lineWidth(LineWidth(OptionalDouble.empty()))
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .target(RenderPhase.ITEM_ENTITY_TARGET)
            .build(false)
    )

    val LINES_PHASE: MultiPhase = RenderLayer.of(
        "Mithras_lines",
        1536,
        false,
        true,
        MithrasRenderPipelines.LINES_PHASE,
        RenderLayer.MultiPhaseParameters.builder()
            .lineWidth(LineWidth(OptionalDouble.empty()))
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .target(RenderPhase.ITEM_ENTITY_TARGET)
            .build(false)
    )

    val LINE_STRIP: MultiPhase = RenderLayer.of(
        "mithras_line_strip",
        1536,
        false,
        true,
        MithrasRenderPipelines.LINE_STRIP,
        RenderLayer.MultiPhaseParameters.builder()
            .lineWidth(LineWidth(OptionalDouble.empty()))
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .target(RenderPhase.ITEM_ENTITY_TARGET)
            .build(false)
    )

    val LINE_STRIP_PHASE: MultiPhase = RenderLayer.of(
        "mithras_line_strip",
        1536,
        false,
        true,
        MithrasRenderPipelines.LINE_STRIP_PHASE,
        RenderLayer.MultiPhaseParameters.builder()
            .lineWidth(LineWidth(OptionalDouble.empty()))
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .target(RenderPhase.ITEM_ENTITY_TARGET)
            .build(false)
    )

    val TRIANGLE_FAN: MultiPhase = RenderLayer.of(
        "mithras_triangle_fan",
        1536,
        false,
        true,
        MithrasRenderPipelines.TRIANGLE_FAN,
        RenderLayer.MultiPhaseParameters.builder().build(false)
    )

    val TRIANGLE_FAN_PHASE: MultiPhase = RenderLayer.of(
        "mithras_triangle_fan",
        1536,
        false,
        true,
        MithrasRenderPipelines.TRIANGLE_FAN_PHASE,
        RenderLayer.MultiPhaseParameters.builder().build(false)
    )

    val QUADS: MultiPhase = RenderLayer.of(
        "mithras_quads",
        1536,
        false,
        true,
        MithrasRenderPipelines.QUADS,
        RenderLayer.MultiPhaseParameters.builder().build(false)
    )

    val QUADS_PHASE: MultiPhase = RenderLayer.of(
        "mithras_quads",
        1536,
        false,
        true,
        MithrasRenderPipelines.QUADS_PHASE,
        RenderLayer.MultiPhaseParameters.builder().build(false)
    )
}