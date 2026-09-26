package com.choirscorepractice.core.omr

import com.choirscorepractice.core.score.Score
import java.io.File

/** App-owned local PDF, copied from the document picker before recognition. */
data class OmrInput(val pdfFile: File)

/**
 * Replaceable on-device recognition boundary; no network or Android UI dependencies.
 * Implementations dispatch blocking work off the main thread, release resources, and
 * propagate cancellation. Engine-specific results must be mapped to the core score model.
 */
interface OmrEngine {
    suspend fun recognize(input: OmrInput): OmrResult
}

sealed interface OmrResult {
    data class Success(val score: Score, val warnings: List<String> = emptyList()) : OmrResult
    data class Failure(val reason: String) : OmrResult
    data object Unavailable : OmrResult
}

/** Honest foundation default: recognition is unavailable until an adapter is selected. */
class UnavailableOmrEngine : OmrEngine {
    override suspend fun recognize(input: OmrInput): OmrResult = OmrResult.Unavailable
}
