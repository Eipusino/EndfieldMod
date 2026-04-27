package endfield.ui.markdown

import arc.Core
import arc.files.Fi
import arc.freetype.FreeType
import arc.freetype.FreeType.Face
import arc.graphics.Color
import arc.graphics.Pixmap
import arc.graphics.Texture.TextureFilter
import arc.graphics.g2d.DistanceFieldFont
import arc.graphics.g2d.Font
import arc.graphics.g2d.Font.FontData
import arc.graphics.g2d.GlyphLayout.GlyphRun
import arc.graphics.g2d.PixmapPacker
import arc.graphics.g2d.PixmapPacker.GuillotineStrategy
import arc.graphics.g2d.PixmapPacker.PackStrategy
import arc.graphics.g2d.PixmapPacker.SkylineStrategy
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.struct.Seq
import arc.util.ArcRuntimeException
import arc.util.Buffers
import arc.util.Disposable
import arc.util.Log
import arc.util.io.Streams
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Generates [Font] and [FontData] instances from TrueType, OTF, and other FreeType supported fonts.
 *
 *
 *
 * Usage example:
 *
 * <pre>
 * FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Core.files.internal(&quot;myfont.ttf&quot;));
 * BitmapFont font = gen.generateFont(16);
 * gen.dispose(); // Don't dispose if doing incremental glyph generation.
</pre> *
 *
 *
 * The generator has to be disposed once it is no longer used. The returned [Font] instances are managed by the user
 * and have to be disposed as usual.
 * @author mzechner
 * @author Nathan Sweet
 * @author Rob Rendell
 */
open class UnkFontGenerator @JvmOverloads constructor(fontFile: Fi, faceIndex: Int = 0) : Disposable {
	val library: FreeType.Library
	val face: Face
	val name: String = fontFile.pathWithoutExtension()
	var bitmapped: Boolean = false

	/**
	 * Creates a new generator from the given font file. Uses [Fi.length] to determine the file size. If the file
	 * length could not be determined (it was 0), an extra copy of the font bytes is performed. Throws a
	 * [ArcRuntimeException] if loading did not succeed.
	 */
	/** [.FreeTypeFontGenerator]  */
	init {
		val fileSize = fontFile.length().toInt()

		library = FreeType.initFreeType()

		var buffer: ByteBuffer? = null

		try {
			buffer = fontFile.map()
		} catch (e: ArcRuntimeException) {
			// Silently error, certain platforms do not support file mapping.
		}

		if (buffer == null) {
			val input = fontFile.read()
			try {
				if (fileSize == 0) {
					// Copy to a byte[] to get the file size, then copy to the buffer.
					val data = Streams.copyBytes(input, 1024 * 16)
					buffer = Buffers.newUnsafeByteBuffer(data.size)
					Buffers.copy(data, 0, buffer, data.size)
				} else {
					// Trust the specified file size.
					buffer = Buffers.newUnsafeByteBuffer(fileSize)
					Streams.copy(input, buffer)
				}
			} catch (ex: IOException) {
				throw ArcRuntimeException(ex)
			} finally {
				Streams.close(input)
			}
		}

		face = library.newMemoryFace(buffer, faceIndex)

		if (!checkForBitmapFont()) setPixelSizes(0, 15)
	}

	private fun getLoadingFlags(parameter: UnkFontParameter): Int {
		var loadingFlags = FreeType.FT_LOAD_DEFAULT
		loadingFlags = when (parameter.hinting) {
			Hinting.none -> loadingFlags or FreeType.FT_LOAD_NO_HINTING
			Hinting.slight -> loadingFlags or FreeType.FT_LOAD_TARGET_LIGHT
			Hinting.medium -> loadingFlags or FreeType.FT_LOAD_TARGET_NORMAL
			Hinting.full -> loadingFlags or FreeType.FT_LOAD_TARGET_MONO
			Hinting.autoSlight -> loadingFlags or (FreeType.FT_LOAD_FORCE_AUTOHINT or FreeType.FT_LOAD_TARGET_LIGHT)

			Hinting.autoMedium -> loadingFlags or (FreeType.FT_LOAD_FORCE_AUTOHINT or FreeType.FT_LOAD_TARGET_NORMAL)

			Hinting.autoFull -> loadingFlags or (FreeType.FT_LOAD_FORCE_AUTOHINT or FreeType.FT_LOAD_TARGET_MONO)
		}
		return loadingFlags
	}

