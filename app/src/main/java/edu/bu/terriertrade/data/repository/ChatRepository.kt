package edu.bu.terriertrade.data.repository

import edu.bu.terriertrade.data.deal.RequestResult
import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Chats, messages and requests. Deal changes (propose, accept, remove, mark done)
 * are added by the deal feature on top of this.
 */
interface ChatRepository {
    /** The signed-in student's chats, most recent first. */
    fun observeChats(): Flow<List<Chat>>

    fun observeChat(chatId: String): Flow<Chat?>

    /** Messages in a chat, oldest first. */
    fun observeMessages(chatId: String): Flow<List<Message>>

    suspend fun sendMessage(chatId: String, text: String): Result<Unit>

    /** Returns the existing chat with [partnerId], or creates one. */
    suspend fun openChatWith(partnerId: String): Result<String>

    /**
     * Requests [listing], or offers to help on a Looking for post. Opens the chat with its owner
     * and starts a deal with the listing when there isn't one yet. See RequestRules.
     */
    suspend fun requestListing(listing: Listing): Result<RequestResult>
}
