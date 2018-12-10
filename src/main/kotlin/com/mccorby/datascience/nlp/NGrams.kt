package com.mccorby.datascience.nlp

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

fun main(args: Array<String>) {
    val data = object {}.javaClass.getResource("/siddharta.txt").readText()
//    val data =
//        "Poovalli Induchoodan  is sentenced for six years prison life for murdering his classmate. " +
//                "The nation of Panem consists of a wealthy Capitol and twelve poorer districts misona misono misona misosol"
    val order = 5
    val lm = train(data, order)
    val nLetters = 2000
    print(generateText(lm, order, nLetters))
}

// Taking from Yersolav blog post
typealias LanguageModel = HashMap<String, MutableMap<Char, Float>>

fun train(data: String, order: Int = 4): LanguageModel {
    val languageModel = LanguageModel()
    val pad = "~".repeat(order)
    val allData = pad + data

    val total = (allData.count() - order)
    for (i in 0..total) {
        val lastIdx = min(i + order, allData.length - 1)
        val history = allData.slice(IntRange(i, lastIdx - 1))
        val aChar = allData[lastIdx]
        val entry = languageModel.getOrElse(history) { mutableMapOf(aChar to 0f) }
        val count = entry.getOrDefault(aChar, 0f)
        entry[aChar] = count.plus(1)

        languageModel[history] = entry
    }

    for ((hist, chars) in languageModel) {
        languageModel[hist] = normalize(chars)
    }

    return languageModel
}

fun normalize(entries: MutableMap<Char, Float>): MutableMap<Char, Float> {
    val total = entries.values.sum()
    for ((aChar, count) in entries) {
        entries[aChar] = count.div(total)
    }
    return entries
}

fun generateLetter(languageModel: LanguageModel, history: String, order: Int): String {
    val currentHistory = history.slice(IntRange(max(history.length - order, 0), history.length - 1))
    var result = ""
    if (languageModel.containsKey(currentHistory)) {
        val distribution = languageModel[currentHistory]!!
        var x = Random.nextFloat()
        for ((aChar, count) in distribution) {
            x -= count
            if (x <= 0) {
                result = aChar.toString()
            }
        }
    }
    return result
}

fun generateText(languageModel: LanguageModel, order: Int, nLetters: Int, seed: String = ""): String {
    var history = "~".repeat(order)
    var out = ""
    for (i in IntRange(0, nLetters)) {
        val aChar = if (i < seed.length) {
            seed[i].toString()
        } else {
            generateLetter(languageModel, history, order)
        }
        history = history.slice(IntRange(history.length - order + 1, history.length - 1)) + aChar
        out += aChar
    }
    return out
}

data class Entry(val char: Char, val count: Int)
