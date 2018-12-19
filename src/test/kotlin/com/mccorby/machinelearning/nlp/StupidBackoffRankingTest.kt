package com.mccorby.machinelearning.nlp

import org.junit.Test
import kotlin.test.assertEquals

class StupidBackoffRankingTest {

    private val cut: StupidBackoffRanking = StupidBackoffRanking()

    @Test
    fun `Given a history and order bigger than 1 when ranking then uses lesser order to compute`() {
        // Given
        val modelOrder = 4
        val currentOrder = 3
        val lm = LanguageModel()
        val history = "star"
        lm[history] = mutableMapOf(' ' to 1)
        lm["sta"] = mutableMapOf('r' to 3)
        val expected = mapOf(' ' to (0.4F * 1) / 3) // 0.4 * 1 / 3

        // When
        val result = cut.rank(lm, history, modelOrder, currentOrder)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `Given a history and current order is same as model order when ranking then returns the next token without penalising probability`() {
        // Given
        val modelOrder = 4
        val currentOrder = 4
        val lm = LanguageModel()
        val history = "star"
        lm[history] = mutableMapOf(' ' to 1)
        lm["sta"] = mutableMapOf('r' to 3)
        val expected = mapOf(' ' to 1F / 3)

        // When
        val result = cut.rank(lm, history, modelOrder, currentOrder)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `Given a history with order 1 then its distribution is returned`() {
        // Given
        val modelOrder = 4
        val order = 1
        val lm = LanguageModel()
        val history = "s"
        lm[history] = mutableMapOf(' ' to 1)
        val expected = mapOf(' ' to 1F)

        // When
        val result = cut.rank(lm, history, modelOrder, order)

        // Then
        assertEquals(expected, result)
    }
}