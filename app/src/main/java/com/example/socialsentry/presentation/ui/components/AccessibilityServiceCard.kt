package com.example.socialsentry.presentation.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AccessibilityServiceCard(
    isServiceEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isServiceEnabled) {
<<<<<<< HEAD
                MaterialTheme.colorScheme.primaryContainer
=======
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) // Pink tinted background
>>>>>>> master
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isServiceEnabled) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isServiceEnabled) {
<<<<<<< HEAD
                    MaterialTheme.colorScheme.onPrimaryContainer
=======
                    MaterialTheme.colorScheme.primary // Pink for enabled state
>>>>>>> master
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Accessibility Service",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = if (isServiceEnabled) {
                        "Service is enabled. Tap to open system settings to disable."
                    } else {
                        "Service is disabled. Tap to enable in system settings."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Open Settings",
<<<<<<< HEAD
                tint = MaterialTheme.colorScheme.primary
=======
                tint = MaterialTheme.colorScheme.primary // Pink to match the design
>>>>>>> master
            )
        }
        
        AnimatedVisibility(visible = !isServiceEnabled) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = "Social Sentry needs accessibility permission to detect when you enter Reels, Shorts, or Stories and redirect you back to the main feed. No personal data is collected.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
