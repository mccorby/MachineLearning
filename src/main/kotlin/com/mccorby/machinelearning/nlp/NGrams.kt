package com.mccorby.machinelearning.nlp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.min

typealias LanguageModel = HashMap<String, MutableMap<Char, Int>>

const val START_CHAR = "~"

class NGrams(private val rankingModel: RankingModel) {

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
            val entry = languageModel.getOrElse(history) { mutableMapOf(aChar to 0) }
            val count = entry.getOrDefault(aChar, 0)
            entry[aChar] = count.plus(1)

            languageModel[history] = entry
        }
        println("END Training for order $ngram")
        return languageModel
    }

    fun generateText(
        languageModel: LanguageModel,
        modelOrder: Int,
        nLetters: Int,
        seed: String = ""
    ): String {
        var history = START_CHAR.repeat(modelOrder)
        var out = ""
        for (i in IntRange(0, nLetters)) {
            // The seed is returned as it is
            val aChar = if (i < seed.length) {
                seed[i].toString()
            } else {
                generateNextChar(languageModel, modelOrder, history)
            }
            // Shift history and add new char
            history = history.drop(1).plus(aChar)
            out += aChar
        }
        return out
    }

    fun generateNextChar(
        languageModel: LanguageModel,
        modelOrder: Int,
        history: String
    ): String {
        val candidates = generateCandidates(languageModel, modelOrder, history)
        return if (candidates.isNotEmpty()) {
            charRand(candidates).toString()
        } else {
            ""
        }
    }

    fun generateCandidates(
        languageModel: LanguageModel,
        modelOrder: Int,
        history: String
    ): Map<Char, Float>  {
        var candidates = mutableMapOf<Char, Float>()
        var backoffOrder = modelOrder
        while (candidates.isEmpty() && backoffOrder > 0) {
            candidates = rankingModel.rank(languageModel, history, modelOrder, backoffOrder--).toMutableMap()
        }
        return candidates
    }
}
