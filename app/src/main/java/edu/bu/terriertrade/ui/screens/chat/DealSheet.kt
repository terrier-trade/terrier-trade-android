package edu.bu.terriertrade.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.terriertrade.data.fake.SampleData
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.ui.components.buttons.TTPrimaryButton
import edu.bu.terriertrade.ui.components.buttons.TTSecondaryButton
import edu.bu.terriertrade.ui.components.feedback.TTErrorText
import edu.bu.terriertrade.ui.model.icon
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Everything the deal sheet can do, so the sheet takes one parameter for it. */
data class DealSheetActions(
    val onClose: () -> Unit = {},
    val onAdd: (AddableListingUi) -> Unit = {},
    val onRemove: (listingId: String) -> Unit = {},
    val onPriceChange: (listingId: String, text: String) -> Unit = { _, _ -> },
    val onSend: () -> Unit = {},
    val onDiscard: () -> Unit = {},
    val onAccept: () -> Unit = {},
    val onDecline: () -> Unit = {},
    val onMarkDone: () -> Unit = {},
    val onCancelDeal: () -> Unit = {},
)

internal val SheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)

/**
 * The deal window as a bottom sheet, laid out like the prototype: the deal split into
 * You get and You give with the money worked out underneath. Change deal switches to ticking
 * listings and setting prices. The other person's changes show at the top with Accept and Decline.
 * When nothing is waiting, Mark as done finishes the deal once both people have tapped it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealSheet(state: DealSheetState, actions: DealSheetActions) {
    if (!state.isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = actions.onClose,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { DragHandle() },
    ) {
        val window = state.window
        if (window == null) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DealSheetContent(window = window, isSaving = state.isSaving, error = state.error, actions = actions)
        }
    }
}

/** The sheet's insides, without the sheet, so it can be previewed. */
@Composable
internal fun DealSheetContent(
    window: DealWindowUi,
    isSaving: Boolean,
    error: String?,
    actions: DealSheetActions,
    modifier: Modifier = Modifier,
    startEditing: Boolean = false,
) {
    // Viewing the deal, or ticking items after Change deal. Their waiting changes always come first.
    var editing by rememberSaveable { mutableStateOf(startEditing) }
    // The "Cancel this deal?" dialog. Only the final yes reaches the ViewModel.
    var confirmCancel by rememberSaveable { mutableStateOf(false) }
    val isEditing = editing && window.mode == DealMode.EDIT
    val viewingOpenDeal = window.mode == DealMode.EDIT && !isEditing

    Column(modifier.fillMaxWidth()) {
        SheetHeader(
            title = if (viewingOpenDeal) "Your deal with ${window.partnerFirstName}" else window.title,
            rule = if (viewingOpenDeal) "Nothing changes until both of you agree." else window.subtitle,
            onClose = actions.onClose,
        )

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (isEditing) {
                EditLists(window = window, actions = actions)
            } else {
                when (window.mode) {
                    DealMode.REVIEW -> ChangesCard(window = window, isSaving = isSaving, actions = actions)
                    DealMode.WAITING -> WaitingNote(window = window)
                    DealMode.EDIT -> if (window.doneByMe || window.doneByPartner) DoneNote(window)
                }
                DealView(window = window)
            }
            TTErrorText(error)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = Spacing.md, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                isEditing -> {
                    NewTotalBox(totalLabel = window.totalLabel, currentTotalLabel = window.currentTotalLabel)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TTSecondaryButton(
                            text = "Back",
                            onClick = {
                                actions.onDiscard()
                                editing = false
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TTPrimaryButton(
                            text = "Send to ${window.partnerFirstName}",
                            onClick = actions.onSend,
                            modifier = Modifier.weight(2f),
                            enabled = window.canSend,
                            loading = isSaving,
                        )
                    }
                }
                window.mode == DealMode.WAITING -> TTSecondaryButton(
                    text = "Withdraw my changes",
                    onClick = actions.onDecline,
                    modifier = Modifier.fillMaxWidth(),
                    loading = isSaving,
                )
                window.mode == DealMode.EDIT -> {
                    // Why Mark as done is off, like an item that was taken down
                    window.markDoneBlockedReason?.let { reason ->
                        Text(reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TTSecondaryButton(
                            text = "Change deal",
                            onClick = { editing = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving,
                        )
                        TTPrimaryButton(
                            text = markDoneLabel(window),
                            onClick = actions.onMarkDone,
                            modifier = Modifier.weight(1f),
                            enabled = window.canMarkDone,
                            loading = isSaving,
                        )
                    }
                }
                // REVIEW: Accept and Decline are in the changes card at the top
                else -> Unit
            }

            // Calling the deal off is always possible, except halfway through editing it
            if (!isEditing) {
                CancelDealButton(enabled = !isSaving, onClick = { confirmCancel = true })
            }
        }
    }

    if (confirmCancel) {
        CancelDealDialog(
            partnerFirstName = window.partnerFirstName,
            onConfirm = {
                confirmCancel = false
                actions.onCancelDeal()
            },
            onDismiss = { confirmCancel = false },
        )
    }
}

// Header ------------------------------------------------------------------------------------------

@Composable
internal fun DragHandle() {
    Box(
        Modifier
            .padding(top = 10.dp, bottom = 6.dp)
            .size(width = 40.dp, height = 4.dp)
            .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
    )
}

@Composable
internal fun SheetHeader(title: String, rule: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(38.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                Icons.Outlined.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp),
            )
            Text(rule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Viewing the deal --------------------------------------------------------------------------------

@Composable
private fun DealView(window: DealWindowUi) {
    // Removed rows are listed in the changes card, not in the deal the changes would make
    val rows = window.rows.filter { it.change != RowChange.REMOVED }
    DealSide(heading = "YOU GET", rows = rows.filter { it.youGet }, headingColor = MaterialTheme.colorScheme.primary)
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    DealSide(heading = "YOU GIVE", rows = rows.filterNot { it.youGet }, headingColor = MaterialTheme.colorScheme.onSurface)
    MoneyCard(totalLabel = window.totalLabel, currentTotalLabel = window.currentTotalLabel)
}

@Composable
private fun DealSide(heading: String, rows: List<DealRowUi>, headingColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(heading, headingColor)
        if (rows.isEmpty()) {
            Text("Nothing yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        rows.forEach { DealItemRow(it) }
    }
}

@Composable
internal fun SectionLabel(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = color,
    )
}

@Composable
private fun DealItemRow(row: DealRowUi) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        ItemTile(category = row.category, faded = !row.isAvailable)
        Column(Modifier.weight(1f)) {
            Text(row.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = rowNote(row),
                style = MaterialTheme.typography.bodySmall,
                color = if (row.isAvailable) muted else MaterialTheme.colorScheme.error,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("\$${row.price}", style = MaterialTheme.typography.titleSmall)
            // The listed price, struck out, when they bargained to a different one
            val listed = row.listedPrice
            if (listed != null && listed != row.price) {
                Text("\$$listed", style = MaterialTheme.typography.bodySmall, color = muted, textDecoration = TextDecoration.LineThrough)
            }
        }
    }
}

private fun rowNote(row: DealRowUi): String = when {
    !row.isAvailable -> "No longer available"
    row.change == RowChange.ADDED -> "New in these changes"
    row.change == RowChange.PRICE_CHANGED -> "Was \$${row.oldPrice}"
    row.isMine -> "Your listing"
    else -> "Their listing"
}

@Composable
private fun ItemTile(category: Category, faded: Boolean = false) {
    val alpha = if (faded) 0.4f else 1f
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f * alpha), MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            category.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            modifier = Modifier.size(20.dp),
        )
    }
}

