package edu.bu.terriertrade.data.firebase

import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.DealProposal
import edu.bu.terriertrade.data.model.ProposalStatus

/*
 * Deal ↔ Firestore fields. The deal is stored inside its chat document under "deal".
 * Firestore gives numbers back as Long, so they're read as Number and converted.
 * Reading never crashes: a broken item is skipped and missing fields get safe defaults.
 */

// Writing -----------------------------------------------------------------------------------

internal fun Deal.toMap(): Map<String, Any?> = mapOf(
    "items" to items.map { it.toMap() },
    "pending" to pending?.toMap(),
    "doneBy" to doneBy,
    "version" to version,
)

internal fun DealItem.toMap(): Map<String, Any?> = mapOf(
    "listingId" to listingId,
    "agreedPrice" to agreedPrice,
)

internal fun DealProposal.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "fromUserId" to fromUserId,
    "items" to items.map { it.toMap() },
    "baseVersion" to baseVersion,
    "status" to status.name,
    "createdAt" to createdAt,
)

// Reading -----------------------------------------------------------------------------------

/** The "deal" field of a chat → Deal, or null when there's no open deal. */
internal fun dealFromMap(raw: Any?): Deal? {
    val data = raw.asMap() ?: return null
    val items = itemsFromList(data["items"])
    val pending = proposalFromMap(data["pending"])
    if (items.isEmpty() && pending == null) return null

    return Deal(
        items = items,
        pending = pending,
        doneBy = (data["doneBy"] as? List<*>).orEmpty().filterIsInstance<String>(),
        version = (data["version"] as? Number)?.toInt() ?: 0,
    )
}

private fun itemsFromList(raw: Any?): List<DealItem> =
    (raw as? List<*>).orEmpty().mapNotNull { entry ->
        val map = entry.asMap() ?: return@mapNotNull null
        val listingId = (map["listingId"] as? String) ?: return@mapNotNull null
        val price = (map["agreedPrice"] as? Number)?.toInt() ?: return@mapNotNull null
        DealItem(listingId, price)
    }

private fun proposalFromMap(raw: Any?): DealProposal? {
    val data = raw.asMap() ?: return null
    val id = (data["id"] as? String) ?: return null
    val fromUserId = (data["fromUserId"] as? String) ?: return null
    val statusName = data["status"] as? String

    return DealProposal(
        id = id,
        fromUserId = fromUserId,
        items = itemsFromList(data["items"]),
        baseVersion = (data["baseVersion"] as? Number)?.toInt() ?: 0,
        status = ProposalStatus.entries.firstOrNull { it.name == statusName } ?: ProposalStatus.PENDING,
        createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
    )
}

@Suppress("UNCHECKED_CAST")
private fun Any?.asMap(): Map<String, Any?>? = this as? Map<String, Any?>