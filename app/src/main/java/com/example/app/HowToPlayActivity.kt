package com.example.app

import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api

class HowToPlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RulesTheme {
                HowToPlayScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToPlayScreen(onBackClick: () -> Unit) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Как играть") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

//                Text(
//                    text = "Как играть",
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
//                    textAlign = TextAlign.Center
//                )

                // Контент с прокруткой
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(scrollState)
                ) {
                    GameRule(
                        title = "1. Цель игры",
                        description = "Соберите 3 своих символа (Х или О) в ряд по горизонтали, вертикали или диагонали",
                        imageRes = R.drawable.win // Замените на ваш ресурс
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GameRule(
                        title = "2. Ход игры",
                        description = "Игроки ходят по очереди. Крестики всегда ходят первыми. Нажмите на любую свободную клетку, чтобы поставить свой символ",
                        imageRes = R.drawable.win
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GameRule(
                        title = "3. Выбор стороны",
                        description = "Перед игрой вы выбираете, за кого будете играть: за крестики (Х) или нолики (О)"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GameRule(
                        title = "4. Игра против бота",
                        description = "Бот использует умный алгоритм и старается выиграть. Будьте внимательны!"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GameRule(
                        title = "5. Система рейтинга",
                        description = "За победу вы получаете 30 очков рейтинга. За поражение теряете 25 очков. Рейтинг может быть отрицательным"
                    )
                }
            }
        }
    }
}

@Composable
fun GameRule(title: String, description: String, imageRes: Int? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (imageRes != null) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Иллюстрация правила: $title",
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp)
                )
            }

            Text(
                text = description,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun RulesTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content, colorScheme = darkColorScheme())
}