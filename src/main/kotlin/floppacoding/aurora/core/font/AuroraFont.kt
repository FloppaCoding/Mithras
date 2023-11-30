package floppacoding.aurora.core.font

import floppacoding.aurora.core.data.OffHeapMemoryConsumer
import floppacoding.aurora.core.data.ResourceLoader
import org.lwjgl.opengl.GL46
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.IntBuffer

/**
 * Font for Aurora Font Rendering.
 *
 * @param name Internal name given to the font.
 * @param path Path to the resource. It looks like:
 *
 *      "/assets/aurora/gui/fonts/roboto-regular.ttf"
 * @param symbols A list of all characters which should be included in the font-atlas.
 * Only symbols in this list will be available for later rendering. It is not guaranteed that all specified symbols will
 * be included in the font-atlas. They have to be defined by the given font file. The symbols also have to fit into the
 * atlas. The limit depends on the characters and the font but should be upwards of 3000 characters.
 * @author Aton
 */
class AuroraFont(val name: String, val path: String, symbols: CharSequence) : Font, OffHeapMemoryConsumer() {
    override val glID: Int
    override val fontMetrics: FontMetrics
    override val glyphMetrics: Map<Char, GlyphMetrics>

    init {
        val fontBuffer : ByteBuffer = ResourceLoader.resourceToByteBuffer(path)
        val fontData = createFont(fontBuffer, symbols)
        glyphMetrics = fontData.glyphMetrics
        fontMetrics = fontData.metrics
        glID = fontData.id
        this.addCleanables(registerTextureCleaner(glID))
        MemoryUtil.memFree(fontBuffer)
    }

    constructor(name: String, path: String) : this(name, path, SYMBOLS)

    /**
     * Binds this font to Texture2D in location 0.
     */
    fun bindFont() {
        GL46.glActiveTexture(GL46.GL_TEXTURE0)
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, glID)
    }

    /**
     * Generates a font atlas and generates a texture for it.
     *
     * This uses unoptimized stb methods can be expected to take some times to run!
     */
    private fun createFont(fontBuffer: ByteBuffer, symbols: CharSequence): Return {
        // Allocate memory
        val fontInfo = STBTTFontinfo.create()
        val bitmap = MemoryUtil.memCalloc(BMP_WIDTH * BMP_HEIGHT)
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

        STBTruetype.stbtt_InitFont(fontInfo, fontBuffer)
        val padding = PADDING
        val onEdge: Byte = ON_EDGE.toByte()
        // with this negative values smaller than on_Edge are inside
        val distScale = (255- ON_EDGE).toFloat() / padding


        // Calculate font metrics for scaling.
        STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap)
        val scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, GLYPH_SIZE.toFloat())
        STBTruetype.stbtt_GetCodepointBox(fontInfo, 'f'.code, x0, y0, width, height)
        val top = height[0]*scale
        STBTruetype.stbtt_GetCodepointBox(fontInfo, 'g'.code, x0, y0, width, height)
        val bot = y0[0]*scale
        val fontMetrics = FontMetrics(top, bot, ascent[0]*scale, descent[0]*scale, lineGap[0]*scale, padding)
        val glyphs: MutableMap<Char, GlyphMetrics> = HashMap(symbols.length)

        yPos.put(0, GLYPH_SIZE)
        val lineOffset = GLYPH_SIZE + padding * 2

        // Iterate through all required characters and generate the SDF and write it into the bitmap
        for (char in symbols) {
            val code = char.code
            STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, code, advance, leftSideBearing)
            // Generate the SDF.
            val sdf = STBTruetype.stbtt_GetCodepointSDF(
                fontInfo,
                scale,
                code,
                padding,
                onEdge,
                distScale,
                width,
                height,
                x0,
                y0
            )
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
            val newPos = (yPos[0] + y0[0])* BMP_WIDTH + xPos[0]
            bitmap.position(newPos)
            mergeBMPs(bitmap, sdf, BMP_WIDTH, width[0], height[0])
            STBTruetype.stbtt_FreeSDF(sdf)

            // Save metrics
            glyphs[char] = GlyphMetrics(
                (xPos[0]).toFloat()/ BMP_WIDTH,
                (-ascent[0]*scale + yPos[0] - padding) / BMP_HEIGHT,
                (xPos[0] + width[0]).toFloat()/ BMP_WIDTH,
                (-descent[0]*scale + yPos[0] + padding) / BMP_HEIGHT,
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

        val texId: Int = GL46.glGenTextures()
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, texId)
        GL46.glTexImage2D(
            GL46.GL_TEXTURE_2D,
            0,
            GL46.GL_RED,
            BMP_WIDTH,
            BMP_HEIGHT,
            0,
            GL46.GL_RED,
            GL46.GL_UNSIGNED_BYTE,
            bitmap
        )
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR)
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR)
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