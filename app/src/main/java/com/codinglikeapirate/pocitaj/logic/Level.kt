package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation

/**
 * Represents a single, teachable level in the curriculum.
 * Its primary job is to generate random exercises that conform to its rules.
 */
interface Level {
    val id: String
    val operation: Operation
    val prerequisites: Set<String>
    fun generateExercise(): Exercise
    fun getAllPossibleFactIds(): List<String>
}

class MixedReviewLevel(
    override val id: String,
    override val operation: Operation,
    private val levelsToReview: List<Level>
) : Level {
    override val prerequisites: Set<String> = levelsToReview.map { it.id }.toSet()

    override fun generateExercise(): Exercise {
        val randomLevel = levelsToReview.random()
        return randomLevel.generateExercise()
    }

    override fun getAllPossibleFactIds(): List<String> {
        return levelsToReview.flatMap { it.getAllPossibleFactIds() }
    }
}