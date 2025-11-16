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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.unit.times
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

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

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(R.drawable.rectangle_11),
            contentDescription = "Логотип",
            modifier = Modifier.fillMaxWidth().height(0.3f * screenHeight).width(0.3f * screenWidth).offset(y = 0.1 * screenHeight)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(0.35f * screenHeight))

            Button(
                onClick = {
                    val intent = Intent(context, HowToPlayActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Text("Как играть?", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.width(0.05f * screenHeight))

            Button(
                onClick = {
                    val intent = Intent(context, GameActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Играть против бота", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(0.05 * screenHeight))

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
                        color = if (rating < 0) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.primary
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
        background = Color(0xFF181A1E),
        surfaceContainer = Color(0xFF888888),
        onSurface = Color(0xFFFFFFF0),
        primary = Color(0xFF6CACE4),
        secondary = Color(0xFFe4575e)
    )
    MaterialTheme(
        colorScheme = darkColorScheme,
        content = content
    )
}