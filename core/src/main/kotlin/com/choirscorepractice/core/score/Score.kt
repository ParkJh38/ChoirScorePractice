package com.choirscorepractice.core.score

/** UNKNOWN preserves unclassified staves for user review. Multiple parts may share a role. */
enum class ChoirRole { DESCANT, SOPRANO, ALTO, TENOR, BASS, UNKNOWN }

data class Score(val title: String, val parts: List<ScorePart>)

data class ScorePart(
    val id: String,
    val label: String,
    val role: ChoirRole,
    val notes: List<NoteEvent>,
)

/** Quarter-note units keep recognition independent of playback tempo. Rests are gaps. */
data class NoteEvent(
    val midiPitch: Int,
    val startBeat: Double,
    val durationBeats: Double,
) {
    init {
        require(midiPitch in 0..127) { "MIDI pitch must be in 0..127" }
        require(startBeat.isFinite() && startBeat >= 0) { "Start beat must be finite and nonnegative" }
        require(durationBeats.isFinite() && durationBeats > 0) { "Duration must be finite and positive" }
    }
}
