package com.example.socialsentry.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import com.example.socialsentry.presentation.ui.components.AnimatedToggleSwitch
import com.example.socialsentry.presentation.viewmodel.SocialSentryViewModel
import com.example.socialsentry.ui.theme.BrightPink
import com.example.socialsentry.ui.theme.DarkGray
import com.example.socialsentry.ui.theme.White
import org.koin.androidx.compose.koinViewModel

@Composable
fun BlockScrollScreen(
    viewModel: SocialSentryViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    
    // Calculate if any reels/shorts are currently blocked
    val isReelsBlocked = settings.instagram.features.find { it.name == "Reels" }?.isEnabled == true ||
            settings.youtube.features.find { it.name == "Shorts" }?.isEnabled == true ||
            settings.facebook.features.find { it.name == "Reels" }?.isEnabled == true
    
    var isToggleEnabled by remember { mutableStateOf(isReelsBlocked) }
    
    // Update toggle state when settings change
    LaunchedEffect(isReelsBlocked) {
        isToggleEnabled = isReelsBlocked
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGray)
            .systemBarsPadding()
    ) {
        // Settings button in top left
        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint = White,
                modifier = Modifier.size(28.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            buildAnimatedText(
                text = "BLOCK SCROLL",
                isEnabled = isToggleEnabled
            )()
            
            // Subtitle
            AnimatedContent(
                targetState = isToggleEnabled,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { -it }
                    ) togetherWith fadeOut(animationSpec = tween(300)) + slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { it }
                    )
                },
                label = "subtitle"
            ) { enabled ->
                Text(
                    text = if (enabled) "Tap To Turn Off" else "Tap To Turn On",
                    fontSize = 18.sp,
                    color = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }
            
            // Animated Toggle Switch
            AnimatedToggleSwitch(
                isEnabled = isToggleEnabled,
                onToggle = {
                    isToggleEnabled = !isToggleEnabled
                    // Toggle reels blocking for all apps
                    viewModel.toggleFeatureBlocking("Instagram", "Reels", isToggleEnabled)
                    viewModel.toggleFeatureBlocking("YouTube", "Shorts", isToggleEnabled)
                    viewModel.toggleFeatureBlocking("Facebook", "Reels", isToggleEnabled)
                }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Status Card
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(500))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkGray.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        BrightPink.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isToggleEnabled,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                                    animationSpec = tween(300),
                                    initialOffsetX = { -it }
                                ) togetherWith fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                                    animationSpec = tween(300),
                                    targetOffsetX = { it }
                                )
                            },
                            label = "status"
                        ) { enabled ->
                            buildAnimatedStatusText(enabled)()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun buildAnimatedText(text: String, isEnabled: Boolean): @Composable () -> Unit {
    val words = text.split(" ")
    return {
        AnimatedText(
            words = words.mapIndexed { index, word ->
                AnimatedWord(
                    text = word,
                    color = if (index == 0) BrightPink else White,
                    isEnabled = isEnabled
                )
            }
        )
    }
}

@Composable
private fun buildAnimatedStatusText(isEnabled: Boolean): @Composable () -> Unit {
    val text = if (isEnabled) "Scrolling is Blocked" else "Scrolling is Unblocked"
    val words = text.split(" ")
    return {
        AnimatedText(
            words = words.mapIndexed { index, word ->
                AnimatedWord(
                    text = word,
                    color = if (word == "Blocked" || word == "Unblocked") BrightPink else White,
                    isEnabled = isEnabled
                )
            }
        )
    }
}

@Composable
private fun AnimatedText(words: List<AnimatedWord>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        words.forEach { word ->
            AnimatedContent(
                targetState = word,
                transitionSpec = {
                    scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) togetherWith scaleOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                },
                label = "word"
            ) { animatedWord ->
                Text(
                    text = animatedWord.text,
                    color = animatedWord.color,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class AnimatedWord(
    val text: String,
    val color: Color,
    val isEnabled: Boolean
)
