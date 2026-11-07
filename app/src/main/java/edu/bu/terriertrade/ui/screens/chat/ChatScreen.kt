package edu.bu.terriertrade.ui.screens.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.data.fake.SampleData
import edu.bu.terriertrade.ui.common.UiState
import edu.bu.terriertrade.ui.common.UiStateContent
import edu.bu.terriertrade.ui.components.TTAvatar
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** One conversation. Gets its data from [ChatViewModel]. */
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onOpenProfile: (userId: String) -> Unit,
    onOpenDeal: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()

    ChatScreenContent(
        state = state,
        draft = draft,
        onDraftChange = viewModel::onDraftChange,
        onSend = viewModel::send,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        onOpenDeal = onOpenDeal,
        modifier = modifier,
    )
}

/** Draws the chat from [state]. No ViewModel, so it can be previewed. */
@Composable
fun ChatScreenContent(
    state: UiState<ChatContent>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    onOpenProfile: (userId: String) -> Unit,
    onOpenDeal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = (state as? UiState.Success)?.data

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        // The top bar shows even while loading, so Back always works
        ChatTopBar(
            content = content,
            onBack = onBack,
            onOpenProfile = onOpenProfile,
        )

        UiStateContent(
            state = state,
            modifier = Modifier.weight(1f),
        ) { loaded ->
            Column(modifier = Modifier.weight(1f)) {
                if (loaded.dealBar != null) {
                    DealBar(
                        deal = loaded.dealBar,
                        onClick = onOpenDeal,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    )
                }
                MessageList(
                    content = loaded,
                    onOpenDeal = onOpenDeal,
                    modifier = Modifier.weight(1f),
                )
                MessageInput(draft = draft, onDraftChange = onDraftChange, onSend = onSend)
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    content: ChatContent?,
    onBack: () -> Unit,
    onOpenProfile: (userId: String) -> Unit,
) {
    val onRed = MaterialTheme.colorScheme.onPrimary
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = onRed,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = Spacing.lg, top = Spacing.xs, bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            if (content != null) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClickLabel = "View profile") { onOpenProfile(content.partnerId) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    TTAvatar(
                        initials = content.partnerInitials,
                        size = 40.dp,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.border(2.dp, onRed, CircleShape),
                    )
                    Column {
                        Text(text = content.partnerName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "★ ${content.partnerRatingLabel} · Tap to view profile",
                            style = MaterialTheme.typography.labelSmall,
                            color = onRed.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageList(
    content: ChatContent,
    onOpenDeal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Jump to the newest message when the chat opens and whenever a new one arrives
    LaunchedEffect(content.messages.size) {
        if (content.messages.isNotEmpty()) {
            listState.animateScrollToItem(content.messages.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (content.messages.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "Say hi to ${content.partnerFirstName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
                )
            }
        }
        items(content.messages, key = { it.id }) { message ->
            MessageItem(message = message, onOpenDeal = onOpenDeal)
        }
    }
}

@Composable
private fun MessageInput(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                shape = CircleShape,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
            )
            FilledIconButton(
                onClick = onSend,
                enabled = draft.isNotBlank(),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ChatScreenPreview() {
    val me = SampleData.ME
    val chat = SampleData.chats.first { it.id == "c_sam" }
    val content = buildChatContent(
        myId = me,
        chat = chat,
        partner = SampleData.user(chat.partnerId(me)),
        messages = SampleData.messages.filter { it.chatId == chat.id },
        dealListings = SampleData.listings.associateBy { it.id },
        nowMillis = System.currentTimeMillis(),
    )
    TerrierTradeTheme {
        Surface {
            ChatScreenContent(
                state = UiState.Success(content),
                draft = "",
                onDraftChange = {},
                onSend = {},
                onBack = {},
                onOpenProfile = {},
                onOpenDeal = {},
            )
        }
    }
}