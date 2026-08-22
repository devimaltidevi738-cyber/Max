package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.SpeechState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AuraVoiceEngine(
    private val context: Context,
    private val onCommandRecognized: (String) -> Unit,
    private val onWakeWordDetected: () -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    private val _speechState = MutableStateFlow(SpeechState.IDLE)
    val speechState = _speechState.asStateFlow()

    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude = _rmsAmplitude.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript = _liveTranscript.asStateFlow()

    private val _isContinuousListening = MutableStateFlow(false)
    val isContinuousListening = _isContinuousListening.asStateFlow()

    private val _lastSpokenResponse = MutableStateFlow("")
    val lastSpokenResponse = _lastSpokenResponse.asStateFlow()

    init {
        initializeTts()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            // Try Hindi or fallback to English India
            val hindiResult = tts?.setLanguage(Locale("hi", "IN"))
            if (hindiResult == TextToSpeech.LANG_MISSING_DATA || hindiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("en", "IN"))
            }
            tts?.setPitch(1.05f)
            tts?.setSpeechRate(0.95f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _speechState.value = SpeechState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    _speechState.value = SpeechState.IDLE
                    if (_isContinuousListening.value) {
                        startListening()
                    }
                }

                override fun onError(utteranceId: String?) {
                    _speechState.value = SpeechState.IDLE
                }
            })
            Log.d(TAG, "MAX TTS initialized successfully")
        } else {
            Log.e(TAG, "TTS Initialization failed with status $status")
        }
    }

    fun speak(text: String, isHindi: Boolean = true, onComplete: (() -> Unit)? = null) {
        _lastSpokenResponse.value = text
        if (!isTtsReady || tts == null) {
            Log.w(TAG, "TTS not ready, skipping voice output: $text")
            _speechState.value = SpeechState.IDLE
            onComplete?.invoke()
            return
        }

        try {
            if (isHindi) {
                tts?.setLanguage(Locale("hi", "IN"))
            } else {
                tts?.setLanguage(Locale("en", "IN"))
            }
            _speechState.value = SpeechState.SPEAKING
            val utteranceId = "max_${System.currentTimeMillis()}"
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error in TTS speak", e)
            _speechState.value = SpeechState.IDLE
        }
    }

    fun startListening() {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.w(TAG, "Speech recognition not available on this device")
                return
            }

            stopListening()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(MaxRecognitionListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            _speechState.value = SpeechState.LISTENING
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognizer", e)
            _speechState.value = SpeechState.ERROR
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            if (_speechState.value == SpeechState.LISTENING) {
                _speechState.value = SpeechState.IDLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer", e)
        }
    }

    fun toggleListening() {
        if (_speechState.value == SpeechState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun setContinuousListening(enabled: Boolean) {
        _isContinuousListening.value = enabled
        if (enabled && _speechState.value == SpeechState.IDLE) {
            startListening()
        } else if (!enabled) {
            stopListening()
        }
    }

    // Process direct text input (e.g. from user keyboard, quick command chip or simulator)
    fun processTextInput(input: String) {
        _liveTranscript.value = input
        _speechState.value = SpeechState.PROCESSING
        checkWakeWordOrExecute(input)
    }

    private fun checkWakeWordOrExecute(text: String) {
        val lower = text.trim().lowercase(Locale.ROOT)
        // Wake Word Detection for MAX: "Hey MAX", "MAX suno", "MAX", "Hey Max"
        val wakePhrases = listOf("hey max", "max suno", "hey maks", "maks suno", "hai max", "max", "maks")
        for (wp in wakePhrases) {
            if (lower.startsWith(wp) || lower == wp) {
                val afterWakeWord = lower.removePrefix(wp).trim()
                if (afterWakeWord.isBlank() || afterWakeWord == "ji" || afterWakeWord == "boli" || afterWakeWord == "boliye") {
                    onWakeWordDetected()
                    speak("Ji, main MAX hoon. Boliye.", isHindi = true)
                    _speechState.value = SpeechState.LISTENING
                    return
                } else {
                    // Wake word + command in one sentence e.g. "Hey MAX, YouTube kholo"
                    onCommandRecognized(afterWakeWord)
                    return
                }
            }
        }

        // Direct command
        onCommandRecognized(text)
    }

    private inner class MaxRecognitionListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _speechState.value = SpeechState.LISTENING
            _liveTranscript.value = "Listening..."
        }

        override fun onBeginningOfSpeech() {
            _speechState.value = SpeechState.LISTENING
        }

        override fun onRmsChanged(rmsdB: Float) {
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _rmsAmplitude.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _speechState.value = SpeechState.PROCESSING
            _rmsAmplitude.value = 0f
        }

        override fun onError(error: Int) {
            Log.w(TAG, "Speech recognition error code: $error")
            _speechState.value = SpeechState.IDLE
            _rmsAmplitude.value = 0f
            if (_isContinuousListening.value && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                startListening()
            }
        }

        override fun onResults(results: Bundle?) {
            _speechState.value = SpeechState.PROCESSING
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()
            if (!recognizedText.isNullOrBlank()) {
                _liveTranscript.value = recognizedText
                checkWakeWordOrExecute(recognizedText)
            } else {
                _speechState.value = SpeechState.IDLE
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull()
            if (!partial.isNullOrBlank()) {
                _liveTranscript.value = partial
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun destroy() {
        stopListening()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        private const val TAG = "MaxVoiceEngine"
    }
}
