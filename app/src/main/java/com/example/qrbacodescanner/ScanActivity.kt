package com.example.qrbacodescanner

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

class ScanActivity : AppCompatActivity() {

    lateinit var surfaceView: SurfaceView
    lateinit var cameraSource: CameraSource
    lateinit var barcodeDetector: BarcodeDetector
    lateinit var textView: TextView
    lateinit var mediaPlayer: MediaPlayer
    private var lastText: String? = null
    private val requestCodeCameraPermission = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        textView = findViewById(R.id.textView)
        surfaceView = findViewById(R.id.surfaceView)
        mediaPlayer = MediaPlayer.create(this@ScanActivity, R.raw.beep3)
        surfaceView.setVisibility(View.INVISIBLE)
        askForCameraPermission()
        setupControls()
    }

    private fun setupControls() {

        barcodeDetector =
            BarcodeDetector.Builder(applicationContext).setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()
        cameraSource = CameraSource.Builder(applicationContext, barcodeDetector)
            .setRequestedPreviewSize(1280, 720).setAutoFocusEnabled(true).build()

        surfaceView.holder.addCallback(surfaceCallBack)

        barcodeDetector.setProcessor(processor)
    }


    private val surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            cameraSource.stop()
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                askForCameraPermission()
            }
            try {
                surfaceView.setVisibility(View.VISIBLE)
                cameraSource.start(holder)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {
            Log.e("RELEASE", "release: ")
        }

        override fun receiveDetections(p0: Detector.Detections<Barcode>) {
            //barcode buradan oxunur "qrcode" adli deyisenden aliriq datani

            val qrcode = p0.detectedItems
            if (qrcode.size() != 0 && qrcode.valueAt(0).displayValue != lastText) {
                Log.e("receiveDetections", qrcode.valueAt(0).displayValue)
                textView.post(object : Runnable {
                    override fun run() {
                        lastText = qrcode.valueAt(0).displayValue
                        textView.text = qrcode.valueAt(0).displayValue
                        mediaPlayer?.start()
                    }
                })
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
                surfaceView.visibility= View.VISIBLE
            }else{
                Toast.makeText(this, "İcazı verilmədi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Permissions
    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }
}