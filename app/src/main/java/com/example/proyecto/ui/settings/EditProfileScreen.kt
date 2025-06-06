package com.example.proyecto.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.R
import com.example.proyecto.data.models.UserProfile
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.example.proyecto.navigation.AppScreens
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.proyecto.ui.profile.ProfileViewModel

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Estados para los campos de texto
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Estados para mostrar/ocultar contraseñas
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    // Estados para los errores
    var usernameError by remember { mutableStateOf<String?>(null) }
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Estado para el diálogo de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var showDeletePassword by remember { mutableStateOf(false) }
    var deletePasswordError by remember { mutableStateOf<String?>(null) }
    
    // Estado para el diálogo de éxito
    var showSuccessDialog by remember { mutableStateOf(false) }
    // Estado para controlar si es una actualización
    var isUpdating by remember { mutableStateOf(false) }

    var showDeleteImageDialog by remember { mutableStateOf(false) }
    
    // Launcher para seleccionar imagen de la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { profileViewModel.updateProfileImage(it) }
    }

    // Función para validar los campos
    fun validateFields(): Boolean {
        var isValid = true
        
        // Validar username
        if (username.isBlank()) {
            usernameError = "Campo obligatorio"
            isValid = false
        } else {
            usernameError = null
        }

        // Validar campos de contraseña
        if (currentPassword.isNotBlank() || newPassword.isNotBlank() || confirmPassword.isNotBlank()) {
            if (currentPassword.isBlank()) {
                currentPasswordError = "Campo obligatorio"
                isValid = false
            } else {
                currentPasswordError = null
            }

            if (newPassword.isBlank()) {
                newPasswordError = "Campo obligatorio"
                isValid = false
            } else {
                newPasswordError = null
            }

            if (confirmPassword.isBlank()) {
                confirmPasswordError = "Campo obligatorio"
                isValid = false
            } else {
                confirmPasswordError = null
            }

            if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                newPasswordError = "Las contraseñas no coinciden"
                confirmPasswordError = "Las contraseñas no coinciden"
                isValid = false
            }
        }

        return isValid
    }

    // Cargar datos del usuario al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Actualizar campos cuando se cargan los datos
    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val userProfile = (profileState as ProfileState.Success).userProfile
            username = userProfile.username
            email = userProfile.email
            // Mostrar diálogo de éxito solo si es una actualización exitosa
            if (isUpdating) {
                showSuccessDialog = true
                isUpdating = false
                // Limpiar campos de contraseña después de una actualización exitosa
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                // Limpiar errores
                currentPasswordError = null
                newPasswordError = null
                confirmPasswordError = null
            }
        } else if (profileState is ProfileState.Error) {
            // Manejar errores específicos
            val errorMessage = (profileState as ProfileState.Error).message
            when {
                errorMessage.contains("Contraseña actual incorrecta", ignoreCase = true) -> {
                    currentPasswordError = "Contraseña actual incorrecta"
                    newPasswordError = null
                    confirmPasswordError = null
                }
                errorMessage.contains("Error al actualizar", ignoreCase = true) -> {
                    currentPasswordError = "Error al actualizar"
                    newPasswordError = null
                    confirmPasswordError = null
                }
                else -> {
                    currentPasswordError = errorMessage
                    newPasswordError = null
                    confirmPasswordError = null
                }
            }
            isUpdating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Editar Perfil",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Imagen de perfil con botones de acción
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            if (profileViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                if (profileViewModel.profileImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profileViewModel.profileImageUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .build(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Después de los botones de acción
        if (profileViewModel.error != null) {
            Text(
                text = profileViewModel.error!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Botones de acción para la imagen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cambiar Foto")
            }
            
            if (profileViewModel.profileImageUrl != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showDeleteImageDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Eliminar")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (profileState) {
            is ProfileState.Loading -> {
                CircularProgressIndicator()
            }
            is ProfileState.Error, is ProfileState.Success, ProfileState.Initial -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        // Campos de texto
                        OutlinedTextField(
                            value = username,
                            onValueChange = { 
                                username = it
                                if (it.isBlank()) {
                                    usernameError = "Campo obligatorio"
                                } else {
                                    usernameError = null
                                }
                            },
                            label = { Text("Usuario", fontSize = 16.sp) },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
                            isError = usernameError != null,
                            supportingText = {
                                if (usernameError != null) {
                                    Text(
                                        text = usernameError!!,
                                        color = Color.Red
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { },
                            label = { Text("Correo electrónico", fontSize = 16.sp) },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
                            enabled = false,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray
                            )
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        // Campo de contraseña actual
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { 
                                currentPassword = it
                                if (it.isNotBlank() && newPassword.isBlank() && confirmPassword.isBlank()) {
                                    newPasswordError = "Campo obligatorio"
                                    confirmPasswordError = "Campo obligatorio"
                                } else {
                                    newPasswordError = null
                                    confirmPasswordError = null
                                }
                            },
                            label = { Text("Contraseña actual", fontSize = 16.sp) },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
                            visualTransformation = if (showCurrentPassword) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                androidx.compose.ui.text.input.PasswordVisualTransformation()
                            },
                            isError = currentPasswordError != null,
                            supportingText = {
                                if (currentPasswordError != null) {
                                    Text(
                                        text = currentPasswordError!!,
                                        color = Color.Red
                                    )
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (showCurrentPassword) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                        ),
                                        contentDescription = if (showCurrentPassword) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        // Campo de nueva contraseña
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { 
                                newPassword = it
                                if (it.isNotBlank() && confirmPassword.isNotBlank() && it != confirmPassword) {
                                    newPasswordError = "Las contraseñas no coinciden"
                                    confirmPasswordError = "Las contraseñas no coinciden"
                                } else {
                                    newPasswordError = null
                                    confirmPasswordError = null
                                }
                            },
                            label = { Text("Nueva contraseña", fontSize = 16.sp) },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
                            visualTransformation = if (showNewPassword) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                androidx.compose.ui.text.input.PasswordVisualTransformation()
                            },
                            isError = newPasswordError != null,
                            supportingText = {
                                if (newPasswordError != null) {
                                    Text(
                                        text = newPasswordError!!,
                                        color = Color.Red
                                    )
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (showNewPassword) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                        ),
                                        contentDescription = if (showNewPassword) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        // Campo de confirmar nueva contraseña
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                if (it.isNotBlank() && newPassword.isNotBlank() && it != newPassword) {
                                    newPasswordError = "Las contraseñas no coinciden"
                                    confirmPasswordError = "Las contraseñas no coinciden"
                                } else {
                                    newPasswordError = null
                                    confirmPasswordError = null
                                }
                            },
                            label = { Text("Confirmar nueva contraseña", fontSize = 16.sp) },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
                            visualTransformation = if (showConfirmPassword) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                androidx.compose.ui.text.input.PasswordVisualTransformation()
                            },
                            isError = confirmPasswordError != null,
                            supportingText = {
                                if (confirmPasswordError != null) {
                                    Text(
                                        text = confirmPasswordError!!,
                                        color = Color.Red
                                    )
                                }
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
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        // Botón de confirmar cambios
                        Button(
                            onClick = {
                                if (validateFields()) {
                                    isUpdating = true
                                    if (newPassword.isNotBlank() || confirmPassword.isNotBlank()) {
                                        // Si se intenta cambiar la contraseña, verificar que todos los campos estén llenos
                                        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                                            viewModel.updateProfile(username, "", "", "")
                                        } else {
                                            viewModel.updateProfile(username, currentPassword, newPassword, confirmPassword)
                                        }
                                    } else {
                                        // Si no se intenta cambiar la contraseña, solo actualizar username
                                        viewModel.updateProfile(username, "", "", "")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmar Cambios")
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
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
                    }
                }
            }
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
                    Column {
                        Text("¿Estás seguro que deseas eliminar tu cuenta?")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = deletePassword,
                            onValueChange = { 
                                deletePassword = it
                                deletePasswordError = null
                            },
                            label = { Text("Contraseña", fontSize = 16.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
                            visualTransformation = if (showDeletePassword) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                androidx.compose.ui.text.input.PasswordVisualTransformation()
                            },
                            isError = deletePasswordError != null,
                            supportingText = {
                                if (deletePasswordError != null) {
                                    Text(
                                        text = deletePasswordError!!,
                                        color = Color.Red
                                    )
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = { showDeletePassword = !showDeletePassword }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (showDeletePassword) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                        ),
                                        contentDescription = if (showDeletePassword) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (deletePassword.isBlank()) {
                                deletePasswordError = "Campo obligatorio"
                            } else {
                                scope.launch {
                                    val result = viewModel.deleteAccount(deletePassword)
                                    if (result) {
                                        showDeleteDialog = false
                                        showSuccessDialog = true
                                        // Navegar al login después de eliminar la cuenta
                                        navController.navigate(AppScreens.LoginScreen.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    } else {
                                        deletePasswordError = "Contraseña incorrecta"
                                    }
                                }
                            }
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

        // Diálogo de éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = {
                    Text(
                        text = "¡Éxito!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                },
                text = {
                    Text("Los cambios han sido guardados correctamente")
                },
                confirmButton = {
                    Button(
                        onClick = { showSuccessDialog = false },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Green
                        )
                    ) {
                        Text("Aceptar")
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            )
        }

        // Diálogo de confirmación para eliminar imagen
        if (showDeleteImageDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteImageDialog = false },
                title = {
                    Text(
                        text = "Eliminar Foto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("¿Estás seguro que deseas eliminar tu foto de perfil?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            profileViewModel.deleteProfileImage()
                            showDeleteImageDialog = false
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
                        onClick = { showDeleteImageDialog = false }
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
} 