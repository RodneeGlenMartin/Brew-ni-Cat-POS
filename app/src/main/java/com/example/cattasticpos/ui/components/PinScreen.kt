package com.example.cattasticpos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

import com.example.cattasticpos.domain.model.AppConfig

class PawVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val paw = "🐾"
        val pawLength = paw.length
        val transformedText = paw.repeat(text.length)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset * pawLength
            override fun transformedToOriginal(offset: Int): Int = offset / pawLength
        }
        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    expectedPinHash: String,
    onPinSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter PIN") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancel")
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
            Text("Admin Access Required", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { 
                    if (it.length <= 4) {
                        pin = it
                        isError = false
                    }
                },
                label = { Text("PIN") },
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PawVisualTransformation(),
                singleLine = true,
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
            Button(
                onClick = {
                    if (AppConfig.verifyPin(pin, expectedPinHash)) {
                        onPinSuccess()
                    } else {
                        isError = true
                        pin = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = pin.length == 4
            ) {
                Text("Verify")
            }
        }
    }
}
