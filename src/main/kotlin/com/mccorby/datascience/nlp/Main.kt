package com.mccorby.datascience.nlp

import arrow.core.Try
import arrow.core.getOrElse
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

suspend fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Usage: <text_file_in_resources> <save_dir> <ngram_order>")
        println("Example: /titles.txt /tmp/saved_model 4")
        return
    }

    val data = object {}.javaClass.getResource(args[0]).readText()
    val modelFile = args[1]
    val order = args[2].toInt()
    val processedData = object : DataPreprocessor {}.processData(order, data)

    val nGrams = NGrams()

    // Load or train the model
    val lm = loadModel(modelFile).getOrElse {
        val model = trainModel(nGrams, processedData, order)
        saveModel(model, modelFile)
        model
    }

    // Inference
    val nLetters = 20
    val rankingModel = StupidBackoffRanking()
    print(nGrams.generateText(lm, order, rankingModel, nLetters, "star w".toLowerCase()))
}

private suspend fun trainModel(nGrams: NGrams, data: String, order: Int)  = nGrams.train(data, order)

private fun saveModel(languageModel: LanguageModel, modelFile: String) {
    ObjectOutputStream(FileOutputStream(modelFile)).use { it -> it.writeObject(languageModel) }
}

private fun loadModel(file: String): Try<LanguageModel> {
    return Try {
        ObjectInputStream(FileInputStream(file)).use { it ->
            it.readObject() as LanguageModel
        }
    }
}
