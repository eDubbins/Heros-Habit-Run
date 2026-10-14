package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// NES Legend of Zelda palette
val NES_BLACK = Color(0xFF000000)
val NES_WHITE = Color(0xFFFFFFFF)
val NES_RED = Color(0xFFD82800)
val NES_PEACH = Color(0xFFFCBCB0)
val NES_GREEN_LIGHT = Color(0xFF00A800)
val NES_GREEN_DARK = Color(0xFF005800)
val NES_GOLD = Color(0xFFF8B800)
val NES_BLUE_DARK = Color(0xFF0050C0)
val NES_BLUE_LIGHT = Color(0xFF3CBCFC)
val NES_BROWN = Color(0xFFAC7C00)
val NES_GREY = Color(0xFF7C7C7C)
val NES_TRANSPARENT = Color.Transparent

object ZeldaSprites {

    // Helper map of chars to Colors
    private val charColorMap = mapOf(
        ' ' to NES_TRANSPARENT,
        'X' to NES_TRANSPARENT,
        'K' to NES_BLACK,
        'W' to NES_WHITE,
        'R' to NES_RED,
        'P' to NES_PEACH,
        'G' to NES_GREEN_LIGHT,
        'D' to NES_GREEN_DARK,
        'Y' to NES_GOLD,
        'U' to NES_BLUE_DARK,
        'L' to NES_BLUE_LIGHT,
        'B' to NES_BROWN,
        'S' to NES_GREY
    )

    fun parsePattern(pattern: List<String>): List<List<Color>> {
        return pattern.map { row ->
            row.map { char ->
                charColorMap[char.uppercaseChar()] ?: NES_TRANSPARENT
            }
        }
    }

    val SAGE_TEMPLATE = listOf(
        "    KKKKK    ",
        "   KGGGGGK   ",
        "  KGDWWDGGK  ",
        " KGDWWKWWDKK ",
        " KDWWKPKWDK  ",
        " KDKKPPPKKDK ",
        " KDKPPPPKDK  ",
        "  KWWWWWWK   ",
        "  KGGGGGGK   ",
        " KGGYDDDYGGK ",
        "KGGYGGGGYGGK ",
        "KKGGGGGGGGKK ",
        "  KDDDKDDDK  ",
        "  KDD   DDK  ",
        "   KK   KK   "
    )

    val KNIGHT_TEMPLATE = listOf(
        "    KKKKK    ",
        "   KGGGGGK   ",
        "  KGDYDYDGK  ",
        " KGDKKKKKDGK ",
        " KDKKKKKKKDK ",
        " KDKYWWWYKDK ",
        " KDKKKKKKKDK ",
        "  KGGGGGGK   ",
        "  KGGGGGGK   ",
        " KGGYGGGYGGK ",
        "KGGGGGGGGGGK ",
        "KKKGGGGGGKKK ",
        "  KDDDKDDDK  ",
        "  KKD   DKK  ",
        "   KK   KK   "
    )

    val FAIRY_TEMPLATE = listOf(
        "   YKKKKKY   ",
        "  YYKGGGYKK  ",
        " YWWKPKWKKK  ",
        "  YWKPPPWK   ",
        "   KPPPKK    ",
        "  KKKPKKK    ",
        " KGGGGGGGK   ",
        "KGGGGGGGGGK  ",
        " KKGDYDGKK   ",
        "  KGGYGGK    ",
        "  KDDYDDK    ",
        "  KDK KK     ",
        "  KK  KK     "
    )

    val HERO_TEMPLATE = listOf(
        "    KKKKK    ",
        "   KGGGGGK   ",
        "  KGDDDGGGK  ",
        " KGPKKKPGPK  ",
        " KPPPPPPPPK  ",
        " KPKKKKKKPK  ",
        " KPYWWWWYPK  ",
        "  KPPPPPK    ",
        "  KGGGGGK    ",
        " KGGKGGKGGK  ",
        "KGGKGGGGKGGK ",
        "KKKKGGGGKKKK ",
        "  KBBKKBBK   ",
        "  KKB  BKK   ",
        "   KK  KK    "
    )

