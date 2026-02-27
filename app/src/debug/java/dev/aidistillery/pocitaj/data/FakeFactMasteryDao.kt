package dev.aidistillery.pocitaj.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFactMasteryDao : FactMasteryDao {
    private val facts = mutableListOf<FactMastery>()
    private val flow = MutableStateFlow<List<FactMastery>>(emptyList())

    override fun getAllFactsForUser(userId: Long): Flow<List<FactMastery>> {
        return flow
    }

    override suspend fun getFactMastery(userId: Long, factId: String, level: String): FactMastery? {
        return facts.find { it.userId == userId && it.factId == factId && it.level == level }
    }

    override suspend fun getFactMastery(userId: Long, factId: String): FactMastery? {
        // Mimic the query: WHERE factId = :factId AND userId = :userId AND level = ''
        return facts.find { it.userId == userId && it.factId == factId && it.level == "" }
    }

    override suspend fun upsert(factMastery: FactMastery) {
        upsert(factMastery, factMastery.level)
    }

    override suspend fun upsert(factMastery: FactMastery, level: String) {
        facts.removeAll { it.userId == factMastery.userId && it.factId == factMastery.factId && it.level == level }
        facts.add(factMastery.copy(level = level))
        facts.add(factMastery.copy(level = ""))
        flow.value = facts.toList()
    }

    suspend fun emit(newFacts: List<FactMastery>) {
        facts.clear()
        facts.addAll(newFacts)
        flow.emit(newFacts)
    }
}