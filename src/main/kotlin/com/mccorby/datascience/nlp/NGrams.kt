package com.mccorby.datascience.nlp

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

typealias LanguageModel = HashMap<String, MutableMap<Char, Float>>

class NGrams {

    suspend fun train(data: String, order: Int): LanguageModel {
        val result = coroutineScope {

            val total = (data.count() - order)
            val listOfAsyncs = mutableListOf<Deferred<LanguageModel>>()
            for (ngram in order downTo 1) {
                listOfAsyncs.add(async { trainForOrder(total, ngram, data) })
            }
            listOfAsyncs.awaitAll()
        }
        val languageModel = LanguageModel()
        result.map { languageModel.putAll(it) }
        return languageModel
    }

    private fun trainForOrder(
        total: Int,
        ngram: Int,
        allData: String
    ): LanguageModel {
        println("Training for order $ngram")
        val languageModel = LanguageModel()
        for (i in 0..total) {
            val lastIdx = min(i + ngram, allData.length - 1)
            val history = allData.slice(IntRange(i, lastIdx - 1))
            val aChar = allData[lastIdx]
            val entry = languageModel.getOrElse(history) { mutableMapOf(aChar to 0f) }
            val count = entry.getOrDefault(aChar, 0f)
            entry[aChar] = count.plus(1)

            languageModel[history] = entry
        }
        println("END Training for order $ngram")
        return languageModel
    }

    private fun stupidBackoffRanking(
        languageModel: LanguageModel,
        history: String,
        order: Int,
        modelOrder: Int
    ): MutableMap<Char, Float> {
        val currentHistory = history.slice(IntRange(max(history.length - order, 0), history.length - 1))
        val candidates = mutableMapOf<Char, Float>()
        if (languageModel.containsKey(currentHistory)) {
            val distribution = languageModel[currentHistory]!!
            val lesserOrderHistory =
                history.slice(IntRange(max(history.length - (order - 1), 0), history.length - 1))
            val lesserOrderCount = languageModel[lesserOrderHistory]!!.values.sum().toInt()

            for ((aChar, count) in distribution) {
                val lambdaCorrection = 0.4.pow(modelOrder - order).toFloat()
                val orderCount = lambdaCorrection * count.div(lesserOrderCount)
                candidates[aChar] = orderCount
            }
        }
        return candidates
    }

    fun generateText(languageModel: LanguageModel, modelOrder: Int, nLetters: Int, seed: String = ""): String {
        var history = "~".repeat(modelOrder)
        var out = ""
        for (i in IntRange(0, nLetters)) {
            val aChar = if (i < seed.length) {
                seed[i].toString()
            } else {
                var candidates = mutableMapOf<Char, Float>()
                var backoffOrder = modelOrder
                while (candidates.isEmpty() && backoffOrder > 0) {
                    println("Doin backoff for modelOrder $backoffOrder")
                    candidates = stupidBackoffRanking(languageModel, history, backoffOrder--, modelOrder)
                    println(candidates)
                }
                if (candidates.isNotEmpty()) {
                    charmax(candidates).toString()
                } else {
                    ""
                }
            }
            history = history.slice(IntRange(history.length - modelOrder + 1, history.length - 1)) + aChar
            out += aChar
        }
        return out
    }

    /**
     * Selects the character with the best score
     */
    fun charmax(candidates: Map<Char, Float>): Char {
        return candidates.toList().sortedByDescending { (_, score) -> score }[0].first
    }
}

interface DataPreprocessor {
    fun processData(order: Int, data: String): String {
        val pad = "~".repeat(order)
        return pad + data.toLowerCase()
    }
}