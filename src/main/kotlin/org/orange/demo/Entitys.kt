package org.orange.demo

import javafx.scene.paint.Color


data class CharProps(
    val index: Int,
    val letter: String,
    val status: LetterStatus
)

enum class LetterStatus(val color: Color) {
    RIGHT(Color.GREEN),
    WRONG(Color.GRAY),
    WRONG_POSITION(Color.YELLOW),
    ;

    fun previous() = if (this == WRONG) {
        RIGHT
    } else if (this == WRONG_POSITION) {
        WRONG
    } else {
        WRONG_POSITION
    }


    fun next() = if (this == WRONG) {
        WRONG_POSITION
    } else if (this == WRONG_POSITION) {
        RIGHT
    } else {
        WRONG
    }

}