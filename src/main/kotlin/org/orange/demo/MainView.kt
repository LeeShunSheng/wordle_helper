package org.orange.demo

import javafx.application.Application
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.scene.shape.Polygon
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.orange.demo.word_list.CSVFileReader
import java.util.regex.Pattern

class MainView : Application() {

    companion object {
        val WORD_LIST = CSVFileReader.getFileWordList()
        val WORD_LENGTH_REGEX: Pattern = Pattern.compile("[0-9]{1,2}")
        val WORD_REGEX: Pattern = Pattern.compile("[A-Za-z]")
        const val SINGEL_CHRT_LENGTH: Double = 25.0
        val CHAR_MAP: MutableMap<String, CharProps> = mutableMapOf()
        var TARGET_WORD_LENGTH = 5

        fun getArrorDown() = Polygon().apply {
            points.addAll(
                0.0, 0.0, 20.0, 0.0, 10.0, 5.0
            )
        }

        fun getArrorUp() = Polygon().apply {
            points.addAll(
                0.0, 5.0, 20.0, 5.0, 10.0, 0.0
            )
        }

    }

    /**
     * <h3>获取用户初始输入</h3>
     * */
    fun initWordLengthInput() = HBox().apply hbox@{
        HBox.getHgrow(this)
        spacing = 10.0
        alignment = Pos.BASELINE_CENTER
        children += Label("单词长度: ")
        children += TextField("$TARGET_WORD_LENGTH").apply {
            maxWidth = SINGEL_CHRT_LENGTH
            textFormatter = TextFormatter<String> { change ->
                val newText = change.controlNewText
                if (newText.isEmpty() || WORD_LENGTH_REGEX.matcher(newText).matches()) change else null
            }
            onKeyPressed = EventHandler {
                if (it.code == KeyCode.ENTER) {
                    TARGET_WORD_LENGTH = text.toInt()
                    isEditable = false
                    this@hbox.isVisible = false
                }
            }
        }
    }

    fun getWordCell(line: Int, position: Int) = VBox().apply {
        val mapKey = "$line-$position"
        var word = CHAR_MAP.getOrDefault(mapKey, CharProps(position, "", LetterStatus.WRONG))

        alignment = Pos.BASELINE_CENTER
        prefWidth = SINGEL_CHRT_LENGTH + 10.0
        spacing = 5.0

        style = "-fx-border-color: gray; -fx-border-width: 0.5px; -fx-border-style: dotted;"
        background = Background(BackgroundFill(word.status.color, null, null))

        fun changeColor(status: LetterStatus) {
            background = Background(BackgroundFill(status.color, null, null))
        }

        fun toPreviousStatus() {
            word = word.copy(status = word.status.previous())
            CHAR_MAP[mapKey] = word
            changeColor(word.status)
        }

        fun toNextStatus() {
            word = word.copy(status = word.status.next())
            CHAR_MAP[mapKey] = word
            changeColor(word.status)
        }
        children += Label().apply {
            graphic = getArrorUp()
            prefWidth = Region.USE_COMPUTED_SIZE
            maxWidth = SINGEL_CHRT_LENGTH
            onMouseClicked = EventHandler {
                toPreviousStatus()
            }
        }

        children += TextField(word.letter).apply {
            maxWidth = SINGEL_CHRT_LENGTH
            textFormatter = TextFormatter<String> { change ->
                val newText = change.controlNewText
                word = word.copy(letter = newText)
                CHAR_MAP[mapKey] = word
                if (newText.isEmpty() || WORD_REGEX.matcher(newText).matches()) change else null
            }

            onKeyPressed = EventHandler {
                if (it.code == KeyCode.UP) {
                    toPreviousStatus()
                } else if (it.code == KeyCode.DOWN) {
                    toNextStatus()
                }
            }
        }

        children += Label().apply {
            graphic = getArrorDown()
            prefWidth = Region.USE_COMPUTED_SIZE
            maxWidth = SINGEL_CHRT_LENGTH
            onMouseClicked = EventHandler {
                toNextStatus()
            }
        }
    }


    fun addWordInputLine(lineNum: Int, senceChildren: ObservableList<Node>) {
        senceChildren += HBox().apply hbox@{
            HBox.getHgrow(this)
            prefHeight = 40.0
            spacing = 10.0
            alignment = Pos.CENTER
            padding = Insets(10.0)
            style = "-fx-border-color: gray; -fx-border-width: 0.5px; -fx-border-style: dotted;"
            for (i in 0 until TARGET_WORD_LENGTH) {
                children += getWordCell(lineNum, i)
            }
            children += Button("新的输入").apply {
                onMouseClicked = EventHandler {
                    isVisible = false
                    addWordInputLine(lineNum + 1, senceChildren)
                }
            }
        }
    }

    fun getCurrentResult() {
        val wordLimits = CHAR_MAP.values.filter {
            it.letter != ""
        }
        /*分类成 不包含，存在但是位置错误 存在且位置正确*/
        val wrongIndeCell = wordLimits.filter {
            it.status == LetterStatus.WRONG_POSITION
        }.toSet()

        val correctLetterCell = wordLimits.filter {
            it.status == LetterStatus.RIGHT
        }.toSet()

        val notIntTheseLetters = wordLimits
            .filter { it.status == LetterStatus.WRONG }
            .map { it.letter }.toSet()
            .minus(wrongIndeCell.map { it.letter }.toSet())
            .minus(correctLetterCell.map { it.letter }.toSet())
            .toSet()


        val respContainer = ScrollPane().apply {
            content = VBox().apply {
                WORD_LIST.filter {
                    it.first.length == TARGET_WORD_LENGTH
                            && notIntTheseLetters.none { letter -> it.first.contains(letter) }
                            && correctLetterCell.all { cell -> it.first[cell.index] + "" == cell.letter }
                            && wrongIndeCell.all { cell -> it.first[cell.index] + "" != cell.letter && it.first.contains(cell.letter)  }
                }.forEachIndexed { index, wordPair ->
                    println(wordPair)
                    if (index < 30) {
                        val showSecond = if (wordPair.second.length > 10) "${
                            wordPair.second.subSequence(0, 7)
                        }..." else wordPair.second
                        children += Text("$wordPair.first - ${showSecond}").apply {
                            onMouseClicked = EventHandler {
                                Clipboard.getSystemClipboard().setContent(
                                    ClipboardContent().apply {
                                        putString(wordPair.first)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        Stage().apply {
            initStyle(StageStyle.DECORATED)
            scene = Scene(respContainer)
            show()
        }


    }

    override fun start(primaryStage: Stage?) {
        val container = ScrollPane().apply {
            isFitToWidth = true
            content = VBox().apply {
                VBox.getVgrow(this)
                alignment = Pos.BASELINE_CENTER
                val wordLengthContainer = initWordLengthInput()
                children += wordLengthContainer
                wordLengthContainer.visibleProperty().addListener { _, _, currentVisibleStatus ->
                    if (!currentVisibleStatus) {
                        children -= wordLengthContainer
                        children += Button("获取当前结果").apply {
                            onMouseClicked = EventHandler {
                                getCurrentResult()
                            }
                        }
                        addWordInputLine(0, children)
                    }
                }
            }
        }


        primaryStage?.apply {
            scene = Scene(container, 600.0, 400.0)
            show()
        }

    }
}


fun main(vararg args: String) {
    Application.launch(MainView::class.java, *args)
}