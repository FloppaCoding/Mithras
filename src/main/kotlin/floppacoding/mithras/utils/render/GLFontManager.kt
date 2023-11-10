package floppacoding.mithras.utils.render

import floppacoding.mithras.Mithras
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.GL46.*
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryUtil
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.IntBuffer

object GLFontManager {

    val ROBOTO: GLFont =
        GLFont("roboto", "/assets/${Mithras.RESOURCE_DOMAIN}/gui/fonts/roboto-regular.ttf")

    val KURINTO: GLFont =
        GLFont("kurinto", "/assets/${Mithras.RESOURCE_DOMAIN}/gui/fonts/KurintoSans-Rg.ttf")

    /**
     * Font for [GLR].
     * @param path path to the resource. It looks like:
     *
     *      "/assets/mithras/gui/fonts/roboto-regular.ttf"
     * @author Aton
     */
    class GLFont(val name: String, val path: String) : Font {
        /**
         * Glyph specific metrics. This contains horizontal metrics as well as coordinates for the font atlas.
         */
        val glyphMetrics: Map<Char, GlyphMetrics>

        /**
         * Font wide metrics. This includes the vertical spacing information.
         * @see glyphMetrics
         */
        val fontMetrics: FontMetrics

        /**
         * The open gl reference to the font atlas.
         */
        val id: Int

        init {
            val fontBuffer : ByteBuffer = resourceToByteBuffer(path)
            val fontData = createFont(fontBuffer)
            glyphMetrics = fontData.glyphMetrics
            fontMetrics = fontData.metrics
            id = fontData.id
            MemoryUtil.memFree(fontBuffer)
        }

        /**
         * Binds this font to Texture2D in location 0.
         */
        fun bindFont() {
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, id)
        }

        /**
         * Generates a font atlas and generates a texture for it.
         *
         * This uses unoptimized stb methods can be expected to take some times to run!
         */
        private fun createFont(fontBuffer: ByteBuffer): Return {
            // Allocate memory
            val fontInfo = STBTTFontinfo.create()
            val glyphBMP = MemoryUtil.memCalloc((GLYPH_SIZE + PADDING) * (GLYPH_SIZE + PADDING))
            val bitmap = MemoryUtil.memCalloc(BMP_WIDTH*BMP_HEIGHT)
            val x0: IntBuffer = MemoryUtil.memAllocInt(1)
            val y0: IntBuffer = MemoryUtil.memAllocInt(1)
            val width: IntBuffer = MemoryUtil.memAllocInt(1)
            val height: IntBuffer = MemoryUtil.memAllocInt(1)
            val ascent: IntBuffer = MemoryUtil.memAllocInt(1)
            val descent: IntBuffer = MemoryUtil.memAllocInt(1)
            val lineGap: IntBuffer = MemoryUtil.memAllocInt(1)
            val advance: IntBuffer = MemoryUtil.memAllocInt(1)
            val leftSideBearing: IntBuffer = MemoryUtil.memAllocInt(1)
            val yPos = MemoryUtil.memCallocInt(1)
            val xPos = MemoryUtil.memCallocInt(1)

            stbtt_InitFont(fontInfo, fontBuffer)
            val padding = PADDING
            val onEdge: Byte = ON_EDGE.toByte()
            // with this negative values smaller than on_Edge are inside
            val distScale = (255- ON_EDGE).toFloat() / padding


            // Calculate font metrics for scaling.
            stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap)
            val scale = stbtt_ScaleForPixelHeight(fontInfo, GLYPH_SIZE.toFloat())
            stbtt_GetCodepointBox(fontInfo, 'f'.code, x0, y0, width, height)
            val top = height[0]*scale
            stbtt_GetCodepointBox(fontInfo, 'g'.code, x0, y0, width, height)
            val bot = y0[0]*scale
            val fontMetrics = FontMetrics(top, bot, ascent[0]*scale, descent[0]*scale, lineGap[0]*scale, padding)
            val glyphs: MutableMap<Char, GlyphMetrics> = HashMap(SYMBOLS.length)

            yPos.put(0, GLYPH_SIZE)
            val lineOffset = GLYPH_SIZE + padding * 2