/** The total, on a light red card with the prototype's hard offset shadow. */
@Composable
private fun MoneyCard(totalLabel: String, currentTotalLabel: String?) {
    val ink = MaterialTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(14.dp)
    Box(Modifier.fillMaxWidth().padding(end = 3.dp, bottom = 3.dp)) {
        // A dark copy of the card, 3dp down and to the right, is the shadow
        Box(
            Modifier
                .matchParentSize()
                .offset(3.dp, 3.dp)
                .background(ink, shape),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape)
                .border(1.5.dp, ink, shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(Icons.Outlined.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(totalLabel, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Worked out from the items · paid outside the app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (currentTotalLabel != null) {
                Text(
                    currentTotalLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough,
                )
            }
        }
    }
}

// Suggestions -------------------------------------------------------------------------------------

/** Their waiting changes, with Accept and Decline, like the prototype's red-edged card. */
@Composable
private fun ChangesCard(window: DealWindowUi, isSaving: Boolean, actions: DealSheetActions) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("${window.partnerFirstName} wants to change the deal", style = MaterialTheme.typography.titleSmall)
        ChangeLines(window.rows)
        Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            TTSecondaryButton(text = "Decline", onClick = actions.onDecline, modifier = Modifier.weight(1f), enabled = !isSaving)
            TTPrimaryButton(text = "Accept", onClick = actions.onAccept, modifier = Modifier.weight(1f), loading = isSaving)
        }
    }
}

