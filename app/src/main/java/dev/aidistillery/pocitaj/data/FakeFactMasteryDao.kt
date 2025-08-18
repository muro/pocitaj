package dev.aidistillery.pocitaj.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFactMasteryDao : FactMasteryDao {
    private val facts = mutableListOf<FactMastery>()
    private val flow = MutableStateFlow<List<FactMastery>>(emptyList())

    override fun getAllFactsForUser(userId: Long): Flow<List<FactMastery>> {
        return flow
    }

    override suspend fun getFactMastery(userId: Long, factId: String): FactMastery? {
        return facts.find { it.userId == userId && it.factId == factId }
    }

    override suspend fun upsert(factMastery: FactMastery) {
        facts.removeAll { it.userId == factMastery.userId && it.factId == factMastery.factId }
        facts.add(factMastery)
        flow.value = facts
    }

    suspend fun emit(newFacts: List<FactMastery>) {
        flow.emit(newFacts)
    }
}