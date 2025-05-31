package com.example.proyecto.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.ExperienceData
import kotlinx.coroutines.launch
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer

@Composable
fun ProfileScreen() {
    var experienceData by remember { mutableStateOf<List<ExperienceData>>(emptyList()) }
    var totalExperience by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Cargar datos de experiencia
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Obtener experiencia total
                val experienceResponse = RetrofitClient.apiService.getUserExperience()
                if (experienceResponse.success) {
                    totalExperience = experienceResponse.totalExperience
                }

                // Obtener datos de los últimos 7 días
                val last7DaysResponse = RetrofitClient.apiService.getLast7DaysExperience()
                experienceData = last7DaysResponse
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }

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
                value = "$totalExperience XP",
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
                
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    error != null -> {
                        Text(
                            text = "Error al cargar los datos: $error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    else -> {
                        // Gráfica de líneas usando Vico con datos reales
                        val experiencePoints = experienceData.map { it.experiencePoints.toFloat() }.toTypedArray()
                        val daysOfWeek = experienceData.map { it.dayOfWeek }.toTypedArray()
                        
                        val bottomAxis = rememberBottomAxis(
                            valueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                                if (value.toInt() in daysOfWeek.indices) {
                                    daysOfWeek[value.toInt()].take(3)
                                } else {
                                    ""
                                }
                            }
                        )
                        
                        Chart(
                            chart = lineChart(),
                            model = entryModelOf(*experiencePoints),
                            startAxis = rememberStartAxis(),
                            bottomAxis = bottomAxis
                        )
                    }
                }
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