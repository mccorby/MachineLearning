package com.mccorby.datascience.nlp

import kotlin.math.max
import kotlin.math.pow

interface RankingModel {
    fun rank(
        languageModel: LanguageModel,
        history: String,
        order: Int,
        modelOrder: Int
    ): Map<Char, Float>
}

class StupidBackoffRanking : RankingModel {
    override fun rank(
        languageModel: LanguageModel,
        history: String,
        order: Int,
        modelOrder: Int
    ): Map<Char, Float> {
        val currentHistory = history.slice(IntRange(max(history.length - order, 0), history.length - 1))
        val lesserOrderHistory = history.slice(IntRange(max(history.length - (order - 1), 0), history.length - 1))

        return languageModel[currentHistory]?.let { distribution ->
            val lesserOrderCount = languageModel[lesserOrderHistory]!!.values.sum().toInt()
            distribution.map {
                val lambdaCorrection = 0.4.pow(modelOrder - order).toFloat()
                val orderCount = lambdaCorrection * it.value.div(lesserOrderCount)
                Pair(it.key, orderCount)
            }.toMap()
        } ?: mapOf()
    }

}
