package edu.bu.terriertrade.data.repository

/**
 * The one failure type every repository returns inside Result.failure.
 * Its message is written for students, so a ViewModel can show it on screen as it is.
 * The Firebase and Open Library classes turn their own exceptions into this before returning.
 */
class DataError(message: String) : Exception(message)