	private fun loadChar(c: Int, flags: Int = FreeType.FT_LOAD_DEFAULT or FreeType.FT_LOAD_FORCE_AUTOHINT): Boolean {
		return face.loadChar(c, flags)
	}

	private fun checkForBitmapFont(): Boolean {
		val faceFlags = face.faceFlags
		if (((faceFlags and FreeType.FT_FACE_FLAG_FIXED_SIZES) == FreeType.FT_FACE_FLAG_FIXED_SIZES)
			&& ((faceFlags and FreeType.FT_FACE_FLAG_HORIZONTAL) == FreeType.FT_FACE_FLAG_HORIZONTAL)
		) {
			if (loadChar(32)) {
				val slot = face.glyph
				if (slot.format == 1651078259) {
					bitmapped = true
				}
			}
		}
		return bitmapped
	}

	/**
	 * Generates a new [Font]. The size is expressed in pixels. Throws a ArcRuntimeException if the font could not be
	 * generated. Using big sizes might cause such an exception.
	 * @param parameter configures how the font is generated
	 */
	@JvmOverloads
	fun generateFont(parameter: UnkFontParameter, data: FreeTypeFontData = FreeTypeFontData()): Font {
		val updateTextureRegions = data.regions == null && parameter.packer != null
		if (updateTextureRegions) data.regions = Seq<TextureRegion>()
		generateData(parameter, data)
		if (updateTextureRegions) parameter.packer!!.updateTextureRegions(
			data.regions,
			parameter.minFilter,
			parameter.magFilter,
			parameter.genMipMaps
		)
		if (data.regions!!.isEmpty) throw ArcRuntimeException("Unable to create a font with no texture regions.")

		data.padLeft += parameter.distanceFieldSpread
		val font = if (parameter.distanceFieldSpread > 0) DistanceFieldFont(data, data.regions, true)
		else Font(data, data.regions, true)
		font.setOwnsTexture(parameter.packer == null)

		return font
	}

	/**
	 * Uses ascender and descender of font to calculate real height that makes all glyphs to fit in given pixel size. Source:
	 * http://nothings.org/stb/stb_truetype.h / stbtt_ScaleForPixelHeight
	 */
	fun scaleForPixelHeight(height: Int): Int {
		setPixelSizes(0, height)
		val fontMetrics = face.size.metrics
		val ascent = FreeType.toInt(fontMetrics.ascender)
		val descent = FreeType.toInt(fontMetrics.descender)
		return height * height / (ascent - descent)
	}

	/**
	 * Uses max advance, ascender and descender of font to calculate real height that makes any n glyphs to fit in given pixel
	 * width.
	 * @param width the max width to fit (in pixels)
	 * @param numChars max number of characters that to fill width
	 */
	fun scaleForPixelWidth(width: Int, numChars: Int): Int {
		val fontMetrics = face.size.metrics
		val advance = FreeType.toInt(fontMetrics.maxAdvance)
		val ascent = FreeType.toInt(fontMetrics.ascender)
		val descent = FreeType.toInt(fontMetrics.descender)
		val unscaledHeight = ascent - descent
		val height = unscaledHeight * width / (advance * numChars)
		setPixelSizes(0, height)
		return height
	}

	/**
	 * Uses max advance, ascender and descender of font to calculate real height that makes any n glyphs to fit in given pixel
	 * width and height.
	 * @param width the max width to fit (in pixels)
	 * @param height the max height to fit (in pixels)
	 * @param numChars max number of characters that to fill width
	 */
	fun scaleToFitSquare(width: Int, height: Int, numChars: Int): Int {
		return min(scaleForPixelHeight(height), scaleForPixelWidth(width, numChars))
	}

