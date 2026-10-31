package cl.cmq.salud.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import cl.cmq.salud.ui.components.PrimaryButton
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Blue
import cl.cmq.salud.ui.theme.Border
import cl.cmq.salud.ui.theme.Green
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.Red
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.CircleShape

@Composable
fun LoginScreen(
    vm: LoginViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit
) {
    val state by vm.uiState.collectAsState()
    val passwordVisible = rememberSaveable { mutableStateOf(false) }

    // Reacciona al cambio de estado de autenticación del ViewModel.
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onLoginSuccess(state.rut.trim())
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(36.dp))
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(Blue, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CMQ", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("CMQ Salud", color = Navy, style = MaterialTheme.typography.titleLarge)
                    Text("Quilpué · Gestión documental", color = Grey, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("Acceso al sistema", color = Navy, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(6.dp))
            Text("Ingresa con tus credenciales institucionales.", color = Grey, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        "Credenciales",
                        style = MaterialTheme.typography.titleLarge,
                        color = Navy
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.rut,
                        onValueChange = vm::onRutChange,
                        label = { Text("RUT") },
                        placeholder = { Text("12.345.678-9") },
                        singleLine = true,
                        isError = state.errorMessage != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = vm::onPasswordChange,
                        label = { Text("Contraseña") },
                        placeholder = { Text("Ingresa tu contraseña") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible.value) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                                val icon = if (passwordVisible.value) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                val desc = if (passwordVisible.value) "Ocultar contraseña" else "Mostrar contraseña"
                                androidx.compose.material3.Icon(
                                    imageVector = icon,
                                    contentDescription = desc,
                                    tint = Grey
                                )
                            }
                        },
                        isError = state.errorMessage != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.errorMessage?.let { message ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            message,
                            color = Red,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(
                        text = "Ingresar al sistema",
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        onClick = { vm.login() },
                        enabled = !state.isAuthenticating,
                        isLoading = state.isAuthenticating
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "¿Primera vez o necesitas recuperar el acceso? Consulta con la persona administradora de tu centro.",
                        color = Grey,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Navy,
    unfocusedTextColor = Navy,
    focusedBorderColor = Green,
    unfocusedBorderColor = Border,
    focusedLabelColor = Green,
    unfocusedLabelColor = Grey,
    cursorColor = Green,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorBorderColor = Red,
    errorLabelColor = Red
)
