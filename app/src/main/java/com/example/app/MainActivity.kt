package com.example.app

import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("game_prefs", MODE_PRIVATE)

        setContent {
            AppTheme {
                MainScreen(
                    sharedPreferences = sharedPreferences
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadContent()
    }

    private fun loadContent() {
        setContent {
            AppTheme {
                MainScreen(
                    sharedPreferences = sharedPreferences
                )
            }
        }
    }
}

@Composable
fun MainScreen(sharedPreferences: SharedPreferences) {
    val context = LocalContext.current
    val rating = sharedPreferences.getInt("user_rating", 0)
    println("📱 MAIN SCREEN RATING: $rating")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(200.dp))
            Text(
                text = "Крестики-Нолики",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFFF0),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 60.dp)
            )

            Spacer(modifier = Modifier.width(50.dp))

            Button(
                onClick = {
                    val intent = Intent(context, HowToPlayActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF888888)
                )
            ) {
                Text("Как играть", fontSize = 18.sp, color = Color(0xFFFFFFF0), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.width(50.dp))

            Button(
                onClick = {
                    val intent = Intent(context, GameActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CACE4))
            ) {
                Text("Играть против бота", fontSize = 18.sp, color = Color(0xFFFFFFF0), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(150.dp))

            // Блок с рейтингом - rating БУДЕТ ОБНОВЛЯТЬСЯ при каждом возврате в MainActivity
            Card(
                modifier = Modifier.fillMaxWidth(0.7f).padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Текущий рейтинг", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = rating.toString(), // ← вот тут будет актуальный рейтинг
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6CACE4)
                    )
                    Text("очков", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                text = "Победа: +30 очков • Поражение: -25 очков",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFBB86FC),
        secondary = Color(0xFF03DAC6),
        tertiary = Color(0xFFCF6679),
        background = Color.Black,
        surface = Color(0xFF121212),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.White
    )
    MaterialTheme(
        colorScheme = darkColorScheme,
        content = content
    )
}