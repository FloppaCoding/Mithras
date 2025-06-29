package floppacoding.aurora.core.font

import floppacoding.aurora.core.*
import org.joml.Matrix3x2f

/**
 * # Implementation of [FontRender2D]
 *
 * @author Aton
 */
object AuroraFontRenderer: FontRender2D {
    private val vaoBuilder by Aurora::vaoBuilder
    private val matrices by Aurora::matrices

    private var _defaultFont: Font? = null
    override var defaultFont: Font
        set(value) { _defaultFont = value }
        get() = _defaultFont ?: throw IllegalStateException("No default font defined.")

    fun fontAtlas(font: Font, x: Float, y: Float) {

        val width = 1024f
        val height = 2048f

        val u1 = 0f
        val u2 = 1f
        val v1 = 0f
        val v2 = 1f

        val positionMatrix = matrices.peek()
        vaoBuilder.begin()

        vaoBuilder.vertex(positionMatrix, x, y).texture(u1, v1).next()
        vaoBuilder.vertex(positionMatrix, x, y+height).texture(u1, v2).next()
        vaoBuilder.vertex(positionMatrix, x+width, y+height).texture(u2, v2).next()
        vaoBuilder.vertex(positionMatrix, x+width, y).texture(u2, v1).next()

        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        Aurora.addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXTURE, font.glID))
    }
    // TODO add formatting support
    override fun text(text: CharSequence, x: Float, y: Float, color: Int, fontSize: Float, font: Font, textAlign: TextAlign, splitWidth: Float?): BoundingBox {
        Aurora.push()
        val (y0, y1, scale) = setupFontTransform(x,y, fontSize, font, textAlign)

        val lines = splitLines(text, font, splitWidth?.div(scale))
        lines.forEach {
            it.offset = when(textAlign.horizontal) {
                TextAlign.Horizontal.LEFT -> 0f
                TextAlign.Horizontal.CENTER -> -it.length/2
                TextAlign.Horizontal.RIGHT -> -it.length
            }
        }

        val positionMatrix = matrices.peek()
        vaoBuilder.begin()
        for (line in lines) {
            drawLineInternal(positionMatrix, line.text, line.offset, y0, y1, font, color)
            Aurora.translate(0f, font.fontMetrics.normalHeight)
        }
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        Aurora.addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXT, font.glID, Aurora.getScale(positionMatrix)))
        Aurora.pop()

        val width = lines.maxOfOrNull { it.length }?.times(scale) ?: 0f
        val height = lines.size * fontSize
        val xmin = when(textAlign.horizontal) {
            TextAlign.Horizontal.LEFT -> 0f
            TextAlign.Horizontal.CENTER -> -width/2
            TextAlign.Horizontal.RIGHT -> -width
        }
        val ymin = y0 + font.fontMetrics.padding
        return BoundingBox.ofDimensions(xmin,ymin, width, height)
    }

    override fun textBox(text: CharSequence, x: Float, y: Float, color: Int, width: Float, fontSize: Float, font: Font, textAlign: TextAlign, boxAlign: TextAlign) {
        Aurora.push()
        val (y0, y1, scale) = setupFontTransform(x, y, fontSize, font, textAlign)


        val lines = splitLines(text, font, width.div(scale))
        lines.forEach {
            it.offset = when(textAlign.horizontal) {
                TextAlign.Horizontal.LEFT -> 0f
                TextAlign.Horizontal.CENTER -> -it.length/2
                TextAlign.Horizontal.RIGHT -> -it.length
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
        Aurora.translate(xShift / scale, yShift)

        val positionMatrix = matrices.peek()
        vaoBuilder.begin()
        for (line in lines) {
            drawLineInternal(positionMatrix, line.text, line.offset, y0, y1, font, color)
            Aurora.translate(0f, font.fontMetrics.normalHeight)
        }
        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        Aurora.addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXT, font.glID, Aurora.getScale(positionMatrix)))
        Aurora.pop()
    }

    override fun textLine(text: CharSequence, x: Float, y: Float, color: Int, fontSize: Float, font: Font, textAlign: TextAlign) {
        Aurora.push()
        val (y0, y1, _) = setupFontTransform(x, y,fontSize, font, textAlign)


        val offset = when(textAlign.horizontal) {
            TextAlign.Horizontal.LEFT -> 0f
            TextAlign.Horizontal.CENTER -> -textWidthInternal(text, font) /2
            TextAlign.Horizontal.RIGHT -> -textWidthInternal(text, font)
        }

        val positionMatrix = matrices.peek()
        vaoBuilder.begin()
        drawLineInternal(positionMatrix, text, offset, y0, y1, font, color)

        val range = vaoBuilder.generateIndices(VAOBuilder2D.Mode.QUADS)
        Aurora.addDrawCall(RenderCall(range, RenderCall.ColorMode.TEXT, font.glID, Aurora.getScale(positionMatrix)))
        Aurora.pop()
    }

    private fun setupFontTransform(x: Float, y:Float, fontSize: Float, font: Font, textAlign: TextAlign): FontPosition {
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

        Aurora.translate(x, y)
        Aurora.scale(scale, scale)

        return FontPosition(y0, y1, scale)
    }

    /**
     * Splits the given [text] into a list of lines containing their text and length.
     * Each line is no longer than [splitWidth].
     * Line termination characters '\n' will also result in a split.
     */
    private fun splitLines(text: CharSequence, font: Font, splitWidth: Float?) : List<Line> {
        val lines = mutableListOf<Line>()
        if (splitWidth != null) {
            try {
                var width = 0f
                var advance: Float
                var jump = 0
                val last = text.length -1
                for ((index, char) in text.withIndex()) {
                    if (char == '\n') {
                        lines.add(Line(text.subSequence(jump, index), width))
                        width = 0f
                        jump = index + 1
                        continue
                    }
                    advance = font.glyphMetrics[char]?.advance ?: 0f
                    width += advance
                    if (width > splitWidth || index == last) {
                        lines.add(Line(text.subSequence(jump, index+1), width - advance))
                        width = 0f
                        jump = index+1
                        continue
                    }
                }
            }catch (_: Exception) {
                return emptyList()
            }
        }else {
            text.split('\n').mapTo(lines){ Line(it, textWidthInternal(it, font)) }
        }
        return lines
    }

    private fun drawLineInternal(positionMatrix: Matrix3x2f, text: CharSequence, xOffs: Float, y0: Float, y1: Float, font: Font, color: Int) {
        var x0: Float; var x1: Float; var pos = xOffs
        var metrics: GlyphMetrics
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
        return textWidthInternal(text, font) * fontSize / font.fontMetrics.normalHeight
    }

    private fun textWidthInternal(text: CharSequence, font: Font): Float {
        var width = 0f
        for(char in text) {
            width += font.glyphMetrics[char]?.advance ?: 0f
        }
        return width
    }

    override fun textBounds(text: CharSequence, width: Float?, fontSize: Float, font: Font): BoundingBox {
        var rows = 1
        var longestLine = 0f
        if (width != null) {
            val scale = fontSize/font.fontMetrics.normalHeight
            var lineWidth = 0f
            var advance: Float
            for (char in text) {
                if (char == '\n') {
                    if (lineWidth > longestLine) longestLine = lineWidth
                    lineWidth = 0f
                    rows++
                    continue
                }
                advance = font.glyphMetrics[char]?.advance?.times(scale) ?: 0f
                if (lineWidth + advance > width) {
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
            longestLine= lineLenghts.max() * fontSize/font.fontMetrics.normalHeight
        }
        val height = rows * fontSize
        return BoundingBox(0f, 0f, longestLine, height)
    }

    private data class FontPosition(val y0: Float, val y1: Float, val scale: Float)

    private class Line(val text: CharSequence, val length: Float) {
        var offset: Float = 0f
    }
}