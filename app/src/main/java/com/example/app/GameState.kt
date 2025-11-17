package com.example.app

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

data class GameState(
    val board: Array<IntArray> = Array(3) { IntArray(3) { GameBot.EMPTY } },
    val currentPlayer: Int = 1, // 1 - пользователь, 2 - бот
    val winner: Int = GameBot.EMPTY,
    val circlePositions: Map<Pair<Int, Int>, Int> = emptyMap()
) {
    fun makeMove(row: Int, col: Int, player: Int): GameState {
        val newBoard = board.map { it.clone() }.toTypedArray()
        newBoard[row][col] = player

        val gameBot = GameBot()
        val newWinner = gameBot.checkWinner(newBoard)

        return this.copy(
            board = newBoard,
            currentPlayer = if (player == 1) 2 else 1,
            winner = newWinner
        )
    }

    fun updateCirclePosition(row: Int, col: Int, circleId: Int?): GameState {
        val newPositions = circlePositions.toMutableMap()
        if (circleId == null) {
            newPositions.remove(Pair(row, col))
        } else {
            newPositions[Pair(row, col)] = circleId
        }
        return this.copy(circlePositions = newPositions)
    }

    fun isBoardFull(): Boolean {
        return board.all { row -> row.all { it != GameBot.EMPTY } }
    }

    fun canPlaceCircle(row: Int, col: Int, newCircleSize: Dp, circleItems: List<CircleItem>): Boolean {
        // Если клетка пустая - можно ставить
        if (board[row][col] == GameBot.EMPTY) return true

        // Если в клетке уже есть кружок, проверяем размер
        val existingCircleId = circlePositions[Pair(row, col)]
        existingCircleId?.let { id ->
            val existingCircle = circleItems.find { it.id == id }
            val newCircle = circleItems.find { it.size == newCircleSize } // Находим кружок по размеру

            // Можно ставить если новый кружок БОЛЬШЕ существующего
            return newCircle != null && existingCircle != null &&
                    newCircle.size > existingCircle.size
        }

        return false
    }

    fun checkWinnerByCircles(circleItems: List<CircleItem>): Int {
        // Проверяем строки
        for (i in 0..2) {
            val rowCircles = listOf(
                circlePositions[Pair(i, 0)],
                circlePositions[Pair(i, 1)],
                circlePositions[Pair(i, 2)]
            )
            if (checkLineWinner(rowCircles, circleItems) != GameBot.EMPTY) {
                return checkLineWinner(rowCircles, circleItems)
            }
        }

        // Проверяем столбцы
        for (j in 0..2) {
            val colCircles = listOf(
                circlePositions[Pair(0, j)],
                circlePositions[Pair(1, j)],
                circlePositions[Pair(2, j)]
            )
            if (checkLineWinner(colCircles, circleItems) != GameBot.EMPTY) {
                return checkLineWinner(colCircles, circleItems)
            }
        }

        // Проверяем диагонали
        val diag1 = listOf(
            circlePositions[Pair(0, 0)],
            circlePositions[Pair(1, 1)],
            circlePositions[Pair(2, 2)]
        )
        if (checkLineWinner(diag1, circleItems) != GameBot.EMPTY) {
            return checkLineWinner(diag1, circleItems)
        }

        val diag2 = listOf(
            circlePositions[Pair(0, 2)],
            circlePositions[Pair(1, 1)],
            circlePositions[Pair(2, 0)]
        )
        if (checkLineWinner(diag2, circleItems) != GameBot.EMPTY) {
            return checkLineWinner(diag2, circleItems)
        }

        return GameBot.EMPTY
    }

    private fun checkLineWinner(circleIds: List<Int?>, circleItems: List<CircleItem>): Int {
        // Все три клетки должны быть заняты
        if (circleIds.any { it == null }) return GameBot.EMPTY

        val circles = circleIds.mapNotNull { id -> circleItems.find { it.id == id } }

        // Проверяем что все три кружка одного цвета (игрока)
        val firstPlayer = if (circles[0].isBot) 2 else 1
        if (circles.all { (if (it.isBot) 2 else 1) == firstPlayer }) {
            return firstPlayer
        }

        return GameBot.EMPTY
    }

    fun replaceCircle(row: Int, col: Int, newCircleId: Int, circleItems: List<CircleItem>): GameState {
        val newCirclePositions = circlePositions.toMutableMap()
        newCirclePositions[Pair(row, col)] = newCircleId

        val newCircle = circleItems.find { it.id == newCircleId }
        val player = if (newCircle?.isBot == true) 2 else 1

        val newBoard = board.map { it.clone() }.toTypedArray()
        newBoard[row][col] = player

        return this.copy(
            circlePositions = newCirclePositions,
            board = newBoard,
            currentPlayer = if (currentPlayer == 1) 2 else 1
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!board.contentDeepEquals(other.board)) return false
        if (currentPlayer != other.currentPlayer) return false
        if (winner != other.winner) return false
        if (circlePositions != other.circlePositions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + currentPlayer
        result = 31 * result + winner
        result = 31 * result + circlePositions.hashCode()
        return result
    }
}

data class CircleItem(
    val id: Int,
    val size: Dp,
    val color: Color,
    val isBot: Boolean = false,
    var isOnField: Boolean = false,
    var isUsed: Boolean = false,
    var position: Pair<Int, Int>? = null
)

@Composable
fun rememberCircleItems(): List<CircleItem> {
    return remember {
        val sizes = listOf(60.dp, 50.dp, 40.dp, 30.dp, 20.dp)

        // Зеленые кружки для игрока (5 штук)
        val playerCircles = sizes.mapIndexed { index, size ->
            CircleItem(
                id = index + 1,
                size = size,
                color = Color.Green,
                isBot = false,
                isUsed = false
            )
        }

        // Красные кружки для бота (5 штук)
        val botCircles = sizes.mapIndexed { index, size ->
            CircleItem(
                id = index + 6,
                size = size,
                color = Color.Red,
                isBot = true,
                isUsed = false
            )
        }

        playerCircles + botCircles
    }
}