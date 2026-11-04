package edu.bu.terriertrade.ui.model

import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Live profiles for several students at once (sellers on a list, reviewers on a profile). */
fun UserRepository.observeUsers(ids: Collection<String>): Flow<Map<String, User?>> {
    val distinct = ids.distinct()
    if (distinct.isEmpty()) return flowOf(emptyMap())
    return combine(distinct.map { id -> observeUser(id).map { id to it } }) { pairs -> pairs.toMap() }
}
