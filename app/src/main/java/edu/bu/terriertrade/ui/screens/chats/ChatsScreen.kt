package edu.bu.terriertrade.ui.screens.chats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.data.fake.SampleData
import edu.bu.terriertrade.ui.common.UiState
import edu.bu.terriertrade.ui.common.UiStateContent
import edu.bu.terriertrade.ui.components.inputs.TTSearchBar
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Chats tab. Gets its data from [ChatsViewModel] and opens a chat through [onOpenChat]. */
@Composable
fun ChatsScreen(
    onOpenChat: (chatId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatsViewModel = viewModel(factory = ChatsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    ChatsScreenContent(
        state = state,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onOpenChat = onOpenChat,
        modifier = modifier,
    )
}

/** Draws the chats list from [state]. No ViewModel, so it can be previewed. */
@Composable
fun ChatsScreenContent(
    state: UiState<ChatsContent>,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenChat: (chatId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    UiStateContent(
        state = state,
        modifier = modifier,
        isEmpty = { it.totalCount == 0 },
        emptyTitle = "No chats yet",
        emptyMessage = "Request an item to start a chat with the seller.",
    ) { content ->
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item(key = "title") {
                ChatsTitle(totalCount = content.totalCount, needsReplyCount = content.needsReplyCount)
            }
            item(key = "search") {
                TTSearchBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    placeholder = "Search people",
                    outlined = true,
                )
            }
            if (content.sections.isEmpty()) {
                item(key = "no-results") {
                    Text(
                        text = "No chats match \"$query\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.lg),
                    )
                }
            }
            content.sections.forEach { section ->
                item(key = section.group.name) {
                    SectionTitle(section.group)
                }
                items(section.rows, key = { it.chatId }) { row ->
                    ChatRow(row = row, onClick = { onOpenChat(row.chatId) })
                }
            }
        }
    }
}

@Composable
private fun ChatsTitle(totalCount: Int, needsReplyCount: Int) {
    val conversations = if (totalCount == 1) "1 conversation" else "$totalCount conversations"
    val subtitle = if (needsReplyCount > 0) "$conversations · $needsReplyCount needs your reply" else conversations

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Chats",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionTitle(group: ChatGroup) {
    Text(
        text = group.title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 1.sp,
        color = if (group == ChatGroup.NEEDS_REPLY) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(top = Spacing.md)
            .semantics { heading() },
    )
}

@PreviewLightDark
@Composable
private fun ChatsScreenPreview() {
    val rows = SampleData.chats.map { chat ->
        val partner = SampleData.user(chat.partnerId(SampleData.ME))
        chat.toRow(SampleData.ME, partner, System.currentTimeMillis())
    }
    val content = ChatsContent(
        sections = rows.toSections(),
        totalCount = rows.size,
        needsReplyCount = rows.count { it.group == ChatGroup.NEEDS_REPLY },
    )
    TerrierTradeTheme {
        Surface {
            ChatsScreenContent(
                state = UiState.Success(content),
                query = "",
                onQueryChange = {},
                onOpenChat = {},
            )
        }
    }
}