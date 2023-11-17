package floppacoding.mithras.utils.render

import org.apache.commons.lang3.tuple.MutablePair
import org.joml.Matrix4f

/**
 * # Implementation of [FontRender2D]
 *
 * @author Aton
 */
object FontRenderer: FontRender2D {
    // This object gets created before GLR so accessing GLR during construction will result in a null pointer.
    private val renderer: GLR by lazy { GLR }
    private val vaoBuilder by GLR::vaoBuilder
    private val matrices by GLR::matrices

    override val defaultFont: Font
        get() = GLFontManager.ROBOTO

    fun fontAtlas(font: GLFontManager.GLFont, x: Float, y: Float) {

        val width = 1024f
        val height = 2048f

        val u1 = 0f
        val u2 = 1f
        val v1 = 0f
        val v2 = 1f

        val positionMatrix = matrices.peek().positionMatrix
        vaoBuilder.begin()

        vaoBuilder.vertex(positionMatrix, x, y).texture(u1, v1).next()
        vaoBuilder.vertex(positionMatrix, x, y+height).texture(u1, v2).next()
        vaoBuilder.vertex(positionMatrix, x+width, y+height).texture(u2, v2).next()
        vaoBuilder.vertex(positionMatrix, x+width, y).texture(u2, v1).next()

        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        renderer.addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXTURE, font.id))
    }

    override fun text(text: CharSequence, x: Float, y: Float, color: Int, fontSize: Float, font: Font, textAlign: TextAlign, splitWidth: Float?) {
        if (font !is GLFontManager.GLFont) return

        renderer.push()
        val (y0, y1, scale) = setupFontTransform(x,y, fontSize, font, textAlign)

        val lines = splitLines(text, font, splitWidth?.div(scale))
        lines.forEach {
            it.right = when(textAlign.horizontal) {
                TextAlign.Horizontal.LEFT -> 0f
                TextAlign.Horizontal.CENTER -> -it.right/2
                TextAlign.Horizontal.RIGHT -> -it.right
            }
        }

        val positionMatrix = matrices.peek().positionMatrix
        vaoBuilder.begin()
        for (line in lines) {
            drawLineInternal(positionMatrix, line.left, line.right, y0, y1, font, color)
            renderer.translate(0f, font.fontMetrics.normalHeight)
        }
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        renderer.addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXT, font.id, renderer.getScale(positionMatrix)))
        renderer.pop()
    }

    override fun textBox(text: CharSequence, x: Float, y: Float, color: Int, width: Float, fontSize: Float, font: Font, textAlign: TextAlign, boxAlign: TextAlign) {
        if (font !is GLFontManager.GLFont) return
        renderer.push()
        val (y0, y1, scale) = setupFontTransform(x, y, fontSize, font, textAlign)


        val lines = splitLines(text, font, width.div(scale))
        lines.forEach {
            it.right = when(textAlign.horizontal) {
                TextAlign.Horizontal.LEFT -> 0f
                TextAlign.Horizontal.CENTER -> -it.right/2
                TextAlign.Horizontal.RIGHT -> -it.right
            }
        }
        val yShift = when(boxAlign.vertical) {
            TextAlign.Vertical.TOP -> 0f
            TextAlign.Vertical.MIDDLE -> -lines.size * (font.fontMetrics.lineHeight) / 2
            TextAlign.Vertical.BOTTOM -> -lines.size * (font.fontMetrics.lineHeight)
            TextAlign.Vertical.BASELINE -> -lines.size * (font.fontMetrics.lineHeight)
        }
        var xShift = when(boxAlign.horizontal) {
            TextAlign.Horizontal.LEFT -> 0f
            TextAlign.Horizontal.CENTER -> - width /2
            TextAlign.Horizontal.RIGHT -> -width
        }
        xShift += when(textAlign.horizontal) {
            TextAlign.Horizontal.LEFT -> 0f
            TextAlign.Horizontal.CENTER ->+ width /2
            TextAlign.Horizontal.RIGHT -> +width
        }
        renderer.translate(xShift / scale, yShift)

        val positionMatrix = matrices.peek().positionMatrix
        vaoBuilder.begin()
        for (line in lines) {
            drawLineInternal(positionMatrix, line.left, line.right, y0, y1, font, color)
            renderer.translate(0f, font.fontMetrics.normalHeight)
        }
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        renderer.addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXT, font.id, renderer.getScale(positionMatrix)))
        renderer.pop()
    }

    override fun textLine(text: CharSequence, x: Float, y: Float, color: Int, fontSize: Float, font: Font, textAlign: TextAlign) {
        if (font !is GLFontManager.GLFont) return
        renderer.push()
        val (y0, y1, _) = setupFontTransform(x, y,fontSize, font, textAlign)


        val offset = when(textAlign.horizontal) {
            TextAlign.Horizontal.LEFT -> 0f
            TextAlign.Horizontal.CENTER -> -textWidthInternal(text, font) /2
            TextAlign.Horizontal.RIGHT -> -textWidthInternal(text, font)
        }

        val positionMatrix = matrices.peek().positionMatrix
        vaoBuilder.begin()
        drawLineInternal(positionMatrix, text, offset, y0, y1, font, color)

        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        renderer.addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXT, font.id, renderer.getScale(positionMatrix)))
        renderer.pop()
    }

    private fun setupFontTransform(x: Float, y:Float, fontSize: Float, font: GLFontManager.GLFont, textAlign: TextAlign): FontPosition {
        val fontMetrics = font.fontMetrics
        val scale = fontSize/(fontMetrics.normalHeight)

        var y0: Float; var y1: Float
        when(textAlign.vertical) {
            TextAlign.Vertical.TOP -> { y0 = -fontMetrics.topOffset; y1 =  y0 + fontMetrics.totalHeight }
            TextAlign.Vertical.MIDDLE -> {
                val mid = (fontMetrics.normalAscent - fontMetrics.normalDescent)/2
                y0 = - mid - fontMetrics.topOffset; y1 = mid - fontMetrics.bottomOffset
            }
            TextAlign.Vertical.BOTTOM -> {y1 = -fontMetrics.bottomOffset; y0 = y1 - fontMetrics.totalHeight}
            TextAlign.Vertical.BASELINE -> { y0 = -fontMetrics.ascent; y1 = -fontMetrics.descent }
        }
        y0 -= fontMetrics.padding
        y1 += fontMetrics.padding

        renderer.translate(x, y)
        renderer.scale(scale, scale)

        return FontPosition(y0, y1, scale)
    }

    private fun splitLines(text: CharSequence, font: GLFontManager.GLFont,  splitWidth: Float?) : List<MutablePair<CharSequence, Float>> {
        val lines = mutableListOf<MutablePair<CharSequence, Float>>()
        if (splitWidth != null) {
            try {
                var width = 0f
                var advance: Float
                var jump = 0
                for ((index, char) in text.withIndex()) {
                    if (char == '\n') {
                        lines.add(MutablePair(text.subSequence(jump, index), width))
                        width = 0f
                        jump = index + 1
                        continue
                    }
                    advance = font.glyphMetrics[char]?.advance ?: 0f
                    if (width > splitWidth) {
                        lines.add(MutablePair(text.subSequence(jump, index - 1), width))
                        width = advance
                        jump = index
                        continue
                    }

                    width += advance
                }
            }catch (_: Exception) {
                return emptyList()
            }
        }else {
            text.split('\n').mapTo(lines){ MutablePair(it, textWidthInternal(it, font)) }
        }
        return lines
    }

    private fun drawLineInternal(positionMatrix: Matrix4f, text: CharSequence, xOffs: Float, y0: Float, y1: Float, font: GLFontManager.GLFont, color: Int) {
        var x0: Float; var x1: Float; var pos = xOffs
        var metrics: GLFontManager.GLFont.GlyphMetrics
        for(char in text) {
            metrics = font.glyphMetrics[char] ?: continue
            x0 = pos+metrics.leftSiderBearing - font.fontMetrics.padding
            x1 = x0 + metrics.width
            vaoBuilder.vertex(positionMatrix, x0,y0).color(color).texture(metrics.u0, metrics.v0).next()
            vaoBuilder.vertex(positionMatrix, x0,y1).color(color).texture(metrics.u0, metrics.v1).next()
            vaoBuilder.vertex(positionMatrix, x1,y1).color(color).texture(metrics.u1, metrics.v1).next()
            vaoBuilder.vertex(positionMatrix, x1,y0).color(color).texture(metrics.u1, metrics.v0).next()
            pos += metrics.advance
        }
    }

    override fun textWidth(text: CharSequence, fontSize: Float, font: Font): Float {
        if (font !is GLFontManager.GLFont) throw Error("Invalid Font")
        return textWidthInternal(text, font) * fontSize / font.fontMetrics.normalHeight
    }

    private fun textWidthInternal(text: CharSequence, font: GLFontManager.GLFont): Float {
        var width = 0f
        for(char in text) {
            width += font.glyphMetrics[char]?.advance ?: 0f
        }
        return width
    }

    override fun textBounds(text: CharSequence, width: Float?, fontSize: Float, font: Font): BoundingBox {
        if (font !is GLFontManager.GLFont) throw Error("Invalid Font")
        var rows = 1
        var longestLine = 0f
        if (width != null) {
            var lineWidth = 0f
            var advance: Float
            for (char in text) {
                if (char == '\n') {
                    if (lineWidth > longestLine) longestLine = lineWidth
                    lineWidth = 0f
                    rows++
                    continue
                }
                advance = font.glyphMetrics[char]?.advance ?: 0f
                if (lineWidth > width) {
                    if (lineWidth > longestLine) longestLine = lineWidth
                    lineWidth = advance
                    rows++
                    continue
                }

                lineWidth += advance
            }

        }else {
            val lineLenghts = text.split('\n').map{ textWidthInternal(it, font) }
            rows = lineLenghts.size
            longestLine= lineLenghts.max()
        }
        longestLine *= fontSize/font.fontMetrics.normalHeight
        val height = rows * font.fontMetrics.normalHeight * fontSize/font.fontMetrics.normalHeight
        return BoundingBox(0f, 0f, longestLine, height)
    }

    override fun textField(text: String, x: Float, y: Float, width: Float, color: Int, fontSize: Float, radius: Float, font: Font) {
        TODO("Not yet implemented")
    }

    private data class FontPosition(val y0: Float, val y1: Float, val scale: Float)
}