            // Iterate through all required characters and generate the SDF and write it into the bitmap
            for (char in SYMBOLS) {
                val code = char.code
                stbtt_GetCodepointHMetrics(fontInfo, code, advance, leftSideBearing)
                // Generate the SDF.
                val sdf = stbtt_GetCodepointSDF(fontInfo, scale, code, padding, onEdge, distScale,  width, height, x0, y0)
                if (sdf == null) {
                    // Default for not defined glyphs. Careful when changing this, as this does include space.
                    glyphs[char] = GlyphMetrics(
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        advance[0]*scale,
                        leftSideBearing[0]*scale,
                    )
                    continue
                }

                // Check whether Glyph fits in the current row, otherwise advance.
                if (xPos[0] + width[0] >= BMP_WIDTH ) {
                    yPos.put(0, yPos[0] + lineOffset)
                    xPos.put(0, 0)
                }

                // Merge the new glyph into the atlas.
                val newPos = (yPos[0] + y0[0])*BMP_WIDTH + xPos[0]
                bitmap.position(newPos)
                mergeBMPs(bitmap, sdf, BMP_WIDTH, width[0], height[0])
                stbtt_FreeSDF(sdf)

                // Save metrics
                glyphs[char] = GlyphMetrics(
                    (xPos[0]).toFloat()/BMP_WIDTH,
                    (-ascent[0]*scale + yPos[0] - padding) /BMP_HEIGHT,
                    (xPos[0] + width[0]).toFloat()/BMP_WIDTH,
                    (-descent[0]*scale + yPos[0] + padding) /BMP_HEIGHT,
                    (width[0]).toFloat(),
                    advance[0]*scale,
                    leftSideBearing[0]*scale,
                )

                // Advance for the next glyph.
                if (xPos[0] + (advance[0]*scale).toInt() + 2* padding >= BMP_WIDTH ) {
                    yPos.put(0, yPos[0] + lineOffset)
                    xPos.put(0, 0)
                }else {
                    xPos.put(0, xPos[0] + (advance[0]*scale).toInt() + 2* padding)
                }
            }

            // Free the memory
            MemoryUtil.memFree(glyphBMP)
            MemoryUtil.memFree(x0)
            MemoryUtil.memFree(y0)
            MemoryUtil.memFree(width)
            MemoryUtil.memFree(height)
            MemoryUtil.memFree(ascent)
            MemoryUtil.memFree(descent)
            MemoryUtil.memFree(lineGap)
            MemoryUtil.memFree(advance)
            MemoryUtil.memFree(leftSideBearing)
            MemoryUtil.memFree(xPos)
            MemoryUtil.memFree(yPos)

            bitmap.position(0)

