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
                // Quote Section
                QuoteSection()
                
                // Statistics Section with Radar Chart
                StatisticsSection()
                
                // System Section
                SystemSection()
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
                // Player Section - Takes most of the space
                PlayerSection()
                
                // Quick Stats Summary
                QuickStatsSection()
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
                text = "Adam",
                fontSize = 18.sp,
                color = White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Player Banner Card with Hunter Silhouette
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00ACC1),
                            Color(0xFF00838F),
                            Color(0xFF006064),
                            Color(0xFF00363A)
                        ),
                        radius = 1200f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Mystical circle effect in background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 3
                
                // Draw glowing circle
                drawCircle(
                    color = Color(0xFF00BCD4).copy(alpha = 0.2f),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color(0xFF00BCD4).copy(alpha = 0.3f),
                    radius = radius * 0.9f,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
                drawCircle(
                    color = Color(0xFF00BCD4).copy(alpha = 0.4f),
                    radius = radius * 0.8f,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
            }
            
            // Hunter Silhouette
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Hunter Avatar",
                tint = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(100.dp)
                    .offset(y = 10.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Level and Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF00BCD4), RoundedCornerShape(8.dp)),
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
                    text = "Level 1 ⚔ The Awakening",
                    fontSize = 16.sp,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rank: E-Rank Hunter",
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
        
        // XP Progress Bar
        Column {
            Text(
                text = "0/100 XP",
                fontSize = 12.sp,
                color = BrightPink,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { 0.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = BrightPink,
                trackColor = Color(0xFF2A2A2A)
            )
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
                value = "0 XP",
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
                    text = "0 Credits",
                    fontSize = 13.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // New Page Button
        OutlinedButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = TextGray
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
            shape = RoundedCornerShape(8.dp)
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
fun QuoteSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "THE SYSTEM USES ME, AND I USE THE SYSTEM",
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
fun StatisticsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Radar Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ImprovedRadarChart()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Powered by credit
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚡",
                fontSize = 12.sp,
                color = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Powered by ChartBase",
                fontSize = 11.sp,
                color = TextGray.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun QuickStatsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(20.dp)
    ) {
        // Quote
        Text(
            text = "THE SYSTEM USES ME, AND I USE THE SYSTEM",
            fontSize = 11.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Mini Radar Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            ImprovedRadarChart()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Powered by
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡",
                fontSize = 10.sp,
                color = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Powered by ChartBase",
                fontSize = 10.sp,
                color = TextGray.copy(alpha = 0.6f)
            )
        }
        
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .padding(24.dp)
    ) {
        // Power Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF0F0F0F), RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(28.dp)),
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
fun ImprovedRadarChart() {
    // Hexagon radar chart matching the reference image
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2.5f
            
            // Draw hexagon radar grid (6 points)
            val points = 6
            val angleStep = (2 * Math.PI / points).toFloat()
            val startAngle = -Math.PI.toFloat() / 2 // Start from top
            
            // Draw concentric hexagons (grid) - 5 levels
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
                
                drawPath(
                    path = path,
                    color = Color(0xFF00BCD4).copy(alpha = 0.15f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
            }
            
            // Draw axis lines from center to each point
            for (i in 0 until points) {
                val angle = startAngle + angleStep * i
                val x = centerX + radius * kotlin.math.cos(angle)
                val y = centerY + radius * kotlin.math.sin(angle)
                
                drawLine(
                    color = Color(0xFF00BCD4).copy(alpha = 0.2f),
                    start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    end = androidx.compose.ui.geometry.Offset(x, y),
                    strokeWidth = 1.5f
                )
            }
            
            // Draw data polygon (filled) - Sample stats values
            val dataValues = listOf(0.85f, 0.95f, 0.78f, 0.65f, 0.72f, 0.88f) // FIT, ?, SOC, INT, DIS, FOC, FIN
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
            
            // Fill with purple/pink gradient effect
            drawPath(
                path = dataPath,
                color = Color(0xFF9C27B0).copy(alpha = 0.5f)
            )
            
            // Stroke outline
            drawPath(
                path = dataPath,
                color = Color(0xFF9C27B0).copy(alpha = 0.8f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )
            
            // Draw data points as circles
            for (i in dataValues.indices) {
                val angle = startAngle + angleStep * i
                val currentRadius = radius * dataValues[i]
                val x = centerX + currentRadius * kotlin.math.cos(angle)
                val y = centerY + currentRadius * kotlin.math.sin(angle)
                
                drawCircle(
                    color = Color(0xFF9C27B0),
                    radius = 4f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
        
        // Labels positioned around the hexagon
        val labels = listOf("FIT", "SOC", "INT", "DIS", "FOC", "FIN")
        Box(modifier = Modifier.fillMaxSize()) {
            // Top label (FIT)
            Text(
                text = labels[0],
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )
            
            // Top-right label (SOC)
            Text(
                text = labels[1],
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 20.dp, top = 40.dp)
            )
            
            // Bottom-right label (INT)
            Text(
                text = labels[2],
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp, bottom = 40.dp)
            )
            
            // Bottom label (DIS)
            Text(
                text = labels[3],
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
            
            // Bottom-left label (FOC)
            Text(
                text = labels[4],
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp, bottom = 40.dp)
            )
            
            // Top-left label (FIN)
            Text(
                text = labels[5],
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 20.dp, top = 40.dp)
            )
        }
    }
}

