package com.example.socialsentry.presentation.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialsentry.ui.theme.*

@Composable
fun GameDashboardScreen() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGray)
            .systemBarsPadding()
    ) {
        if (isLandscape) {
            // Landscape: Side by side layout (like reference image)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Section
                Box(modifier = Modifier.weight(1f)) {
                    PlayerSection()
                }
                
                // Statistics Section
                Box(modifier = Modifier.weight(1f)) {
                    StatisticsSection()
                }
                
                // System Section
                Box(modifier = Modifier.weight(1f)) {
                    SystemSection()
                }
            }
        } else {
            // Portrait: Vertical stacked layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Section
                PlayerSection()
                
                // Statistics Section
                StatisticsSection()
                
                // System Section
                SystemSection()
            }
        }
    }
}

@Composable
fun PlayerSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
            .border(1.dp, BrightPink.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        // Title
        Text(
            text = "PLAYER",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = BrightPink,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            letterSpacing = 4.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Player Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Player",
                tint = White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Adam",
                fontSize = 20.sp,
                color = White,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Player Card/Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00BCD4),
                            Color(0xFF006064)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Player Avatar",
                tint = White.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Level Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Level",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Level 11 ⚔️ | Rising from the Ashes",
                fontSize = 14.sp,
                color = White,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Rank
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rank: C-Rank Hunter ⭐⭐⭐",
                fontSize = 14.sp,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress Bar
        Column {
            LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = BrightPink,
                trackColor = Color(0xFF2A2A2A)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "790/1500 XP",
                fontSize = 12.sp,
                color = BrightPink,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem(
                label = "You require",
                value = "710 XP",
                valueColor = Color(0xFFFF5722)
            )
            StatItem(
                label = "XP earned:",
                value = "2790 XP",
                valueColor = BrightPink
            )
            StatItem(
                label = "Gold earned 💰:",
                value = "5350 Credits",
                valueColor = Color(0xFFFFD700)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // New Page Button
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = TextGray
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, TextGray.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "+ New page",
                fontSize = 14.sp,
                color = TextGray
            )
        }
    }
}

