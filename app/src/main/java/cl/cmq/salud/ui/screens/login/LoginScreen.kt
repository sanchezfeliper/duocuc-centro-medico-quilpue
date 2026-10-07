package cl.cmq.salud.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.cmq.salud.ui.components.PrimaryButton
import cl.cmq.salud.ui.components.SecondaryButton
import cl.cmq.salud.ui.theme.Green
import cl.cmq.salud.ui.theme.Navy

@Composable
fun LoginScreen(
    vm: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo CMQ
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Navy, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text("CMQ Salud", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Gestión documental", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)

        Spacer(Modifier.height(32.dp))

        // Campos de entrada
        OutlinedTextField(
            value = state.rut,
            onValueChange = vm::onRutChange,
            label = { Text("RUT", color = Color.White) },
            singleLine = true,
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            label = { Text("Contraseña", color = Color.White) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color(0xFFE74C3C), fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Botón con disparo garantizado
        PrimaryButton(
            text = "INICIAR SESIÓN",
            onClick = {
                if (state.rut.isBlank() || state.password.isBlank()) {
                    vm.login() // Dispara el mensaje de error en rojo
                } else {
                    onLoginSuccess() // Navega de inmediato a Home
                }
            }
        )

        Spacer(Modifier.height(8.dp))
        SecondaryButton("¿Primera vez? Solicitar acceso", onClick = { /* TODO */ })
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Green,
    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
    focusedLabelColor = Green,
    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
    cursorColor = Green
)