package com.mccorby.datascience.nlp

import arrow.core.Try
import arrow.core.getOrElse
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

suspend fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Usage: <text_file_in_resources> <save_dir> <ngram_order> [evaluate]")
        println("Example: /titles.txt /tmp/saved_model 4 true")
        return
    }

    val data = object {}.javaClass.getResource(args[0]).readText()
    val modelFile = args[1]
    val order = args[2].toInt()
    val evaluate = args.getOrElse(3) { "false" }.toBoolean()
    val processedData = object : DataPreprocessor {}.processData(order, data)

    val nGrams = NGrams(StupidBackoffRanking())

    // Load or train the model
    val lm = loadModel(modelFile).getOrElse {
        val model = trainModel(nGrams, processedData, order)
        saveModel(model, modelFile)
        model
    }

    if (evaluate) {
        val lm2 = trainModel(nGrams, processedData, order - 1)
        val lm3 = trainModel(nGrams, processedData, order + 1)
        println("Perplexity ${perplexity(lm, "life: three stories of love, lust, and liberation", order)}")
        println("Perplexity model ${order - 1} ${perplexity(lm2, "life: three stories of love, lust, and liberation", order)}")
        println("Perplexity model ${order + 1} ${perplexity(lm3, "life: three stories of love, lust, and liberation", order)}")
        println()
    }

    // Inference
    val nLetters = 200
    println(nGrams.generateText(lm, order, nLetters, "sta".toLowerCase()))
    println(nGrams.generateText(lm, order, nLetters, "sta".toLowerCase()))
    println(nGrams.generateText(lm, order, nLetters, "sta".toLowerCase()))
    println(nGrams.generateText(lm, order, nLetters, "sta".toLowerCase()))
    println(nGrams.generateText(lm, order, nLetters, "sta".toLowerCase()))
    println(nGrams.generateText(lm, order, nLetters, "sta".toLowerCase()))
}

private suspend fun trainModel(nGrams: NGrams, data: String, order: Int) = nGrams.train(data, order)

private fun saveModel(languageModel: LanguageModel, modelFile: String) {
    ObjectOutputStream(FileOutputStream(modelFile)).use { it -> it.writeObject(languageModel) }
}

@Suppress("UNCHECKED_CAST")
private fun loadModel(file: String): Try<LanguageModel> {
    return Try {
        ObjectInputStream(FileInputStream(file)).use { it ->
            it.readObject() as LanguageModel
        }
    }
}
