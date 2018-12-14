package com.mccorby.datascience.nlp

import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

typealias LanguageModel = HashMap<String, MutableMap<Char, Float>>

class NGrams {

    suspend fun train(data: String, order: Int): LanguageModel {
        val result = coroutineScope {
            // Discount the added start token
            val total = (data.count() - order)
            (1..order).map { async(Dispatchers.IO) { trainForOrder(total, it, data) } }.awaitAll()
        }
        return LanguageModel().apply {
            result.map { putAll(it) }
        }
    }

    private fun trainForOrder(total: Int, ngram: Int, allData: String): LanguageModel {
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

    fun generateText(languageModel: LanguageModel, modelOrder: Int, rankingModel: RankingModel, nLetters: Int, seed: String = ""): String {
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
                    candidates = rankingModel.rank(languageModel, history, backoffOrder--, modelOrder).toMutableMap()
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