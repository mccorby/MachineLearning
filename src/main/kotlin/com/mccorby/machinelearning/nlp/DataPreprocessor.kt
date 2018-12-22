package com.mccorby.machinelearning.nlp

interface DataPreprocessor {
    fun processData(order: Int, data: String): String {
        val pad = START_CHAR.repeat(order)
        return pad + data.toLowerCase()
    }
}
