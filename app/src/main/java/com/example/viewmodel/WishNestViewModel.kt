package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.AppDatabase
import com.example.data.Event
import com.example.data.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Dashboard : Screen()
    object Timeline : Screen()
    object AddEvent : Screen()
    data class EventDetail(val eventId: Int) : Screen()
    object WishGenerator : Screen()
    object ContactSync : Screen()
    object Premium : Screen()
    object Notifications : Screen()
    object Settings : Screen()
}

@Suppress("DEPRECATION")
class WishNestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository

    // Native reactive list of events
    val events: StateFlow<List<Event>>
    val favoriteEvents: StateFlow<List<Event>>

    // Custom reactive Backstack Engine
    val screenStack = mutableStateListOf<Screen>(Screen.Splash)
    val currentScreen: Screen get() = screenStack.lastOrNull() ?: Screen.Dashboard

    // State bindings
    var showSplash by mutableStateOf(true)
    var onboardingStep by mutableStateOf(0)
    var onboardingName by mutableStateOf("")
    var onboardingBirthdate by mutableStateOf("1998-08-15")

    // Filters and search logic
    var searchQuery by mutableStateOf("")
    var filterType by mutableStateOf("All")
    var filterRelationship by mutableStateOf("All")

    // AI Wish Generator parameters & outcomes
    var generatorName by mutableStateOf("")
    var generatorType by mutableStateOf("Birthday")
    var generatorRelation by mutableStateOf("Friend")
    var generatorTone by mutableStateOf("Warm")
    var generatorMemories by mutableStateOf("")
    var generatorInsideJokes by mutableStateOf("")
    var generatorRelationshipDetails by mutableStateOf("")
    var generatedWish by mutableStateOf("")
    var isGeneratingWish by mutableStateOf(false)

    // Gift Recommendation states
    var giftHobbiesInput by mutableStateOf("")
    var isGeneratingGifts by mutableStateOf(false)
    var generatedGifts by mutableStateOf("")

    // Notifications
    val notifications = mutableStateListOf<NotificationItem>()

    // Global customization
    var isDarkTheme by mutableStateOf(false)
    var isPremiumUser by mutableStateOf(false)

    // Quick status
    var relationshipScoreFilter by mutableStateOf(50)

    init {
        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        events = repository.allEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        favoriteEvents = repository.favoriteEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data automatically if db is empty
        viewModelScope.launch {
            events.collect { list ->
                if (list.isEmpty()) {
                    seedDefaultEvents()
                }
            }
        }

        // Initialize default alerts
        notifications.add(
            NotificationItem(
                id = 1,
                title = "Welcome to WishNest!",
                body = "Your emotional celebration tracker is ready. Synchronize contacts or add important dates to start generating wishes!",
                time = "Just now",
                isUnread = true
            )
        )
        notifications.add(
            NotificationItem(
                id = 2,
                title = "🎂 Cake Ordering Reminder",
                body = "Mom's Birthday is in 2 days. Consider pre-ordering her favorite fruit delight today!",
                time = "10m ago",
                isUnread = true
            )
        )
    }

    // Navigation triggers
    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            screenStack.add(screen)
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            return true
        }
        return false
    }

    fun resetNavigation(screen: Screen) {
        screenStack.clear()
        screenStack.add(screen)
    }

    // Seeding logic (relative to simulated date: May 22, 2026)
    private suspend fun seedDefaultEvents() {
        val defaults = listOf(
            Event(
                name = "Mom",
                date = "1973-05-24", // May 24 (2 days away)
                type = "Birthday",
                relationship = "Family",
                isFavorite = true,
                notes = "Loves garden tulips and red velvet cakes.",
                giftBudget = 100.0,
                hasOrderedCake = false,
                checkedPlannerItemsJson = "Send card,Call at midnight",
                phone = "+15550240101",
                streak = 9,
                relationshipScore = 98,
                memoryPhotosJson = "Mom smiling,Bday cake,Gardening",
                voiceNotesCount = 2,
                celebrationBadgeJson = "Devotion,Bright Light"
            ),
            Event(
                name = "Bestie Alice",
                date = "1997-05-30", // May 30 (8 days away)
                type = "Birthday",
                relationship = "Friend",
                isFavorite = true,
                notes = "Loves concert tickets and retro photography.",
                giftBudget = 50.0,
                hasOrderedCake = false,
                checkedPlannerItemsJson = "",
                phone = "+15553010202",
                streak = 15,
                relationshipScore = 88,
                memoryPhotosJson = "Alice beach,Graduation",
                voiceNotesCount = 1,
                celebrationBadgeJson = "Loyal Bestie,Laughter Maker"
            ),
            Event(
                name = "Sarah & Marcus",
                date = "2018-05-28", // May 28 (6 days away)
                type = "Anniversary",
                relationship = "Partner",
                isFavorite = true,
                notes = "6-year marriage milestone. Silver Jubilee theme.",
                giftBudget = 250.0,
                hasOrderedCake = true,
                checkedPlannerItemsJson = "Order anniversary bouquet,Book premium dine-in",
                phone = "+15552800303",
                streak = 24,
                relationshipScore = 95,
                memoryPhotosJson = "Wedding vows,Paris holiday",
                voiceNotesCount = 0,
                celebrationBadgeJson = "Soulmates,Infinite Bond"
            ),
            Event(
                name = "Boss Richard",
                date = "1981-06-15", // Next month
                type = "Birthday",
                relationship = "Colleague",
                isFavorite = false,
                notes = "Enjoys fine ground roast coffee and executive planners.",
                giftBudget = 40.0,
                hasOrderedCake = false,
                checkedPlannerItemsJson = "Sign corporate card",
                phone = "+15551500404",
                streak = 1,
                relationshipScore = 65,
                memoryPhotosJson = "",
                voiceNotesCount = 0,
                celebrationBadgeJson = "Leader"
            )
        )

        for (e in defaults) {
            repository.insert(e)
        }
    }

    // CRUD database actions
    fun addEvent(
        name: String,
        date: String,
        type: String,
        relationship: String,
        isFavorite: Boolean,
        notes: String,
        giftBudget: Double,
        phone: String
    ) {
        viewModelScope.launch {
            val element = Event(
                name = name,
                date = date,
                type = type,
                relationship = relationship,
                isFavorite = isFavorite,
                notes = notes,
                giftBudget = giftBudget,
                phone = phone,
                streak = (2..12).random(),
                relationshipScore = (60..99).random(),
                checkedPlannerItemsJson = "",
                memoryPhotosJson = "",
                voiceNotesCount = 0,
                celebrationBadgeJson = "Sparkle"
            )
            repository.insert(element)
            addNotification(
                "📅 Event Created",
                "Successfully added $name's $type to WishNest! We will remind you ahead of time."
            )
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.update(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.delete(event)
            addNotification("🗑️ Event Removed", "Removed reminder for ${event.name}.")
        }
    }

    fun toggleFavorite(event: Event) {
        viewModelScope.launch {
            repository.update(event.copy(isFavorite = !event.isFavorite))
        }
    }

    fun toggleOrderCake(event: Event) {
        viewModelScope.launch {
            repository.update(event.copy(hasOrderedCake = !event.hasOrderedCake))
        }
    }

    fun togglePlannerItem(event: Event, item: String) {
        viewModelScope.launch {
            val items = event.checkedPlannerItemsJson.split(",").filter { it.isNotEmpty() }.toMutableList()
            if (item in items) {
                items.remove(item)
            } else {
                items.add(item)
            }
            repository.update(event.copy(checkedPlannerItemsJson = items.joinToString(",")))
        }
    }

    fun addManualPlannerItem(event: Event, newItem: String) {
        if (newItem.isBlank()) return
        viewModelScope.launch {
            val currentChecked = event.checkedPlannerItemsJson.split(",").filter { it.isNotEmpty() }.toMutableList()
            currentChecked.add(newItem) // Add it to checklist
            repository.update(event.copy(checkedPlannerItemsJson = currentChecked.joinToString(",")))
        }
    }

    fun addMemoryPhoto(event: Event, name: String) {
        viewModelScope.launch {
            val currentPhotos = event.memoryPhotosJson.split(",").filter { it.isNotEmpty() }.toMutableList()
            currentPhotos.add(name)
            repository.update(event.copy(memoryPhotosJson = currentPhotos.joinToString(",")))
        }
    }

    fun recordMockVoiceNote(event: Event) {
        viewModelScope.launch {
            repository.update(event.copy(voiceNotesCount = event.voiceNotesCount + 1))
            addNotification("🎙️ Greeting Voice Note Saved", "Successfully recorded a personalized audio wish for ${event.name}!")
        }
    }

    fun incrementStreak(event: Event) {
        viewModelScope.launch {
            repository.update(event.copy(
                streak = event.streak + 1,
                relationshipScore = (event.relationshipScore + 3).coerceAtMost(100)
            ))
            addNotification("🔥 Friendship Streak Level Up!", "Your streak with ${event.name} reached ${event.streak + 1}! Relationship status is strong.")
        }
    }

    // AI Generation wrappers
    fun triggerAIGeneration(name: String, relation: String, eventType: String, tone: String, initialNotes: String = "") {
        generatorName = name
        generatorRelation = relation
        generatorType = eventType
        generatorTone = tone
        generatorMemories = ""
        generatorInsideJokes = ""
        generatorRelationshipDetails = initialNotes
        generatedWish = ""
        navigateTo(Screen.WishGenerator)
        generateWish()
    }

    fun generateWish() {
        if (generatorName.isBlank()) {
            generatedWish = "Please enter a name to generate wishes."
            return
        }

        isGeneratingWish = true
        viewModelScope.launch {
            val personalContext = StringBuilder()
            if (generatorRelationshipDetails.isNotBlank()) {
                personalContext.append("- Relationship details/context: $generatorRelationshipDetails\n")
            }
            if (generatorMemories.isNotBlank()) {
                personalContext.append("- Shared memories or stories: $generatorMemories\n")
            }
            if (generatorInsideJokes.isNotBlank()) {
                personalContext.append("- Inside jokes or slang: $generatorInsideJokes\n")
            }

            val prompt = if (personalContext.isNotEmpty()) {
                """
                    Generate a highly personalized, creative, and extremely heartfelt $generatorTone $generatorType wish for my $generatorRelation named $generatorName.
                    Here are some unique personal details to naturally weave into the message:
                    $personalContext
                    Rules for generation:
                    1. Incorporate the memories and inside jokes seamlessly. Do NOT just list them; integrate them gracefully in a way a real close friend or family member would write.
                    2. Maintain a fittingly $generatorTone tone with some suitable emojis.
                    3. Max 3-4 sentences. Do NOT include generic placeholder greetings. Be direct, authentic, and engaging.
                """.trimIndent()
            } else {
                """
                    Generate a highly personalized, creative, and extremely heartfelt $generatorTone $generatorType wish for my $generatorRelation named $generatorName.
                    Keep it charming, engaging, fitting the tone, and add relevant emojis.
                    Avoid dry language. Max 3 sentences. Be direct, authentic, and engaging.
                """.trimIndent()
            }
            
            val result = GeminiClient.generateWish(prompt)
            generatedWish = result
            isGeneratingWish = false
        }
    }

    fun generateGiftSuggestions(event: Event) {
        isGeneratingGifts = true
        viewModelScope.launch {
            val prompt = """
                Suggest 4 specific gift ideas for my $generatorRelation named ${event.name} for their ${event.type}.
                Budget: $${event.giftBudget}.
                Interests or custom details: $giftHobbiesInput.
                Output as a list with interesting emojis. Keep it extremely creative.
            """.trimIndent()
            
            val result = GeminiClient.generateGifts(prompt)
            generatedGifts = result
            isGeneratingGifts = false
        }
    }

    // Import Contact list simulator (Google contacts synchronization)
    fun syncMockContacts() {
        viewModelScope.launch {
            val mockContacts = listOf(
                Pair("Dr. Helen Carter", "1979-11-12"),
                Pair("Uncle Bob", "1966-07-20"),
                Pair("Cousin Leo", "1995-05-30"),
                Pair("Audrey Hep", "1994-08-04")
            )
            for (contact in mockContacts) {
                val element = Event(
                    name = contact.first,
                    date = contact.second,
                    type = "Birthday",
                    relationship = if (contact.first.contains("Uncle")) "Family" else "Friend",
                    isFavorite = false,
                    notes = "Imported from phone book synchronization.",
                    giftBudget = 50.0,
                    streak = 1,
                    relationshipScore = 70
                )
                repository.insert(element)
            }
            addNotification(
                "✨ Core Synced",
                "Imported and synchronized birthdays from Google and Phone local contacts."
            )
        }
    }

    // System reminders setup helper
    fun addNotification(title: String, body: String) {
        val nextId = (notifications.maxOfOrNull { it.id } ?: 0) + 1
        notifications.add(
            0, // Insert at top
            NotificationItem(
                id = nextId,
                title = title,
                body = body,
                time = "Just now",
                isUnread = true
            )
        )
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAll()
            notifications.clear()
            screenStack.clear()
            screenStack.add(Screen.Dashboard)
            addNotification("🧹 Reset Complete", "Cleared database state back to clean environment.")
        }
    }

    fun markNotificationsRead() {
        for (i in notifications.indices) {
            notifications[i] = notifications[i].copy(isUnread = false)
        }
    }
}

data class NotificationItem(
    val id: Int,
    val title: String,
    val body: String,
    val time: String,
    val isUnread: Boolean
)
