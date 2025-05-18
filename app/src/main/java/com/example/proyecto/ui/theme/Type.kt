package com.example.proyecto.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.proyecto.R

val nunitoFontFamily = FontFamily(
    Font(R.font.nunito_black, FontWeight.Black),
    Font(R.font.nunito_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

)