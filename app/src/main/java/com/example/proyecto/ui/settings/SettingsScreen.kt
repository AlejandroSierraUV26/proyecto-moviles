package com.example.proyecto.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.proyecto.navigation.AppScreens
import com.example.proyecto.utils.DoubleBackToExitHandler

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    
    DoubleBackToExitHandler {
        // Cierra la aplicación
        android.os.Process.killProcess(android.os.Process.myPid())
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Cuenta",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                navController.navigate(AppScreens.EditProfileScreen.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Text(
                "Editar Perfil",
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { navController.navigate(AppScreens.DeleteCourseScreen.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Text(
                "Eliminar Curso",
                fontSize = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(navController = androidx.navigation.compose.rememberNavController())
} 