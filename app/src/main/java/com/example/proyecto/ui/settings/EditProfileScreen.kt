package com.example.proyecto.ui.settings

import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.proyecto.R

@Composable
fun EditProfileScreen() {
    // Estados para los campos de texto
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Estados para mostrar/ocultar contraseñas
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    // Estado para el diálogo de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Editar Perfil",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Imagen de perfil circular
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Foto de perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Campos de texto
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp)
        )

        // Campo de contraseña con botón para mostrar/ocultar
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
            visualTransformation = if (showPassword) {
                androidx.compose.ui.text.input.VisualTransformation.None
            } else {
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        painter = painterResource(
                            id = if (showPassword) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        ),
                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )

        // Campo de confirmar contraseña con botón para mostrar/ocultar
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña", fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
            visualTransformation = if (showConfirmPassword) {
                androidx.compose.ui.text.input.VisualTransformation.None
            } else {
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        painter = painterResource(
                            id = if (showConfirmPassword) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        ),
                        contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de confirmar cambios
        Button(
            onClick = {
                // TODO: Implementar lógica para guardar cambios
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar Cambios")
        }

        // Botón de eliminar cuenta
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.Red
            )
        ) {
            Text("Eliminar Cuenta")
        }

        // Diálogo de confirmación para eliminar cuenta
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = "Eliminando Cuenta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("¿Estás seguro que deseas eliminar tu cuenta?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // TODO: Implementar lógica para eliminar cuenta
                            showDeleteDialog = false
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("No")
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            )
        }
    }
} 