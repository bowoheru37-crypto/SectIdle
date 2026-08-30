package com.sect.idle.ai

interface ISectAiRepository {
    suspend fun getGrandmasterWisdom(sectContext: String, userQuery: String? = null): GrandmasterWisdom
    suspend fun generateRandomSectEvent(sectContext: String): SectRandomEvent
    suspend fun generateDynamicQuest(sectContext: String, sectLevel: Int): DynamicSectQuest
    suspend fun consultTribulationTrial(discipleName: String, realm: Int, element: String): TribulationTrial
}
