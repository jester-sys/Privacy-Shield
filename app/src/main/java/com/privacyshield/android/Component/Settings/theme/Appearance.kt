package com.privacyshield.android.Component.Settings.theme


import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.caverock.androidsvg.SVG
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.android.material.color.utilities.CorePalette
import com.privacyshield.android.App.Const.PALETTE
import com.privacyshield.android.Component.Settings.SettingsViewModel
import com.privacyshield.android.Component.Settings.theme.providable.LocalAppSettings
import com.privacyshield.android.R
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.ChevronLeft
import compose.icons.tablericons.Contrast
import compose.icons.tablericons.Moon
import compose.icons.tablericons.TestPipe


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Appearance(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    val appSettings = LocalAppSettings.current


    val context = LocalContext.current
    val dynamicColorScheme = if (appSettings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (appSettings.darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        null
    }

    // Current color scheme - Dynamic color agar enabled hai toh, nahi toh default
    val baseColorScheme = dynamicColorScheme ?: colorScheme

    // High contrast theme - User ke selected theme color ke saath high contrast apply karo
    val currentColorScheme = if (appSettings.highContrast) {
        if (appSettings.darkTheme) {
            // High contrast dark theme - User ke theme color ke saath
            baseColorScheme.copy(
                background = Color.Black, // Pure black background
                onBackground = Color.White, // Pure white text
                surface = Color.Black, // Pure black surface
                onSurface = Color.White, // Pure white text
                surfaceVariant = Color(0xFF111111), // Very dark gray
                onSurfaceVariant = Color.White, // White text
                outline = Color.White, // White borders
                outlineVariant = Color(0xFF666666) // Gray borders
                // Primary colors same rahenge (user ke selected color)
            )
        } else {
            // High contrast light theme - User ke theme color ke saath
            baseColorScheme.copy(
                background = Color.White, // Pure white background
                onBackground = Color.Black, // Pure black text
                surface = Color.White, // Pure white surface
                onSurface = Color.Black, // Pure black text
                surfaceVariant = Color(0xFFEEEEEE), // Light gray
                onSurfaceVariant = Color.Black, // Black text
                outline = Color.Black, // Black borders
                outlineVariant = Color(0xFF666666) // Gray borders
                // Primary colors same rahenge (user ke selected color)
            )
        }
    } else {
        baseColorScheme
    }

    // Custom color scheme apply karo
    androidx.compose.material3.MaterialTheme(
        colorScheme = currentColorScheme
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(stringResource(R.string.settings_appearance))
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = TablerIcons.ChevronLeft,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = currentColorScheme.surface,
                        scrolledContainerColor = currentColorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(currentColorScheme.background)
            ) {
                item {
                    DynamicImageCard(
                        isDarkMode = appSettings.darkTheme,
                        themeColor = appSettings.themeColor.color,
                    )
                }
                item {
                    ThemePalettes(
                        isDynamicColor = appSettings.dynamicColor,
                        themeColor = appSettings.themeColor,
                        onDynamicColorChange = viewModel::setDynamicColor,
                        onThemeColorChange = viewModel::setThemeColor,
                        isHighContrast = appSettings.highContrast,
                        isDarkMode = appSettings.darkTheme
                    )
                }
                item {
                    SettingItem(
                        title = stringResource(R.string.pref_dynamic_color),
                        description = stringResource(R.string.pref_dynamic_color_desc),
                        icon = TablerIcons.TestPipe,
                        onClick = { viewModel.setDynamicColor(!appSettings.dynamicColor) },
                        isHighContrast = appSettings.highContrast,
                        isDarkMode = appSettings.darkTheme
                    )
                }
                item {
                    SettingItem(
                        title = stringResource(R.string.pref_dark_theme),
                        description = "Enable dark mode",
                        icon = TablerIcons.Moon,
                        onClick = { viewModel.setDarkTheme(!appSettings.darkTheme) },
                        isHighContrast = appSettings.highContrast,
                        isDarkMode = appSettings.darkTheme
                    )
                }
                item {
                    if (appSettings.darkTheme) {
                        SettingItem(
                            title = stringResource(R.string.pref_high_contrast),
                            description = "Increase contrast for better visibility",
                            icon = TablerIcons.Contrast,
                            onClick = {
                                viewModel.setContrastMode(!appSettings.highContrast)
                            },
                            isHighContrast = appSettings.highContrast,
                            isDarkMode = appSettings.darkTheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicImageCard(
    isDarkMode: Boolean,
    themeColor: Color,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val pic by remember(isDarkMode, size, themeColor) {
        mutableStateOf(
            PictureDrawable(
                SVG.getFromString(
                    PALETTE.applyColor(
                        color = themeColor.toArgb(),
                        isDarkMode = isDarkMode
                    )
                ).renderToPicture(size.width, size.height)
            )
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .aspectRatio(1.38f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                colorScheme.inverseOnSurface
            )
            .clickable { }
            .padding(60.dp)
            .onGloballyPositioned {
                if (it.size != IntSize.Zero) {
                    size = it.size
                }
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Crossfade(targetState = pic, label = "svg") {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(data = it)
                        .apply {
                            crossfade(true)
                            scale(Scale.FIT)
                            precision(Precision.AUTOMATIC)
                            size(Size.ORIGINAL)
                        }.build()
                ),
                contentDescription = "",
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
fun ThemePalettes(
    themeColor: ThemeColor,
    isDynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    onThemeColorChange: (ThemeColor) -> Unit,
    isHighContrast: Boolean = false,
    isDarkMode: Boolean = false // Ye parameter add karo
) {
    val currentColorScheme = MaterialTheme.colorScheme

    Column {
        val groupedColorPalettes = colorPalettes.chunked(4)

        val pagerState = rememberPagerState(pageCount = { groupedColorPalettes.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) { page ->
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                groupedColorPalettes[page].forEach { color ->
                    ThemeColorPalettes(
                        color = color.color.toArgb(),
                        isSelected = !isDynamicColor && themeColor == color,
                        onSelected = {
                            onDynamicColorChange(false)
                            onThemeColorChange(color)
                        },
                        isHighContrast = isHighContrast,
                        isDarkMode = isDarkMode // Ye parameter pass karo
                    )
                }
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            pageCount = groupedColorPalettes.size,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 12.dp),
            activeColor = currentColorScheme.primary,
            inactiveColor = currentColorScheme.outlineVariant,
            indicatorHeight = 6.dp,
            indicatorWidth = 6.dp
        )
    }
}
@SuppressLint("RestrictedApi")
@Composable
private fun RowScope.ThemeColorPalettes(
    color: Int,
    isSelected: Boolean,
    onSelected: () -> Unit,
    isHighContrast: Boolean = false,
    isDarkMode: Boolean = false // Ye parameter add karo
) {
    val currentColorScheme = colorScheme
    val corePalette = remember { CorePalette.of(color) }
    val animatedSize by animateDpAsState(if (isSelected) 28.dp else 0.dp)
    val iconSize by animateDpAsState(if (isSelected) 16.dp else 0.dp)

    // High contrast mein background change karo
    val surfaceColor = if (isHighContrast) {
        if (isDarkMode) Color(0xFF111111) else Color(0xFFEEEEEE) // isDarkMode use karo
    } else {
        currentColorScheme.surfaceContainer
    }

    Surface(
        onClick = onSelected,
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        modifier = Modifier
            .padding(4.dp)
            .weight(1f)
            .aspectRatio(1f)
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color(corePalette.a1.tone(80))) // User ka selected color
            ) {
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .size(24.dp)
                        .background(Color(corePalette.a2.tone(90))) // User ka color
                )
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .background(Color(corePalette.a3.tone(60))) // User ka color
                )
                Box(
                    Modifier
                        .size(animatedSize)
                        .align(Alignment.Center)
                        .background(currentColorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = TablerIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize).align(Alignment.Center),
                        tint = currentColorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
private val colorPalettes = ThemeColor.entries