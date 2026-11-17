package edu.bu.terriertrade.data.repository

import edu.bu.terriertrade.data.deal.RequestResult
import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Chats, messages, requests and changes to the deal inside a chat.
 * Every deal change follows DealRules and saves the deal and its chat message together.
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

    /** Suggests [items] as the new deal. The other person accepts or declines. See DealRules.propose. */
    suspend fun proposeDeal(chatId: String, items: List<DealItem>): Result<Unit>

    /** Accepts the other person's suggested changes. See DealRules.accept. */
    suspend fun acceptProposal(chatId: String): Result<Unit>

    /** Declines the other person's suggested changes, or withdraws mine. See DealRules.decline. */
    suspend fun declineProposal(chatId: String): Result<Unit>
}
