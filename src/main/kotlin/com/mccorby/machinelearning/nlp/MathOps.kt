package com.mccorby.machinelearning.nlp

import kotlin.random.Random

/**
 * Selects the character with the best score
 */
fun charRand(candidates: Map<Char, Float>): Char {
    val rand = Random.nextFloat()
    return candidates.toList().find { it.second > rand }?.first ?: candidates.keys.first()
}

fun charMax(candidates: Map<Char, Float>): Char {
    return candidates.toList().maxBy { (_, score) -> score }!!.first
}