@Composable
fun StatisticsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
            .border(1.dp, BrightPink.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "STATISTICS",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = BrightPink,
            textAlign = TextAlign.Center,
            letterSpacing = 4.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "THE SYSTEM USERS ME, NOW I USE THE SYSTEM",
            fontSize = 10.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Radar Chart Placeholder
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            RadarChartPlaceholder()
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Creator credit
        Text(
            text = "yt@mkshaon7",
            fontSize = 12.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SystemSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
            .border(1.dp, BrightPink.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        // Title
        Text(
            text = "SYSTEM",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = BrightPink,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            letterSpacing = 4.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Power Button
        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(48.dp)
                .background(Color.Transparent, RoundedCornerShape(24.dp))
                .border(1.dp, TextGray.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Power",
                tint = TextGray,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // The System
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "The System",
                fontSize = 16.sp,
                color = White,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Status Items
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusItem(
                icon = Icons.Default.Star,
                title = "⚡ STREAK STATUS ⚡",
                value = "Streak: 3 Days",
                valueColor = Color(0xFF00BCD4)
            )
            
            StatusItem(
                icon = Icons.Default.CheckCircle,
                title = "🎁 SYSTEM GIFT 🎁",
                value = "1.2x Boost Activated",
                valueColor = Color(0xFFFF9800)
            )
            
            StatusItem(
                icon = Icons.Default.Check,
                title = "Logged-in",
                value = "",
                valueColor = BrightGreen
            )
            
            HorizontalDivider(
                color = TextGray.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            StatusItem(
                icon = Icons.Default.Warning,
                title = "⚠️ SYSTEM WARNING ⚠️",
                value = "⚔️ Your Quest is Not Yet Complete!",
                valueColor = Color(0xFFFF5722)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // System Message
        Text(
            text = "Your path is still uncertain. The System urges you to continue...",
            fontSize = 12.sp,
            color = TextGray,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Divider(color = TextGray.copy(alpha = 0.2f))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quest Progress
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚔️ Progress: [0/4]",
                    fontSize = 14.sp,
                    color = BrightPink,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            QuestItem(icon = Icons.Default.Check, text = "Remaining Quests", checked = false)
            QuestItem(icon = Icons.Default.Check, text = "Study Programming", checked = false)
            QuestItem(icon = Icons.Default.Check, text = "Workout", checked = false)
            QuestItem(icon = Icons.Default.Check, text = "Learn JavaScript", checked = false)
            QuestItem(icon = Icons.Default.Check, text = "Read Atomic Habits", checked = false)
            
            HorizontalDivider(
                color = TextGray.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = "❌ A new challenge awaits! ❌",
                fontSize = 12.sp,
                color = Color(0xFF00BCD4),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "🎯 Next Gate unlocks at: 4000 XP",
                fontSize = 12.sp,
                color = Color(0xFFFF9800),
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "📊 XP Needed: 1210 XP",
                fontSize = 12.sp,
                color = Color(0xFFFF5722),
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "The System awaits your next move...",
            fontSize = 12.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // New Page Button
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = TextGray
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, TextGray.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "+ New page",
                fontSize = 14.sp,
                color = TextGray
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
fun StatusItem(
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = valueColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = White,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = 12.sp,
                color = valueColor,
                modifier = Modifier.padding(start = 26.dp)
            )
        }
    }
}

@Composable
fun QuestItem(
    icon: ImageVector,
    text: String,
    checked: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckCircle else icon,
            contentDescription = null,
            tint = if (checked) BrightGreen else TextGray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = White
        )
    }
}

@Composable
fun RadarChartPlaceholder() {
    // Simple pentagon radar chart visual
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2
            
            // Draw pentagon radar grid
            val points = 5
            val angleStep = (2 * Math.PI / points).toFloat()
            
            // Draw concentric pentagons (grid)
            for (level in 1..5) {
                val currentRadius = radius * level / 5
                val path = androidx.compose.ui.graphics.Path()
                
                for (i in 0 until points) {
                    val angle = angleStep * i - Math.PI.toFloat() / 2
                    val x = centerX + currentRadius * kotlin.math.cos(angle)
                    val y = centerY + currentRadius * kotlin.math.sin(angle)
                    
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.close()
                
                drawPath(
                    path = path,
                    color = TextGray.copy(alpha = 0.2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )
            }
            
            // Draw data polygon (filled)
            val dataValues = listOf(0.9f, 0.3f, 0.4f, 0.8f, 0.6f) // Sample data
            val dataPath = androidx.compose.ui.graphics.Path()
            
            for (i in dataValues.indices) {
                val angle = angleStep * i - Math.PI.toFloat() / 2
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
            
            // Fill
            drawPath(
                path = dataPath,
                color = BrightPink.copy(alpha = 0.3f)
            )
            
            // Stroke
            drawPath(
                path = dataPath,
                color = BrightPink,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
            
            // Draw axis lines from center
            for (i in 0 until points) {
                val angle = angleStep * i - Math.PI.toFloat() / 2
                val x = centerX + radius * kotlin.math.cos(angle)
                val y = centerY + radius * kotlin.math.sin(angle)
                
                drawLine(
                    color = TextGray.copy(alpha = 0.2f),
                    start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    end = androidx.compose.ui.geometry.Offset(x, y),
                    strokeWidth = 1f
                )
            }
        }
        
        // Labels
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PHY", fontSize = 10.sp, color = TextGray, modifier = Modifier.padding(top = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PSN", fontSize = 10.sp, color = TextGray, modifier = Modifier.padding(start = 8.dp))
                Text("INT", fontSize = 10.sp, color = TextGray, modifier = Modifier.padding(end = 8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("FOC", fontSize = 10.sp, color = TextGray, modifier = Modifier.padding(bottom = 8.dp))
                Text("DIS", fontSize = 10.sp, color = TextGray, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}

