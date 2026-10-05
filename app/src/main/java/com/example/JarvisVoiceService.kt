private fun speak(text: String) {

    if (isSpeaking) return

    isSpeaking = true

    CoroutineScope(Dispatchers.Main).launch {

        try {

            val audioFile = ttsClient.generateSpeech(text)

            if (audioFile == null) {
                isSpeaking = false
                return@launch
            }

            mediaPlayer?.release()

            mediaPlayer =
                MediaPlayer().apply {

                    setDataSource(audioFile.absolutePath)

                    setOnCompletionListener {

                        isSpeaking = false

                        release()

                        mediaPlayer = null

                        if (waitingForCommand) {

                            handler.postDelayed({

                                startRecognition()

                            }, 300)
                        }
                    }

                    setOnErrorListener { _, _, _ ->

                        isSpeaking = false

                        release()

                        mediaPlayer = null

                        true
                    }

                    prepare()
                    start()
                }

        } catch (e: Exception) {

            e.printStackTrace()

            isSpeaking = false
        }
    }
}