	/**
	 * Returns null if glyph was not found. If there is nothing to render, for example with various space characters, then bitmap
	 * is null.
	 */
	fun generateGlyphAndBitmap(c: Int, size: Int, flip: Boolean): GlyphAndBitmap? {
		setPixelSizes(0, size)

		val fontMetrics = face.size.metrics
		val baseline = FreeType.toInt(fontMetrics.ascender)

		// Check if character exists in this font.
		// 0 means 'undefined character code'
		if (face.getCharIndex(c) == 0) {
			return null
		}

		// Try to load character
		if (!loadChar(c)) {
			throw ArcRuntimeException("Unable to load character!")
		}

		val slot = face.glyph

		// Try to render to bitmap
		val bitmap = if (bitmapped) {
			slot.bitmap
		} else if (!slot.renderGlyph(FreeType.FT_RENDER_MODE_NORMAL)) {
			null
		} else {
			slot.bitmap
		}

		val metrics = slot.metrics

		val glyph = Font.Glyph()
		if (bitmap != null) {
			glyph.width = bitmap.width
			glyph.height = bitmap.rows
		} else {
			glyph.width = 0
			glyph.height = 0
		}
		glyph.xoffset = slot.bitmapLeft
		glyph.yoffset = if (flip) -slot.bitmapTop + baseline else -(glyph.height - slot.bitmapTop) - baseline
		glyph.xadvance = FreeType.toInt(metrics.horiAdvance)
		glyph.srcX = 0
		glyph.srcY = 0
		glyph.id = c

		val result = GlyphAndBitmap()
		result.glyph = glyph
		result.bitmap = bitmap
		return result
	}

	/**
	 * Generates a new [FontData] instance, expert usage only. Throws a ArcRuntimeException if something went wrong.
	 * @param size the size in pixels
	 */
	fun generateData(size: Int): FreeTypeFontData {
		val parameter = UnkFontParameter()
		parameter.size = size
		return generateData(parameter)
	}

	fun setPixelSizes(pixelWidth: Int, pixelHeight: Int) {
		if (!bitmapped && !face.setPixelSizes(
				pixelWidth,
				pixelHeight
			)
		) throw ArcRuntimeException("Couldn't set size for font")
	}

