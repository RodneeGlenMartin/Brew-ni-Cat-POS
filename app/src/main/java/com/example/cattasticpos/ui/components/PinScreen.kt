package com.example.cattasticpos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter PIN") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                            FluentIcon(
                                imageVector = FluentIcons.ArrowLeft,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.onSurface,
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Admin Access Required",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
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
            if (isError) {
                Text(
                    text = "Incorrect PIN",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
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
                Text(if (isVerifying) "Verifying..." else "Verify")
            }
        }
    }
}
