package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ModelPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ModelContent(
    @Json(name = "parts") val parts: List<ModelPart>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<ModelContent>
)

@JsonClass(generateAdapter = true)
data class ModelCandidate(
    @Json(name = "content") val content: ModelContent? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<ModelCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object GeminiClient {
    suspend fun generateWish(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return generateMockWishFallback(prompt)
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(ModelContent(parts = listOf(ModelPart(text = prompt))))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: generateMockWishFallback(prompt)
        } catch (e: Exception) {
            e.printStackTrace()
            return generateMockWishFallback(prompt)
        }
    }

    suspend fun generateGifts(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return generateMockGiftsFallback(prompt)
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(ModelContent(parts = listOf(ModelPart(text = prompt))))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: generateMockGiftsFallback(prompt)
        } catch (e: Exception) {
            e.printStackTrace()
            return generateMockGiftsFallback(prompt)
        }
    }

    private fun generateMockWishFallback(prompt: String): String {
        val lower = prompt.lowercase()
        val name = when {
            "name " in lower -> {
                val idx = lower.indexOf("name ")
                prompt.substring(idx + 5).trim().split(" ", "\n", ",").firstOrNull() ?: "Friend"
            }
            "named " in lower -> {
                val idx = lower.indexOf("named ")
                prompt.substring(idx + 6).trim().split(" ", "\n", ",").firstOrNull() ?: "Friend"
            }
            "for my " in lower -> {
                val idx = lower.indexOf("for my ")
                prompt.substring(idx + 7).trim().split(" ", "\n", ",").firstOrNull() ?: "Friend"
            }
            else -> "Friend"
        }.replace(Regex("[^A-Za-z0-9]"), "").trim()

        val type = when {
            "anniversary" in lower -> "Anniversary"
            "custom" in lower || "event" in lower -> "Celebrating"
            else -> "Birthday"
        }

        val tone = when {
            "funny" in lower -> "funny"
            "professional" in lower -> "professional"
            "emotional" in lower -> "emotional"
            "caption" in lower -> "caption"
            else -> "warm"
        }

        // Parse custom personalization criteria if provided in prompt lines
        val lines = prompt.split("\n")
        var detailsContext = ""
        var memoryDetail = ""
        var insideJoke = ""

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("- Relationship details/context:")) {
                detailsContext = trimmed.removePrefix("- Relationship details/context:").trim()
            } else if (trimmed.startsWith("- Shared memories or stories:")) {
                memoryDetail = trimmed.removePrefix("- Shared memories or stories:").trim()
            } else if (trimmed.startsWith("- Inside jokes or slang:")) {
                insideJoke = trimmed.removePrefix("- Inside jokes or slang:").trim()
            }
        }

        val hasCustomAttributes = detailsContext.isNotBlank() || memoryDetail.isNotBlank() || insideJoke.isNotBlank()

        if (hasCustomAttributes) {
            val greeting = when (type) {
                "Anniversary" -> "Happy Anniversary, $name! 🎉"
                "Celebrating" -> "Cheers to your special milestone, $name! 🌟"
                else -> "Happy Birthday, $name! 🎂"
            }

            val detailsSentence = if (detailsContext.isNotBlank()) {
                "Having you as my close connection ($detailsContext) is a true blessing."
            } else ""

            val memorySentence = if (memoryDetail.isNotBlank()) {
                "I was just smiling thinking back to $memoryDetail—what a wonderful memory together!"
            } else ""

            val jokeSentence = if (insideJoke.isNotBlank()) {
                "Never forget our gold-tier moment of '$insideJoke'! 😂"
            } else ""

            val signoff = when (tone) {
                "funny" -> "Keep being retro-awesome! Hope you get loaded with cake! 🍰🥂"
                "professional" -> "Wishing you continued heights of achievements and success. Best regards."
                "emotional" -> "You mean so incredibly much to me, and I'm always here for you. ❤️"
                "caption" -> "Another beautiful trip around the sun! Send cake! 🍿✨ #TrueBond #WishNest"
                else -> "Sending massive love and good vibes your way for this incredible year! ✨🎁"
            }

            return listOf(greeting, detailsSentence, memorySentence, jokeSentence, signoff)
                .filter { it.isNotBlank() }
                .joinToString(" ")
        }

        return when (type) {
            "Anniversary" -> when (tone) {
                "funny" -> "Happy Anniversary to my favorite partners in crime, $name! Another year of successfully ignoring each other's weird habits! 😂🥂 Here's to 100 more!"
                "professional" -> "Warmest congratulations on your Anniversary, $name. Wishing you continued collaboration, success, and peak performance in life's ultimate partnership. Best regards!"
                "emotional" -> "Dearest $name, happy anniversary. Seeing your love bloom and stand tall through the test of time is incredibly beautiful. May you continue to find solace and endless joy in each other's arms. ❤️🌟"
                "caption" -> "Happy Anniversary to the realest duo dynamic! 🥂✨ Another year of true love, laughter, and high scores. #Anniversary #CoupleGoals #LoveStory"
                else -> "Happy Anniversary, $name! Wishing you both a beautiful day filled with timeless memories, laughter, and a bright future together. Cheers to your love!"
            }
            "Celebrating" -> "Wishing you a phenomenal celebration today, $name! May this special event bring new heights of fulfillment, happy connections, and lasting memories! 🌟🎉"
            else -> when (tone) {
                "funny" -> "Happy Birthday, $name! 🎂 You are not old, you are just... retro! May you receive tons of cake, tons of gifts, and zero spam notifications today! 😂🎉"
                "professional" -> "Dear $name, wishing you a happy and prosperous birthday. May your dedication continue to yield exceptional achievements in your personal and career endeavors. Warm regards."
                "emotional" -> "Happy Birthday, $name. ❤️ On this special day, I just wanted to remind you how exceptionally loved and appreciated you are. Thank you for being a constant light in my life. Wishing you endless peace."
                "caption" -> "Another trip around the sun for the absolute best! 🎂✨ May this year bring pure magic, big wins, and less screen time. Send cake! 🍰 #WishNest #BirthdayVibes #HBD"
                else -> "Happy Birthday, $name! 🎂 Sending you warmest wishes filled with radiant health, prosperity, and continuous happiness today and always! Have an absolute blast!"
            }
        }
    }

    private fun generateMockGiftsFallback(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            "partner" in lower -> "• Custom star map from the night you met 🌌\n• Engraved memory locket or smart watch ⌚\n• Weekend getaway voucher or private dining experience 🥂\n• Handwritten letter collection in a decorated casket 💌"
            "family" in lower -> "• Personalized family tree portrait frame 🖼️\n• Smart digital photo frame pre-loaded with family pictures 📸\n• Luxurious massage chair cushion or heating pad 💆‍♂️\n• Guided journal to write memories of grandma/grandpa 📖"
            "friend" in lower -> "• Wireless Bluetooth speaker with glowing party lights 🔊\n• Premium coffee tasting kit or craft beverage flight ☕\n• Fun multi-player board game (e.g. Catan or Ticket to Ride) 🎲\n• Instant pocket printer for mobile snapshots 📸"
            "colleague" in lower -> "• Dual-mode wireless ergonomic charging desk dock 🔌\n• Premium thermal double-walled smart mug with temperature controls ☕\n• Vegan leather notebook holder with customized initials 🖋️\n• Desktop Zen sand garden or miniature desk waterfall 🪴"
            else -> "• Elegant hand-baked gourmet dessert hamper 🎂\n• Customized wellness aromatherapy scent diffuser 🕯️\n• Smart item tracker (Tile or AirTag) with a leather case 🏷️\n• Scratch-off adventure challenge poster 🗺️"
        }
    }
}