	/**
	 * Generates a new [FontData] instance, expert usage only. Throws a ArcRuntimeException if something went wrong.
	 * @param parameter configures how the font is generated
	 */
	@JvmOverloads
	fun generateData(parameter: UnkFontParameter?, data: FreeTypeFontData = FreeTypeFontData()): FreeTypeFontData {
		var parameter = parameter
		parameter = parameter ?: UnkFontParameter()
		val characters = parameter.characters.toCharArray()
		val charactersLength = characters.size
		val incremental = parameter.incremental
		val flags = getLoadingFlags(parameter)

		setPixelSizes(0, parameter.size)

		// set general font data
		val fontMetrics = face.size.metrics
		data.flipped = parameter.flip
		data.ascent = FreeType.toInt(fontMetrics.ascender).toFloat()
		data.descent = FreeType.toInt(fontMetrics.descender).toFloat()
		data.lineHeight = FreeType.toInt(fontMetrics.height).toFloat()
		val baseLine = data.ascent

		// if bitmapped
		if (bitmapped && (data.lineHeight == 0f)) {
			for (c in 32..<(32 + face.numGlyphs)) {
				if (loadChar(c, flags)) {
					val lh = FreeType.toInt(face.glyph.metrics.height)
					data.lineHeight = if (lh > data.lineHeight) lh.toFloat() else data.lineHeight
				}
			}
		}
		data.lineHeight += parameter.spaceY.toFloat()

		// determine space width
		if (loadChar(' '.code, flags) || loadChar('l'.code, flags)) {
			data.spaceXadvance = FreeType.toInt(face.glyph.metrics.horiAdvance).toFloat()
		} else {
			data.spaceXadvance = face.maxAdvanceWidth.toFloat() // Possibly very wrong.
		}

		// determine x-height
		for (xChar in data.xChars) {
			if (!loadChar(xChar.code, flags)) continue
			data.xHeight = FreeType.toInt(face.glyph.metrics.height).toFloat()
			if (data.xHeight > 0) break
		}
		if (data.xHeight == 0f) throw ArcRuntimeException("No x-height character found in font")

		// determine cap height
		for (capChar in data.capChars) {
			if (!loadChar(capChar.code, flags)) continue
			data.capHeight =
				(FreeType.toInt(face.glyph.metrics.height)).toFloat()
			break
		}
		if (!bitmapped && data.capHeight == 1f) throw ArcRuntimeException("No cap character found in font")

		data.ascent -= data.capHeight
		data.down = -data.lineHeight
		if (parameter.flip) {
			data.ascent = -data.ascent
			data.down = -data.down
		}

		var ownsAtlas = false

		var packer = parameter.packer

		if (packer == null) {
			// Create a packer.
			var size: Int
			val packStrategy: PackStrategy?
			if (incremental) {
				size = maxTextureSize
				packStrategy = GuillotineStrategy()
			} else {
				val maxGlyphHeight = ceil(data.lineHeight.toDouble()).toInt()
				size =
					Mathf.nextPowerOfTwo(sqrt((maxGlyphHeight * maxGlyphHeight * charactersLength).toDouble()).toInt())
				if (maxTextureSize > 0) size =
					min(size, maxTextureSize)
				packStrategy = SkylineStrategy()
			}
			ownsAtlas = true
			packer = PixmapPacker(size, size, 1, false, packStrategy)
			packer.setTransparentColor(parameter.color)
			packer.getTransparentColor().a = 0f
			if (parameter.borderWidth > 0) {
				packer.setTransparentColor(parameter.borderColor)
				packer.getTransparentColor().a = 0f
			}
		}

		if (incremental) data.glyphs = Seq<Font.Glyph>(charactersLength + 32)

		var stroker: FreeType.Stroker? = null
		if (parameter.borderWidth > 0) {
			stroker = library.createStroker()
			stroker.set(
				(parameter.borderWidth * 64f).toInt(),
				if (parameter.borderStraight) FreeType.FT_STROKER_LINECAP_BUTT else FreeType.FT_STROKER_LINECAP_ROUND,
				if (parameter.borderStraight) FreeType.FT_STROKER_LINEJOIN_MITER_FIXED else FreeType.FT_STROKER_LINEJOIN_ROUND,
				0
			)
		}

		// Create glyphs largest height first for best packing.
		val heights = IntArray(charactersLength)
		for (i in 0..<charactersLength) {
			val c = characters[i]

			val height = if (loadChar(c.code, flags)) FreeType.toInt(face.glyph.metrics.height) else 0
			heights[i] = height

			if (c == '\u0000') {
				val missingGlyph = createGlyph('\u0000', data, parameter, stroker, baseLine, packer)
				if (missingGlyph != null && missingGlyph.width != 0 && missingGlyph.height != 0) {
					data.setGlyph('\u0000'.code, missingGlyph)
					data.missingGlyph = missingGlyph
					if (incremental) data.glyphs!!.add(missingGlyph)
				}
			}
		}
		var heightsCount = heights.size
		while (heightsCount > 0) {
			var best = 0
			var maxHeight = heights[0]
			for (i in 1..<heightsCount) {
				val height = heights[i]
				if (height > maxHeight) {
					maxHeight = height
					best = i
				}
			}

			val c = characters[best]
			if (data.getGlyph(c) == null) {
				val glyph = createGlyph(c, data, parameter, stroker, baseLine, packer)
				if (glyph != null) {
					data.setGlyph(c.code, glyph)
					if (incremental) data.glyphs!!.add(glyph)
				}
			}

			heightsCount--
			heights[best] = heights[heightsCount]
			val tmpChar = characters[best]
			characters[best] = characters[heightsCount]
			characters[heightsCount] = tmpChar
		}

		if (stroker != null && !incremental) stroker.dispose()

		if (incremental) {
			data.generator = this
			data.parameter = parameter
			data.stroker = stroker
			data.packer = packer
		}

		// Generate kerning.
		parameter.kerning = parameter.kerning and face.hasKerning()
		if (parameter.kerning) {
			for (i in 0..<charactersLength) {
				val firstChar = characters[i]
				val first = data.getGlyph(firstChar) ?: continue
				val firstIndex = face.getCharIndex(firstChar.code)
				for (ii in i..<charactersLength) {
					val secondChar = characters[ii]
					val second = data.getGlyph(secondChar) ?: continue
					val secondIndex = face.getCharIndex(secondChar.code)

					var kerning =
						face.getKerning(firstIndex, secondIndex, 0) // FT_KERNING_DEFAULT (scaled then rounded).
					if (kerning != 0) first.setKerning(secondChar.code, FreeType.toInt(kerning))

					kerning = face.getKerning(secondIndex, firstIndex, 0) // FT_KERNING_DEFAULT (scaled then rounded).
					if (kerning != 0) second.setKerning(firstChar.code, FreeType.toInt(kerning))
				}
			}
		}

		// Generate texture regions.
		if (ownsAtlas) {
			data.regions = Seq()
			packer.updateTextureRegions(data.regions, parameter.minFilter, parameter.magFilter, parameter.genMipMaps)
		}

		// Set space glyph.
		var spaceGlyph = data.getGlyph(' ')
		if (spaceGlyph == null) {
			spaceGlyph = Font.Glyph()
			spaceGlyph.xadvance = data.spaceXadvance.toInt() + parameter.spaceX
			spaceGlyph.id = ' '.code
			data.setGlyph(' '.code, spaceGlyph)
		}
		if (spaceGlyph.width == 0) spaceGlyph.width = (spaceGlyph.xadvance + data.padRight).toInt()

		return data
	}

