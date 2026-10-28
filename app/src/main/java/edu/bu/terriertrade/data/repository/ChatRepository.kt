package edu.bu.terriertrade.data.repository

import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Chats and messages. Deal changes (request, propose, accept, remove, mark done)
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
}
