            val intent =
                Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                ).apply {

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        "tr-TR"
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                        "tr-TR"
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                        true
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_MAX_RESULTS,
                        1
                    )
                }

            speechRecognizer?.startListening(intent)

            _voiceState.value =
                AssistantVoiceState.LISTENING

            _statusMessage.value =
                "Dinliyorum, efendim..."

        } catch (e: Exception) {

            Log.e(
                "VoiceManager",
                "Start listening error",
                e
            )

            _voiceState.value =
                AssistantVoiceState.ERROR

            _statusMessage.value =
                "Mikrofon başlatılamadı."
        }
    }

    fun stopListening() {

        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(
                "VoiceManager",
                "Stop listening error",
                e
            )
        }

        if (
            _voiceState.value ==
            AssistantVoiceState.LISTENING
        ) {
            _voiceState.value =
                AssistantVoiceState.IDLE

            _statusMessage.value =
                "JARVIS Hazır"
        }
    }

    fun setThinking() {

        _voiceState.value =
            AssistantVoiceState.THINKING

        _statusMessage.value =
            "Analiz ediliyor..."
    }

    fun speak(text: String) {

        if (
            !preferences.isAutoSpeakEnabled() ||
            text.isBlank()
        ) {
            _voiceState.value =
                AssistantVoiceState.IDLE

            return
        }

        stopListening()

        speechJob?.cancel()

        speechJob = scope.launch {

            try {

                _voiceState.value =
                    AssistantVoiceState.SPEAKING

                _statusMessage.value =
                    "JARVIS Konuşuyor..."

                val audioFile =
                    ttsClient.generateSpeech(text)

                if (audioFile == null) {

                    Log.e(
                        "VoiceManager",
                        "OpenAI TTS ses oluşturamadı."
                    )

                    _voiceState.value =
                        AssistantVoiceState.ERROR

                    _statusMessage.value =
                        "Ses oluşturulamadı."

                    return@launch
                }

                playAudio(audioFile)

            } catch (e: Exception) {

                Log.e(
                    "VoiceManager",
                    "TTS error",
                    e
                )

                _voiceState.value =
                    AssistantVoiceState.ERROR

                _statusMessage.value =
                    "Ses oluşturulamadı."
            }
        }
    }

    private fun playAudio(file: File) {

        try {

            mediaPlayer?.release()

            val player =
                MediaPlayer()

            mediaPlayer = player

            player.setDataSource(
                file.absolutePath
            )

            player.setOnCompletionListener {

                _voiceState.value =
                    AssistantVoiceState.IDLE

                _statusMessage.value =
                    "JARVIS Hazır"

                player.release()

                mediaPlayer = null

                file.delete()
            }

            player.setOnErrorListener { _, _, _ ->

                _voiceState.value =
                    AssistantVoiceState.ERROR

                _statusMessage.value =
                    "Ses oynatılamadı."

                player.release()

                mediaPlayer = null

                file.delete()

                true
            }

            player.prepare()
            player.start()

        } catch (e: Exception) {

            Log.e(
                "VoiceManager",
                "Audio playback error",
                e
            )

            mediaPlayer = null

            file.delete()

            _voiceState.value =
                AssistantVoiceState.ERROR

            _statusMessage.value =
                "Ses oynatılamadı."
        }
    }

    fun stopSpeaking() {

        speechJob?.cancel()

        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }

        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }

        mediaPlayer = null

        _voiceState.value =
            AssistantVoiceState.IDLE

        _statusMessage.value =
            "JARVIS Hazır"
    }

    fun destroy() {

        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {
        }

        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }

        speechRecognizer = null

        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }

        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }

        mediaPlayer = null

        speechJob?.cancel()
    }
}