	/** @return null if glyph was not found.
	 */
	fun createGlyph(
		c: Char, data: FreeTypeFontData, parameter: UnkFontParameter, stroker: FreeType.Stroker?, baseLine: Float,
		packer: PixmapPacker,
	): Font.Glyph? {
		val missing = face.getCharIndex(c.code) == 0 && c.code != 0
		if (missing) return null

		if (!loadChar(c.code, getLoadingFlags(parameter))) return null

		val slot = face.glyph
		var mainGlyph = slot.glyph
		try {
			mainGlyph.toBitmap(if (parameter.mono) FreeType.FT_RENDER_MODE_MONO else FreeType.FT_RENDER_MODE_NORMAL)
		} catch (e: ArcRuntimeException) {
			mainGlyph.dispose()
			Log.infoTag("FreeTypeFontGenerator", "Couldn't render char: $c")
			return null
		}
		val mainBitmap = mainGlyph.bitmap
		var mainPixmap = mainBitmap.getPixmap(parameter.color, parameter.gamma)

		if (mainBitmap.width != 0 && mainBitmap.rows != 0) {
			var offsetX: Int
			var offsetY: Int
			if (parameter.borderWidth > 0) {
				// execute stroker; this generates a glyph "extended" along the outline
				val top = mainGlyph.top
				val left = mainGlyph.left
				val borderGlyph = slot.glyph
				borderGlyph.strokeBorder(stroker, false)
				borderGlyph.toBitmap(if (parameter.mono) FreeType.FT_RENDER_MODE_MONO else FreeType.FT_RENDER_MODE_NORMAL)
				offsetX = left - borderGlyph.left
				offsetY = -(top - borderGlyph.top)

				// Render border (pixmap is bigger than main).
				val borderBitmap = borderGlyph.bitmap
				val borderPixmap = borderBitmap.getPixmap(parameter.borderColor, parameter.borderGamma)

				// Draw main glyph on top of border.
				var i = 0
				val n = parameter.renderCount
				while (i < n) {
					borderPixmap.draw(mainPixmap, offsetX, offsetY, true)
					i++
				}

				mainPixmap.dispose()
				mainGlyph.dispose()
				mainPixmap = borderPixmap
				mainGlyph = borderGlyph
			}

			if (parameter.borderWidth == 0f) {
				// No shadow and no border, draw glyph additional times.
				var i = 0
				val n = parameter.renderCount - 1
				while (i < n) {
					mainPixmap.draw(mainPixmap, 0, 0, true)
					i++
				}
			}

			if (parameter.padTop > 0 || parameter.padLeft > 0 || parameter.padBottom > 0 || parameter.padRight > 0) {
				val padPixmap = Pixmap(
					mainPixmap.width + parameter.padLeft + parameter.padRight,
					mainPixmap.height + parameter.padTop + parameter.padBottom
				)
				padPixmap.draw(mainPixmap, parameter.padLeft, parameter.padTop, true)
				mainPixmap.dispose()
				mainPixmap = padPixmap
			}

			if (parameter.distanceFieldSpread > 0) {
				val spread = parameter.distanceFieldSpread
				val spreadPixmap = Pixmap(
					mainPixmap.width + spread * 2,
					mainPixmap.height + spread * 2
				)
				spreadPixmap.draw(mainPixmap, spread, spread, true)

				val distPixmap = generateDistanceField(
					parameter.distanceFieldDownscale,
					parameter.distanceFieldSpread,
					parameter.distanceFieldColor,
					spreadPixmap,
				)
				mainPixmap.dispose()
				mainPixmap = distPixmap
			}
		}

		val metrics = slot.metrics
		val glyph = Font.Glyph()
		glyph.id = c.code
		glyph.width = mainPixmap.width //- parameter.distanceFieldSpread*2
		glyph.height = mainPixmap.height //- parameter.distanceFieldSpread*2
		glyph.xoffset = mainGlyph.left //+ parameter.distanceFieldSpread
		if (parameter.flip) glyph.yoffset = -mainGlyph.top + baseLine.toInt()
		else glyph.yoffset = -(glyph.height - mainGlyph.top) - baseLine.toInt()
		glyph.xadvance = FreeType.toInt(metrics.horiAdvance) + parameter.borderWidth.toInt() + parameter.spaceX

		if (bitmapped) {
			mainPixmap.fill(Color.clearRgba)
			val buf = mainBitmap.buffer
			val whiteIntBits = Color.white.abgr()
			val clearIntBits = Color.clear.abgr()
			for (h in 0..<glyph.height) {
				val idx = h * mainBitmap.pitch
				for (w in 0..<(glyph.width + glyph.xoffset)) {
					val bit = (buf.get(idx + (w / 8)).toInt() ushr (7 - (w % 8))) and 1
					mainPixmap.set(w, h, (if (bit == 1) whiteIntBits else clearIntBits))
				}
			}
		}

		val rect = packer.pack(mainPixmap)
		glyph.page = packer.getPages().size - 1 // Glyph is always packed into the last page for now.
		glyph.srcX = rect.x.toInt()
		glyph.srcY = rect.y.toInt()

		// If a page was added, create a new texture region for the incrementally added glyph.
		if (parameter.incremental && data.regions != null && data.regions!!.size <= glyph.page) packer.updateTextureRegions(
			data.regions,
			parameter.minFilter,
			parameter.magFilter,
			parameter.genMipMaps
		)

		mainPixmap.dispose()
		mainGlyph.dispose()

		return glyph
	}

