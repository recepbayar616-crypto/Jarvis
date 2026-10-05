package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.ui.JarvisApp
import com.example.ui.JarvisViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    private val microphonePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startJarvisService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        handleVoiceCommand(intent)

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startJarvisService()
        } else {
            microphonePermission.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }

        setContent {
            MyApplicationTheme {
                JarvisApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        handleVoiceCommand(intent)
    }

    private fun handleVoiceCommand(intent: Intent?) {

        val command = intent?.getStringExtra(
            "JARVIS_VOICE_COMMAND"
        )

        if (!command.isNullOrBlank()) {

            viewModel.processVoiceInput(command)

            intent.removeExtra(
                "JARVIS_VOICE_COMMAND"
            )
        }
    }

    private fun startJarvisService() {

        val intent = Intent(
            this,
            JarvisVoiceService::class.java
        )

        ContextCompat.startForegroundService(
            this,
            intent
        )
    }
}
