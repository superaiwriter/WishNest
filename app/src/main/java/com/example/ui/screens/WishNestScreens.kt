package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Event
import com.example.viewmodel.NotificationItem
import com.example.viewmodel.Screen
import com.example.viewmodel.WishNestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WishNestAppContainer(viewModel: WishNestViewModel) {
    val currentScreen = viewModel.currentScreen

    // Handle system back button reactively
    BackHandler(enabled = currentScreen !is Screen.Dashboard && currentScreen !is Screen.Splash && currentScreen !is Screen.Onboarding) {
        viewModel.navigateBack()
    }

    Scaffold(
        bottomBar = {
            if (currentScreen !is Screen.Splash && currentScreen !is Screen.Onboarding) {
                WishNestBottomNavigation(viewModel = viewModel)
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.Splash -> SplashScreen(viewModel = viewModel)
                    is Screen.Onboarding -> OnboardingScreen(viewModel = viewModel)
                    is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                    is Screen.Timeline -> TimelineScreen(viewModel = viewModel)
                    is Screen.AddEvent -> AddNewEventScreen(viewModel = viewModel)
                    is Screen.EventDetail -> EventDetailScreen(eventId = screen.eventId, viewModel = viewModel)
                    is Screen.WishGenerator -> WishGeneratorScreen(viewModel = viewModel)
                    is Screen.ContactSync -> ContactSyncScreen(viewModel = viewModel)
                    is Screen.Premium -> PremiumSubscriptionScreen(viewModel = viewModel)
                    is Screen.Notifications -> NotificationCenterScreen(viewModel = viewModel)
                    is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN (Pulsating and Auto-Advance)
// ==========================================
@Composable
fun SplashScreen(viewModel: WishNestViewModel) {
    var scale by remember { mutableStateOf(0.7f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )

    LaunchedEffect(Unit) {
        scale = 1.0f
        delay(2200) // Beautiful cinematic load time
        viewModel.showSplash = false
        viewModel.navigateTo(Screen.Onboarding)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6), // Royal Indigo
                        Color(0xFFEC4899), // Neon Pink
                        Color(0xFF0F0B1E)  // Depth dark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(animatedScale)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(2.dp, Color.White, RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "WishNest Mascot",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "WishNest",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI-Powered Birthday & Anniversary Vault",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

// ==========================================
// 2. ONBOARDING SCREEN (Wizard Carousel)
// ==========================================
@Composable
fun OnboardingScreen(viewModel: WishNestViewModel) {
    val step = viewModel.onboardingStep
    val gradientBackground = Brush.verticalGradient(
        colors = if (viewModel.isDarkTheme) {
            listOf(Color(0xFF1B1435), Color(0xFF0F0B1E))
        } else {
            listOf(Color(0xFFFAF5FF), Color(0xFFFAF7FD))
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step indicator ticks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == step) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Center card container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (step) {
                    0 -> {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Emotions",
                            tint = Color(0xFFEC4899),
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Never Forget a Moment",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Track birthdays, anniversaries, and custom moments with a smart auto-repeat reminder engine.",
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    1 -> {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Genie",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "AI-Generated Warmth",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Harness Gemini AI to draft funny, emotional, professional greetings and custom gift recommendations in one tap.",
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    2 -> {
                        Icon(
                            imageVector = Icons.Default.Input,
                            contentDescription = "Personalization",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Let's Get Acquainted",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = viewModel.onboardingName,
                            onValueChange = { viewModel.onboardingName = it },
                            label = { Text("What is your name?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_username_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = viewModel.onboardingBirthdate,
                            onValueChange = { viewModel.onboardingBirthdate = it },
                            label = { Text("Your Birthdate (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Onboarding buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                TextButton(onClick = { viewModel.onboardingStep-- }) {
                    Text("Prev", fontSize = 16.sp)
                }
            } else {
                Spacer(modifier = Modifier.width(60.dp))
            }

            Button(
                onClick = {
                    if (step < 2) {
                        viewModel.onboardingStep++
                    } else {
                        // Complete onboarding
                        if (viewModel.onboardingName.isNotBlank()) {
                            viewModel.addEvent(
                                name = viewModel.onboardingName + " (You)",
                                date = viewModel.onboardingBirthdate,
                                type = "Birthday",
                                relationship = "Self",
                                isFavorite = true,
                                notes = "Your personal birthdate record.",
                                giftBudget = 0.0,
                                phone = ""
                            )
                        }
                        viewModel.resetNavigation(Screen.Dashboard)
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (step == 2) "Get Started" else "Next", fontSize = 16.sp)
            }
        }
    }
}

// ==========================================
// 3. HOME DASHBOARD (Dynamic view feed)
// ==========================================
@Composable
fun DashboardScreen(viewModel: WishNestViewModel) {
    val eventsList by viewModel.events.collectAsState()
    val favoritesList by viewModel.favoriteEvents.collectAsState()
    val context = LocalContext.current

    // Quick calculations
    val totalReminders = eventsList.size
    val favoriteCount = favoritesList.size

    // Search and Filtering implementations
    val filteredList = eventsList.filter { event ->
        val matchesQuery = event.name.contains(viewModel.searchQuery, ignoreCase = true) ||
                event.relationship.contains(viewModel.searchQuery, ignoreCase = true) ||
                event.notes.contains(viewModel.searchQuery, ignoreCase = true)

        val matchesType = viewModel.filterType == "All" || event.type.equals(viewModel.filterType, ignoreCase = true)
        val matchesRelationship = viewModel.filterRelationship == "All" || event.relationship.equals(viewModel.filterRelationship, ignoreCase = true)

        matchesQuery && matchesType && matchesRelationship
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header bar in Bento Style
        item {
            val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
            val greeting = when {
                currentHour in 0..11 -> "Good Morning"
                currentHour in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting.uppercase(Locale.US),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC084FC), // soft purple accent
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "WishNest",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B), // slate-800
                        letterSpacing = (-0.5).sp
                    )
                }

                // Alert notification icon in Bento capsule look
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Notifications) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFFFF1F2), CircleShape)
                        .testTag("notifications_icon")
                ) {
                    val unreads = viewModel.notifications.count { it.isUnread }
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Alert notifications",
                            tint = Color(0xFF475569) // slate-600
                        )
                        if (unreads > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFEC4899), CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                            )
                        }
                    }
                }
            }
        }

        // BENTO GRID AREA
        // 1. Primary Highlight Card: Next Big Event (Full Width)
        item {
            val sortedUpcoming = eventsList.map { event ->
                event to calculateDaysUntil(event.date)
            }.sortedBy { it.second }

            val nextEventPair = sortedUpcoming.firstOrNull()
            if (nextEventPair != null) {
                val event = nextEventPair.first
                val daysUntil = nextEventPair.second
                val formattedDays = when (daysUntil) {
                    0 -> "TODAY 🎉"
                    1 -> "TOMORROW ⚡"
                    else -> "In $daysUntil Days"
                }

                val milestoneNum = try {
                    val parts = event.date.split("-")
                    if (parts.size >= 3) {
                        val birthYear = parts[0].toInt()
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val diff = currentYear - birthYear
                        if (diff > 0) diff else 1
                    } else {
                        30
                    }
                } catch (e: Exception) {
                    30
                }

                val labelText = if (event.type.equals("Birthday", ignoreCase = true)) {
                    "Turning $milestoneNum • ${formatDateDisplay(event.date)}"
                } else {
                    "${milestoneNum}th Anniversary • ${formatDateDisplay(event.date)}"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(50.dp),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = formattedDays,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = event.name,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = labelText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    tonalElevation = 2.dp
                                ) {
                                    Text(
                                        text = "$milestoneNum",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                        color = Color(0xFF8B5CF6),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.navigateTo(Screen.EventDetail(event.id)) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Text("Prepare Gift", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        viewModel.triggerAIGeneration(
                                            name = event.name,
                                            relation = event.relationship,
                                            eventType = event.type,
                                            tone = "Warm",
                                            initialNotes = event.notes
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF8B5CF6)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Draft AI Wish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Welcome to WishNest! ✨",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add family and friends to populate your celebrated grid.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.navigateTo(Screen.AddEvent) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF8B5CF6)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Add First Event", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // 2. Secondary Bento Rows
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary Card: AI Wish Generator (1/2 Width)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(136.dp)
                        .clickable { viewModel.navigateTo(Screen.WishGenerator) }
                        .testTag("bento_card_wish_generator"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFFCE7F3)), // border-pink-100
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 18.sp)
                        }

                        Column {
                            Text(
                                text = "AI Wish\nGenerator",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                lineHeight = 18.sp
                            )
                            Text(
                                text = "Create perfect messages instantly",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(top = 2.dp),
                                lineHeight = 12.sp
                            )
                        }
                    }
                }

                // Secondary Card: Bond Streak (1/2 Width)
                val connectionCount = eventsList.count { it.streak > 1 || it.relationshipScore > 80 }
                val streakText = if (connectionCount > 0) "$connectionCount people kept in touch" else "12 people kept in touch"
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(136.dp)
                        .clickable { viewModel.navigateTo(Screen.ContactSync) }
                        .testTag("bento_card_bond_streak"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF3E8FF)), // border-purple-100
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF3E8FF), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥", fontSize = 18.sp)
                        }

                        Column {
                            Text(
                                text = "Bond\nStreak",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                lineHeight = 18.sp
                            )
                            Text(
                                text = streakText,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(top = 2.dp),
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. List Card: Coming Up Timeline (Full Width Bento Row Card)
        item {
            val sortedUpcoming = eventsList.map { event ->
                event to calculateDaysUntil(event.date)
            }.sortedBy { it.second }
            val remainingEvents = sortedUpcoming.drop(1).take(2)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMING UP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(Screen.Timeline) }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (remainingEvents.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            remainingEvents.forEach { (event, daysLeft) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.navigateTo(Screen.EventDetail(event.id)) }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Initials Circle
                                    val initials = event.name.split(" ")
                                        .filter { it.isNotEmpty() }
                                        .take(2)
                                        .joinToString("") { it.take(1).uppercase(Locale.US) }
                                        .ifEmpty { "W" }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                when (event.relationship) {
                                                    "Family" -> Color(0xFFDBEAFE) // soft blue
                                                    "Friend" -> Color(0xFFFEF3C7) // soft orange/yellow
                                                    "Partner" -> Color(0xFFFCE7F3) // soft pink
                                                    else -> Color(0xFFE2E8F0) // slate
                                                },
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = when (event.relationship) {
                                                "Family" -> Color(0xFF1E40AF)
                                                "Friend" -> Color(0xFF92400E)
                                                "Partner" -> Color(0xFF9D174D)
                                                else -> Color(0xFF475569)
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1.0f)) {
                                        Text(
                                            text = event.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "${formatDateDisplay(event.date)} • ${event.relationship}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Text(
                                        text = when (daysLeft) {
                                            0 -> "Today"
                                            1 -> "Tomorrow"
                                            else -> "$daysLeft Days"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B5CF6)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No other upcoming events stored yet.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Search Bar Row
        item {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                placeholder = { Text("Search friends, relationships, notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                )
            )
        }

        // Horizontal filter chips
        item {
            Column {
                Text(
                    text = "Event Categories",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val types = listOf("All", "Birthday", "Anniversary", "Custom Event")
                    items(types) { t ->
                        FilterChip(
                            selected = viewModel.filterType == t,
                            onClick = { viewModel.filterType = t },
                            label = { Text(t) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "Relationships Selector",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val relations = listOf("All", "Family", "Friend", "Partner", "Colleague")
                    items(relations) { r ->
                        FilterChip(
                            selected = viewModel.filterRelationship == r,
                            onClick = { viewModel.filterRelationship = r },
                            label = { Text(r) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Horizontal Favorite ribbon
        if (favoritesList.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Favorite Circle ❤️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(favoritesList) { fav ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { viewModel.navigateTo(Screen.EventDetail(fav.id)) }
                                    .testTag("fav_person_${fav.name}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF8B5CF6),
                                                    Color(0xFFEC4899)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = fav.name.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fav.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(60.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Header for Main List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reminders Feed (${filteredList.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.navigateTo(Screen.Timeline) }) {
                    Text("Flow Mode View →")
                }
            }
        }

        // Empty State Handler
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your WishNest is Empty!",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add custom dates using the '+' button or tap contact import synchronization above.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Reminders Feed List
        items(filteredList) { event ->
            EventCard(
                event = event,
                onCardClick = { viewModel.navigateTo(Screen.EventDetail(event.id)) },
                onFavoriteToggle = { viewModel.toggleFavorite(event) },
                onAITrigger = {
                    viewModel.triggerAIGeneration(
                        name = event.name,
                        relation = event.relationship,
                        eventType = event.type,
                        tone = "Warm",
                        initialNotes = event.notes
                    )
                }
            )
        }

        // Admob simulated block banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable { viewModel.navigateTo(Screen.Premium) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color.Yellow, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Ad", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WishNest Pro: Unlimited AI wishes and ad-free experience.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(220.dp)
                        )
                    }
                    Text("Go Pro", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ==========================================
// EVENT LIST CARD COMPONENT
// ==========================================
@Composable
fun EventCard(
    event: Event,
    onCardClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAITrigger: () -> Unit
) {
    val countdown = calculateDaysUntil(event.date)
    val cardColor = if (countdown <= 3) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val typeIcon = when (event.type) {
        "Birthday" -> Icons.Default.Cake
        "Anniversary" -> Icons.Default.Favorite
        else -> Icons.Default.CalendarMonth
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("event_item_${event.name}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = event.type,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (event.isFavorite) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite icon",
                                tint = Color.Red,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Relationship tag badge
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = event.relationship,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Text(
                            text = formatDateDisplay(event.date),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Countdown banner
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (countdown) {
                        0 -> "TODAY 🎉"
                        1 -> "Tomorrow"
                        else -> "$countdown Days"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (countdown <= 3) Color.Red else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                IconButton(
                    onClick = { onAITrigger() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Wish Generator Spark icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. TIMELINE SCREEN
// ==========================================
@Composable
fun TimelineScreen(viewModel: WishNestViewModel) {
    val eventsList by viewModel.events.collectAsState()

    // Sort events by immediate next event proximity
    val sortedTimeline = eventsList.sortedBy { calculateDaysUntil(it.date) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upcoming Timeline", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sortedTimeline.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No upcoming events found. Add your first Nest record!",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sortedTimeline) { event ->
                    val daysLeft = calculateDaysUntil(event.date)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timeline Bullet Node
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(60.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(
                                        if (daysLeft <= 3) Color.Red else MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(60.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            )
                        }

                        // Event card itself
                        Box(modifier = Modifier.weight(1f)) {
                            EventCard(
                                event = event,
                                onCardClick = { viewModel.navigateTo(Screen.EventDetail(event.id)) },
                                onFavoriteToggle = { viewModel.toggleFavorite(event) },
                                onAITrigger = {
                                    viewModel.triggerAIGeneration(
                                        name = event.name,
                                        relation = event.relationship,
                                        eventType = event.type,
                                        tone = "Warm",
                                        initialNotes = event.notes
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. ADD EVENT SCREEN (Complete form)
// ==========================================
@Composable
fun AddNewEventScreen(viewModel: WishNestViewModel) {
    var name by remember { mutableStateOf("") }
    var rawDate by remember { mutableStateOf("1995-05-24") }
    var selectedType by remember { mutableStateOf("Birthday") }
    var selectedRelation by remember { mutableStateOf("Friend") }
    var isFavorite by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var budgetStr by remember { mutableStateOf("50") }
    var phone by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Celebrant", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large aesthetic input card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Celebrant's Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_event_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Date
                OutlinedTextField(
                    value = rawDate,
                    onValueChange = { rawDate = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("e.g. 1995-12-25") }
                )

                // Event type selectors
                Column {
                    Text("Select Celebration Type", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val typesList = listOf("Birthday", "Anniversary", "Custom Event")
                        typesList.forEach { type ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedType == type) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    )
                                    .border(
                                        1.dp,
                                        if (selectedType == type) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedType = type }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedType == type) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Relationship tag selectors
                Column {
                    Text("Relationship Connection", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tagsList = listOf("Family", "Friend", "Partner", "Colleague")
                        tagsList.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selectedRelation == tag) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        1.dp,
                                        if (selectedRelation == tag) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedRelation = tag }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedRelation == tag) Color.White else MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // Phone for WhatsApp integration
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("WhatsApp Phone Number (with code)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    placeholder = { Text("e.g. +1234567890") }
                )

                // Target budget planner
                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Gift Budget target ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                // Custom Memo note
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Personal details, hobbies, or ideas") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                // Favorite Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFavorite = !isFavorite }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Pin Favorite",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Favorite Circle?", fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Create Button
        Button(
            onClick = {
                if (name.isBlank() || rawDate.isBlank()) {
                    Toast.makeText(context, "Please fill in Name and Date!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addEvent(
                        name = name,
                        date = rawDate,
                        type = selectedType,
                        relationship = selectedRelation,
                        isFavorite = isFavorite,
                        notes = notes,
                        giftBudget = budgetStr.toDoubleOrNull() ?: 50.0,
                        phone = phone
                    )
                    viewModel.navigateBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("add_event_submit_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Insert into Nest Vault", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 6. AI GREETINGS WISH GENERATOR
// ==========================================
@Composable
fun WishGeneratorScreen(viewModel: WishNestViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI Nest Writer", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Config controller inside card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Configure parameters ⚡",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )

                OutlinedTextField(
                    value = viewModel.generatorName,
                    onValueChange = { viewModel.generatorName = it },
                    label = { Text("Friend's Name") },
                    modifier = Modifier.fillMaxWidth().testTag("generator_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Event Selector row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val evs = listOf("Birthday", "Anniversary", "Custom")
                    evs.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (viewModel.generatorType == item) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                )
                                .clickable { viewModel.generatorType = item }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                item,
                                color = if (viewModel.generatorType == item) Color.White else MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Tone selector grid
                Text("Select Style / Tone", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                val tones = listOf("Warm", "Funny", "Emotional", "Professional", "Caption")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tones.forEach { tone ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (viewModel.generatorTone == tone) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                )
                                .clickable { viewModel.generatorTone = tone }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                tone,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.generatorTone == tone) Color.White else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Personalization details
                Text(
                    text = "Personalize Wish ✨",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                OutlinedTextField(
                    value = viewModel.generatorRelationshipDetails,
                    onValueChange = { viewModel.generatorRelationshipDetails = it },
                    label = { Text("Relationship context (or how you met)") },
                    placeholder = { Text("e.g. My childhood best friend since 3rd grade") },
                    modifier = Modifier.fillMaxWidth().testTag("generator_context_input"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 1,
                    maxLines = 3
                )

                OutlinedTextField(
                    value = viewModel.generatorMemories,
                    onValueChange = { viewModel.generatorMemories = it },
                    label = { Text("Specific shared memory") },
                    placeholder = { Text("e.g. Getting lost on our drive to Colorado") },
                    modifier = Modifier.fillMaxWidth().testTag("generator_memories_input"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 1,
                    maxLines = 3
                )

                OutlinedTextField(
                    value = viewModel.generatorInsideJokes,
                    onValueChange = { viewModel.generatorInsideJokes = it },
                    label = { Text("Inside jokes or slang references") },
                    placeholder = { Text("e.g. 'pineapple pizza debate' or 'the big cheese'") },
                    modifier = Modifier.fillMaxWidth().testTag("generator_jokes_input"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 1,
                    maxLines = 2
                )

                // Run Generation action
                Button(
                    onClick = { viewModel.generateWish() },
                    modifier = Modifier.fillMaxWidth().testTag("generate_wish_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.isGeneratingWish) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Drafting wish...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Spark", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Customized Message")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI generated Output Container
        Text("AI Generation Result", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (viewModel.generatedWish.isNotBlank()) viewModel.generatedWish
                    else "Ready to draft an emotional and smart message template. Complete configuration above and trigger AI spark!",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (viewModel.generatedWish.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val clip = android.content.ClipData.newPlainText("WishNest", viewModel.generatedWish)
                                (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                                Toast.makeText(context, "Copied wish template to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Content")
                        }

                        Button(
                            onClick = {
                                try {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, viewModel.generatedWish)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Wish via:"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error sharing wish.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Wish")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. CONTACT SYNC VIEW
// ==========================================
@Composable
fun ContactSyncScreen(viewModel: WishNestViewModel) {
    var isSyncing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contact Synchronization", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Syncing",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Google Contacts Integration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Instantly extract friends, family relatives, and anniversaries stored inside your google and local telephone micro-directory accounts.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isSyncing = true
                        scope.launch {
                            delay(1800) // Aesthetic delay simulating network synchronization reads
                            viewModel.syncMockContacts()
                            isSyncing = false
                            Toast.makeText(context, "Contacts Synced successfully!", Toast.LENGTH_SHORT).show()
                            viewModel.navigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("sync_contacts_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Reading Google contacts directory...")
                    } else {
                        Text("Simulate Import Synchronizer")
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. EVENT DETAIL SCREEN (Premium Interactive view)
// ==========================================
@Composable
fun EventDetailScreen(eventId: Int, viewModel: WishNestViewModel) {
    val eventsList by viewModel.events.collectAsState()
    val event = eventsList.find { it.id == eventId }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Celebration reference target not found.")
        }
        return
    }

    // Days countdown
    val daysUntil = calculateDaysUntil(event.date)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App header Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Celebration File", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(
                onClick = { viewModel.toggleFavorite(event) },
                modifier = Modifier.testTag("detail_favorite_button")
            ) {
                Icon(
                    imageVector = if (event.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite Toggle",
                    tint = if (event.isFavorite) Color.Red else MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large aesthetic gradient banner detailing age/anniversary counter
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        event.name.take(2).uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = event.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${event.relationship} Connection • ${event.type}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful Countdown Ring Graphic
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (daysUntil) {
                                0 -> "TODAY! 🎉"
                                else -> "$daysUntil Days"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (daysUntil <= 3) Color.Red else MaterialTheme.colorScheme.primary
                        )
                        Text("Countdown Left", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatDateDisplay(event.date),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Anniversary Day", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interaction Actions grid: AI Generation trigger, WhatsApp send
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.triggerAIGeneration(
                        name = event.name,
                        relation = event.relationship,
                        eventType = event.type,
                        tone = "Warm",
                        initialNotes = event.notes
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Spark")
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Wish Generator", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    // One-tap WhatsApp sending integration
                    val text = "Thinking of you today on your anniversary! Wishing you endless light and cheer! From WishNest."
                    val phoneClean = event.phone.replace("+", "").replace("-", "").replace(" ", "")
                    val url = "https://api.whatsapp.com/send?phone=$phoneClean&text=${Uri.encode(text)}"
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(browserIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Launch Browser fallback: $url", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "WhatsApp")
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp Wish", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gamification / badges score cards
        Text("Social Bond Gamification 🏅", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Friendship Streak 🔥", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${event.streak} Daily Beats",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEC4899)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { viewModel.incrementStreak(event) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFEC4899).copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add beat streak", tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bond Level %", fontWeight = FontWeight.SemiBold)
                    Text("${event.relationshipScore}/100", fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                }
                LinearProgressIndicator(
                    progress = { event.relationshipScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(CircleShape),
                    color = Color(0xFF38BDF8),
                    trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Acquired Badges:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val badges = event.celebrationBadgeJson.split(",").filter { it.isNotEmpty() }
                    badges.forEach { badge ->
                        Box(
                            modifier = Modifier
                                .background(Color.Yellow.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("⭐ $badge", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB5A642))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Smart checklist events planning (Cake pre-order, call scheduling)
        Text("Party & Event Checklist 🎂", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Cake Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleOrderCake(event) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = "Cake reminder",
                            tint = if (event.hasOrderedCake) Color(0xFFEC4899) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pre-Order Cake & Delivery",
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (event.hasOrderedCake) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    }
                    Checkbox(
                        checked = event.hasOrderedCake,
                        onCheckedChange = { viewModel.toggleOrderCake(event) }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Custom Planning elements list
                val defaultItems = listOf("Send Greeting Card", "Call at Midnight", "Purchase Gift Present")
                val checkedItems = event.checkedPlannerItemsJson.split(",").filter { it.isNotEmpty() }

                defaultItems.forEach { plannerItem ->
                    val isChecked = plannerItem in checkedItems
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.togglePlannerItem(event, plannerItem) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = "Checked planning",
                                tint = if (isChecked) Color.Green else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = plannerItem,
                                fontWeight = FontWeight.Normal,
                                textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                        }
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { viewModel.togglePlannerItem(event, plannerItem) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Gift recommendation & budget planner
        Text("AI Gift Recommendation Engine 🎁", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            "Insert budget & hobbies below to query real Gemini suggestions",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target Budget limit: $${event.giftBudget}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { viewModel.navigateTo(Screen.Settings) }) {
                        Text("Tune Budget")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = viewModel.giftHobbiesInput,
                    onValueChange = { viewModel.giftHobbiesInput = it },
                    label = { Text("Enter interests (e.g. coffee, hiking, books)") },
                    modifier = Modifier.fillMaxWidth().testTag("gift_hobbies_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.generateGiftSuggestions(event) },
                    modifier = Modifier.fillMaxWidth().testTag("generate_gifts_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (viewModel.isGeneratingGifts) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Querying Gemini recommendations...")
                    } else {
                        Text("Generate AI Gift List")
                    }
                }

                if (viewModel.generatedGifts.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Yellow.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Yellow.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = viewModel.generatedGifts,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Emotional memory gallery section
        Text("Memory Polaroid Box 📸", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Snap Journal Gallery", fontWeight = FontWeight.SemiBold)
                    IconButton(
                        onClick = {
                            viewModel.addMemoryPhoto(event, "Photo ${System.currentTimeMillis().toString().takeLast(4)}")
                            Toast.makeText(context, "Added simulated beautiful polaroid snap!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("add_photo_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Add photo snapshot", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Photos list
                val photos = event.memoryPhotosJson.split(",").filter { it.isNotEmpty() }
                if (photos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No Memory Polaroids snapped yet! Add one.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(photos) { photo ->
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, contentDescription = "Photo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(photo, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom countdown audio voice notes greetings segment
        Text("Warm Audio Greetings 🎙️", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Pre-recorded voice note greetings", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        "Stored clips: ${event.voiceNotesCount}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = {
                        viewModel.recordMockVoiceNote(event)
                        Toast.makeText(context, "Voice Note Recorded Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("record_audionote_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Record")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Clip", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Delete celebrant button
        Button(
            onClick = {
                viewModel.deleteEvent(event)
                viewModel.navigateBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("delete_event_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Celebrant from Nest", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 9. PREMIUM SUBSCRIPTION PAGE
// ==========================================
@Composable
fun PremiumSubscriptionScreen(viewModel: WishNestViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("WishNest Premium", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Premium crown illustration background card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "Premium King Crown logo",
                    tint = Color.Yellow,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Upgrade to Pro Unlimited",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Never see ads, gain unlimited smart Gemini API inputs, unlock premium emotional voice cards and automatic cloud synchronization backups.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Benefits grid
        Text("Exclusive Perks 🔥", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        val perks = listOf(
            Pair("No interruption ads", Icons.Default.Block),
            Pair("Unlimited Gemini generative cycles", Icons.Default.AutoAwesome),
            Pair("Premium customized themes", Icons.Default.ColorLens),
            Pair("Automated Cloud server synchronizer", Icons.Default.CloudQueue)
        )

        perks.forEach { perk ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(perk.second, contentDescription = perk.first, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(perk.first, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle pricing models
        var billingMonthly by remember { mutableStateOf(true) }

        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (billingMonthly) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { billingMonthly = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Monthly $1.99", color = if (billingMonthly) Color.White else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!billingMonthly) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { billingMonthly = false }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Yearly $15.99", color = if (!billingMonthly) Color.White else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.isPremiumUser = true
                viewModel.addNotification("👑 Premium Pro Active", "Thank you for getting WishNest Premium. Advertisements have been disabled and full smart utilities sparked!")
                Toast.makeText(context, "Subscribed Successfully! Welcome to WishNest Pro.", Toast.LENGTH_SHORT).show()
                viewModel.navigateBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("subscribe_premium_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Subscribe and activate Pro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 10. NOTIFICATION CENTER
// ==========================================
@Composable
fun NotificationCenterScreen(viewModel: WishNestViewModel) {
    val items = viewModel.notifications

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notification Center", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = { viewModel.markNotificationsRead() }) {
                Text("Mark Read")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Inbox is clear! No active reminders yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (item.isUnread) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    item.time,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                item.body,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(viewModel: WishNestViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Settings Page", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category parameters Card
        Text("Preferences", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Dark Theme Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.isDarkTheme = !viewModel.isDarkTheme }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Display Theme", fontWeight = FontWeight.SemiBold)
                        Text("Switch presentation color theme style", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = viewModel.isDarkTheme,
                        onCheckedChange = { viewModel.isDarkTheme = it },
                        modifier = Modifier.testTag("dark_theme_switch")
                    )
                }

                Divider()

                // Premium Status Card info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.Premium) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Premium Membership status", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (viewModel.isPremiumUser) "Pro Activated 👑" else "Free Account (Contains Ads)",
                            fontSize = 12.sp,
                            color = if (viewModel.isPremiumUser) Color(0xFFEC4899) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    Icon(Icons.Default.Stars, contentDescription = "Premium info", tint = if (viewModel.isPremiumUser) Color.Yellow else MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Core Developer API Utilities
        Text("Secrets Configuration", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Gemini API Vault", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Status code: Injected from Secrets panel",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.Green.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E5631))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Reset database button
        Button(
            onClick = {
                viewModel.clearAllData()
                Toast.makeText(context, "Cleared database state successfully!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("clear_data_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Purge data", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Purge WishNest Records", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// CENTRAL NAVIGATION BAR
// ==========================================
@Composable
fun WishNestBottomNavigation(viewModel: WishNestViewModel) {
    val currentScreen = viewModel.currentScreen

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("app_bottom_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen is Screen.Dashboard,
            onClick = { viewModel.resetNavigation(Screen.Dashboard) },
            label = { Text("Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Hub") }
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Timeline,
            onClick = { viewModel.resetNavigation(Screen.Timeline) },
            label = { Text("Calendar Flow", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar Timeline") }
        )
        NavigationBarItem(
            selected = currentScreen is Screen.AddEvent,
            onClick = { viewModel.navigateTo(Screen.AddEvent) },
            label = { Text("Add New", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add celebrant record") }
        )
        NavigationBarItem(
            selected = currentScreen is Screen.WishGenerator,
            onClick = { viewModel.navigateTo(Screen.WishGenerator) },
            label = { Text("AI message writer", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Message generator screen") }
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Settings,
            onClick = { viewModel.resetNavigation(Screen.Settings) },
            label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Config page") }
        )
    }
}

// ==========================================
// GLOBAL MOCK UTILS & DATE CALCULATORS
// ==========================================
fun calculateDaysUntil(dateStr: String): Int {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val birthdate = sdf.parse(dateStr) ?: return 10

        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val target = Calendar.getInstance()
        target.time = birthdate
        target.set(Calendar.YEAR, today.get(Calendar.YEAR))

        if (target.before(today)) {
            // Event has passed this year, look at next year's date
            target.add(Calendar.YEAR, 1)
        }

        val difference = target.timeInMillis - today.timeInMillis
        return (difference / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        return (5..15).random()
    }
}

fun formatDateDisplay(dateStr: String): String {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdf.parse(dateStr) ?: return dateStr
        val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return displayFormat.format(date)
    } catch (e: Exception) {
        return dateStr
    }
}
