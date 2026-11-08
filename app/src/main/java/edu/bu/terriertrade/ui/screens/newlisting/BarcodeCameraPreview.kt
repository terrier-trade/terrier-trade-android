package edu.bu.terriertrade.ui.screens.newlisting

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Live back-camera preview that reads EAN-13 barcodes (the kind printed on books) with ML Kit.
 * Calls onBarcode once, for the first code that accept() likes, then stops reporting.
 * Needs the CAMERA permission; the caller asks for it.
 */
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeCameraPreview(
    onBarcode: (String) -> Unit,
    modifier: Modifier = Modifier,
    accept: (String) -> Boolean = { true },
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val latestOnBarcode by rememberUpdatedState(onBarcode)
    val latestAccept by rememberUpdatedState(accept)

    DisposableEffect(lifecycleOwner) {
        val analyzerThread = Executors.newSingleThreadExecutor()
        val mainThread = ContextCompat.getMainExecutor(context)
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_EAN_13).build(),
        )
        val reported = AtomicBoolean(false)
        val providerFuture = ProcessCameraProvider.getInstance(context)

        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(analyzerThread) { frame ->
                val mediaImage = frame.image
                if (mediaImage == null || reported.get()) {
                    frame.close()
                    return@setAnalyzer
                }
                val input = InputImage.fromMediaImage(mediaImage, frame.imageInfo.rotationDegrees)
                scanner.process(input)
                    .addOnSuccessListener(mainThread) { barcodes ->
                        val code = barcodes.mapNotNull { it.rawValue }.firstOrNull(latestAccept)
                        if (code != null && reported.compareAndSet(false, true)) latestOnBarcode(code)
                    }
                    .addOnCompleteListener { frame.close() }
            }

            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, mainThread)

        onDispose {
            if (providerFuture.isDone) providerFuture.get().unbindAll()
            scanner.close()
            analyzerThread.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
