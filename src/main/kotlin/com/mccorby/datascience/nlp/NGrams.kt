package com.mccorby.datascience.nlp

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

fun main(args: Array<String>) {
//    val data = object {}.javaClass.getResource("/titles.txt").readText()
    val data =
        "Poovalli Induchoodan  is sentenced for six years prison life for murdering his classmate. " +
                "The nation of Panem consists of a wealthy Capitol and twelve poorer districts misona " +
                "misono misona misosol mishako misha"
    val order = 5
    val lm = train(data, order)
    val nLetters = 200
//    print(generateText(lm, order, nLetters, "Star war".toLowerCase()))
    print(stupidBackoff(lm, "mis", order - 1, order))
}

// Taking from Yoav Goldberg blog post
// http://nbviewer.jupyter.org/gist/yoavg/d76121dfde2618422139
typealias LanguageModel = HashMap<String, MutableMap<Char, Float>>

fun train(data: String, order: Int = 4): LanguageModel {
    val languageModel = LanguageModel()
    val pad = "~".repeat(order)
    val allData = pad + data.toLowerCase()

    val total = (allData.count() - order)
    for (ngram in order downTo 1) {
        for (i in 0..total) {
            val lastIdx = min(i + ngram, allData.length - 1)
            val history = allData.slice(IntRange(i, lastIdx - 1))
            val aChar = allData[lastIdx]
            val entry = languageModel.getOrElse(history) { mutableMapOf(aChar to 0f) }
            val count = entry.getOrDefault(aChar, 0f)
            entry[aChar] = count.plus(1)

            languageModel[history] = entry
        }
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
        val normDistribution = normalize(distribution)
        var x = Random.nextFloat()
        for ((aChar, count) in normDistribution) {
            x -= count
            if (x <= 0) {
                result = aChar.toString()
            }
        }
    } else if (order > 1) {
        result = generateLetter(languageModel, history, order - 1)
    }
    return result
}

fun stupidBackoff(languageModel: LanguageModel, history: String, order: Int, modelOrder: Int): MutableMap<Char, Float> {
    val currentHistory = history.slice(IntRange(max(history.length - order, 0), history.length - 1))
    val candidates = mutableMapOf<Char, Float>()
    if (languageModel.containsKey(currentHistory)) {
        val distribution = languageModel[currentHistory]!!
        val lesserOrderHistory = history.slice(IntRange(max(history.length - order - 1, 0), history.length - 1))
        val lesserOrderCount = languageModel[lesserOrderHistory]!!.values.sum().toInt()

        for ((aChar, count) in distribution) {
            val lambdaCorrection = 0.4.pow(modelOrder - order).toFloat()
            val orderCount = lambdaCorrection * count.div(lesserOrderCount)
            candidates[aChar] = orderCount
        }
    }
    return candidates
}


fun generateText(languageModel: LanguageModel, order: Int, nLetters: Int, seed: String = ""): String {
    var history = "~".repeat(order)
    var out = ""
    for (i in IntRange(0, nLetters)) {
        val aChar = if (i < seed.length) {
            seed[i].toString()
        } else {
            generateLetter(languageModel, history, order)
//            stupidBackoff(languageModel, history, order)
        }
        history = history.slice(IntRange(history.length - order + 1, history.length - 1)) + aChar
        out += aChar
    }
    return out
}

data class Entry(val char: Char, val count: Int)