	private fun squareDist(x1: Int, y1: Int, x2: Int, y2: Int): Int {
		val dx = x1 - x2
		val dy = y1 - y2
		return dx * dx + dy * dy
	}

	fun generateDistanceField(downscale: Int, spread: Int, color: Color, inImage: Pixmap): Pixmap {
		val inWidth = inImage.width
		val inHeight = inImage.height
		val outWidth = inWidth / downscale
		val outHeight = inHeight / downscale
		val outImage = Pixmap(outWidth, outHeight)
		val bitmap = Array(inHeight) { BooleanArray(inWidth) }

		outImage.fill(Color.clear)

		for (y in 0..<inHeight) {
			for (x in 0..<inWidth) {
				bitmap[y][x] = isInside(inImage.get(x, y))
			}
		}

		for (y in 0..<outHeight) {
			for (x in 0..<outWidth) {
				val centerX = x * downscale + downscale / 2
				val centerY = y * downscale + downscale / 2
				val signedDistance = findSignedDistance(spread, centerX, centerY, bitmap)
				outImage.set(x, y, distanceToRGB(spread, inImage.get(centerX, centerY), signedDistance))
			}
		}

		return outImage
	}

	private fun isInside(rgba: Int): Boolean {
		return Color.ai(rgba) > 32
	}

	private fun distanceToRGB(spread: Int, color: Int, signedDistance: Float): Int {
		var alpha = 0.5f + 0.5f * (signedDistance / spread)
		val a = Color.ai(color) / 255f
		alpha = Mathf.clamp(alpha) * a
		val alphaByte = (alpha * 255.0f).toInt()
		return alphaByte or (color and 0xffffff00.toInt())
	}

