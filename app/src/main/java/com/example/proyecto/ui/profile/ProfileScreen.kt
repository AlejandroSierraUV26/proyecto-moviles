package com.example.proyecto.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto.R
import com.example.proyecto.utils.DoubleBackToExitHandler
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun ProfileScreen() {
    DoubleBackToExitHandler {
        android.os.Process.killProcess(android.os.Process.myPid())
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Foto de perfil
        Image(
            painter = painterResource(id = R.drawable.ic_profile),
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Nombre de usuario
        Text(
            text = "Usuario Ejemplo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Sección de Estadísticas
        Text(
            text = "Estadísticas",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tarjetas de estadísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Racha
            StatCard(
                title = "Racha",
                value = "7 días",
                modifier = Modifier.weight(1f)
            )
            
            // Tarjeta de Experiencia
            StatCard(
                title = "Experiencia",
                value = "1,250 XP",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Gráfica de experiencia
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Experiencia Semanal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gráfica de líneas usando Vico
                Chart(
                    chart = lineChart(),
                    model = entryModelOf(0f, 2f, 4f, 6f, 8f, 10f, 12f),
                    startAxis = startAxis(),
                    bottomAxis = bottomAxis()
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
} 