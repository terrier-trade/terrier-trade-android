package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tap to pick a date, then a time. Only today and later can be picked.
 * The value is epoch millis in the phone's time zone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTDateTimeField(
    valueMillis: Long?,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date and time",
    errorMessage: String? = null,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    var step by remember { mutableStateOf(PickerStep.None) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = label, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
        Surface(
            onClick = { step = PickerStep.Date },
            shape = MaterialTheme.shapes.medium,
            color = colors.surface,
            border = BorderStroke(1.dp, if (errorMessage != null) colors.error else colors.outline),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Icon(Icons.Outlined.Event, contentDescription = null, tint = colors.primary)
                Text(
                    text = valueMillis?.let { formatDateTime(it, zone) } ?: "Pick a date",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (valueMillis == null) colors.onSurfaceVariant else colors.onSurface,
                )
            }
        }
        TTErrorText(message = errorMessage)
    }

    if (step == PickerStep.Date) {
        val todayUtcMillis = LocalDate.now(zone).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = valueMillis?.let { toUtcDateMillis(it, zone) },
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtcMillis
            },
        )
        DatePickerDialog(
            onDismissRequest = { step = PickerStep.None },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDate = dateState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        step = if (pickedDate != null) PickerStep.Time else PickerStep.None
                    },
                ) { Text("Next") }
            },
            dismissButton = { TextButton(onClick = { step = PickerStep.None }) { Text("Cancel") } },
        ) {
            DatePicker(state = dateState)
        }
    }

    if (step == PickerStep.Time) {
        val current = valueMillis?.let { Instant.ofEpochMilli(it).atZone(zone) }
        val timeState = rememberTimePickerState(
            initialHour = current?.hour ?: 19,
            initialMinute = current?.minute ?: 0,
        )
        AlertDialog(
            onDismissRequest = { step = PickerStep.None },
            title = { Text("Pick a time") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDate?.let { date ->
                            onValueChange(date.atTime(timeState.hour, timeState.minute).atZone(zone).toInstant().toEpochMilli())
                        }
                        step = PickerStep.None
                    },
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { step = PickerStep.None }) { Text("Cancel") } },
        )
    }
}

private enum class PickerStep { None, Date, Time }

private val dateTimeFormat = DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a", Locale.US)

/** "Fri, Nov 14 · 7:00 PM" */
fun formatDateTime(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(millis).atZone(zone).format(dateTimeFormat)

/** The date picker works in UTC midnights, so convert the local date to that. */
private fun toUtcDateMillis(millis: Long, zone: ZoneId): Long =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

@PreviewLightDark
@Composable
private fun TTDateTimeFieldPreview() {
    TTPreviewColumn {
        TTDateTimeField(valueMillis = null, onValueChange = {})
        TTDateTimeField(valueMillis = 1_794_700_800_000L, onValueChange = {})
        TTDateTimeField(valueMillis = null, onValueChange = {}, errorMessage = "Pick the date and time")
    }
}