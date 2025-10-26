package com.example.app

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

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

@Composable
fun GameScreen(
    sharedPreferences: SharedPreferences,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val gameBot = remember { GameBot() }
    var gameState by remember { mutableStateOf(GameState()) }
    var showSymbolSelection by remember { mutableStateOf(true) }
    var gameStatus by remember { mutableStateOf("Выберите сторону") }

    // Обработка хода бота
    LaunchedEffect(key1 = gameState.currentPlayer, key2 = showSymbolSelection) {
        // Проверяем все условия для хода бота в одном месте
        val shouldBotMove = !showSymbolSelection &&
                gameState.winner == GameBot.EMPTY &&
                !gameState.isBoardFull() &&
                gameState.currentPlayer == gameState.botSymbol

        if (shouldBotMove) {
            delay(1000) // Задержка для реалистичности

            val (botRow, botCol) = gameBot.makeMove(gameState.board, gameState.botSymbol)
            if (botRow != -1 && botCol != -1) {
                gameState = gameState.makeMove(botRow, botCol, gameState.botSymbol)
                updateGameStatus(gameState, gameBot) { status ->
                    gameStatus = status
                }

                // Обновляем рейтинг если игра окончена
                if (gameState.winner != GameBot.EMPTY || gameState.isBoardFull()) {
                    updateRating(sharedPreferences, gameState.winner, gameState.userSymbol)
                }
            }
        }
    }

    // Показываем диалог выбора символа
    if (showSymbolSelection) {
        AlertDialog(
            onDismissRequest = { /* Нельзя закрыть по клику вне диалога */ },
            title = { Text("Выберите сторону") },
            text = {
                Column {
                    Text("За кого хотите играть?")
                    Text("Крестики ходят первыми", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            gameState = gameState.copy(
                                userSymbol = GameBot.SYMBOL_X,
                                botSymbol = GameBot.SYMBOL_O,
                                currentPlayer = GameBot.SYMBOL_X
                            )
                            showSymbolSelection = false
                            gameStatus = "Ваш ход (Х)"
                        }
                    ) {
                        Text("Крестики (Х)")
                    }

                    Button(
                        onClick = {
                            gameState = gameState.copy(
                                userSymbol = GameBot.SYMBOL_O,
                                botSymbol = GameBot.SYMBOL_X,
                                currentPlayer = GameBot.SYMBOL_X
                            )
                            showSymbolSelection = false
                            gameStatus = "Ход бота (Х)"
                        }
                    ) {
                        Text("Нолики (О)")
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Статус игры
        Text(
            text = gameStatus,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Игровое поле 3x3
        GameBoard(
            board = gameState.board,
            onCellClick = { row, col ->
                if (!showSymbolSelection &&
                    gameState.winner == GameBot.EMPTY &&
                    gameState.currentPlayer == gameState.userSymbol &&
                    gameState.board[row][col] == GameBot.EMPTY
                ) {
                    // Ход пользователя
                    gameState = gameState.makeMove(row, col, gameState.userSymbol)
                    updateGameStatus(gameState, gameBot) { status ->
                        gameStatus = status
                    }

                    // Обновляем рейтинг если игра окончена
                    if (gameState.winner != GameBot.EMPTY || gameState.isBoardFull()) {
                        updateRating(sharedPreferences, gameState.winner, gameState.userSymbol)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Кнопка новой игры
        Button(
            onClick = {
                gameState = GameState()
                showSymbolSelection = true
                gameStatus = "Выберите сторону"
            },
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Text("Новая игра")
        }

        // Кнопка назад
        Button(onClick = onBackClick) {
            Text("В главное меню")
        }
    }
}

@Composable
fun GameBoard(
    board: Array<IntArray>,
    onCellClick: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        for (i in 0..2) {
            Row {
                for (j in 0..2) {
                    GameCell(
                        cellState = board[i][j],
                        onClick = { onCellClick(i, j) },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameCell(
    cellState: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellColor = when (cellState) {
        GameBot.SYMBOL_X -> MaterialTheme.colorScheme.primary
        GameBot.SYMBOL_O -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val cellText = when (cellState) {
        GameBot.SYMBOL_X -> "X"
        GameBot.SYMBOL_O -> "O"
        else -> ""
    }

    val textColor = when (cellState) {
        GameBot.SYMBOL_X -> MaterialTheme.colorScheme.onPrimary
        GameBot.SYMBOL_O -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cellColor)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cellText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// Функция для обновления статуса игры
private fun updateGameStatus(
    gameState: GameState,
    gameBot: GameBot,
    onStatusUpdate: (String) -> Unit
) {
    val winner = gameBot.checkWinner(gameState.board)

    val status = when {
        winner != GameBot.EMPTY -> when (winner) {
            gameState.userSymbol -> "Вы победили! 🎉"
            gameState.botSymbol -> "Бот победил! 🤖"
            else -> "Ничья!"
        }
        gameState.isBoardFull() -> "Ничья!"
        gameState.currentPlayer == gameState.userSymbol -> "Ваш ход"
        else -> "Ход бота..."
    }

    onStatusUpdate(status)
}

// Функция для обновления рейтинга
private fun updateRating(
    sharedPreferences: SharedPreferences,
    winner: Int,
    userSymbol: Int
) {
    val currentRating = sharedPreferences.getInt("user_rating", 0)
    val newRating = when {
        winner == userSymbol -> currentRating + 30
        winner != GameBot.EMPTY && winner != userSymbol -> maxOf(0, currentRating - 25)
        else -> currentRating
    }
    sharedPreferences.edit().putInt("user_rating", newRating).apply()
}

// Состояние игры
data class GameState(
    val board: Array<IntArray> = Array(3) { IntArray(3) { GameBot.EMPTY } },
    val currentPlayer: Int = GameBot.SYMBOL_X,
    val userSymbol: Int = GameBot.SYMBOL_X,
    val botSymbol: Int = GameBot.SYMBOL_O,
    val winner: Int = GameBot.EMPTY
) {
    fun makeMove(row: Int, col: Int, symbol: Int): GameState {
        val newBoard = board.map { it.clone() }.toTypedArray()
        newBoard[row][col] = symbol

        // Проверяем победителя после хода
        val gameBot = GameBot()
        val newWinner = gameBot.checkWinner(newBoard)

        return this.copy(
            board = newBoard,
            currentPlayer = if (symbol == GameBot.SYMBOL_X) GameBot.SYMBOL_O else GameBot.SYMBOL_X,
            winner = newWinner
        )
    }

    fun isBoardFull(): Boolean {
        return board.all { row -> row.all { it != GameBot.EMPTY } }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GameState
        if (!board.contentDeepEquals(other.board)) return false
        if (currentPlayer != other.currentPlayer) return false
        if (userSymbol != other.userSymbol) return false
        if (botSymbol != other.botSymbol) return false
        if (winner != other.winner) return false
        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + currentPlayer
        result = 31 * result + userSymbol
        result = 31 * result + botSymbol
        result = 31 * result + winner
        return result
    }
}

@Composable
fun GameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}