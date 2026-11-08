package edu.bu.terriertrade.ui.screens.newlisting

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import edu.bu.terriertrade.ui.components.buttons.TTPrimaryButton
import edu.bu.terriertrade.ui.components.feedback.TTErrorText
import edu.bu.terriertrade.ui.components.inputs.TTTextField
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme
import edu.bu.terriertrade.util.Isbn

/** Full-screen camera for scanning a textbook's barcode, with a box to type the ISBN instead. */
@Composable
fun IsbnScannerDialog(
    onIsbn: (String) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val context = LocalContext.current
        var hasCamera by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
            )
        }
        val askForCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasCamera = granted
        }
        LaunchedEffect(Unit) {
            if (!hasCamera) askForCamera.launch(Manifest.permission.CAMERA)
        }

        IsbnScannerContent(
            hasCamera = hasCamera,
            onIsbn = onIsbn,
            onDismiss = onDismiss,
            errorMessage = errorMessage,
        )
    }
}

/** The scanner's layout, split out so it can be previewed without a camera. */
@Composable
private fun IsbnScannerContent(
    hasCamera: Boolean,
    onIsbn: (String) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String?,
) {
    var typed by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = ScannerBackground) {
        Column(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close scanner", tint = Color.White)
                }
                Text(
                    text = "Scan the barcode on the back",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                if (hasCamera) {
                    BarcodeCameraPreview(
                        onBarcode = onIsbn,
                        accept = { Isbn.normalize(it) != null },
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Frame to line the barcode up in.
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(2f),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                    ) {}
                } else {
                    Text(
                        text = "Camera access is off. Type the ISBN below, or allow the camera in Settings.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Text(
                        text = "No barcode? Type the ISBN",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    TTTextField(
                        value = typed,
                        onValueChange = { typed = it.filter { ch -> ch.isDigit() || ch == 'X' || ch == 'x' || ch == '-' }.take(17) },
                        label = "ISBN",
                        placeholder = "978…",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onIsbn(typed) }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TTErrorText(message = errorMessage)
                    TTPrimaryButton(
                        text = "Look up book",
                        onClick = { onIsbn(typed) },
                        enabled = typed.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private val ScannerBackground = Color(0xFF1E1A1A)

@PreviewLightDark
@Composable
private fun IsbnScannerNoCameraPreview() {
    TerrierTradeTheme {
        IsbnScannerContent(
            hasCamera = false,
            onIsbn = {},
            onDismiss = {},
            errorMessage = "That isn't a valid ISBN. Check the number on the back cover.",
        )
    }
}
