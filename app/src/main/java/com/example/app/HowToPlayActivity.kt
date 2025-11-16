package com.example.app

import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp

class HowToPlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RulesTheme {
                HowToPlayScreen(onBackClick = { finish() }, onBackToMenu = { finish() })
            }
        }
    }
}

data class Rule(
    val id: Int,
    val description: String,
    val imageRes: Int
)

val rulesList = listOf(
    Rule(
        id = 1,
        description = "Перед вами поле 3х3. Ваша задача собрать 3 зеленых кружка в ряд или по вертикали, " +
                "или по горизонтали, или по диагонали",
        imageRes = R.drawable.rule1
    ),
    Rule(
        id = 2,
        description = "Игроки ходят по очереди. " +
                "Перед началом игры Вы должны выбрать, кто будет ходить первым: Вы или Ваш противник",
        imageRes = R.drawable.rule2
    ),
    Rule(
        id = 3,
        description = "Нажмите на любую клетку поля, чтобы поставить свою фигуру",
        imageRes = R.drawable.rule3
    ),
    Rule(
        id = 4,
        description = "У вас есть 5 кружочков разного размера. " +
                "На поле своим кружочком вы можете накрыть любой уже стоящий, " +
                "если по размеру Ваш выбранный кружочек строго больше того, что стоит в клетке." +
                "Вы можете накрывать как свои кружочки, так и кружочки противника.",
        imageRes = R.drawable.rule4
    ),
    Rule(
        id = 5,
        description = "За победу вы получате 30 очков рейтинга, за поражение теряете 25. " +
                "Рейтинг может быть отрицательным",
        imageRes = R.drawable.rule5
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToPlayScreen(onBackClick: () -> Unit, onBackToMenu: () -> Unit) {
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
        var currentPage by remember { mutableIntStateOf(0) }
        val totalPages = rulesList.size

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "${currentPage + 1}/$totalPages",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFFFFF0),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = rulesList[currentPage].description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(50.dp))

            Image(
                painter = painterResource(id = rulesList[currentPage].imageRes),
                contentDescription = "Правило",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.weight(0.3f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage > 0) {
                    Button(
                        onClick = { currentPage-- },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF888888)
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .width(150.dp)
                            .padding(end = 24.dp)
                    ) {
                        Text(
                            "Назад",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentPage == totalPages - 1) {
                            onBackToMenu()
                        } else {
                            currentPage++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6CACE4)
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .width(150.dp)
                ) {
                    Text(
                        if (currentPage == totalPages - 1) "В меню" else "Далее",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RulesTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content, colorScheme = darkColorScheme())
}