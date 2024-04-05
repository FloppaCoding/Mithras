package floppacoding.ui.elements.impl

import floppacoding.aurora.core.images.Image
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.elements.Element

class Image(
    val image: Image,
    constraints: Constraints?,
    private val radius: Float,
    private val imagePos: FloatArray?
) : Element(constraints) {

    override fun draw() {
//        val width = if (width == 0f) image.width.toFloat() else width
//        val height = if (height == 0f) image.height.toFloat() else height
        if (imagePos != null) {
            renderer.roundedImage(image, x, y, width, height, radius, imagePos[0], imagePos[1], imagePos[2], imagePos[3])
        } else {
            renderer.roundedImage(image, x, y, width, height, radius)
        }
    }
}