/** My waiting changes, on the prototype's grey note. Withdraw is in the footer. */
@Composable
private fun WaitingNote(window: DealWindowUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Waiting for ${window.partnerFirstName} to accept your changes.", style = MaterialTheme.typography.bodyMedium)
        ChangeLines(window.rows)
    }
}

// Finishing and cancelling ------------------------------------------------------------------------

/** What the Mark as done button says, so each person can tell whose turn it is. */
private fun markDoneLabel(window: DealWindowUi): String = when {
    window.doneByMe -> "Waiting for ${window.partnerFirstName}"
    window.doneByPartner -> "Confirm done"
    else -> "Mark as done"
}

/**
 * Shown above the deal once someone has tapped Mark as done.
 * Green when it's my turn to confirm, grey when I'm the one waiting.
 */
@Composable
private fun DoneNote(window: DealWindowUi) {
    val myTurn = window.doneByPartner
    val container = if (myTurn) TTTheme.colors.successContainer else MaterialTheme.colorScheme.surfaceVariant
    val content = if (myTurn) TTTheme.colors.onSuccessContainer else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Outlined.TaskAlt, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Text(
            text = if (myTurn) {
                "${window.partnerFirstName} marked the deal as done. Tap Confirm done once you've both swapped everything."
            } else {
                "You marked the deal as done. Waiting for ${window.partnerFirstName} to confirm."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = content,
        )
    }
}

