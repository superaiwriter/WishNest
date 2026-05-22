package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val date: String, // "YYYY-MM-DD" style or "MM-DD"
    val type: String, // "Birthday", "Anniversary", "Custom Event"
    val relationship: String, // "Family", "Friend", "Partner", "Colleague", "Other"
    val isFavorite: Boolean = false,
    val notes: String = "",
    val giftBudget: Double = 0.0,
    val hasOrderedCake: Boolean = false,
    val checkedPlannerItemsJson: String = "", // comma-separated checked list strings
    val phone: String = "", // For WhatsApp sending
    val streak: Int = 1, // friendship streaks
    val relationshipScore: Int = 75, // gamification relationship score (0-100)
    val memoryPhotosJson: String = "", // list of mock photo resources/drawables or texts
    val voiceNotesCount: Int = 0, // countdown voice note wishes
    val celebrationBadgeJson: String = "Spark" // custom badge comma-separated
) {
    // Helper to extract day and month for proximity checks
    val monthAndDay: Pair<Int, Int>
        get() {
            return try {
                val parts = date.split("-")
                if (parts.size >= 3) {
                    Pair(parts[1].toInt(), parts[2].toInt())
                } else if (parts.size == 2) {
                    Pair(parts[0].toInt(), parts[1].toInt())
                } else {
                    Pair(1, 1)
                }
            } catch (e: Exception) {
                Pair(1, 1) // Fallback
            }
        }
}
