package com.mccorby.datascience.nlp

interface Evaluator {
    fun evaluate(languageModel: LanguageModel, testData: String): Float
}

class Perplexity : Evaluator {

    override fun evaluate(languageModel: LanguageModel, testData: String): Float {
        return 0F
    }
}