	private fun findSignedDistance(spread: Int, centerX: Int, centerY: Int, bitmap: Array<BooleanArray>): Float {
		val width = bitmap[0].size
		val height = bitmap.size
		val base = bitmap[centerY][centerX]
		val startX = max(0, centerX - spread)
		val endX = min(width - 1, centerX + spread)
		val startY = max(0, centerY - spread)
		val endY = min(height - 1, centerY + spread)
		var closestSquareDist = spread * spread

		for (y in startY..endY) {
			for (x in startX..endX) {
				if (base != bitmap[y][x]) {
					val squareDist = squareDist(centerX, centerY, x, y)
					if (squareDist < closestSquareDist) {
						closestSquareDist = squareDist
					}
				}
			}
		}

		val closestDist = sqrt(closestSquareDist.toDouble()).toFloat()
		return (if (base) 1 else -1).toFloat() * min(closestDist, spread.toFloat())
	}

	/** Cleans up all resources of the generator. Call this if you no longer use the generator.  */
	override fun dispose() {
		face.dispose()
		library.dispose()
	}

	/** Font smoothing algorithm.  */
	enum class Hinting {
		/** Disable hinting. Generated glyphs will look blurry.  */
		none,

		/** Light hinting with fuzzy edges, but close to the original shape  */
		slight,

		/** Average hinting  */
		medium,

		/** Strong hinting with crisp edges at the expense of shape fidelity  */
		full,

		/** Light hinting with fuzzy edges, but close to the original shape. Uses the FreeType auto-hinter.  */
		autoSlight,

		/** Average hinting. Uses the FreeType auto-hinter.  */
		autoMedium,

		/** Strong hinting with crisp edges at the expense of shape fidelity. Uses the FreeType auto-hinter.  */
		autoFull,
	}

	/**
	 * [FontData] used for fonts generated via the [UnkFontGenerator]. The texture storing the glyphs is
	 * held in memory, thus the [.getImagePaths] and [.getFontFile] methods will return null.
	 * @author mzechner
	 * @author Nathan Sweet
	 */
	class FreeTypeFontData : FontData(), Disposable {
		var regions: Seq<TextureRegion>? = null

		// Fields for incremental glyph generation.
		var generator: UnkFontGenerator? = null
		var parameter: UnkFontParameter? = null
		var stroker: FreeType.Stroker? = null
		var packer: PixmapPacker? = null
		var glyphs: Seq<Font.Glyph>? = null

		private var dirty = false

		override fun getGlyph(ch: Char): Font.Glyph? {
			var glyph = super.getGlyph(ch)
			if (glyph == null && generator != null) {
				val generator = generator!!
				val parameter = parameter!!
				val packer = packer!!
				generator.setPixelSizes(0, parameter.size)
				val baseline = ((if (flipped) -ascent else ascent) + capHeight) / scaleY
				glyph = generator.createGlyph(ch, this, parameter, stroker, baseline, packer)
				if (glyph == null) return missingGlyph

				setGlyphRegion(glyph, regions!!.get(glyph.page))

				setGlyph(ch.code, glyph)
				glyphs!!.add(glyph)
				dirty = true

				val face = generator.face
				if (parameter.kerning) {
					val glyphIndex = face.getCharIndex(ch.code)
					var i = 0
					val n = glyphs!!.size
					while (i < n) {
						val other = glyphs!!.get(i)
						val otherIndex = face.getCharIndex(other.id)

						var kerning = face.getKerning(glyphIndex, otherIndex, 0)
						if (kerning != 0) glyph.setKerning(other.id, FreeType.toInt(kerning))

						kerning = face.getKerning(otherIndex, glyphIndex, 0)
						if (kerning != 0) other.setKerning(ch.code, FreeType.toInt(kerning))
						i++
					}
				}
			}
			return glyph
		}

		override fun getGlyphs(run: GlyphRun?, str: CharSequence?, start: Int, end: Int, lastGlyph: Font.Glyph?) {
			if (packer != null) packer!!.setPackToTexture(true) // All glyphs added after this are packed directly to the texture.

			super.getGlyphs(run, str, start, end, lastGlyph)
			if (dirty && !ignoreDirty) {
				//queuing font updates fixes a crash on iOS.
				Core.app.post {
					if (dirty) {
						dirty = false
						packer!!.updateTextureRegions(
							regions,
							parameter!!.minFilter,
							parameter!!.magFilter,
							parameter!!.genMipMaps
						)
					}
				}
			}
		}