/** A quiet red text button, so calling the deal off never competes with the main action. */
@Composable
private fun CancelDealButton(enabled: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
    ) {
        Text("Cancel deal", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun CancelDealDialog(partnerFirstName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel this deal?") },
        text = { Text("The deal closes for you and $partnerFirstName, and nothing is marked sold. Your chat stays.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Cancel deal") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Keep deal") } },
    )
}

/** One line per change: green for what's added, red for what's removed, and price changes. */
@Composable
private fun ChangeLines(rows: List<DealRowUi>) {
    rows.forEach { row ->
        val (text, color) = when (row.change) {
            RowChange.ADDED -> "+ Adds ${row.title} · \$${row.price}" to TTTheme.colors.success
            RowChange.REMOVED -> "− Removes ${row.title}" to MaterialTheme.colorScheme.primary
            RowChange.PRICE_CHANGED -> "${row.title}: \$${row.oldPrice} → \$${row.price}" to MaterialTheme.colorScheme.onSurface
            RowChange.SAME -> return@forEach
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

// Changing the deal -------------------------------------------------------------------------------

@Composable
private fun EditLists(window: DealWindowUi, actions: DealSheetActions) {
    EditSection(
        heading = "${window.partnerFirstName.uppercase()}'S LISTINGS",
        headingColor = MaterialTheme.colorScheme.primary,
        inDeal = window.rows.filterNot { it.isMine },
        addable = window.addable.filterNot { it.isMine },
        actions = actions,
    )
    EditSection(
        heading = "YOUR LISTINGS",
        headingColor = MaterialTheme.colorScheme.onSurface,
        inDeal = window.rows.filter { it.isMine },
        addable = window.addable.filter { it.isMine },
        actions = actions,
    )
    Text(
        "Tick items to add or remove them. Tap a price to bargain on that item.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EditSection(
    heading: String,
    headingColor: Color,
    inDeal: List<DealRowUi>,
    addable: List<AddableListingUi>,
    actions: DealSheetActions,
) {
    Column {
        SectionLabel(heading, headingColor)
        if (inDeal.isEmpty() && addable.isEmpty()) {
            Text(
                "No listings to add",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = Spacing.sm),
            )
        }
        // Ticked rows first: they're what's in the deal now
        inDeal.forEach { row ->
            EditRow(
                title = row.title,
                note = if (row.isAvailable) "Listed at \$${row.listedPrice ?: row.price}" else "No longer available",
                checked = true,
                onToggle = { actions.onRemove(row.listingId) },
                price = row.price,
                onPriceChange = { text -> actions.onPriceChange(row.listingId, text) },
            )
        }
        addable.forEach { item ->
            EditRow(
                title = item.title,
                note = "Listed at \$${item.price}",
                checked = false,
                onToggle = { actions.onAdd(item) },
                price = null,
                onPriceChange = {},
            )
        }
    }
}

@Composable
private fun EditRow(
    title: String,
    note: String,
    checked: Boolean,
    onToggle: () -> Unit,
    price: Int?,
    onPriceChange: (String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // The tick box and the text are one tap target, read out as a checkbox
            Row(
                modifier = Modifier
                    .weight(1f)
                    .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                TickBox(checked)
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (checked && price != null) {
                PriceBox(price = price, onPriceChange = onPriceChange, label = "Price for $title")
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun TickBox(checked: Boolean) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .size(22.dp)
            .then(
                if (checked) Modifier.background(MaterialTheme.colorScheme.primary, shape)
                else Modifier.border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, shape),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
        }
    }
}

/** The red "$ 40" pill for bargaining on one item. Only digits reach the ViewModel's price. */
@Composable
private fun PriceBox(price: Int, onPriceChange: (String) -> Unit, label: String) {
    val red = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .width(88.dp)
            .height(40.dp)
            .border(1.5.dp, red, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$", style = MaterialTheme.typography.titleSmall, color = red)
        BasicTextField(
            value = price.toString(),
            onValueChange = onPriceChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleSmall.copy(color = red),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(red),
            modifier = Modifier
                .weight(1f)
                .padding(start = 2.dp)
                .semantics { contentDescription = label },
        )
    }
}

@Composable
private fun NewTotalBox(totalLabel: String, currentTotalLabel: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("New total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(totalLabel, style = MaterialTheme.typography.titleMedium)
        }
        if (currentTotalLabel != null) {
            Text(
                currentTotalLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
            )
        }
    }
}

// Previews ----------------------------------------------------------------------------------------

/** The sample data's deal in [chatId], as the signed-in sample student sees it. */
private fun previewWindow(chatId: String, doneBy: List<String> = emptyList()): DealWindowUi {
    val me = SampleData.ME
    val chat = SampleData.chats.first { it.id == chatId }
    val partnerId = chat.partnerId(me)
    return buildDealWindow(
        myId = me,
        partnerFirstName = SampleData.user(partnerId)?.firstName ?: "them",
        deal = chat.deal!!.copy(doneBy = doneBy),
        draft = null,
        listings = SampleData.listings.associateBy { it.id },
        myListings = SampleData.listings.filter { it.ownerId == me },
        partnerListings = SampleData.listings.filter { it.ownerId == partnerId },
    )
}

@PreviewLightDark
@Composable
private fun DealSheetReviewPreview() {
    TerrierTradeTheme {
        Surface { DealSheetContent(previewWindow("c_sam"), isSaving = false, error = null, actions = DealSheetActions()) }
    }
}

@PreviewLightDark
@Composable
private fun DealSheetViewPreview() {
    TerrierTradeTheme {
        Surface { DealSheetContent(previewWindow("c_nina"), isSaving = false, error = null, actions = DealSheetActions()) }
    }
}

@PreviewLightDark
@Composable
private fun DealSheetEditPreview() {
    TerrierTradeTheme {
        Surface {
            DealSheetContent(
                previewWindow("c_nina"),
                isSaving = false,
                error = null,
                actions = DealSheetActions(),
                startEditing = true,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DealSheetConfirmDonePreview() {
    TerrierTradeTheme {
        Surface {
            DealSheetContent(
                previewWindow("c_nina", doneBy = listOf("nina")),
                isSaving = false,
                error = null,
                actions = DealSheetActions(),
            )
        }
    }
}