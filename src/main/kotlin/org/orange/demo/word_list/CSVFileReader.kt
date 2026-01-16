package org.orange.demo.word_list

import java.io.File
import java.io.FileReader
import java.nio.charset.Charset

class CSVFileReader {
    companion object {
        fun getFileWordList() = buildList {
            Companion::class.java.classLoader.getResource("ecdict.mini.csv")?.apply {
                FileReader(File(this.toURI()), Charset.forName("GB2312")).readLines().forEach {
                    if (!it.isEmpty()) {
                        val wordAndMeaning = it.split(Regex(","), 2)
                        val word = wordAndMeaning[0].trim()
                        val meaning = wordAndMeaning[1].trim()
                        val innoreLine = !Regex("[a-z]+").matches(word)
                                || meaning.contains("人名")
                                || meaning.contains("abbr.")
                        if (!innoreLine) {
                            add(Pair(word.lowercase(), meaning))
                        }
                    }
                }
            }
        }
    }
}