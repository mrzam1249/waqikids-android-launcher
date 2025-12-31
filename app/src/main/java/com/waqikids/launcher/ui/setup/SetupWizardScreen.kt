package com.waqikids.launcher.ui.setup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.waqikids.launcher.R
import com.waqikids.launcher.service.DnsVpnService
import com.waqikids.launcher.ui.theme.BackgroundEnd
import com.waqikids.launcher.ui.theme.BackgroundStart
import com.waqikids.launcher.ui.theme.KidBlue
import com.waqikids.launcher.ui.theme.KidGreen
import com.waqikids.launcher.ui.theme.KidOrange
import com.waqikids.launcher.ui.theme.KidPink
import com.waqikids.launcher.ui.theme.KidPurple
import com.waqikids.launcher.ui.theme.Primary
import com.waqikids.launcher.ui.theme.Success
import kotlinx.coroutines.launch

@Composable
fun SetupWizardScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentStep by viewModel.currentSetupStep.collectAsState(initial = 0)
    
    // VPN permission state
    var vpnPermissionGranted by remember { mutableStateOf(false) }
    
    // Notification permission state (Android 13+)
    var notificationPermissionGranted by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Not needed below Android 13
            }
        )
    }
    
    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications help keep protection active", Toast.LENGTH_LONG).show()
        }
    }
    
    // Function to request notification permission
    val requestNotificationPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notificationPermissionGranted = true
                Toast.makeText(context, "Notifications already enabled!", Toast.LENGTH_SHORT).show()
            }
        } else {
            notificationPermissionGranted = true
        }
    }
    
    // VPN permission launcher
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vpnPermissionGranted = true
            // Start VPN service immediately
            val vpnIntent = Intent(context, DnsVpnService::class.java).apply {
                action = DnsVpnService.ACTION_START
            }
            context.startForegroundService(vpnIntent)
            Toast.makeText(context, "Website protection enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "VPN permission required for website filtering", Toast.LENGTH_LONG).show()
        }
    }
    
    // Function to request VPN permission
    val requestVpnPermission: () -> Unit = {
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            // Already have permission
            vpnPermissionGranted = true
            val vpnIntent = Intent(context, DnsVpnService::class.java).apply {
                action = DnsVpnService.ACTION_START
            }
            context.startForegroundService(vpnIntent)
            Toast.makeText(context, "Website protection enabled!", Toast.LENGTH_SHORT).show()
        }
    }
    
    val steps = listOf(
        SetupStepData(
            icon = Icons.Default.Home,
            iconColor = KidBlue,
            title = stringResource(R.string.setup_launcher_title),
            subtitle = stringResource(R.string.setup_launcher_subtitle),
            buttonText = "Set as Home",
            action = {
                // Open home app picker
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                context.startActivity(intent)
            }
        ),
        SetupStepData(
            icon = Icons.Default.AccessibilityNew,
            iconColor = KidGreen,
            title = stringResource(R.string.setup_accessibility_title),
            subtitle = stringResource(R.string.setup_accessibility_subtitle),
            buttonText = "Enable Protection",
            action = {
                // Open accessibility settings
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        ),
        SetupStepData(
            icon = Icons.Default.Notifications,
            iconColor = KidOrange,
            title = "Enable Notifications",
            subtitle = "Allow notifications to receive instant updates when parents change settings.",
            buttonText = if (notificationPermissionGranted) "Notifications Enabled ✓" else "Enable Notifications",
            action = requestNotificationPermission,
            extraContent = { NotificationSetupContent(notificationPermissionGranted) }
        ),
        SetupStepData(
            icon = Icons.Default.VpnKey,
            iconColor = KidPurple,
            title = "Website Protection",
            subtitle = "Enable VPN-based filtering to block unsafe websites and allow only parent-approved sites.",
            buttonText = if (vpnPermissionGranted) "Protection Enabled ✓" else "Enable Website Filter",
            action = requestVpnPermission,
            extraContent = { VpnSetupContent(vpnPermissionGranted) }
        ),
        SetupStepData(
            icon = Icons.Default.Security,
            iconColor = KidPink,
            title = stringResource(R.string.setup_mode_title),
            subtitle = stringResource(R.string.setup_mode_subtitle),
            buttonText = "Complete Setup",
            action = {
                coroutineScope.launch {
                    viewModel.completeSetup()
                    onSetupComplete()
                }
            },
            extraContent = { ModeSelectionContent() }
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Progress indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                steps.forEachIndexed { index, _ ->
                    val isComplete = index < currentStep
                    val isCurrent = index == currentStep
                    
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 14.dp else 10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isComplete -> Success
                                    isCurrent -> Primary
                                    else -> Primary.copy(alpha = 0.3f)
                                }
                            )
                    )
                    
                    if (index < steps.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Step ${currentStep + 1} of ${steps.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = Primary.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Current step content
            val step = steps[currentStep]
            
            // Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(step.iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = step.iconColor
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = step.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Extra content if any
            step.extraContent?.invoke()
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action button
            Button(
                onClick = { step.action() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = step.buttonText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            
            if (currentStep < steps.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { viewModel.setCurrentStep(currentStep + 1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "I've done this",
                        color = Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NotificationSetupContent(isEnabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) Success.copy(alpha = 0.1f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Notifications,
                contentDescription = null,
                tint = if (isEnabled) Success else KidOrange,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (isEnabled) "Notifications Active" else "Enable Notifications",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isEnabled) Success else Primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isEnabled) 
                    "You'll receive instant updates when parents change settings." 
                else 
                    "Notifications allow instant syncing when parents update website permissions.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Benefits list
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VpnBenefitItem("✓ Instant updates from parents")
        VpnBenefitItem("✓ Real-time website permission changes")
        VpnBenefitItem("✓ Protection status notifications")
    }
}

@Composable
private fun VpnSetupContent(isEnabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) Success.copy(alpha = 0.1f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.VpnKey,
                contentDescription = null,
                tint = if (isEnabled) Success else KidPurple,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (isEnabled) "Protection Active" else "Enable VPN Filter",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isEnabled) Success else Primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isEnabled) 
                    "All website traffic is now filtered through WaqiKids protection." 
                else 
                    "This creates a local VPN to filter websites. No data leaves your device.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Benefits list
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VpnBenefitItem("✓ Blocks unsafe websites automatically")
        VpnBenefitItem("✓ Only allows parent-approved sites")
        VpnBenefitItem("✓ Works across all apps and browsers")
        VpnBenefitItem("✓ All filtering happens on-device")
    }
}

@Composable
private fun VpnBenefitItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
    )
}

@Composable
private fun ModeSelectionContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModeCard(
            title = "Easy Mode",
            description = "Strong protection. Can be reset if needed.",
            icon = Icons.Default.Security,
            isSelected = true,
            color = KidGreen
        )
        
        ModeCard(
            title = "Fort Knox Mode",
            description = "Maximum security. Requires factory reset to remove.",
            icon = Icons.Default.Security,
            isSelected = false,
            color = KidPink
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color
                )
            }
        }
    }
}

private data class SetupStepData(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val action: () -> Unit,
    val extraContent: (@Composable () -> Unit)? = null
)
