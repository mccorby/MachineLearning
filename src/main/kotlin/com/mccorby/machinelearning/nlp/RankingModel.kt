package com.mccorby.machinelearning.nlp

import kotlin.math.max
import kotlin.math.pow

interface RankingModel {
    fun rank(
        languageModel: LanguageModel,
        history: String,
        modelOrder: Int,
        order: Int
    ): Map<Char, Float>
}

class StupidBackoffRanking : RankingModel {
    override fun rank(
        languageModel: LanguageModel,
        history: String,
        modelOrder: Int,
        order: Int
    ): Map<Char, Float> {
        val currentHistory = history.drop(max(history.length - order - 1 , 0))
        val lesserOrderHistory = currentHistory.dropLast(1)
        return if (order == 1) {
            languageModel[currentHistory]?.mapValues { it.value.toFloat() } ?: mapOf()
        } else {
            languageModel[currentHistory]?.let { distribution ->
                // Can force non nullability since lm[history - 1[ will always exist
                val lesserOrderCount = languageModel[lesserOrderHistory]!!.values.sum()
                val lambdaCorrection = 0.4.pow(modelOrder - order).toFloat()

                distribution.map {
                    val orderCount = lambdaCorrection * it.value.toFloat().div(lesserOrderCount)
                    Pair(it.key, orderCount)
                }.toMap()
            } ?: mapOf()
        }
    }
}