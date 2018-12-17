package com.mccorby.datascience.nlp

import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class EvaluationTest {

//    @Test
//    fun `Given a language model and a test text when entropy is calculated this is the expected`() {
//        // Given
//        val corpus = "En un lugar de la Mancha habia un hidalgo en un caballo"
//        val order = 5
//        val data = object : DataPreprocessor {}.processData(order, corpus)
//        val expected = 10.0f
//        val ngrams = NGrams(StupidBackoffRanking())
//        val lm = runBlocking { ngrams.train(data, order) }
//
//
//        // When
//        val result = entropy(lm, "habia un caballo", order)
//
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun `Given a language model and a test text when perplexity is calculated this is the expected`() {
//        // Given
//        val corpus = "En un lugar de la Mancha habia un hidalgo en un caballo"
//        val order = 3
//        val data = object : DataPreprocessor {}.processData(order, corpus)
//        val expected = 100.0f
//        val ngrams = NGrams(StupidBackoffRanking())
//        val lm = runBlocking { ngrams.train(data, order) }
//
//
//        // When
//        val result = perplexity(lm, "en una mancha", order)
//
//        assertEquals(expected, result)
//    }
}