package dev.aidistillery.pocitaj.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId

class FakeExerciseAttemptDao : ExerciseAttemptDao {
    private val _attempts = MutableStateFlow<List<ExerciseAttempt>>(emptyList())

    override suspend fun insert(attempt: ExerciseAttempt) {
        _attempts.value += attempt
    }

    override fun getAttemptsForDate(userId: Long, dateString: String): Flow<List<ExerciseAttempt>> {
        return _attempts.asStateFlow().map { list ->
            list.filter {
                it.userId == userId &&
                        Instant.ofEpochMilli(it.timestamp)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                            .toString() == dateString
            }
        }
    }

    override fun getDailyActivityCounts(userId: Long): Flow<List<DailyActivityCount>> {
        return _attempts.asStateFlow().map { list ->
            list.filter { it.userId == userId }
                .groupBy {
                    Instant.ofEpochMilli(it.timestamp)
                        .atZone(ZoneId.systemDefault()).toLocalDate().toString()
                }
                .map { DailyActivityCount(it.key, it.value.size) }
        }
    }

    override suspend fun getAttemptCountForUser(userId: Long): Int {
        return _attempts.value.count { it.userId == userId }
    }
}