            val texId: Int = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, texId)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BMP_WIDTH,BMP_HEIGHT, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            MemoryUtil.memFree(bitmap)

            return Return(texId, fontMetrics, glyphs)
        }

        /**
         * Writes the data from [new] into [bitmap] at the current position.
         *
         * This method does not check for line jumps mid data.
         *
         * @param stride The width of [bitmap]
         * @param width The width of [new]
         * @param height the height of [new]
         * @return false if the data did not fit into [bitmap] true otherwise.
         */
        private fun mergeBMPs(bitmap: ByteBuffer, new: ByteBuffer, stride: Int, width: Int, height: Int): Boolean {
            val pos = bitmap.position()
            if (pos + height * stride + width > bitmap.limit()) return false

            for (ii in 0 ..< height) {
                try {
                    bitmap.put(pos + ii * stride, new, ii * width, width)
                }catch (e: Exception){
                    throw e
                }
            }
            return true
        }

        /**
         * Loads the resource as a byte buffer for use with NanoVG.
         * For mod assets the path has to look like:
         *
         *      "/assets/mithras/gui/fonts/roboto-regular.ttf"
         *
         * @throws FileNotFoundException when the file does not exist.
         */
        @Throws(IOException::class)
        private fun resourceToByteBuffer(path: String): ByteBuffer {
            val stream = this.javaClass.getResourceAsStream(path) ?: throw FileNotFoundException(path)
            val bytes = IOUtils.toByteArray(stream)
            val data = MemoryUtil.memAlloc(bytes.size).put(0, bytes)
            stream.close()
            return data
        }

        /**
         * Stores glyph specific spacing information.
         * ([u0],[v0]) is the top left corner of the glyph in the font atlas. This includes surrounding padding.
         * ([u1],[v1]) is the bottom right corner respectively.
         */
        class GlyphMetrics(
            /**
             * Left edge of the Glyph in the font atlas including padding.
             */
            val u0: Float,
            /**
             * Top edge of the Glyph in the font atlas including padding.
             */
            val v0: Float,
            /**
             * Right edge of the Glyph in the font atlas including padding.
             */
            val u1: Float,
            /**
             * Bottom edge of the Glyph in the font atlas including padding.
             */
            val v1: Float,
            /**
             * The total width of the glyph including padding.
             * The quad used for drawing the glyph with [u0], [u1], [v0], [v1], should have this width.
             */
            val width: Float,
            /**
             * The distance by which the origin should be advanced horizontally for the next glyph.
             */
            val advance: Float,
            /**
             * Horizontal shift of this glyph relative to the texture origin. This does not include the padding.
             */
            val leftSiderBearing: Float,
        )

        /**
         * Stores general font size information. Mainly those are vertical distances.
         */
        class FontMetrics(
            /**
             * The ascent of tall 'normal' letters above the baseline.
             */
            val normalAscent: Float,
            /**
             * The maximum descent of 'normal' letters below the baseline.
             */
            val normalDescent: Float,
            /**
             * How far the tallest letters ascent above the baseline. This is positive.
             */
            val ascent: Float,
            /**
             * How far lowest glyph in the font extends below the baseline. This is negative.
             */
            val descent: Float,
            /**
             * Gap between lines as required by the font.
             * This may be 0.
             */
            val lineGap: Float,
            /**
             * The size of the padding around glyphs.
             */
            val padding: Int,
        ) {
            /**
             * The size of 'normal' underlying glyphs.
             * This is determined by the highest point of the letter 'f' and the lowest point of the letter 'g'.
             * It is what should be used for scaling the font.
             * Accents and special characters may very well exceed this.
             * @see totalHeight
             */
            val normalHeight: Float = normalAscent - normalDescent
            /**
             * The total height of the font.
             * This is defined as the distance between the highest and lowest point reachable by any glyph in this font.
             * @see normalHeight
             */
            val totalHeight = ascent - descent
            /**
             * Sum of [totalHeight] and [lineGap]
             */
            val lineHeight = ascent - descent + lineGap
            /**
             * Difference between [ascent] and [normalAscent].
             * This is >= 0.
             */
            val topOffset: Float = ascent - normalAscent
            /**
             * Difference between [descent] and [normalDescent].
             * This is <= 0.
             */
            val bottomOffset: Float = descent - normalDescent
        }

        private data class Return(val id: Int, val metrics: FontMetrics, val glyphMetrics: Map<Char, GlyphMetrics>)

        companion object {
            const val BMP_WIDTH = 1024
            const val BMP_HEIGHT = 2048
            const val GLYPH_SIZE = 32
            const val PADDING = 2
            const val ON_EDGE = 128
            const val SYMBOLS: String =
                """ !"#$%&'()*+,-./""" +
                """0123456789:;<=>?""" +
                """@ABCDEFGHIJKLMNO""" +
                """PQRSTUVWXYZ[\]^_""" +
                """`abcdefghijklmno""" +
                """pqrstuvwxyz{|}~""" +
                """£ƒ""" +
                """ªº¬«»""" +
                """░▒▓│┤╡╢╖╕╣║╗╝╜╛┐""" +
                """└┴┬├─┼╞╟╚╔╩╦╠═╬╧""" +
                """╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀""" +
                """∅∈""" +
                """≡±≥≤⌠⌡÷≈°∙√ⁿ²■""" +

                """¡‰­·₴≠¿×ØÞһðøþΑΒ""" +
                """ΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣ""" +
                """ΤΥΦΧΨΩαβγδεζηθικ""" +
                """λμνξοπρςστυφχψωЂ""" +
                """ЅІЈЉЊЋАБВГДЕЖЗИК""" +
                """ЛМНОПРСТУФХЦЧШЩЪ""" +
                """ЫЬЭЮЯабвгдежзикл""" +
                """мнопрстуфхцчшщъы""" +
                """ьэюяєѕіјљњ–—‘’“”""" +
                """„…⁊←↑→↓⇄＋ƏəɛɪҮүӨ""" +
                """өʻˌ;ĸẞß₽€ѢѣѴѵӀѲѳ""" +
                """⁰¹³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾ⁱ™""" +
                """ʔʕ⧈⚔☠ҚқҒғҰұӘәҖҗҢ""" +
                """ңҺאבגדהוזחטיכלמם""" +
                """נןסעפףצץקר¢¤¥©®µ""" +
                """¶¼½¾·‐‚†‡•‱′″‴‵‶""" +
                """‷‹›※‼‽⁂⁈⁉⁋⁎⁏⁑⁒⁗℗""" +
                """−∓∞☀☁☈Є☲☵☽♀♂⚥♠♣♥""" +
                """♦♩♪♫♬♭♮♯⚀⚁⚂⚃⚄⚅ʬ⚡""" +
                """⛏✔❄❌❤⭐⸘⸮⸵⸸⹁⹋⥝ᘔƐ߈""" +
                """ϛㄥⱯᗺƆᗡƎℲ⅁ꞰꞀԀꝹᴚ⟘∩""" +
                """Ʌ⅄ɐɔǝɟᵷɥᴉɾʞꞁɯɹʇʌ""" +
                """ʍʎԱԲԳԴԶԷԹԺԻԼԽԾԿՀ""" +
                """ՁՂՃՄՅՆՇՈՉՋՌՍՎՏՐՑ""" +
                """ՒՓՔՕՖՙաբգդեզէըթժ""" +
                """իլխծկհձղճմյնշոչպ""" +
                """ջռսվտրցւփքօֆևשתԸ""" +
                """՚՛՜՝՞՟ՠֈ֏¯ſƷʒǷƿȜ""" +
                """ȝȤȥ˙Ꝛꝛ‑⅋⏏⏩⏪⏭⏮⏯⏴⏵""" +
                """⏶⏷⏸⏹⏺⏻⏼⏽⭘▲▶▼◀●◦◘""" +
                """⚓⛨ĲĳǉꜨꜩꜹꜻﬀﬁﬂﬃﬅ�Ե""" +
                """Պᚠᚢᚣᚤᚥᚦᚧᚨᚩᚪᚫᚬᚭᚮᚯ""" +
                """ᚰᚱᚲᚳᚴᚶᚷᚸᚹᚺᚻᚼᚽᚾᚿᛀ""" +
                """ᛁᛂᛃᛄᛅᛆᛇᛈᛉᛊᛋᛌᛍᛎᛏᛐ""" +
                """ᛑᛒᛓᛔᛕᛖᛗᛘᛙᛚᛛᛜᛝᛞᛟᛠ""" +
                """ᛡᛢᛣᛤᛥᛦᛧᛨᛩᛪ᛫᛬᛭ᛮᛯᛰ""" +
                """ᛱᛲᛳᛴᛵᛶᛷᛸ☺☻¦☹ך׳״װ""" +
                """ױײ־׃׆´¨ᴀʙᴄᴅᴇꜰɢʜᴊ""" +
                """ᴋʟᴍɴᴏᴘꞯʀꜱᴛᴜᴠᴡʏᴢ§""" +
                """ɱɳɲʈɖɡʡɕʑɸʝʢɻʁɦʋ""" +
                """ɰɬɮʘǀǃǂǁɓɗᶑʄɠʛɧɫ""" +
                """ɨʉʊɘɵɤɜɞɑɒɚɝƁƉƑƩ""" +
                """ƲႠႡႢႣႤႥႦႧႨႩႪႫႬႭႮ""" +
                """ႯႰႱႲႳႴႵႶႷႸႹႺႻႼႽႾ""" +
                """ႿჀჁჂჃჄჅჇჍაბგდევზ""" +
                """თიკლმნოპჟრსტუფქღ""" +
                """ყშჩცძწჭხჯჰჱჲჳჴჵჶ""" +
                """ჷჸჹჺ჻ჼჽჾჿתּשׂפֿפּכּײַיִ""" +
                """וֹוּבֿבּꜧꜦɺⱱʠʗʖɭɷɿʅʆ""" +
                """ʓʚ₪₾֊ⴀⴁⴂⴃⴄⴅⴆⴡⴇⴈⴉ""" +
                """ⴊⴋⴌⴢⴍⴎⴏⴐⴑⴒⴣⴓⴔⴕⴖⴗ""" +
                """ⴘⴙⴚⴛⴜⴝⴞⴤⴟⴠⴥ⅛⅜⅝⅞⅓""" +
                """⅔✉☂☔☄⛄☃⌛⌚⚐✎❣♤♧♡♢""" +
                """⛈☰☱☳☴☶☷↔⇒⇏⇔⇵∀∃∄∉""" +
                """∋∌⊂⊃⊄⊅∧∨⊻⊼⊽∥≢⋆∑⊤""" +
                """⊥⊢⊨≔∁∴∵∛∜∂⋃⊆⊇□△▷""" +
                """▽◁◆◇○◎☆★✘₀₁₂₃₄₅₆""" +
                """₇₈₉₊₋₌₍₎∫∮∝⌀⌂⌘〒ɼ""" +
                """ƄƅẟȽƚƛȠƞƟƧƨƪƸƹƻƼ""" +
                """ƽƾȡȴȵȶȺⱥȻȼɆɇȾⱦɁɂ""" +
                """ɃɄɈɉɊɋɌɍɎɏẜẝỼỽỾỿ""" +
                """Ꞩꞩ𐌰𐌱𐌲𐌳𐌴𐌵𐌶𐌷𐌸𐌹𐌺𐌻𐌼𐌽""" +
                """𐌾𐌿𐍀𐍁𐍂𐍃𐍄𐍅𐍆𐍇𐍈𐍉𐍊🌧🔥🌊""" +
                """⅐⅑⅕⅖⅗⅙⅚⅟↉🗡🏹🪓🔱🎣🧪⚗""" +
                """⯪⯫Ɑ🛡✂🍖🪣🔔⏳⚑₠₡₢₣₤₥""" +
                """₦₩₫₭₮₰₱₲₳₵₶₷₸₹₺₻""" +
                """₼₿""" +

                """ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ""" +
                """ÐÑÒÓÔÕÖÙÚÛÜÝàáâã""" +
                """äåæçìíîïñòóôõöùú""" +
                """ûüýÿĀāĂăĄąĆćĈĉĊċ""" +
                """ČčĎďĐđĒēĔĕĖėĘęĚě""" +
                """ĜĝḠḡĞğĠġĢģĤĥĦħĨĩ""" +
                """ĪīĬĭĮįİıĴĵĶķĹĺĻļ""" +
                """ĽľĿŀŁłŃńŅņŇňŊŋŌō""" +
                """ŎŏŐőŒœŔŕŖŗŘřŚśŜŝ""" +
                """ŞşŠšŢţŤťŦŧŨũŪūŬŭ""" +
                """ŮůŰűŲųŴŵŶŷŸŹźŻżŽ""" +
                """žǼǽǾǿȘșȚțΆΈΉΊΌΎΏ""" +
                """ΐΪΫάέήίΰϊϋόύώЀЁЃ""" +
                """ЇЌЍЎЙйѐёђѓїћќѝўџ""" +
                """ҐґḂḃḊḋḞḟḢḣḰḱṀṁṖṗ""" +
                """ṠṡṪṫẀẁẂẃẄẅỲỳèéêë""" +
                """ŉǧǫЏḍḥṛṭẒỊịỌọỤụ№""" +
                """ȇƔɣʃ⁇ǱǲǳǄǅǆǇǈǊǋǌ""" +
                """ℹᵫꜲꜳꜴꜵꜶꜷꜸꜺꜼꜽꝎꝏꝠꝡ""" +
                """ﬄﬆᚡᚵƠơƯưẮắẤấẾếốỚ""" +
                """ớỨứẰằẦầỀềồỜờỪừẢả""" +
                """ẲẳẨẩẺẻổỞỂểỈỉỎỏỔở""" +
                """ỦủỬửỶỷẠạẶặẬậẸẹỆệ""" +
                """ỘộỢợỰựỴỵỐƕẪẫỖỗữ☞""" +
                """☜☮ẴẵẼẽỄễỒỠỡỮỸỹҘҙ""" +
                """ҠҡҪҫǶ⚠⓪①②③④⑤⑥⑦⑧⑨""" +
                """⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳ⒶⒷⒸⒹⒺ""" +
                """ⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊ""" +
                """ⓋⓌⓍⓎⓏⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚ""" +
                """ⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ̧""" +
                """ʂʐɶǍǎǞǟǺǻȂȃȦȧǠǡḀ""" +
                """ḁȀȁḆḇḄḅᵬḈḉḐḑḒḓḎḏ""" +
                """ḌᵭḔḕḖḗḘḙḜḝȨȩḚḛȄȅ""" +
                """ȆᵮǴǵǦḦḧḨḩḪḫȞȟḤẖḮ""" +
                """ḯȊȋǏǐȈȉḬḭǰȷǨǩḲḳḴ""" +
                """ḵḺḻḼḽḶḷḸḹⱢḾḿṂṃᵯṄ""" +
                """ṅṆṇṊṋǸǹṈṉᵰǬǭȬȭṌṍ""" +
                """ṎṏṐṑṒṓȎȏȪȫǑǒȮȯȰȱ""" +
                """ȌȍǪṔṕᵱȒȓṘṙṜṝṞṟȐȑ""" +
                """ṚᵳᵲṤṥṦṧṢṣṨṩᵴṰṱṮṯ""" +
                """ṬẗᵵṲṳṶṷṸṹṺṻǓǔǕǖǗ""" +
                """ǘǙǚǛǜṴṵȔȕȖṾṿṼṽẆẇ""" +
                """ẈẉẘẌẍẊẋȲȳẎẏẙẔẕẐẑ""" +
                """ẓᵶǮǯẛꜾꜿǢǣᵺỻᴂᴔꭣȸʣ""" +
                """ʥʤʩʪʫȹʨʦʧꭐꭑ₧Ỻאַאָƀ""" +
                """ƂƃƇƈƊƋƌƓǤǥƗƖɩƘƙƝ""" +
                """ƤƥɽƦƬƭƫƮȗƱƜƳƴƵƶƢ""" +
                """ƣȢȣʭʮʯﬔﬕﬗﬖﬓӐӑӒӓӶ""" +
                """ӷҔҕӖӗҼҽҾҿӚӛӜӝӁӂӞ""" +
                """ӟӢӣӤӥӦӧӪӫӰӱӮӯӲӳӴ""" +
                """ӵӸӹӬӭѶѷӔӺԂꚂꚀꚈԪԬꚄ""" +
                """ԄԐӠԆҊӃҞҜԞԚӅԮԒԠԈԔ""" +
                """ӍӉԨӇҤԢԊҨԤҦҎԖԌꚐҬꚊ""" +
                """ꚌԎҲӼӾԦꚔҴꚎҶӋҸꚒꚖꚆҌ""" +
                """ԘԜӕӻԃꚃꚁꚉԫԭꚅԅԑӡԇҋ""" +
                """ӄҟҝԟԛӆԯԓԡԉԕӎӊԩӈҥ""" +
                """ԣԋҩԥҧҏԗԍꚑҭꚋꚍԏҳӽӿ""" +
                """ԧꚕҵꚏҷӌҹꚓꚗꚇҍԙԝἈἀἉ""" +
                """ἁἊἂἋἃἌἄἍἅἎἆἏἇᾺὰᾸ""" +
                """ᾰᾹᾱΆάᾈᾀᾉᾁᾊᾂᾋᾃᾌᾄᾍ""" +
                """ᾅᾎᾆᾏᾇᾼᾴᾶᾷᾲᾳἘἐἙἑἚ""" +
                """ἒἛἓἜἔἝἕῈΈὲέἨἠῊὴἩ""" +
                """ἡἪἢἫἣἬἤἭἥἮἦἯἧᾘᾐᾙ""" +
                """ᾑᾚᾒᾛᾓᾜᾔᾝᾕᾞᾖᾟᾗΉήῌ""" +
                """ῃῂῄῆῇῚὶΊίἸἰἹἱἺἲἻ""" +
                """ἳἼἴἽἵἾἶἿἷῘῐῙῑῒΐῖ""" +
                """ῗῸὸΌόὈὀὉὁὊὂὋὃὌὄὍ""" +
                """ὅῬῤῥῪὺΎύὙὑὛὓὝὕὟὗ""" +
                """ῨῠῩῡϓϔῢΰῧὐὒὔῦὖῺὼ""" +
                """ΏώὨὠὩὡὪὢὫὣὬὤὭὥὮὦ""" +
                """Ὧὧᾨᾠᾩᾡᾪᾢᾫᾣᾬᾤᾭᾥᾮᾦ""" +
                """ᾯᾧῼῳῲῴῶῷ☯☐☑☒ƍƺⱾȿ""" +
                """ⱿɀᶀꟄꞔᶁᶂᶃꞕᶄᶅᶆᶇᶈᶉᶊ""" +
                """ᶋᶌᶍꟆᶎᶏᶐᶒᶓᶔᶕᶖᶗᶘᶙᶚ""" +
                """ẚ⅒⅘₨₯"""



        }
    }
}