package com.mccorby.datascience.nlp

import kotlin.math.log2
import kotlin.math.pow

/**
 * Calculate the approximate cross-entropy of the n-gram model for a test text.
 * This is the average log probability of each character in the text.
 */
fun entropy(languageModel: LanguageModel, testData: String, order: Int): Float {
    // (I,spent,three,years,before,the,mast)=
    //-(log2(P(I))+log2(P(spent|I))+log2(P(three|spent))+log2(P(years|three))
    //+log2(P(before|years))+log2(P(the|before))+log2(P(mast|the)))
    var history = START_CHAR.repeat(order)
    return testData.asSequence().fold(0f) { acc, it ->
        val total = languageModel[history]?.values?.sum() ?: 1.0f
        val prob = (languageModel[history]?.get(it) ?: 0.0f).div(total)
        val logOfProb = if (prob > 0) log2(prob) else 0.0f

        history = history.drop(1).plus(it)
        acc + (-logOfProb)
    }
}

fun perplexity(languageModel: LanguageModel, testData: String, order: Int) = entropy(languageModel, testData, order).pow(2)
