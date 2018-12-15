package com.mccorby.datascience.nlp

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class NGramsTest {

    private lateinit var cut: NGrams

    @Before
    fun setUp() {
        cut = NGrams(StupidBackoffRanking())
    }

    @Test
    fun `Given a corpus and an order when train then the model contains expected ngrams in orders down to 1`() {
        // Given
        val corpus = "En un lugar de la Mancha"
        val order = 3
        val data = object : DataPreprocessor {}.processData(order, corpus)

        // When
        val result = runBlocking { cut.train(data, order) }

        // Then
        assertTrue(result.contains(START_CHAR + "en"))
        assertTrue(result.contains("en "))
        assertTrue(result.contains("en"))
        assertTrue(result.contains("e"))
        assertTrue(result.contains("n u"))
    }

    @Test
    fun `Given a corpus and an order when train the entries in the model contain the number of occurrences of each ngram`() {
        // Given
        val corpus = "star wars star trek street"
        val order = 3
        val expectedForSta = mutableMapOf(
            'r' to 2F
        )
        val expectedForSt = mutableMapOf(
            'a' to 2F,
            'r' to 1F
        )

        // When
        val result = runBlocking { cut.train(corpus, order) }

        // Then
        assertEquals(result["sta"], expectedForSta)
        assertEquals(result["st"], expectedForSt)
    }

    @Test
    fun `Given a map with characters and scores when charmax is invoked it returns the character with the highest score`() {
        val candidates = mapOf(
            'A' to 0.4f,
            'b' to 0.1f,
            'c' to 0.33f,
            'e' to 0.5f,
            'd' to 0.6f
        )

        val expected = 'd'

        // When
        val result = cut.charmax(candidates)

        // Then
        assert(expected == result)
    }

    @Test
    fun `Given a model when generates text it returns a sequence`() {
        // Given
        val corpus = "En un lugar de la Mancha"
        val order = 3
        val data = object : DataPreprocessor {}.processData(order, corpus)
        val languageModel = runBlocking { cut.train(data, order) }
        val expected = "en un "

        // When
        val result = cut.generateText(languageModel, order, 5)

        // Then
        assertEquals(result, expected)
    }

    @Test
    fun `Given a model and a seed when generates text it returns a sequence following the seed`() {
        // Given
        val corpus = "En un lugar de la Mancha"
        val order = 3
        val data = object : DataPreprocessor {}.processData(order, corpus)
        val languageModel = runBlocking { cut.train(data, order) }
        val expected = "lugar "
        val seed = "lu"

        // When
        val result = cut.generateText(languageModel, order, 5, seed)

        // Then
        assertEquals(result, expected)
    }
}