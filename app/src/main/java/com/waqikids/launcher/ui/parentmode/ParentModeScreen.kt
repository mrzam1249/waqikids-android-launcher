package com.waqikids.launcher.ui.parentmode

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

/**
 * Parent Mode unlock screen
 * Shows PIN entry to allow parent to temporarily access device
 */
@Composable
fun ParentModeScreen(
    onDismiss: () -> Unit,
    onUnlocked: () -> Unit,
    viewModel: ParentModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(uiState.isUnlocked) {
        if (uiState.isUnlocked) {
            onUnlocked()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B),
                        Color(0xFF312E81)
                    )
                )
            )
    ) {
        if (uiState.isUnlocked) {
            // Show unlocked parent menu
            ParentModeMenu(
                onDismiss = onDismiss,
                onOpenPlayStore = {
                    try {
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_MARKET)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback to Google Play Store package
                        try {
                            val playIntent = context.packageManager.getLaunchIntentForPackage("com.android.vending")
                            if (playIntent != null) {
                                context.startActivity(playIntent)
                            }
                        } catch (_: Exception) {}
                    }
                },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        } else {
            // Show PIN entry
            PinEntryContent(
                uiState = uiState,
                onDismiss = onDismiss,
                onNumberClick = { viewModel.appendDigit(it) },
                onBackspace = { viewModel.deleteLastDigit() },
                onClear = { viewModel.clearPin() }
            )
        }
    }
}

@Composable
private fun ParentModeMenu(
    onDismiss: () -> Unit,
    onOpenPlayStore: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showExitConfirmation by remember { mutableStateOf(false) }
    
    // Exit confirmation dialog
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            containerColor = Color(0xFF1E1B4B),
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Exit Parent Mode?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "You'll need to re-enter your PIN to access Parent Mode again.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmation = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exit Parent Mode",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmation = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Stay in Parent Mode",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Unlocked icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Parent Mode Active",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Access unlocked for 10 minutes",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Menu Options
        MenuOption(
            icon = Icons.Default.ShoppingCart,
            title = "Open Play Store",
            subtitle = "Install or update apps",
            onClick = onOpenPlayStore
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MenuOption(
            icon = Icons.Default.Settings,
            title = "Open Settings",
            subtitle = "Access device settings",
            onClick = onOpenSettings
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Enterprise Exit Button
        Button(
            onClick = { showExitConfirmation = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Exit Parent Mode",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFEF4444)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Exiting will lock device to child mode",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MenuOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C3AED).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun PinEntryContent(
    uiState: ParentModeUiState,
    onDismiss: () -> Unit,
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Lock icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C3AED).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = "Parent Mode",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Enter your 4-digit PIN to unlock",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // PIN Entry
            PinEntryRow(
                pin = uiState.enteredPin,
                hasError = uiState.error != null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = Color(0xFFEF4444),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            // Rate limit warning
            if (uiState.attemptsRemaining != null && uiState.attemptsRemaining!! < 5) {
                Text(
                    text = "${uiState.attemptsRemaining} attempts remaining",
                    color = Color(0xFFFBBF24),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Number pad
            NumberPad(
                onNumberClick = { digit ->
                    if (uiState.enteredPin.length < 4) {
                        onNumberClick(digit)
                    }
                },
                onBackspace = onBackspace,
                onClear = onClear,
                enabled = !uiState.isLoading && !uiState.isLocked
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
}

@Composable
private fun PinEntryRow(
    pin: String,
    hasError: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        repeat(4) { index ->
            PinDot(
                isFilled = index < pin.length,
                hasError = hasError
            )
        }
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    hasError: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1.2f else 1f,
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(20.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                when {
                    hasError -> Color(0xFFEF4444)
                    isFilled -> Color.White
                    else -> Color.White.copy(alpha = 0.2f)
                }
            )
            .border(
                width = 2.dp,
                color = if (hasError) Color(0xFFEF4444) else Color.White.copy(alpha = 0.5f),
                shape = CircleShape
            )
    )
}

@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: 1 2 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NumberButton("1", onClick = { onNumberClick("1") }, enabled = enabled)
            NumberButton("2", onClick = { onNumberClick("2") }, enabled = enabled)
            NumberButton("3", onClick = { onNumberClick("3") }, enabled = enabled)
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Row 2: 4 5 6
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NumberButton("4", onClick = { onNumberClick("4") }, enabled = enabled)
            NumberButton("5", onClick = { onNumberClick("5") }, enabled = enabled)
            NumberButton("6", onClick = { onNumberClick("6") }, enabled = enabled)
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Row 3: 7 8 9
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NumberButton("7", onClick = { onNumberClick("7") }, enabled = enabled)
            NumberButton("8", onClick = { onNumberClick("8") }, enabled = enabled)
            NumberButton("9", onClick = { onNumberClick("9") }, enabled = enabled)
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Row 4: Clear 0 Backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            ActionButton(
                icon = Icons.Default.Clear,
                onClick = onClear,
                enabled = enabled
            )
            NumberButton("0", onClick = { onNumberClick("0") }, enabled = enabled)
            ActionButton(
                icon = Icons.Default.Backspace,
                onClick = onBackspace,
                enabled = enabled
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun NumberButton(
    number: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 0.15f else 0.05f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.3f)
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 0.1f else 0.03f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = if (enabled) 0.7f else 0.2f),
            modifier = Modifier.size(28.dp)
        )
    }
}
