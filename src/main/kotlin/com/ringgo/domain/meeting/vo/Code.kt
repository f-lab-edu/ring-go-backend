package com.ringgo.domain.meeting.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
class Code private constructor(
    @Column(name = "code", nullable = false, length = 15, unique = true)
    val value: String
) {
    companion object {
        private const val CODE_LENGTH = 15

        fun generate(attempt: Int = 0): Code {
            val baseCode = generateBaseCode()
            val uniqueCode = generateUniqueCode(baseCode, attempt)
            return Code(uniqueCode)
        }

        private fun generateBaseCode() =
            (UUID.randomUUID().toString() + System.nanoTime())
                .replace("-", "")
                .take(CODE_LENGTH)

        private fun generateUniqueCode(base: String, attempt: Int = 0): String {
            val suffix = if (attempt > 0) attempt.toString() else ""
            return base.take(CODE_LENGTH - suffix.length) + suffix
        }
    }

    override fun equals(other: Any?): Boolean =
        other is Code && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value
}