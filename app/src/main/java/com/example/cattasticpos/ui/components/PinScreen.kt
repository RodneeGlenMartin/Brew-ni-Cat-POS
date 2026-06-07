package com.example.cattasticpos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cattasticpos.ui.adaptive.AdaptiveScaffold
import com.example.cattasticpos.ui.adaptive.AdaptiveTopAppBar
import com.example.cattasticpos.ui.adaptive.CupertinoSection
import com.example.cattasticpos.ui.adaptive.LocalCupertinoColors
import com.example.cattasticpos.ui.components.unstyled.PosOutlinedTextField
import com.example.cattasticpos.ui.components.unstyled.PosPrimaryButton
import com.example.cattasticpos.ui.config.PinGateViewModel
import com.example.cattasticpos.ui.icons.FluentIcon
import com.example.cattasticpos.ui.icons.FluentIcons
import kotlinx.coroutines.launch

class BulletVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val mask = "\u2022"
        val transformedText = mask.repeat(text.length)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset
            override fun transformedToOriginal(offset: Int): Int = offset
        }
        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    onPinSuccess: () -> Unit,
    onCancel: () -> Unit,
    pinGateViewModel: PinGateViewModel = viewModel(factory = PinGateViewModel.Factory)
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val cupertino = LocalCupertinoColors.current

    AdaptiveScaffold(
        topBar = {
            AdaptiveTopAppBar(
                title = "Admin Access",
                largeTitle = false,
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                            FluentIcon(
                                imageVector = FluentIcons.ArrowLeft,
                                contentDescription = "Cancel",
                                tint = cupertino.accent,
                                size = 24.dp
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Enter PIN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                "Admin access is required to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
            )

            CupertinoSection {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(
                                        color = if (index < pin.length) {
                                            cupertino.accent
                                        } else {
                                            cupertino.fill
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    PosOutlinedTextField(
                        value = pin,
                        onValueChange = {
                            val digits = it.filter { char -> char.isDigit() }
                            if (digits.length <= 4) {
                                pin = digits
                                isError = false
                            }
                        },
                        label = "PIN",
                        isError = isError,
                        keyboardType = KeyboardType.NumberPassword,
                        visualTransformation = BulletVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (isError) {
                Text(
                    text = "Incorrect PIN",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            PosPrimaryButton(
                onClick = {
                    scope.launch {
                        isVerifying = true
                        val verified = pinGateViewModel.verifyPin(pin)
                        isVerifying = false
                        if (verified) {
                            onPinSuccess()
                        } else {
                            isError = true
                            pin = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = pin.length == 4 && !isVerifying
            ) {
                Text(
                    text = if (isVerifying) "Verifying..." else "Unlock",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}
