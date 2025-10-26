package com.example.app

class GameBot {
    companion object {
        const val EMPTY = 0
        const val SYMBOL_X = 1
        const val SYMBOL_O = 2
    }

    fun makeMove(board: Array<IntArray>, botSymbol: Int): Pair<Int, Int> {
        // Может ли бот выиграть следующим ходом
        val winningMove = findWinningMove(board, botSymbol)
        if (winningMove != null) return winningMove

        // Может ли игрок выиграть следующим ходом -> заблокировать
        val opponentSymbol = if (botSymbol == SYMBOL_X) SYMBOL_O else SYMBOL_X
        val blockingMove = findWinningMove(board, opponentSymbol)
        if (blockingMove != null) return blockingMove

        // Занять центр если свободен
        if (board[1][1] == EMPTY) return Pair(1, 1)

        // 4. Занять углы
        val corners = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2))
        val availableCorners = corners.filter { board[it.first][it.second] == EMPTY }
        if (availableCorners.isNotEmpty()) {
            return availableCorners.random()
        }

        // 5. Занять любую свободную клетку
        val availableMoves = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    availableMoves.add(Pair(i, j))
                }
            }
        }

        return if (availableMoves.isNotEmpty()) availableMoves.random() else Pair(-1, -1)
    }

    private fun findWinningMove(board: Array<IntArray>, symbol: Int): Pair<Int, Int>? {
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    val tempBoard = board.map { it.clone() }.toTypedArray()
                    tempBoard[i][j] = symbol

                    if (checkWinner(tempBoard) == symbol) {
                        return Pair(i, j)
                    }
                }
            }
        }
        return null
    }

    fun checkWinner(board: Array<IntArray>): Int {
        // Проверяем горизонтали
        for (i in 0..2) {
            if (board[i][0] != EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0]
            }
        }

        // Проверяем вертикали
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
}