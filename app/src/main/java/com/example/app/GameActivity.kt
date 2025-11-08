package com.example.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.painterResource
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.times
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.max

class GameActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("game_prefs", MODE_PRIVATE)

        setContent {
            GameTheme {
                GameScreen(
                    sharedPreferences = sharedPreferences,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    sharedPreferences: SharedPreferences,
    onBackClick: () -> Unit
) {
    val gameBot = remember { GameBot() }
    var gameState by remember { mutableStateOf(GameState()) }
    var gameStatus by remember { mutableStateOf("Ваш ход") }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val circleItems = rememberCircleItems()
    var availableCircles by remember { mutableStateOf(circleItems.filter { !it.isBot }) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showFirstMoveDialog by remember { mutableStateOf(true) }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var gameResult by remember { mutableStateOf("") } // "win", "lose", "draw"

    LaunchedEffect(gameState.board) {
        val winner = gameState.checkWinnerByCircles(circleItems)
        val isBoardFull = gameState.isBoardFull()

        if (winner != GameBot.EMPTY || isBoardFull) {
            gameResult = when {
                winner == 1 -> "win"
                winner == 2 -> "lose"
                else -> "draw"
            }
            showGameOverDialog = true
            updateRating(sharedPreferences, winner)
        }
    }


    if (showFirstMoveDialog) {
        AlertDialog(
            onDismissRequest = { /* Нельзя закрыть, нужно выбрать */ },
            title = { Text("Кто ходит первым?", color = Color(0xFFFFFFF0)) },
            text = { Text("Выберите, кто будет делать первый ход", color = Color(0xFFFFFFF0)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        gameState = gameState.copy(currentPlayer = 1)
                        gameStatus = "Ваш ход"
                        showFirstMoveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6CACE4)
                    )
                ) {
                    Text("Я первый", color = Color(0xFFFFFFF0))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        gameState = gameState.copy(currentPlayer = 2)
                        gameStatus = "Ход бота"
                        showFirstMoveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6CACE4)
                    )
                ) {
                    Text("Бот первый", color = Color(0xFFFFFFF0))
                }
            }
        )
    }

    // Обработка хода бота
    LaunchedEffect(key1 = gameState.currentPlayer, key2 = gameState.board) {
        val shouldBotMove = gameState.winner == GameBot.EMPTY &&
                !gameState.isBoardFull() &&
                gameState.currentPlayer == 2 &&
                !showFirstMoveDialog

        val currentWinner = gameState.checkWinnerByCircles(circleItems)
        if (currentWinner != GameBot.EMPTY) {
            return@LaunchedEffect // Выходим если уже есть победитель
        }

        if (shouldBotMove) {
            delay(2000)
            val (botRow, botCol) = gameBot.makeMove(gameState.board, 2)
            if (botRow != -1 && botCol != -1) {
                val botCircle = gameBot.findSuitableBotCircle(
                    gameState.board,
                    botRow,
                    botCol,
                    circleItems,
                    gameState.circlePositions
                )

                botCircle?.let { circle ->
                    if (gameState.canPlaceCircle(botRow, botCol, circle.size, circleItems)) {
                        if (gameState.board[botRow][botCol] == GameBot.EMPTY) {
                            gameState = gameState.updateCirclePosition(botRow, botCol, circle.id)
                            gameState = gameState.makeMove(botRow, botCol, 2)
                        } else {
                            gameState = gameState.replaceCircle(botRow, botCol, circle.id, circleItems)
                        }
                        updateGameStatus(gameState, circleItems) { status -> gameStatus = status }

                        if (gameState.winner != GameBot.EMPTY || gameState.isBoardFull()) {
                            showGameOverDialog = true
                            updateRating(sharedPreferences, gameState.winner)
                        }
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Выход из игры", color = Color(0xFFFFFFF0)) },
            text = { Text("Вы уверены, что хотите выйти? Это засчитается как поражение (-25 очков)", color = Color(0xFFFFFFF0)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentRating = sharedPreferences.getInt("user_rating", 0)
                        val newRating = currentRating - 25
                        sharedPreferences.edit().putInt("user_rating", newRating).apply()
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6CACE4)
                    )
                ) {
                    Text("Да, выйти", color = Color(0xFFFFFFF0))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6CACE4)
                    )
                ) {
                    Text("Отмена", color = Color(0xFFFFFFF0))
                }
            }
        )
    }

    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = { /* Нельзя закрыть, нужно выбрать */ },
            title = {
                Text(
                    when (gameResult) {
                        "win" -> "Победа!"
                        "lose" -> "Поражение"
                        else -> "Ничья!"
                    }
                )
            },
            text = {
                Text(
                    when (gameResult) {
                        "win" -> "Вы победили бота! +30 очков"
                        "lose" -> "Бот оказался сильнее... -25 очков"
                        else -> "Игра закончилась вничью"
                    }, color = Color(0xFFFFFFF0)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Новая игра
                        gameState = GameState()
                        gameStatus = "Выберите кто ходит первым"
                        selectedCell = null
                        availableCircles = circleItems.filter { !it.isBot }.map { it.copy(isUsed = false) }
                        showFirstMoveDialog = true
                        showGameOverDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6CACE4)
                    )
                ) {
                    Text("Новая игра", color = Color(0xFFFFFFF0))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Выход в главное меню
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6CACE4)
                    )
                ) {
                    Text("В главное меню", color = Color(0xFFFFFFF0))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Игра", color = Color(0xFFFFFFF0)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (gameState.winner == GameBot.EMPTY && !gameState.isBoardFull()) {
                                showExitDialog = true
                            } else {
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = gameStatus,
                    fontSize = 20.sp,
                    color = Color(0xFFFFFFF0),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                ImageGameBoard(
                    board = gameState.board,
                    circlePositions = gameState.circlePositions,
                    circleItems = circleItems,
                    selectedCell = selectedCell,
                    onCellClick = { row, col ->
                        if (gameState.winner == GameBot.EMPTY &&
                            gameState.currentPlayer == 1
                        ) {
                            selectedCell = Pair(row, col)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                CirclePalette(
                    availableCircles = availableCircles,
                    selectedCell = selectedCell,
                    onCircleClick = { circle ->
                        selectedCell?.let { (row, col) ->
                            if (gameState.winner == GameBot.EMPTY &&
                                gameState.currentPlayer == 1 &&
                                gameState.canPlaceCircle(row, col, circle.size, circleItems)
                            ) {
                                if (gameState.board[row][col] == GameBot.EMPTY) {
                                    gameState = gameState.updateCirclePosition(row, col, circle.id)
                                    gameState = gameState.makeMove(row, col, 1)
                                } else {
                                    gameState = gameState.replaceCircle(row, col, circle.id, circleItems)
                                }

                                availableCircles = availableCircles.map {
                                    if (it.id == circle.id) it.copy(isUsed = true) else it
                                }
                                selectedCell = null
                                updateGameStatus(gameState, circleItems) { status -> gameStatus = status }

                                if (gameState.winner != GameBot.EMPTY || gameState.isBoardFull()) {
                                    showGameOverDialog = true
                                    updateRating(sharedPreferences, gameState.winner)
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ImageGameBoard(
    board: Array<IntArray>,
    circlePositions: Map<Pair<Int, Int>, Int>,
    circleItems: List<CircleItem>,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit
) {
    val imageSize = 300.dp

    Box(modifier = Modifier.size(imageSize)) {
        Image(
            painter = painterResource(id = R.drawable.board),
            contentDescription = "Игровое поле",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        GameBoardOverlay(
            imageSize = imageSize,
            selectedCell = selectedCell,
            onCellClick = onCellClick
        )

        CirclePositionsOverlay(
            circlePositions = circlePositions,
            circleItems = circleItems,
            imageSize = imageSize
        )
    }
}

@Composable
fun GameBoardOverlay(
    imageSize: Dp,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit
) {
    val cellSize = imageSize / 3

    Box(modifier = Modifier.size(imageSize)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            selectedCell?.let { (row, col) ->
                val left = col * cellSize.toPx()
                val top = row * cellSize.toPx()
                drawRect(
                    color = Color(0x8034C759),
                    topLeft = Offset(left, top),
                    size = Size(cellSize.toPx(), cellSize.toPx())
                )
            }
        }

        for (row in 0..2) {
            for (col in 0..2) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = col * cellSize,
                            y = row * cellSize
                        )
                        .size(cellSize)
                        .clickable {
                            onCellClick(row, col)
                        }
                        .background(Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun CirclePositionsOverlay(
    circlePositions: Map<Pair<Int, Int>, Int>,
    circleItems: List<CircleItem>,
    imageSize: Dp
) {
    val cellSize = imageSize / 3

    Box(modifier = Modifier.size(imageSize)) {
        circlePositions.forEach { (position, circleId) ->
            val (row, col) = position
            val circle = circleItems.find { it.id == circleId }

            circle?.let {
                val circleColor = if (it.isBot) Color.Red else Color.Green

                Box(
                    modifier = Modifier
                        .offset(
                            x = col * cellSize + (cellSize - it.size) / 2,
                            y = row * cellSize + (cellSize - it.size) / 2
                        )
                        .size(it.size)
                        .clip(CircleShape)
                        .background(circleColor)
                )
            }
        }
    }
}

@Composable
fun CirclePalette(
    availableCircles: List<CircleItem>,
    selectedCell: Pair<Int, Int>?,
    onCircleClick: (CircleItem) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (selectedCell != null) "Выберите кружок для установки" else "Сначала выберите клетку на поле",
            fontSize = 16.sp,
            color = Color(0xFFFFFFF0),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            availableCircles.forEach { circle ->
                SelectableCircle(
                    circle = circle,
                    isEnabled = selectedCell != null && !circle.isUsed,
                    onClick = { onCircleClick(circle) }
                )
            }
        }
    }
}

@Composable
fun SelectableCircle(
    circle: CircleItem,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val circleColor = if (circle.isUsed) Color.Gray else Color.Green
    val circleAlpha = if (isEnabled) 1f else 0.5f

    Box(
        modifier = Modifier
            .size(60.dp)
            .clickable(
                enabled = isEnabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(circle.size)
                .clip(CircleShape)
                .background(circleColor.copy(alpha = circleAlpha))
        )
    }
}

private fun updateGameStatus(
    gameState: GameState,
    circleItems: List<CircleItem>,
    onStatusUpdate: (String) -> Unit
) {
    // Используем новую проверку по кружкам вместо старой по board
    val winner = gameState.checkWinnerByCircles(circleItems)

    val status = when {
        winner != GameBot.EMPTY -> when (winner) {
            1 -> "Вы победили!"
            2 -> "Бот победил!"
            else -> "Ничья!"
        }
        gameState.isBoardFull() -> "Ничья!"
        gameState.currentPlayer == 1 -> "Ваш ход"
        else -> "Ход бота"
    }

    onStatusUpdate(status)
}

private fun updateRating(
    sharedPreferences: SharedPreferences,
    winner: Int
) {
    val currentRating = sharedPreferences.getInt("user_rating", 0)
    val newRating = when (winner) {
        1 -> currentRating + 30
        2 -> max(0, currentRating - 25)
        else -> currentRating
    }
    sharedPreferences.edit().putInt("user_rating", newRating).apply()
}

@Composable
fun GameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}