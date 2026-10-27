package com.decibeltracker.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.decibeltracker.app.audio.SoundLevelThresholds

// Refined Professional Instrument Base Palette
val CharcoalBg = Color(0xFF0D1117)
val SlateSurface = Color(0xFF151D27)
val SlateSurfaceElevated = Color(0xFF1E2836)
val MutedSlateBorder = Color(0xFF344152)
val GridLineColor = Color(0xFF222C38)

// Typography Palette
val SoftWhiteText = Color(0xFFF3F6FA)
val CoolGreyText = Color(0xFFA4AFBD)
val SubtleMutedText = Color(0xFF6B7A8D)

// Brand Accent (Turquoise / Cyan)
val BrandTurquoise = Color(0xFF22C7C7)
val BrandTurquoiseDim = Color(0xFF1A9A9A)

// Status / UI Accents
val AccentPurple = Color(0xFFB388FF)

// Measurement Band Colors (Mandatory Thresholds)
val LevelGreen = SoundLevelThresholds.COLOR_GREEN   // < 60 dB (#22C55E)
val LevelBlue = SoundLevelThresholds.COLOR_BLUE     // 60 - 85 dB (#3B82F6)
val LevelYellow = SoundLevelThresholds.COLOR_YELLOW // 85 - 100 dB (#FACC15)
val LevelRed = SoundLevelThresholds.COLOR_RED       // >= 100 dB (#EF4444)

// Backwards-compatible aliases for existing components
val InstrumentBg = CharcoalBg
val InstrumentSurface = SlateSurface
val InstrumentSurfaceElevated = SlateSurfaceElevated
val InstrumentBorder = MutedSlateBorder
val InstrumentGrid = GridLineColor

val TextPrimary = SoftWhiteText
val TextSecondary = CoolGreyText
val TextMuted = SubtleMutedText

val NeonGreen = LevelGreen
val WarningAmber = LevelYellow
val AlertRed = LevelRed
val AccentBlue = LevelBlue