    fun getCustomHeroSprite(
        spriteType: String,
        primaryColorName: String,
        secondaryColorName: String,
        accessoryColorName: String
    ): List<List<Color>> {
        val pattern = when (spriteType.lowercase()) {
            "sage" -> SAGE_TEMPLATE
            "knight" -> KNIGHT_TEMPLATE
            "fairy" -> FAIRY_TEMPLATE
            else -> HERO_TEMPLATE
        }

        val primary = when (primaryColorName.lowercase()) {
            "green" -> NES_GREEN_LIGHT
            "red" -> NES_RED
            "blue" -> NES_BLUE_LIGHT
            "gold" -> NES_GOLD
            "white" -> NES_WHITE
            else -> NES_GREEN_LIGHT
        }

        val secondary = when (secondaryColorName.lowercase()) {
            "dark_green" -> NES_GREEN_DARK
            "dark_red" -> Color(0xFF801800)
            "dark_blue" -> NES_BLUE_DARK
            "brown" -> NES_BROWN
            "grey" -> NES_GREY
            else -> NES_GREEN_DARK
        }

        val accessory = when (accessoryColorName.lowercase()) {
            "gold" -> NES_GOLD
            "white" -> NES_WHITE
            "peach" -> NES_PEACH
            "pink" -> Color(0xFFFC80A0)
            else -> NES_GOLD
        }

        return pattern.map { row ->
            row.map { char ->
                when (char.uppercaseChar()) {
                    ' ' -> NES_TRANSPARENT
                    'X' -> NES_TRANSPARENT
                    'K' -> NES_BLACK
                    'W' -> NES_WHITE
                    'R' -> NES_RED
                    'P' -> NES_PEACH
                    'G' -> primary
                    'D' -> secondary
                    'Y' -> accessory
                    'U' -> NES_BLUE_DARK
                    'L' -> NES_BLUE_LIGHT
                    'B' -> NES_BROWN
                    'S' -> NES_GREY
                    else -> NES_TRANSPARENT
                }
            }
        }
    }

    val HERO = parsePattern(HERO_TEMPLATE)

    val HEART = parsePattern(listOf(
        " KK  KK ",
        "KRKKRKKK",
        "KRRRRRRK",
        "KRRRRRRK",
        " KRRRRK ",
        "  KRRK  ",
        "   KK   "
    ))

    val HEART_EMPTY = parsePattern(listOf(
        " KK  KK ",
        "KWKKWKKK",
        "KWWWWWWK",
        "KWWWWWWK",
        " KWWWWK ",
        "  KWWK  ",
        "   KK   "
    ))

    val SWORD = parsePattern(listOf(
        "      SK    ",
        "     SSK    ",
        "    SSK     ",
        "   SSK      ",
        "  SSK       ",
        " SSK        ",
        " KKK        ",
        "  KK        ",
        "   KB       ",
        "    K       "
    ))

    val SHIELD = parsePattern(listOf(
        "KKKKKKKK",
        "KUUUULUK",
        "KULWWWLK",
        "KWWYYWWK",
        "KWYYYYWK",
        " KYYYYK ",
        "  KYYK  ",
        "   KK   "
    ))

    val CHEST = parsePattern(listOf(
        " KKKKKKKK ",
        "KBBBBBBBBK",
        "KBYYYYYYBK",
        "KBYKKKKBYK",
        "KKKKKKKKKK",
        "KBBKKKKBBK",
        "KBBBBBBBBK",
        " KKKKKKKK "
    ))

    val WATER_POTION = parsePattern(listOf(
        "  KKKK  ",
        "  KWWK  ",
        " KKKKKK ",
        "KLLLLLLK",
        "KLWWWWLK",
        "KLLLLLLK",
        " KKKKKK "
    ))

    val STAR_GOLD = parsePattern(listOf(
        "   KK   ",
        "  KYYK  ",
        "KKKYYKKK",
        " KYYYYK ",
        "  KYYK  ",
        " KYYYYK ",
        "KKK  KKK"
    ))

    val STRETCH_BOOTS = parsePattern(listOf(
        "   KKKKK   ",
        "  KYYYYYK  ",
        "  KYWWWYK  ",
        "  KYYYYYK  ",
        "  KBBBBK   ",
        " KBBBBBK   ",
        "KBBBBBBK   ",
        "KKKKKKKK   "
    ))

    val SCROLL_JOURNAL = parsePattern(listOf(
        "  KKKKKKKK  ",
        " KYWWWWWWYK ",
        "KYWKKKKKWWYK",
        "KWWKWWWKWWK ",
        "KWWKWWWKWWK ",
        "KYWKKKKKWWYK",
        " KYWWWWWWYK ",
        "  KKKKKKKK  "
    ))
}

@Composable
fun PixelSprite(
    spriteColors: List<List<Color>>,
    modifier: Modifier = Modifier,
    pixelSize: Dp = 3.dp
) {
    val rows = spriteColors.size
    val cols = if (rows > 0) spriteColors[0].size else 0
    if (rows == 0 || cols == 0) return

    val totalWidth = pixelSize * cols
    val totalHeight = pixelSize * rows

    Canvas(modifier = modifier.size(totalWidth, totalHeight)) {
        val px = pixelSize.toPx()
        spriteColors.forEachIndexed { rIndex, row ->
            row.forEachIndexed { cIndex, color ->
                if (color != NES_TRANSPARENT) {
                    drawRect(
                        color = color,
                        topLeft = Offset(cIndex * px, rIndex * px),
                        size = Size(px + 0.5f, px + 0.5f) // overlapping subpixel buffer to prevent rendering lines
                    )
                }
            }
        }
    }
}
