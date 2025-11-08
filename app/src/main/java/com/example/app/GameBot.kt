package com.example.app
class GameBot {
    companion object {
        const val EMPTY = 0
    }

    // Проверка победителя
    fun checkWinner(board: Array<IntArray>): Int {
        // Проверяем строки
        for (i in 0..2) {
            if (board[i][0] != EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0]
            }
        }

        // Проверяем столбцы
        for (j in 0..2) {
            if (board[0][j] != EMPTY && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                return board[0][j]
            }
        }

        // Проверяем диагонали
        if (board[0][0] != EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0]
        }
        if (board[0][2] != EMPTY && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2]
        }

        return EMPTY
    }

    // Ход бота
    fun makeMove(board: Array<IntArray>, botSymbol: Int): Pair<Int, Int> {
        // 1. Попробовать выиграть
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    val testBoard = copyBoard(board)
                    testBoard[i][j] = botSymbol
                    if (checkWinner(testBoard) == botSymbol) {
                        return Pair(i, j)
                    }
                }
            }
        }

        // 2. Попробовать заблокировать игрока
        val playerSymbol = if (botSymbol == 1) 2 else 1
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    val testBoard = copyBoard(board)
                    testBoard[i][j] = playerSymbol
                    if (checkWinner(testBoard) == playerSymbol) {
                        return Pair(i, j)
                    }
                }
            }
        }

        // 3. Занять центр если свободен
        if (board[1][1] == EMPTY) {
            return Pair(1, 1)
        }

        // 4. Занять углы
        val corners = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2))
        for (corner in corners.shuffled()) {
            if (board[corner.first][corner.second] == EMPTY) {
                return corner
            }
        }

        // 5. Любая свободная клетка
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    return Pair(i, j)
                }
            }
        }

        return Pair(-1, -1) // Нет ходов
    }

    // Вспомогательная функция для копирования доски
    private fun copyBoard(board: Array<IntArray>): Array<IntArray> {
        return board.map { it.clone() }.toTypedArray()
    }

    // Проверка на ничью (можно использовать из GameState)
    fun isBoardFull(board: Array<IntArray>): Boolean {
        return board.all { row -> row.all { it != EMPTY } }
    }

    // В классе GameBot добавь метод:
    fun findSuitableBotCircle(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        circleItems: List<CircleItem>,
        circlePositions: Map<Pair<Int, Int>, Int>
    ): CircleItem? {
        val availableBotCircles = circleItems.filter {
            it.isBot && !circlePositions.values.contains(it.id)
        }

        // Если клетка пустая - берем любой доступный кружок
        if (board[row][col] == EMPTY) {
            return availableBotCircles.firstOrNull()
        }

        // Если клетка занята - находим самый маленький кружок, который БОЛЬШЕ существующего
        val existingCircleId = circlePositions[Pair(row, col)]
        existingCircleId?.let { id ->
            val existingCircle = circleItems.find { it.id == id }
            existingCircle?.let { existing ->
                // Ищем самый маленький кружок, который больше существующего
                return availableBotCircles
                    .filter { it.size > existing.size }
                    .minByOrNull { it.size }
            }
        }

        return null
    }
}