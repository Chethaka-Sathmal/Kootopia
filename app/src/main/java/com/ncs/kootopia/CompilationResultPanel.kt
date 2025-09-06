package com.ncs.kootopia

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ncs.kootopia.ui.theme.KootopiaColors

@Composable
fun CompilationResultPanel(
    isVisible: Boolean,
    isCompiling: Boolean,
    result: String,
    onDismiss: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    var panelHeight by remember { mutableStateOf(200.dp) }
    
    val animatedHeight by animateFloatAsState(
        targetValue = if (isVisible && isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "panel_height"
    )

    if (isVisible) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KootopiaColors.primaryDark) // Editor background color
        ) {
            // Header bar with controls (using app header color)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KootopiaColors.surfaceDark) // App header color
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCompiling) "Compiling..." else "Compilation Result",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompiling) KootopiaColors.accentBlue else {
                            when {
                                result.contains("failed", ignoreCase = true) || 
                                result.contains("error", ignoreCase = true) -> KootopiaColors.errorRed
                                result.contains("successful", ignoreCase = true) -> KootopiaColors.successGreen
                                else -> KootopiaColors.textPrimary
                            }
                        }
                    )
                    
                    if (isCompiling) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = KootopiaColors.accentBlue,
                            strokeWidth = 2.dp
                        )
                    }
                }
                
                Row {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = KootopiaColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = KootopiaColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Content area (collapsible and resizable)
            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(panelHeight)
                        .background(KootopiaColors.primaryDark) // Editor background color
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                val newHeight = (panelHeight + dragAmount.y.dp).coerceIn(120.dp, 400.dp)
                                panelHeight = newHeight
                            }
                        }
                        .padding(12.dp)
                ) {
                    if (isCompiling) {
                        // Show loading state
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = KootopiaColors.accentBlue
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Compiling your code...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = KootopiaColors.textPrimary
                            )
                        }
                    } else {
                        // Show result
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                result.contains("failed", ignoreCase = true) || 
                                result.contains("error", ignoreCase = true) -> KootopiaColors.errorRed
                                result.contains("successful", ignoreCase = true) -> KootopiaColors.successGreen
                                else -> KootopiaColors.textPrimary
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
