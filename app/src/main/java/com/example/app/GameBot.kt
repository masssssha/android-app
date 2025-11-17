package com.example.app
class GameBot {
    companion object {
        const val EMPTY = 0
    }

    // Проверка победителя
    private var difficulty: Difficulty = Difficulty.MEDIUM

    fun setDifficulty(difficulty: Difficulty) {
        this.difficulty = difficulty
    }

    fun makeMove(board: Array<IntArray>, player: Int): Pair<Int, Int> {
        return when (difficulty) {
            Difficulty.EASY -> makeEasyMove(board, player)
            Difficulty.MEDIUM -> makeMediumMove(board, player)
            Difficulty.HARD -> makeHardMove(board, player)
        }
    }

    private fun makeEasyMove(board: Array<IntArray>, player: Int): Pair<Int, Int> {
        // Простые случайные ходы
        val availableMoves = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    availableMoves.add(Pair(i, j))
                }
            }
        }
        return if (availableMoves.isNotEmpty()) {
            availableMoves.random()
        } else {
            Pair(-1, -1)
        }
    }

    private fun makeMediumMove(board: Array<IntArray>, player: Int): Pair<Int, Int> {
        // Сначала проверяем можем ли выиграть
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    val testBoard = board.map { it.clone() }.toTypedArray()
                    testBoard[i][j] = player
                    if (checkWinner(testBoard) == player) {
                        return Pair(i, j)
                    }
                }
            }
        }

        // Затем блокируем ход противника
        val opponent = if (player == 1) 2 else 1
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    val testBoard = board.map { it.clone() }.toTypedArray()
                    testBoard[i][j] = opponent
                    if (checkWinner(testBoard) == opponent) {
                        return Pair(i, j)
                    }
                }
            }
        }

        // Иначе случайный ход
        return makeEasyMove(board, player)
    }

    private fun makeHardMove(board: Array<IntArray>, player: Int): Pair<Int, Int> {
        // Сначала пытаемся выиграть
        val winMove = findWinningMove(board, player)
        if (winMove.first != -1) return winMove

        // Затем блокируем противника
        val opponent = if (player == 1) 2 else 1
        val blockMove = findWinningMove(board, opponent)
        if (blockMove.first != -1) return blockMove

        // Пытаемся занять центр
        if (board[1][1] == EMPTY) return Pair(1, 1)

        // Пытаемся занять углы
        val corners = listOf(Pair(0,0), Pair(0,2), Pair(2,0), Pair(2,2))
        val availableCorners = corners.filter { board[it.first][it.second] == EMPTY }
        if (availableCorners.isNotEmpty()) return availableCorners.random()

        // Иначе случайный ход
        return makeEasyMove(board, player)
    }

    private fun findWinningMove(board: Array<IntArray>, player: Int): Pair<Int, Int> {
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    val testBoard = board.map { it.clone() }.toTypedArray()
                    testBoard[i][j] = player
                    if (checkWinner(testBoard) == player) {
                        return Pair(i, j)
                    }
                }
            }
        }
        return Pair(-1, -1)
    }

    fun checkWinner(board: Array<IntArray>): Int {
        // Проверка строк и столбцов
        for (i in 0..2) {
            if (board[i][0] != EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0]
            }
            if (board[0][i] != EMPTY && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return board[0][i]
            }
        }
        // Проверка диагоналей
        if (board[0][0] != EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0]
        }
        if (board[0][2] != EMPTY && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2]
        }
        return EMPTY
    }

    private fun copyBoard(board: Array<IntArray>): Array<IntArray> {
        return board.map { it.clone() }.toTypedArray()
    }

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