		override fun dispose() {
			if (stroker != null) stroker!!.dispose()
			if (packer != null) packer!!.dispose()
		}

		companion object {
			/** Set to true to disable font caching. Only use if you know what you're doing.  */
			var ignoreDirty: Boolean = false
		}
	}

	/**
	 * Parameter container class that helps configure how [FreeTypeFontData] and [Font] instances are
	 * generated.
	 *
	 *
	 * The packer field is for advanced usage, where it is necessary to pack multiple BitmapFonts (i.e. styles, sizes, families)
	 * into a single Texture atlas. If no packer is specified, the generator will use its own PixmapPacker to pack the glyphs into
	 * a power-of-two sized texture, and the resulting [FreeTypeFontData] will have a valid [TextureRegion] which
	 * can be used to construct a new [Font].
	 * @author siondream
	 * @author Nathan Sweet
	 */
	class UnkFontParameter {
		/** The size in pixels  */
		var size: Int = 16

		/** If true, font smoothing is disabled.  */
		var mono: Boolean = false

		/** Strength of hinting  */
		var hinting: Hinting = Hinting.autoMedium

		/** Foreground color (required for non-black borders)  */
		var color: Color = Color.white

		/** Glyph gamma. Values > 1 reduce antialiasing.  */
		var gamma: Float = 1.8f

		/** Number of times to render the glyph. Useful with a shadow or border, so it doesn't show through the glyph.  */
		var renderCount: Int = 2

		/** Border width in pixels, 0 to disable  */
		var borderWidth: Float = 0f

		/** Border color; only used if borderWidth > 0  */
		var borderColor: Color = Color.black

		/** true for straight (mitered), false for rounded borders  */
		var borderStraight: Boolean = false

		/** Values < 1 increase the border size.  */
		var borderGamma: Float = 1.8f


		var distanceFieldColor: Color = Color.white
		var distanceFieldDownscale = 1
		var distanceFieldSpread = 1


		/** Pixels to add to glyph spacing when text is rendered. Can be negative.  */
		var spaceX: Int = 0
		var spaceY: Int = 0

		/** Pixels to add to the glyph in the texture. Can be negative.  */
		var padTop: Int = 0
		var padLeft: Int = 0
		var padBottom: Int = 0
		var padRight: Int = 0

		/** The characters the font should contain. If '\0' is not included then [FontData.missingGlyph] is not set.  */
		var characters: String = DEFAULT_CHARS

		/** Whether the font should include kerning  */
		var kerning: Boolean = true

		/**
		 * The optional PixmapPacker to use for packing multiple fonts into a single texture.
		 * @see UnkFontParameter
		 */
		var packer: PixmapPacker? = null

		/** Whether to flip the font vertically  */
		var flip: Boolean = false

		/** Whether to generate mip maps for the resulting texture  */
		var genMipMaps: Boolean = false

		/** Minification filter  */
		var minFilter: TextureFilter = TextureFilter.nearest

		/** Magnification filter  */
		var magFilter: TextureFilter = TextureFilter.nearest

		/**
		 * When true, glyphs are rendered on the fly to the font's glyph page textures as they are needed. The
		 * FreeTypeFontGenerator must not be disposed until the font is no longer needed. The FreeTypeBitmapFontData must be
		 * disposed (separately from the generator) when the font is no longer needed. The FreeTypeFontParameter should not be
		 * modified after creating a font. If a PixmapPacker is not specified, the font glyph page textures will use
		 * [UnkFontGenerator.getMaxTextureSize].
		 */
		var incremental: Boolean = false
	}

	class GlyphAndBitmap {
		var glyph: Font.Glyph? = null
		var bitmap: FreeType.Bitmap? = null
	}

	companion object {
		const val DEFAULT_CHARS: String =
			"\u0000ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$€-%+=#_&~*\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF"

		/** A hint to scale the texture as needed, without capping it at any maximum size  */
		const val NO_MAXIMUM: Int = -1

		/**
		 * The maximum texture size allowed by generateData, when storing in a texture atlas. Multiple texture pages will be created
		 * if necessary. Default is 1024.
		 * @see .setMaxTextureSize
		 */
		var maxTextureSize: Int = 1024
	}
}
