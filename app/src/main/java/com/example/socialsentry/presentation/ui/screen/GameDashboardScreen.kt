package com.example.socialsentry.presentation.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialsentry.ui.theme.*
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.socialsentry.presentation.viewmodel.SocialSentryViewModel
import org.koin.androidx.compose.koinViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@Composable
fun GameDashboardScreen(
    viewModel: SocialSentryViewModel = koinViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val dashboard = settings.gameDashboard
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .systemBarsPadding()
    ) {
        if (isLandscape) {
            // Landscape: Show Statistics and System sections
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Quote Section with animation
                AnimatedSection(visible = visible, delay = 0) {
                    QuoteSection(text = dashboard.quote)
                }
                
                // Statistics Section with Radar Chart
                AnimatedSection(visible = visible, delay = 100) {
                    StatisticsSection(
                        stats = listOf(
                            dashboard.statFit,
                            dashboard.statSoc,
                            dashboard.statInt,
                            dashboard.statDis,
                            dashboard.statFoc,
                            dashboard.statFin
                        )
                    )
                }
                
                // System Section
                AnimatedSection(visible = visible, delay = 200) {
                    SystemSection()
                }
            }
        } else {
            // Portrait: Show Player Profile prominently
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Section with animation
                AnimatedSection(visible = visible, delay = 0) {
                    PlayerSection(
                        playerName = dashboard.playerName,
                        title = dashboard.title,
                        rank = dashboard.rank,
                        currentXp = dashboard.currentXp,
                        maxXp = dashboard.maxXp,
                        goldCredits = dashboard.goldCredits
                    )
                }
                
                // Quick Stats Summary
                AnimatedSection(visible = visible, delay = 150) {
                    QuickStatsSection(
                        quote = dashboard.quote,
                        stats = listOf(
                            dashboard.statFit,
                            dashboard.statSoc,
                            dashboard.statInt,
                            dashboard.statDis,
                            dashboard.statFoc,
                            dashboard.statFin
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedSection(
    visible: Boolean,
    delay: Long = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(visible) {
        if (visible) {
            delay(delay)
            isVisible = true
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut()
    ) {
        content()
    }
}

@Composable
fun PlayerSection(
    playerName: String,
    title: String,
    rank: String,
    currentXp: Int,
    maxXp: Int,
    goldCredits: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = BrightPink.copy(alpha = 0.15f),
                spotColor = BrightPink.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(20.dp)
    ) {
        // Player Name Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Player",
                tint = White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = playerName,
                fontSize = 18.sp,
                color = White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Player Banner Card (full image when selected)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Show selected banner image as full background if present
            val context = LocalContext.current
            val settingsViewModel = koinViewModel<SocialSentryViewModel>()
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val bannerUri = settings.gameDashboard.bannerImageUri
            if (bannerUri != null) {
                AsyncImage(
                    model = bannerUri,
                    contentDescription = "Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Minimal placeholder
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.minDimension / 3
                    drawCircle(
                        color = Color(0xFF444444),
                        radius = radius,
                        center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Level and Title with glowing icon
        val infiniteTransition = rememberInfiniteTransition(label = "icon glow")
        val iconGlow by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "icon alpha"
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(8.dp),
                        ambientColor = Color(0xFF00BCD4).copy(alpha = iconGlow * 0.5f),
                        spotColor = Color(0xFF00BCD4).copy(alpha = iconGlow * 0.5f)
                    )
                    .background(Color(0xFF00BCD4).copy(alpha = iconGlow), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚔",
                    fontSize = 18.sp,
                    color = White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rank: $rank",
                        fontSize = 13.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "⭐",
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // XP Progress Bar with pulsing animation
        Column {
            Text(
                text = "${currentXp}/${maxXp} XP",
                fontSize = 12.sp,
                color = BrightPink,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val progress = (currentXp.toFloat() / maxXp.coerceAtLeast(1)).coerceIn(0f, 1f)
            AnimatedProgressBar(progress = progress)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Stats with better styling
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "You require 100 XP to complete this level!",
                fontSize = 13.sp,
                color = Color(0xFFFF5722),
                fontWeight = FontWeight.Medium
            )
            
            StatItem(
                label = "XP earned:",
                value = "${currentXp} XP",
                valueColor = BrightPink
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gold earned ",
                    fontSize = 13.sp,
                    color = White
                )
                Text(
                    text = "💰",
                    fontSize = 13.sp
                )
                Text(
                    text = " : ",
                    fontSize = 13.sp,
                    color = White
                )
                Text(
                    text = "$goldCredits Credits",
                    fontSize = 13.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // New Page Button with press animation
        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "button scale"
        )
        
        OutlinedButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .scale(scale),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = TextGray
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
            shape = RoundedCornerShape(8.dp),
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect { interaction ->
                            when (interaction) {
                                is androidx.compose.foundation.interaction.PressInteraction.Press -> {
                                    isPressed = true
                                }
                                is androidx.compose.foundation.interaction.PressInteraction.Release,
                                is androidx.compose.foundation.interaction.PressInteraction.Cancel -> {
                                    isPressed = false
                                }
                            }
                        }
                    }
                }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = TextGray.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New page",
                fontSize = 13.sp,
                color = TextGray.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun AnimatedProgressBar(progress: Float) {
    // Pulsing animation for the progress bar
    val infiniteTransition = rememberInfiniteTransition(label = "progress pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress alpha"
    )
    
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF2A2A2A))
        )
        
        // Animated progress
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BrightPink.copy(alpha = alpha))
        )
    }
}

@Composable
fun QuoteSection(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextGray.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            letterSpacing = 1.5.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun StatisticsSection(stats: List<Float>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Radar Chart - Larger and more spacious
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ImprovedRadarChart(stats)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Creator credit
        Text(
            text = "yt@mkshaon7",
            fontSize = 11.sp,
            color = TextGray.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QuickStatsSection(quote: String, stats: List<Float>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(24.dp)
    ) {
        // Quote
        Text(
            text = quote,
            fontSize = 11.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Radar Chart - Bigger and more spacious
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            ImprovedRadarChart(stats)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Creator credit
        Text(
            text = "yt@mkshaon7",
            fontSize = 11.sp,
            color = TextGray.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        HorizontalDivider(color = TextGray.copy(alpha = 0.2f))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // The System Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = BrightPink,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "The System",
                fontSize = 16.sp,
                color = White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status message
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF8B0000).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFFF5722),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "No new quests detected.",
                fontSize = 13.sp,
                color = Color(0xFFFF5722),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SystemSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(24.dp)
    ) {
        // Power Button with hover animation
        var powerPressed by remember { mutableStateOf(false) }
        val powerScale by animateFloatAsState(
            targetValue = if (powerPressed) 0.85f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "power button scale"
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(powerScale)
                    .background(Color(0xFF0F0F0F), RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Power button action
                    }
                    .then(
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect { interaction ->
                                            when (interaction) {
                                                is androidx.compose.foundation.interaction.PressInteraction.Press -> {
                                                    powerPressed = true
                                                }
                                                is androidx.compose.foundation.interaction.PressInteraction.Release,
                                                is androidx.compose.foundation.interaction.PressInteraction.Cancel -> {
                                                    powerPressed = false
                                                }
                                            }
                                        }
                                    }
                                },
                            indication = null,
                            onClick = {}
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Power",
                    tint = TextGray.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // The System Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = BrightPink,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "The System",
                fontSize = 18.sp,
                color = White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // No new quests message
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF8B0000).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFFF5722),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "No new quests detected.",
                fontSize = 14.sp,
                color = Color(0xFFFF5722),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label ",
            fontSize = 13.sp,
            color = White
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ImprovedRadarChart(stats: List<Float>) {
    // Subtle breathing animation for the radar chart
    val infiniteTransition = rememberInfiniteTransition(label = "radar pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar scale"
    )
    
    // Hexagon radar chart matching the reference image
    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            // Make the radar chart bigger - use more of the available space
            val radius = size.minDimension / 2.8f
            
            // Draw hexagon radar grid (6 points)
            val points = 6
            val angleStep = (2 * Math.PI / points).toFloat()
            val startAngle = -Math.PI.toFloat() / 2 // Start from top
            
            // Draw concentric hexagons (grid) - 5 levels with better visibility
            for (level in 1..5) {
                val currentRadius = radius * level / 5
                val path = androidx.compose.ui.graphics.Path()
                
                for (i in 0 until points) {
                    val angle = startAngle + angleStep * i
                    val x = centerX + currentRadius * kotlin.math.cos(angle)
                    val y = centerY + currentRadius * kotlin.math.sin(angle)
                    
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.close()
                
                // Make grid lines more visible
                drawPath(
                    path = path,
                    color = Color(0xFF00BCD4).copy(alpha = 0.2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
            
            // Draw axis lines from center to each point - thicker
            for (i in 0 until points) {
                val angle = startAngle + angleStep * i
                val x = centerX + radius * kotlin.math.cos(angle)
                val y = centerY + radius * kotlin.math.sin(angle)
                
                drawLine(
                    color = Color(0xFF00BCD4).copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    end = androidx.compose.ui.geometry.Offset(x, y),
                    strokeWidth = 2f
                )
            }
            
            // Draw data polygon (filled) - Sample stats values
            val dataValues = stats.takeIf { it.size == 6 } ?: listOf(0.85f, 0.95f, 0.78f, 0.65f, 0.72f, 0.88f)
            val dataPath = androidx.compose.ui.graphics.Path()
            
            for (i in dataValues.indices) {
                val angle = startAngle + angleStep * i
                val currentRadius = radius * dataValues[i]
                val x = centerX + currentRadius * kotlin.math.cos(angle)
                val y = centerY + currentRadius * kotlin.math.sin(angle)
                
                if (i == 0) {
                    dataPath.moveTo(x, y)
                } else {
                    dataPath.lineTo(x, y)
                }
            }
            dataPath.close()
            
            // Fill with purple gradient effect - more visible
            drawPath(
                path = dataPath,
                color = Color(0xFF9C27B0).copy(alpha = 0.6f)
            )
            
            // Stroke outline - thicker
            drawPath(
                path = dataPath,
                color = Color(0xFF9C27B0).copy(alpha = 0.9f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
            
            // Draw data points as circles - bigger
            for (i in dataValues.indices) {
                val angle = startAngle + angleStep * i
                val currentRadius = radius * dataValues[i]
                val x = centerX + currentRadius * kotlin.math.cos(angle)
                val y = centerY + currentRadius * kotlin.math.sin(angle)
                
                // Outer glow circle
                drawCircle(
                    color = Color(0xFF9C27B0).copy(alpha = 0.3f),
                    radius = 8f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
                // Inner solid circle
                drawCircle(
                    color = Color(0xFF9C27B0),
                    radius = 5f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
        
        // Labels positioned around the hexagon - better spacing
        val labels = listOf("FIT", "SOC", "INT", "DIS", "FOC", "FIN")
        Box(modifier = Modifier.fillMaxSize()) {
            // Top label (FIT)
            Text(
                text = labels[0],
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 0.dp)
            )
            
            // Top-right label (SOC)
            Text(
                text = labels[1],
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = 32.dp)
            )
            
            // Bottom-right label (INT)
            Text(
                text = labels[2],
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 32.dp)
            )
            
            // Bottom label (DIS)
            Text(
                text = labels[3],
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 0.dp)
            )
            
            // Bottom-left label (FOC)
            Text(
                text = labels[4],
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 32.dp)
            )
            
            // Top-left label (FIN)
            Text(
                text = labels[5],
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 32.dp)
            )
        }
    }
}

