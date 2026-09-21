package com.listinha.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
//  Color.kt  —  a paleta ROXO PASTEL do Listinha
// ------------------------------------------------------------
//  Roxo "dusty" (empoeirado): mais sóbrio e com menos brilho que
//  um roxo vibrante, mantendo o clima pastel e acolhedor.
//  Cada nome é um "papel" que o Material 3 espera.
// ============================================================

// --- Roxos da marca ---
val ListinhaPurple      = Color(0xFF6E5A94) // primário (claro) — botões, destaques
val ListinhaPurpleDeep  = Color(0xFF574471) // roxo mais escuro — base de gradientes
val ListinhaPurpleSoft  = Color(0xFF8672AD) // roxo médio — topo de gradientes
val ListinhaPurpleLight = Color(0xFFC6B7E6) // roxo claro — primário no modo escuro

// --- TEMA CLARO ---
val LightPrimary            = ListinhaPurple
val LightOnPrimary          = Color(0xFFFFFFFF)
val LightPrimaryContainer   = Color(0xFFE7E0F2)
val LightOnPrimaryContainer = Color(0xFF2C2440)
val LightSecondaryContainer = Color(0xFFE7E0F2) // usado nos chips selecionados
val LightOnSecondaryContainer = Color(0xFF2C2440)
val LightBackground         = Color(0xFFEDE7F5)
val LightOnBackground       = Color(0xFF2B2233)
val LightSurface            = Color(0xFFFFFFFF)
val LightOnSurface          = Color(0xFF2B2233)
val LightSurfaceVariant     = Color(0xFFEFEAF5)
val LightOnSurfaceVariant   = Color(0xFF6B6178)
val LightOutline            = Color(0xFFD8D0E4)
val LightError              = Color(0xFFD9604E)
val LightOnError            = Color(0xFFFFFFFF)
val LightErrorContainer     = Color(0xFFF7E1DC)
val LightOnErrorContainer   = Color(0xFF5A2119)

// --- TEMA ESCURO ---
val DarkPrimary            = ListinhaPurpleLight
val DarkOnPrimary          = Color(0xFF2A1E3E)
val DarkPrimaryContainer   = Color(0xFF4A3B6B)
val DarkOnPrimaryContainer = Color(0xFFECE3FB)
val DarkSecondaryContainer = Color(0xFF4A3B6B)
val DarkOnSecondaryContainer = Color(0xFFECE3FB)
val DarkBackground         = Color(0xFF17131F)
val DarkOnBackground       = Color(0xFFECE6F5)
val DarkSurface            = Color(0xFF221C2E)
val DarkOnSurface          = Color(0xFFECE6F5)
val DarkSurfaceVariant     = Color(0xFF2E2740)
val DarkOnSurfaceVariant   = Color(0xFFB9AECC)
val DarkOutline            = Color(0xFF423A55)
val DarkError              = Color(0xFFF2998A)
val DarkOnError            = Color(0xFF3A0E08)
val DarkErrorContainer     = Color(0xFF6E2018)
val DarkOnErrorContainer   = Color(0xFFF9DAD3)
