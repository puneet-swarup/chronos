package com.chronos.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Typography tokens, centralised so a future theme can swap fonts/sizes.
object ChronosType {
 val typography = Typography(
 headlineMedium = TextStyle(
 fontFamily = FontFamily.SansSerif,
 fontWeight = FontWeight.Bold,
 fontSize = 26.sp,
 lineHeight = 32.sp,
 ),
 titleMedium = TextStyle(
 fontFamily = FontFamily.SansSerif,
 fontWeight = FontWeight.SemiBold,
 fontSize = 18.sp,
 lineHeight = 24.sp,
 ),
 bodyMedium = TextStyle(
 fontFamily = FontFamily.SansSerif,
 fontWeight = FontWeight.Normal,
 fontSize = 15.sp,
 lineHeight = 22.sp,
 ),
 labelSmall = TextStyle(
 fontFamily = FontFamily.SansSerif,
 fontWeight = FontWeight.Medium,
 fontSize = 12.sp,
 lineHeight = 16.sp,
 ),
 )
}
