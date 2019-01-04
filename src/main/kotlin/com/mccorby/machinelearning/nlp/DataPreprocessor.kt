package com.mccorby.machinelearning.nlp

import com.mccorby.machinelearning.nlp.NGrams.Companion.START_CHAR

interface DataPreprocessor {
    fun processData(order: Int, data: String): String {
        val pad = START_CHAR.repeat(order)
        return pad + data.toLowerCase()
    }
}
