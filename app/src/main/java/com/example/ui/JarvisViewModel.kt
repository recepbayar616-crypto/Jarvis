package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.gemini.OpenAIClient
import com.example.ai.gemini.JarvisAiResult
import com.example.ai.gemini.JarvisToolExecutor
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ConversationMessageEntity
import com.example.data.local.entity.TaskMemoryEntity
import com.example.data.local.entity.VideoEntity
import com.example.data.preferences.JarvisPreferences
import com.example.data.youtube.TodayReport
import com.example.data.youtube.VideoComparison
import com.example.data.youtube.YouTubeRepository
import com.example.voice.AssistantVoiceState
import com.example.voice.JarvisVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    val database = AppDatabase.getInstance(application)
    val dao = database.jarvisDao()
    val preferences = JarvisPreferences(application)
    val youTubeRepository = YouTubeRepository(dao, preferences)
    private val toolExecutor = JarvisToolExecutor(youTubeRepository, dao)
    private val openAIClient = OpenAIClient(toolExecutor)

    val channel: StateFlow<ChannelEntity?> = youTubeRepository.channelFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val videos: StateFlow<List<VideoEntity>> = youTubeRepository.allVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskMemoryEntity>> = dao.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversationHistory: StateFlow<List<ConversationMessageEntity>> = dao.getConversationFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _todayReport = MutableStateFlow<TodayReport?>(null)
    val todayReport: StateFlow<TodayReport?> = _todayReport.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>("JARVIS YouTube Sistemi Hazır")
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _lastUserSpeech = MutableStateFlow("")
    val lastUserSpeech: StateFlow<String> = _lastUserSpeech.asStateFlow()

    private val _lastJarvisReply = MutableStateFlow("Sisteme bağlanıldı. YouTube kanalınız ve vaka analizleriniz için hazırım, efendim. 'Bugün neler yaptık?' diyerek başlayabilirsiniz.")
    val lastJarvisReply: StateFlow<String> = _lastJarvisReply.asStateFlow()

    private val _activeComparison = MutableStateFlow<VideoComparison?>(null)
    val activeComparison: StateFlow<VideoComparison?> = _activeComparison.asStateFlow()

    val voiceManager = JarvisVoiceManager(
        context = application,
        preferences = preferences,
        onVoiceInputRecognized = { text ->
            processVoiceInput(text)
        }
    )

    val voiceState: StateFlow<AssistantVoiceState> = voiceManager.voiceState
    val audioRms: StateFlow<Float> = voiceManager.audioRms
    val statusMessage: StateFlow<String> = voiceManager.statusMessage

    init {
        viewModelScope.launch {
            _isRefreshing.value = true
            val target = preferences.getChannelId().ifBlank { "@polisvakasi" }
            val result = youTubeRepository.connectChannel(target)
            _isRefreshing.value = false
            if (result.isSuccess) {
                _syncMessage.value = result.getOrNull()
            }
            loadTodayReport()
        }
    }

    fun startListening() {
        voiceManager.startListening()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun toggleListening() {
        if (voiceState.value == AssistantVoiceState.LISTENING) {
            stopListening()
        } else if (voiceState.value == AssistantVoiceState.SPEAKING) {
            stopSpeaking()
        } else {
            startListening()
        }
    }

    fun speak(text: String) {
        voiceManager.speak(text)
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun processVoiceInput(input: String) {
        if (input.isBlank()) return
        _lastUserSpeech.value = input

        viewModelScope.launch {
            // Save user query to conversation history
            dao.insertMessage(
                ConversationMessageEntity(
                    sender = "USER",
                    message = input
                )
            )

            voiceManager.setThinking()
            val result: JarvisAiResult = openAIClient.processUserVoiceInput(input)

            _lastJarvisReply.value = result.replyText

            // Save Jarvis reply
            dao.insertMessage(
                ConversationMessageEntity(
                    sender = "JARVIS",
                    message = result.replyText,
                    toolName = result.toolUsed,
                    toolResultSnippet = result.toolDataSnippet
                )
            )

            // Speak answer verbally in Turkish
            voiceManager.speak(result.replyText)

            // Reload today report if changed
            loadTodayReport()
        }
    }

    fun loadTodayReport() {
        viewModelScope.launch {
            _todayReport.value = youTubeRepository.getTodayReport()
        }
    }

    fun refreshYouTube() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = youTubeRepository.refreshYouTubeData()
            _isRefreshing.value = false
            if (result.isSuccess) {
                _syncMessage.value = result.getOrNull()
            } else {
                _syncMessage.value = "Hata: ${result.exceptionOrNull()?.localizedMessage}"
            }
            loadTodayReport()
        }
    }

    fun connectChannel(channelInput: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = youTubeRepository.connectChannel(channelInput)
            _isRefreshing.value = false
            if (result.isSuccess) {
                _syncMessage.value = result.getOrNull()
                _lastJarvisReply.value = "Kanal başarıyla bağlandı, canlı veriler getirildi efendim."
            } else {
                _syncMessage.value = "Bağlantı hatası: ${result.exceptionOrNull()?.localizedMessage}"
            }
            loadTodayReport()
        }
    }

    fun clearDemoData() {
        viewModelScope.launch {
            youTubeRepository.clearAllData()
            _todayReport.value = null
            _syncMessage.value = "Önbellek ve demo veriler temizlendi."
            _lastJarvisReply.value = "Demo ve önbellek verileri temizlendi. Canlı YouTube bağlantınız bekleniyor."
            loadTodayReport()
        }
    }

    fun compareVideos(videoId1: String, videoId2: String) {
        viewModelScope.launch {
            _activeComparison.value = youTubeRepository.compareVideos(videoId1, videoId2)
        }
    }

    fun addTask(title: String, category: String = "Vaka Araştırması", details: String? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            dao.insertTask(
                TaskMemoryEntity(
                    title = title.trim(),
                    category = category,
                    details = details
                )
            )
        }
    }

    fun toggleTask(task: TaskMemoryEntity) {
        viewModelScope.launch {
            dao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskMemoryEntity) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearConversation()
            _lastJarvisReply.value = "Sohbet ve komut geçmişi temizlendi, efendim."
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            dao.clearVideos()
            dao.clearChannel()
            preferences.clearAll()
            _syncMessage.value = "Önbellek ve kimlik bilgileri sıfırlandı."
            _todayReport.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
