package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.ProfileEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DailyRunEntity
import com.example.data.HabitEntity
import com.example.data.SufferingEntity
import java.util.Locale

enum class ActiveTab {
    TAVERN, CLOSET, MORGUE
}

@Composable
fun RetroDoubleBorderContainer(
    modifier: Modifier = Modifier,
    borderColor: Color = NES_WHITE,
    backgroundColor: Color = NES_BLACK,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .drawBehind {
                val strokeWidth = 3.dp.toPx()
                val innerGap = 4.dp.toPx()

                // Outer border
                drawRect(
                    color = borderColor,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth)
                )

                // Inner double line
                drawRect(
                    color = borderColor,
                    topLeft = Offset(strokeWidth + innerGap, strokeWidth + innerGap),
                    size = Size(
                        size.width - (strokeWidth + innerGap) * 2,
                        size.height - (strokeWidth + innerGap) * 2
                    ),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun MainLayout(viewModel: MainViewModel) {
    var activeTab by remember { mutableStateOf(ActiveTab.TAVERN) }

    val showCheckIn by viewModel.showCheckIn.collectAsState()
    val checkInDate by viewModel.checkInDate.collectAsState()

    val profileState by viewModel.profile.collectAsState()
    val activeSufferingState by viewModel.activeSuffering.collectAsState()
    val habitsState by viewModel.allHabits.collectAsState()
    val runsState by viewModel.allRuns.collectAsState()
    val todayRunState by viewModel.todayRun.collectAsState()

    // Lifted states for custom additions from the gamepad
    var showAddHabitDialogFromMain by remember { mutableStateOf(false) }
    var showAddWorryDialogFromMain by remember { mutableStateOf(false) }
    var showStartMenuDialog by remember { mutableStateOf(false) }
    var showCharacterCreatorDialog by remember { mutableStateOf(false) }
    var itemToConquerFromController by remember { mutableStateOf<SufferingEntity?>(null) }

    val heroSprite = remember(profileState) {
        ZeldaSprites.getCustomHeroSprite(
            spriteType = profileState.spriteType,
            primaryColorName = profileState.primaryColorName,
            secondaryColorName = profileState.secondaryColorName,
            accessoryColorName = profileState.accessoryColorName
        )
    }

    // Calculated Weighted habits
    val weightedHabits = remember(habitsState, runsState) {
        viewModel.getWeightedHabits(habitsState, runsState)
    }

    val completedIds = remember(todayRunState) {
        todayRunState?.completedHabitIdsCsv?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("app_scaffold"),
        containerColor = NES_BLACK,
        bottomBar = {
            Column(modifier = Modifier.background(NES_BLACK)) {
                // Tactical custom navigation bar
                RetroNavigationBar(
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it },
                    heroSprite = heroSprite
                )

                // Immersive Retro Gamepad tactile interface
                ImmersiveGamepad(
                    onSelectClick = {
                        activeTab = when (activeTab) {
                            ActiveTab.TAVERN -> ActiveTab.CLOSET
                            ActiveTab.CLOSET -> ActiveTab.MORGUE
                            ActiveTab.MORGUE -> ActiveTab.TAVERN
                        }
                    },
                    onStartClick = {
                        showStartMenuDialog = true
                    },
                    onBClick = {
                        when (activeTab) {
                            ActiveTab.TAVERN -> showAddHabitDialogFromMain = true
                            ActiveTab.CLOSET -> showAddWorryDialogFromMain = true
                            ActiveTab.MORGUE -> showStartMenuDialog = true
                        }
                    },
                    onAClick = {
                        when (activeTab) {
                            ActiveTab.TAVERN -> {
                                val firstUncompleted = weightedHabits.firstOrNull { (habit, _) -> !completedIds.contains(habit.id) }
                                if (firstUncompleted != null) {
                                    viewModel.toggleHabit(firstUncompleted.first.id)
                                }
                            }
                            ActiveTab.CLOSET -> {
                                val firstWorry = activeSufferingState.firstOrNull()
                                if (firstWorry != null) {
                                    itemToConquerFromController = firstWorry
                                }
                            }
                            ActiveTab.MORGUE -> {
                                showStartMenuDialog = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NES_BLACK)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // HUD / Header Stats bar with full Zelda characteristics
                HeaderStatsBar(
                    xp = profileState.xp,
                    streak = profileState.streak,
                    currentDate = viewModel.todayDateStr,
                    habitsCount = habitsState.size,
                    activeSufferingCount = activeSufferingState.size
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MAP POS: ${when(activeTab) {
                            ActiveTab.TAVERN -> "7-G (TAVERN)"
                            ActiveTab.CLOSET -> "3-C (CLOSET)"
                            ActiveTab.MORGUE -> "9-M (MORGUE)"
                        }}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "WORLD STATE: EAGLE",
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Render Active Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (showCheckIn) {
                        MorningCheckInPanel(
                            viewModel = viewModel,
                            checkInDate = checkInDate,
                            heroSprite = heroSprite
                        )
                    } else {
                        when (activeTab) {
                            ActiveTab.TAVERN -> TavernPanel(
                                viewModel = viewModel,
                                weightedHabits = weightedHabits,
                                todayRun = todayRunState,
                                heroSprite = heroSprite,
                                profileState = profileState,
                                onOpenCharacterCreator = { showCharacterCreatorDialog = true }
                            )
                            ActiveTab.CLOSET -> ClosetPanel(
                                viewModel = viewModel,
                                activeSuffering = activeSufferingState,
                                heroSprite = heroSprite
                            )
                            ActiveTab.MORGUE -> MorguePanel(
                                viewModel = viewModel,
                                runs = runsState,
                                habits = habitsState,
                                weightedHabits = weightedHabits
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog trigger states from controller buttons
    if (showAddHabitDialogFromMain) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialogFromMain = false },
            onConfirm = { name, desc ->
                viewModel.addHabit(name, desc)
                showAddHabitDialogFromMain = false
            }
        )
    }

    if (showAddWorryDialogFromMain) {
        AddWorryDialogFromController(
            onDismiss = { showAddWorryDialogFromMain = false },
            onConfirm = { name, size ->
                viewModel.addWorry(name, size)
                showAddWorryDialogFromMain = false
            }
        )
    }

    if (showStartMenuDialog) {
        StartMenuDialog(
            xp = profileState.xp,
            streak = profileState.streak,
            habitsCompleted = completedIds.size,
            totalHabits = habitsState.size,
            heroSprite = heroSprite,
            onDismiss = { showStartMenuDialog = false },
            onManualCheckIn = {
                viewModel.forceMorningCheckIn()
            }
        )
    }

    if (itemToConquerFromController != null) {
        val selectedItem = itemToConquerFromController!!
        val xpToEarn = viewModel.calculateSufferingValue(selectedItem)
        ConquerConfirmDialog(
            item = selectedItem,
            xp = xpToEarn,
            heroSprite = heroSprite,
            onDismiss = { itemToConquerFromController = null },
            onConfirm = {
                viewModel.conquerWorry(selectedItem, xpToEarn)
                itemToConquerFromController = null
            }
        )
    }

    if (showCharacterCreatorDialog) {
        CharacterCreatorDialog(
            profile = profileState,
            onDismiss = { showCharacterCreatorDialog = false },
            onSave = { type, pCol, sCol, aCol ->
                viewModel.updateCharacter(type, pCol, sCol, aCol)
                showCharacterCreatorDialog = false
            }
        )
    }
}

@Composable
fun HeaderStatsBar(
    xp: Int,
    streak: Int,
    currentDate: String,
    habitsCount: Int,
    activeSufferingCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NES_BLACK)
            .border(width = 4.dp, color = NES_WHITE)
            .padding(12.dp)
            .testTag("header_stats_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // LIFE ROW
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "- LIFE -  ",
                        color = NES_GOLD,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        val activeHearts = Math.min(Math.max(streak, 1), 4) // Show at least 1 heart to represent alive
                        val emptyHearts = 4 - activeHearts
                        repeat(activeHearts) {
                            Text(text = "❤️", fontSize = 12.sp)
                        }
                        repeat(emptyHearts) {
                            Text(text = "🖤", fontSize = 12.sp, color = NES_GREY)
                        }
                    }
                }

                // Inventory HUD / Stats Icons in a row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔷", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "X %03d".format(xp),
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🗝️", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "X %02d".format(activeSufferingCount),
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💣", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "X %02d".format(habitsCount),
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val level = (xp / 1000) + 1
                Text(
                    text = "LEVEL-$level",
                    color = NES_GOLD,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    text = "EAGLE",
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = currentDate,
                    color = NES_GREY,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
        }
    }
}


@Composable
fun RetroNavigationBar(
    activeTab: ActiveTab,
    onTabSelected: (ActiveTab) -> Unit,
    heroSprite: List<List<Color>>
) {
    // Bottom bar using NES start menu aesthetics
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NES_BLACK)
            .border(width = 2.dp, color = NES_GREY)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabMenuItem(
                label = "TAVERN",
                sprite = heroSprite,
                isActive = activeTab == ActiveTab.TAVERN,
                onClick = { onTabSelected(ActiveTab.TAVERN) }
            )

            TabMenuItem(
                label = "CLOSET",
                sprite = ZeldaSprites.CHEST,
                isActive = activeTab == ActiveTab.CLOSET,
                onClick = { onTabSelected(ActiveTab.CLOSET) }
            )

            TabMenuItem(
                label = "MORGUE",
                sprite = ZeldaSprites.SCROLL_JOURNAL,
                isActive = activeTab == ActiveTab.MORGUE,
                onClick = { onTabSelected(ActiveTab.MORGUE) }
            )
        }
    }
}

@Composable
fun RowScope.TabMenuItem(
    label: String,
    sprite: List<List<Color>>,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .testTag("tab_button_${label.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            PixelSprite(
                spriteColors = sprite,
                pixelSize = if (isActive) 1.8.dp else 1.2.dp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActive) {
                // Selector chevron
                Text(
                    text = "> ",
                    color = NES_GOLD,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = label,
                color = if (isActive) NES_GOLD else NES_GREY,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun MorningCheckInPanel(
    viewModel: MainViewModel,
    checkInDate: String,
    heroSprite: List<List<Color>>
) {
    var currentStep by remember { mutableStateOf("MOOD") } // "MOOD" or "SUMMARY"
    val runsState by viewModel.allRuns.collectAsState()
    val habitsState by viewModel.allHabits.collectAsState()

    if (currentStep == "MOOD") {
        RetroDoubleBorderContainer(
            borderColor = NES_RED,
            modifier = Modifier.fillMaxSize().testTag("morning_check_in")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PixelSprite(spriteColors = heroSprite, pixelSize = 4.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A NEW SUN RISES!",
                    color = NES_GOLD,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "How did your previous run of $checkInDate feel, Hero?",
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Three options
                MoodSelectionBlock(onSelectMood = { moodSelected ->
                    viewModel.registerCheckInMood(moodSelected)
                    currentStep = "SUMMARY"
                })
            }
        }
    } else {
        val pastRun = runsState.find { it.date == checkInDate } ?: DailyRunEntity(date = checkInDate)

        RunSummaryEditorPanel(
            viewModel = viewModel,
            run = pastRun,
            allHabits = habitsState,
            runsList = runsState,
            title = "YESTERDAY'S ADVENTURE LOG",
            onActionLabel = "BEGIN TODAY'S ADVENTURE",
            onAction = {
                viewModel.finishCheckIn()
            }
        )
    }
}

@Composable
fun RunSummaryEditorPanel(
    viewModel: MainViewModel,
    run: DailyRunEntity,
    allHabits: List<HabitEntity>,
    runsList: List<DailyRunEntity>,
    title: String,
    onActionLabel: String,
    onAction: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var showAddHabitMenu by remember { mutableStateOf(false) }
    var selectedHabitToInsert by remember { mutableStateOf<HabitEntity?>(null) }
    var insertionIndex by remember { mutableStateOf(-1) } // -1 means end
    var notableEventsText by remember(run.notableEvents) { mutableStateOf(run.notableEvents) }

    val completedIds = remember(run.completedHabitIdsCsv) {
        run.completedHabitIdsCsv.split(",").filter { it.isNotEmpty() }
    }

    RetroDoubleBorderContainer(
        borderColor = NES_GOLD,
        modifier = Modifier
            .fillMaxSize()
            .testTag("run_summary_editor_panel")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = title,
                    color = NES_GOLD,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )

                Text(
                    text = "RUN DATE: ${run.date}",
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Mood display and quick changing
                Text(
                    text = "MOOD STATE:",
                    color = NES_GREY,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val moodOptions = listOf(
                        Triple(1, "(: GREAT", NES_GREEN_LIGHT),
                        Triple(0, "(| MEH", NES_GREY),
                        Triple(-1, "): HARD", NES_RED)
                    )

                    moodOptions.forEach { (mVal, mText, mColor) ->
                        val isSelected = run.moodReported == mVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.updateRun(run.copy(moodReported = mVal))
                                }
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) mColor else NES_GREY.copy(alpha = 0.5f)
                                )
                                .background(if (isSelected) mColor.copy(alpha = 0.2f) else NES_BLACK)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isSelected) "> $mText" else mText,
                                color = if (isSelected) mColor else NES_GREY,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Completed List with reordering and removal
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BUFF EXECUTION ORDER:",
                        color = NES_GREEN_LIGHT,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clickable { isEditing = !isEditing }
                            .border(1.dp, NES_WHITE)
                            .background(if (isEditing) NES_GOLD.copy(alpha = 0.2f) else NES_BLACK)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isEditing) "DONE EDIT" else "EDIT RUN",
                            color = if (isEditing) NES_GOLD else NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                        )
                    }
                }
            }

            if (completedIds.isEmpty()) {
                item {
                    Text(
                        text = "No buffs logged yet. Click 'EDIT RUN' to add completed habits.",
                        color = NES_GREY,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                itemsIndexed(completedIds) { index, id ->
                    val habit = allHabits.find { it.id == id }
                    val habitName = habit?.name ?: "Habit ($id)"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isEditing) NES_GOLD else NES_GREY.copy(alpha = 0.3f))
                            .background(Color(0xFF0C0C0C))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "STEP #${index + 1}: $habitName",
                                    color = NES_WHITE,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                if (habit != null && habit.description.isNotEmpty()) {
                                    Text(
                                        text = habit.description,
                                        color = NES_GREY,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            if (isEditing) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Move Up
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable(enabled = index > 0) {
                                                val mutable = completedIds.toMutableList()
                                                val tmp = mutable[index]
                                                mutable[index] = mutable[index - 1]
                                                mutable[index - 1] = tmp
                                                viewModel.updateRun(run.copy(completedHabitIdsCsv = mutable.joinToString(",")))
                                            }
                                            .border(1.dp, if (index > 0) NES_GOLD else NES_GREY)
                                            .background(NES_BLACK),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "▲",
                                            color = if (index > 0) NES_GOLD else NES_GREY,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Move Down
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable(enabled = index < completedIds.size - 1) {
                                                val mutable = completedIds.toMutableList()
                                                val tmp = mutable[index]
                                                mutable[index] = mutable[index + 1]
                                                mutable[index + 1] = tmp
                                                viewModel.updateRun(run.copy(completedHabitIdsCsv = mutable.joinToString(",")))
                                            }
                                            .border(1.dp, if (index < completedIds.size - 1) NES_GOLD else NES_GREY)
                                            .background(NES_BLACK),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "▼",
                                            color = if (index < completedIds.size - 1) NES_GOLD else NES_GREY,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Delete
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                val mutable = completedIds.toMutableList()
                                                mutable.removeAt(index)
                                                viewModel.updateRun(run.copy(completedHabitIdsCsv = mutable.joinToString(",")))
                                            }
                                            .border(1.dp, NES_RED)
                                            .background(NES_BLACK),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "❌",
                                            color = NES_RED,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "✔",
                                    color = NES_GREEN_LIGHT,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }

            // Insertion menu for adding a habit anywhere between/after logged ones
            if (isEditing) {
                item {
                    if (!showAddHabitMenu) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAddHabitMenu = true
                                    selectedHabitToInsert = null
                                    insertionIndex = -1
                                }
                                .border(1.dp, NES_GOLD)
                                .background(Color(0xFF1E1500))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ INSERT COMPLETED BUFF",
                                color = NES_GOLD,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NES_GOLD)
                                .background(Color(0xFF1A1A1A))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "SELECT BUFF:",
                                    color = NES_GOLD,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 120.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    allHabits.forEach { h ->
                                        val isSelected = selectedHabitToInsert?.id == h.id
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedHabitToInsert = h }
                                                .background(if (isSelected) NES_GOLD.copy(alpha = 0.2f) else NES_BLACK)
                                                .border(1.dp, if (isSelected) NES_GOLD else Color.DarkGray)
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = h.name,
                                                color = if (isSelected) NES_GOLD else NES_WHITE,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                if (selectedHabitToInsert != null) {
                                    Text(
                                        text = "PLACEMENT POSITION:",
                                        color = NES_GOLD,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 120.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        // Option 1: At Start
                                        val isFirstSelected = insertionIndex == 0
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { insertionIndex = 0 }
                                                .background(if (isFirstSelected) NES_GOLD.copy(alpha = 0.2f) else NES_BLACK)
                                                .border(1.dp, if (isFirstSelected) NES_GOLD else Color.DarkGray)
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = "At Star / Step #1",
                                                color = if (isFirstSelected) NES_GOLD else NES_WHITE,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp
                                            )
                                        }

                                        // Option 2: After each existing habit
                                        completedIds.forEachIndexed { idx, existingId ->
                                            val existingHabit = allHabits.find { it.id == existingId }
                                            val existingName = existingHabit?.name ?: "Step #${idx + 1}"
                                            val targetIndex = idx + 1
                                            val isThisSelected = insertionIndex == targetIndex

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { insertionIndex = targetIndex }
                                                    .background(if (isThisSelected) NES_GOLD.copy(alpha = 0.2f) else NES_BLACK)
                                                    .border(1.dp, if (isThisSelected) NES_GOLD else Color.DarkGray)
                                                    .padding(6.dp)
                                            ) {
                                                Text(
                                                    text = "After Step #${idx + 1} ($existingName)",
                                                    color = if (isThisSelected) NES_GOLD else NES_WHITE,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        // Option 3: At End
                                        val isLastSelected = insertionIndex == -1
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { insertionIndex = -1 }
                                                .background(if (isLastSelected) NES_GOLD.copy(alpha = 0.2f) else NES_BLACK)
                                                .border(1.dp, if (isLastSelected) NES_GOLD else Color.DarkGray)
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = "At the end",
                                                color = if (isLastSelected) NES_GOLD else NES_WHITE,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    val sel = selectedHabitToInsert
                                                    if (sel != null) {
                                                        val mutable = completedIds.toMutableList()
                                                        if (insertionIndex == -1 || insertionIndex > mutable.size) {
                                                            mutable.add(sel.id)
                                                        } else {
                                                            mutable.add(insertionIndex, sel.id)
                                                        }
                                                        viewModel.updateRun(run.copy(completedHabitIdsCsv = mutable.joinToString(",")))
                                                        showAddHabitMenu = false
                                                        selectedHabitToInsert = null
                                                        insertionIndex = -1
                                                    }
                                                }
                                                .border(1.dp, NES_GREEN_LIGHT)
                                                .background(Color(0xFF002200))
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "CONFIRM INSERT",
                                                color = NES_GREEN_LIGHT,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    showAddHabitMenu = false
                                                    selectedHabitToInsert = null
                                                    insertionIndex = -1
                                                }
                                                .border(1.dp, NES_RED)
                                                .background(Color(0xFF220000))
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "CANCEL",
                                                color = NES_RED,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // General Notable Events Input (Auto Saver)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MEMORABLE EVENTS / FACTORS TODAY:",
                    color = NES_GOLD,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = notableEventsText,
                    onValueChange = {
                        notableEventsText = it
                        viewModel.updateRun(run.copy(notableEvents = it))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    textStyle = TextStyle(
                        color = NES_WHITE,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NES_GOLD,
                        unfocusedBorderColor = NES_WHITE,
                        focusedContainerColor = NES_BLACK,
                        unfocusedContainerColor = NES_BLACK
                    ),
                    singleLine = false,
                    placeholder = {
                        Text(
                            text = "Write what contributed to your mood (e.g., drank coffee too late, bad sleep, heavy rain)...",
                            color = NES_GREY,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp
                        )
                    }
                )
            }

            // Trend Graph Display
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "MOOD TREND GRAPH (LAST 7 RUNS):",
                    color = NES_BLUE_LIGHT,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                MoodTrendGraphRow(runs = runsList.take(7))
            }

            // Main Dismiss / Action button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction() }
                        .border(width = 2.dp, color = NES_RED)
                        .background(Color(0xFF320000))
                        .padding(12.dp)
                        .testTag("summary_proceed_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = onActionLabel,
                        color = NES_PEACH,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MoodSelectionBlock(onSelectMood: (Int) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        MoodOptionButton(
            title = "(: GREAT RUN (+1)",
            description = "Triumphant, habits flowed smoothly.",
            color = NES_GREEN_LIGHT,
            onClick = { onSelectMood(1) },
            tag = "mood_great"
        )

        MoodOptionButton(
            title = "(| MEH RUN (0)",
            description = "Survival. Just a basic, standard day.",
            color = NES_GREY,
            onClick = { onSelectMood(0) },
            tag = "mood_neutral"
        )

        MoodOptionButton(
            title = "): HARD RUN (-1)",
            description = "Struggled, heavily avoided tasks.",
            color = NES_RED,
            onClick = { onSelectMood(-1) },
            tag = "mood_hard"
        )
    }
}

@Composable
fun MoodOptionButton(
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(width = 2.dp, color = color)
            .background(NES_BLACK)
            .padding(12.dp)
            .testTag(tag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ">",
                color = color,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                Text(
                    text = title,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = description,
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun TavernPanel(
    viewModel: MainViewModel,
    weightedHabits: List<Pair<HabitEntity, Double>>,
    todayRun: DailyRunEntity?,
    heroSprite: List<List<Color>>,
    profileState: ProfileEntity,
    onOpenCharacterCreator: () -> Unit
) {
    var showAddHabitDialog by remember { mutableStateOf(false) }

    val completedIds = remember(todayRun) {
        todayRun?.completedHabitIdsCsv?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }

    RetroDoubleBorderContainer(
        borderColor = NES_GREEN_LIGHT,
        modifier = Modifier.fillMaxSize().testTag("tavern_panel")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TAVERN BOARD",
                color = NES_GREEN_LIGHT,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Character Creator Button
                Box(
                    modifier = Modifier
                        .clickable { onOpenCharacterCreator() }
                        .border(width = 1.dp, color = NES_GOLD)
                        .background(Color(0xFF332200))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("open_hero_creator_btn")
                ) {
                    Text(
                        text = "⚔️ HERO CREATOR",
                        color = NES_GOLD,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Add Custom Habit Button styled retro-style
                Box(
                    modifier = Modifier
                        .clickable { showAddHabitDialog = true }
                        .border(width = 1.dp, color = NES_GREEN_LIGHT)
                        .background(Color(0xFF003800))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("add_habit_button")
                ) {
                    Text(
                        text = "+ FORGE BUFF",
                        color = NES_GREEN_LIGHT,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dialogue Box / Flavor Check
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = NES_GREY)
                .background(Color(0xFF111111))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelSprite(spriteColors = heroSprite, pixelSize = 1.2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                val heroName = profileState.spriteType.uppercase()
                Text(
                    text = "\"A pristine day greets you, $heroName adventurer! Activate your daily buffs to bolster your spirit.\"",
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TODAY'S ACTIVE BUFFS (DAILY RUN)",
            color = NES_GREY,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (weightedHabits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO ACTIVE BUFFS LOADED",
                    color = NES_GREY,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weightedHabits) { (habit, weight) ->
                    val isChecked = completedIds.contains(habit.id)
                    HabitRowItem(
                        habit = habit,
                        weight = weight,
                        isChecked = isChecked,
                        onCheckedChange = { viewModel.toggleHabit(habit.id) },
                        onDelete = { viewModel.deleteHabit(habit) }
                    )
                }
            }
        }
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onConfirm = { name, desc ->
                viewModel.addHabit(name, desc)
                showAddHabitDialog = false
            }
        )
    }
}

@Composable
fun HabitRowItem(
    habit: HabitEntity,
    weight: Double,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    onDelete: () -> Unit
) {
    // Select potion sprite based on default status
    val spriteColors = if (habit.name.lowercase().contains("water")) {
        ZeldaSprites.WATER_POTION
    } else if (habit.name.lowercase().contains("stretch") || habit.name.lowercase().contains("run")) {
        ZeldaSprites.STRETCH_BOOTS
    } else {
        ZeldaSprites.SCROLL_JOURNAL
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (isChecked) NES_GREY else if (weight >= 1.0) NES_GOLD else NES_WHITE
            )
            .background(if (isChecked) Color(0xFF1C1C1C) else Color(0xFF000000))
            .clickable { onCheckedChange() }
            .padding(10.dp)
            .testTag("habit_row_${habit.name.lowercase().replace(" ", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Selector chevron
                Text(
                    text = if (isChecked) "[x] " else "[ ] ",
                    color = if (isChecked) NES_GREY else NES_GREEN_LIGHT,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                PixelSprite(spriteColors = spriteColors, pixelSize = 1.3.dp)
                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = habit.name,
                            color = if (isChecked) NES_GREY else NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        if (weight >= 1.0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            PixelSprite(spriteColors = ZeldaSprites.STAR_GOLD, pixelSize = 1.dp)
                        }
                    }
                    Text(
                        text = habit.description,
                        color = NES_GREY,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Show Dynamic correlation Strength if calculated
                if (!isChecked) {
                    if (weight >= 1.0) {
                        Text(
                            text = "BUFF: +${weight.toInt()}",
                            color = NES_GOLD,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    } else if (weight < 0.0) {
                        Text(
                            text = "BUFF: ${weight.toInt()}",
                            color = NES_RED,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                if (!habit.isDefault) {
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.size(24.dp).testTag("delete_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete custom buff",
                            tint = NES_RED
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        RetroDoubleBorderContainer(borderColor = NES_GOLD) {
            Text(
                text = "FORGE CUSTOM BUFF",
                color = NES_GOLD,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Custom Retro styled inputs
            Text(text = "BUFF NAME:", color = NES_WHITE, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                textStyle = TextStyle(color = NES_WHITE, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NES_GOLD,
                    unfocusedBorderColor = NES_WHITE,
                    focusedContainerColor = NES_BLACK,
                    unfocusedContainerColor = NES_BLACK
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("add_habit_name_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "DESCRIPTION:", color = NES_WHITE, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                textStyle = TextStyle(color = NES_WHITE, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NES_GOLD,
                    unfocusedBorderColor = NES_WHITE,
                    focusedContainerColor = NES_BLACK,
                    unfocusedContainerColor = NES_BLACK
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("add_habit_desc_field")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("ABORT", color = NES_GREY, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { if (name.isNotBlank()) onConfirm(name, desc) },
                    colors = ButtonDefaults.buttonColors(containerColor = NES_GOLD)
                ) {
                    Text("FORGE", color = NES_BLACK, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ClosetPanel(
    viewModel: MainViewModel,
    activeSuffering: List<SufferingEntity>,
    heroSprite: List<List<Color>>
) {
    var name by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("small") }
    var itemToConquer by remember { mutableStateOf<SufferingEntity?>(null) }

    RetroDoubleBorderContainer(
        borderColor = NES_GOLD,
        modifier = Modifier.fillMaxSize().testTag("closet_panel")
    ) {
        Text(
            text = "SUFFERING CLOSET",
            color = NES_GOLD,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Worry Input form
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = NES_GOLD)
                .background(Color(0xFF111111))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "EXTERNALISE ANXIETY / WORRY PORTAL:",
                    color = NES_GOLD,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("What worries you?", color = NES_GREY, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    textStyle = TextStyle(color = NES_WHITE, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NES_GOLD,
                        unfocusedBorderColor = NES_WHITE,
                        focusedContainerColor = NES_BLACK,
                        unfocusedContainerColor = NES_BLACK
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("worry_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Size options
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        WorrySizeOption(label = "S", isSelected = selectedSize == "small", onClick = { selectedSize = "small" })
                        WorrySizeOption(label = "M", isSelected = selectedSize == "medium", onClick = { selectedSize = "medium" })
                        WorrySizeOption(label = "L", isSelected = selectedSize == "large", onClick = { selectedSize = "large" })
                    }

                    Box(
                        modifier = Modifier
                            .clickable {
                                if (name.isNotBlank()) {
                                    viewModel.addWorry(name, selectedSize)
                                    name = ""
                                }
                            }
                            .border(width = 1.dp, color = NES_GOLD)
                            .background(Color(0xFF332200))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("submit_worry_button")
                    ) {
                        Text(
                            text = "DUMP IRRITATION",
                            color = NES_GOLD,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "ACTIVE INVENTORY (PASSIVE HARVEST)",
            color = NES_GREY,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (activeSuffering.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "THE CLOSET IS EMPTY OF IRRITATIONS.\nYOU ARE TRULY AT PEACE HERO!",
                    color = NES_GREY,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeSuffering) { item ->
                    val finalXpVal = viewModel.calculateSufferingValue(item)
                    WorryRowItem(
                        item = item,
                        currentXp = finalXpVal,
                        days = viewModel.getDaysBetween(item.createdDate, viewModel.todayDateStr),
                        onConquer = { itemToConquer = item }
                    )
                }
            }
        }
    }

    if (itemToConquer != null) {
        val selectedItem = itemToConquer!!
        val xpToEarn = viewModel.calculateSufferingValue(selectedItem)
        ConquerConfirmDialog(
            item = selectedItem,
            xp = xpToEarn,
            heroSprite = heroSprite,
            onDismiss = { itemToConquer = null },
            onConfirm = {
                viewModel.conquerWorry(selectedItem, xpToEarn)
                itemToConquer = null
            }
        )
    }
}

@Composable
fun WorrySizeOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .border(width = 1.dp, color = if (isSelected) NES_GOLD else NES_GREY)
            .background(if (isSelected) Color(0xFF553C00) else NES_BLACK)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("worry_size_${label.lowercase()}")
    ) {
        Text(
            text = label,
            color = if (isSelected) NES_GOLD else NES_GREY,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WorryRowItem(
    item: SufferingEntity,
    currentXp: Int,
    days: Int,
    onConquer: () -> Unit
) {
    val spriteColors = when (item.size.lowercase()) {
        "small" -> ZeldaSprites.SWORD
        "medium" -> ZeldaSprites.SHIELD
        else -> ZeldaSprites.CHEST
    }

    val statusText = when {
        item.size.lowercase() == "large" -> "Constant heavy pressure..."
        days == 0 -> "A fresh concern..."
        item.size.lowercase() == "medium" -> "Growing heavier (+3.0 XP/day)..."
        else -> "Growing heavier (+1.5 XP/day)..."
    }

    val borderColor = when (item.size.lowercase()) {
        "large" -> NES_RED
        "medium" -> NES_BLUE_LIGHT
        else -> NES_GREY
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = borderColor)
            .background(NES_BLACK)
            .clickable { onConquer() }
            .padding(10.dp)
            .testTag("worry_item_${item.name.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                PixelSprite(spriteColors = spriteColors, pixelSize = 1.5.dp)
                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.name.uppercase(Locale.US),
                        color = NES_WHITE,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = statusText,
                        color = NES_GREY,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "Avoided for $days ${if (days == 1) "day" else "days"}",
                        color = Color(0xFFCCAA44),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }
            }

            // XP potential harvest bubble
            Box(
                modifier = Modifier
                    .border(width = 1.dp, color = NES_GOLD)
                    .background(Color(0xFF221100))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "VAL: $currentXp XP",
                    color = NES_GOLD,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ConquerConfirmDialog(
    item: SufferingEntity,
    xp: Int,
    heroSprite: List<List<Color>>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        RetroDoubleBorderContainer(borderColor = NES_RED) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PixelSprite(spriteColors = heroSprite, pixelSize = 3.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "READY TO SWAT DOWN?",
                    color = NES_RED,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Conquer \"${item.name}\" to harvest its passive accumulated $xp XP in wisdom?",
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .border(width = 1.dp, color = NES_GREY)
                            .background(NES_BLACK)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "RETREAT",
                            color = NES_GREY,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clickable { onConfirm() }
                            .border(width = 2.dp, color = NES_GREEN_LIGHT)
                            .background(Color(0xFF003800))
                            .padding(8.dp)
                            .testTag("verify_conquer_button")
                    ) {
                        Text(
                            text = "SWAT DOWN",
                            color = NES_GREEN_LIGHT,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MorguePanel(
    viewModel: MainViewModel,
    runs: List<DailyRunEntity>,
    habits: List<HabitEntity>,
    weightedHabits: List<Pair<HabitEntity, Double>>
) {
    // Analytics screen of previous runs
    RetroDoubleBorderContainer(
        borderColor = NES_BLUE_LIGHT,
        modifier = Modifier.fillMaxSize().testTag("morgue_panel")
    ) {
        Text(
            text = "THE SAGES' MORGUE",
            color = NES_BLUE_LIGHT,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // MOOD PROGRESSION TREND GRAPH
        Text(
            text = "MOOD HISTORY (LAST 7 RUNS)",
            color = NES_GREY,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        MoodTrendGraphRow(runs = runs.take(7))

        Spacer(modifier = Modifier.height(12.dp))

        // DYNAMIC INSIGHTS PANEL
        Text(
            text = "THE SAGES OBSERVE:",
            color = NES_GOLD,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        InsightsPanel(weightedHabits = weightedHabits, runs = runs)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "HISTORIC RUN LOGS",
            color = NES_GREY,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (runs.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO COMPLETED QUEST RUNS YET",
                    color = NES_GREY,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(runs) { run ->
                    HistoricRunRowItem(viewModel = viewModel, run = run, habits = habits, runsList = runs)
                }
            }
        }
    }
}

@Composable
fun MoodTrendGraphRow(runs: List<DailyRunEntity>) {
    // Render simple retro blocks representing mood sorted ASC chronologically for 7 runs
    val sortedRuns = remember(runs) { runs.sortedBy { it.date } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = NES_GREY)
            .background(Color(0xFF111111))
            .padding(8.dp)
    ) {
        if (sortedRuns.isEmpty()) {
            Text(
                text = "Begin checking in daily to chart progress.",
                color = NES_GREY,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                sortedRuns.forEach { run ->
                    val color = when (run.moodReported) {
                        1 -> NES_GREEN_LIGHT  // Great
                        0 -> NES_GREY        // Meh
                        -1 -> NES_RED        // Hard
                        else -> Color.DarkGray
                    }
                    val colHeight = when (run.moodReported) {
                        1 -> 40.dp
                        0 -> 24.dp
                        -1 -> 12.dp
                        else -> 8.dp
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(colHeight)
                                .background(color)
                                .border(width = 1.dp, color = NES_BLACK)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = run.date.takeLast(5), // shows "05-26"
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsPanel(weightedHabits: List<Pair<HabitEntity, Double>>, runs: List<DailyRunEntity>) {
    val bestHabit = weightedHabits
        .filter { it.second > 0.1 }
        .maxByOrNull { it.second }

    val totalConqueredWorries = remember(runs) {
        runs.sumOf { run ->
            run.sufferingConqueredRaw.split(",").filter { it.isNotEmpty() }.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = NES_GOLD)
            .background(Color(0xFF221A00))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (bestHabit != null) {
                Text(
                    text = "* Completing '${bestHabit.first.name}' positively influences your next day (+${bestHabit.second.toInt() * 10}% correlation). Maintain this Power-Up!",
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            } else {
                Text(
                    text = "* Keep completing habits on consecutive days. Sages observe correlation triggers dynamically as data grows.",
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            if (totalConqueredWorries > 0) {
                Text(
                    text = "* You have swatted down $totalConqueredWorries anxieties. Your suffering inventory is converted into wisdom XP.",
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            } else {
                Text(
                    text = "* Defeat nagging worries in the CLOSET to earn passive wisdom XP.",
                    color = NES_WHITE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun HistoricRunRowItem(
    viewModel: MainViewModel,
    run: DailyRunEntity,
    habits: List<HabitEntity>,
    runsList: List<DailyRunEntity>
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomizer by remember { mutableStateOf(false) }

    val completedCount = remember(run) {
        val splitList = run.completedHabitIdsCsv.split(",").filter { it.isNotEmpty() }
        splitList.size
    }

    val conqueredWorriesList = remember(run) {
        run.sufferingConqueredRaw.split(",").filter { it.isNotEmpty() }
    }

    val moodLabel = when (run.moodReported) {
        1 -> "(: GREAT"
        0 -> "([M] MEH)"
        -1 -> "): HARD"
        else -> "NO CHECKIN"
    }

    val moodColor = when (run.moodReported) {
        1 -> NES_GREEN_LIGHT
        0 -> NES_GREY
        -1 -> NES_RED
        else -> Color.DarkGray
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = NES_WHITE)
            .background(if (expanded) Color(0xFF111111) else Color(0xFF000000))
            .clickable { expanded = !expanded }
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expanded) "v " else "> ",
                        color = NES_GOLD,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "RUN: ${run.date}",
                        color = NES_WHITE,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = moodLabel,
                        color = moodColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF003800))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "BUFFS: $completedCount",
                            color = NES_GREEN_LIGHT,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NES_GREY))
                Spacer(modifier = Modifier.height(6.dp))

                // Detail section
                Text(
                    text = "COMPLETED BUFFS (ORDER COMPLETED):",
                    color = NES_GREEN_LIGHT,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                val completedIds = run.completedHabitIdsCsv.split(",").filter { it.isNotEmpty() }

                if (completedIds.isEmpty()) {
                    Text(
                        text = "- None",
                        color = NES_GREY,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                } else {
                    completedIds.forEachIndexed { index, id ->
                        val h = habits.find { it.id == id }
                        val nameStr = h?.name ?: "Habit ($id)"
                        Text(
                            text = "- Step #${index + 1}: $nameStr ✔",
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "CONQUERED WORRIES:",
                    color = NES_RED,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                if (conqueredWorriesList.isEmpty()) {
                    Text(
                        text = "- None",
                        color = NES_GREY,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                } else {
                    conqueredWorriesList.forEach { name ->
                        Text(
                            text = "- Swatted down \"$name\" ⚔",
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }

                if (run.notableEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "MEMORABLE EVENTS / FACTORS:",
                        color = NES_GOLD,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = run.notableEvents,
                        color = NES_PEACH,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                // Edit Button
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showCustomizer = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, NES_GOLD),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "📝 CUSTOMIZE BUFF ORDER & MOOD/EVENTS",
                        color = NES_GOLD,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showCustomizer) {
        Dialog(onDismissRequest = { showCustomizer = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                RunSummaryEditorPanel(
                    viewModel = viewModel,
                    run = run,
                    allHabits = habits,
                    runsList = runsList,
                    title = "EDIT PAST RUN LOG",
                    onActionLabel = "SAVE & CLOSE RECORD",
                    onAction = { showCustomizer = false }
                )
            }
        }
    }
}

@Composable
fun ImmersiveGamepad(
    onSelectClick: () -> Unit,
    onStartClick: () -> Unit,
    onBClick: () -> Unit,
    onAClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NES_BLACK)
            .border(width = 4.dp, color = NES_WHITE)
            .padding(16.dp)
            .testTag("immersive_gamepad")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Select & Start physical style buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // SELECT
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(height = 24.dp, width = 52.dp)
                            .border(width = 3.dp, color = Color(0xFF555555))
                            .background(Color(0xFF333333))
                            .clickable { onSelectClick() }
                            .testTag("gamepad_select"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SEL",
                            color = Color(0xFF888888),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SELECT",
                        color = NES_GREY,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // START
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(height = 24.dp, width = 52.dp)
                            .border(width = 3.dp, color = Color(0xFF555555))
                            .background(Color(0xFF333333))
                            .clickable { onStartClick() }
                            .testTag("gamepad_start"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "STR",
                            color = Color(0xFF888888),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "START",
                        color = NES_GREY,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tactile arcade circular B & A Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // BUTTON B (Add worries / habits)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onBClick() }
                            .testTag("gamepad_btn_b")
                            .drawBehind {
                                // Red 3D physical look
                                drawCircle(
                                    color = Color(0xFF8B0000),
                                    radius = size.minDimension / 2
                                )
                                drawCircle(
                                    color = NES_WHITE,
                                    radius = (size.minDimension / 2) - 2.dp.toPx()
                                )
                                drawCircle(
                                    color = NES_RED,
                                    radius = (size.minDimension / 2) - 5.dp.toPx()
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "B",
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "INVENTORY",
                        color = NES_WHITE,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // BUTTON A (Quick Complete Tasks)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onAClick() }
                            .testTag("gamepad_btn_a")
                            .drawBehind {
                                // Red 3D physical look
                                drawCircle(
                                    color = Color(0xFF8B0000),
                                    radius = size.minDimension / 2
                                )
                                drawCircle(
                                    color = NES_WHITE,
                                    radius = (size.minDimension / 2) - 2.dp.toPx()
                                )
                                drawCircle(
                                    color = NES_RED,
                                    radius = (size.minDimension / 2) - 5.dp.toPx()
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "COMPLETE",
                        color = NES_WHITE,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddWorryDialogFromController(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("small") }

    Dialog(onDismissRequest = onDismiss) {
        RetroDoubleBorderContainer(borderColor = NES_RED) {
            Text(
                text = "DUMP IRRITATION",
                color = NES_RED,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(text = "WORRY / ANXIETY DETECTED:", color = NES_WHITE, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                textStyle = TextStyle(color = NES_WHITE, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NES_RED,
                    unfocusedBorderColor = NES_WHITE,
                    focusedContainerColor = NES_BLACK,
                    unfocusedContainerColor = NES_BLACK
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("worry_dialog_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "SIZE / IMPACT VALUE:", color = NES_WHITE, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WorrySizeOption(label = "S", isSelected = selectedSize == "small", onClick = { selectedSize = "small" })
                WorrySizeOption(label = "M", isSelected = selectedSize == "medium", onClick = { selectedSize = "medium" })
                WorrySizeOption(label = "L", isSelected = selectedSize == "large", onClick = { selectedSize = "large" })
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("ABORT", color = NES_GREY, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { if (name.isNotBlank()) onConfirm(name, selectedSize) },
                    colors = ButtonDefaults.buttonColors(containerColor = NES_RED)
                ) {
                    Text("DUMP", color = NES_WHITE, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StartMenuDialog(
    xp: Int,
    streak: Int,
    habitsCompleted: Int,
    totalHabits: Int,
    heroSprite: List<List<Color>>,
    onDismiss: () -> Unit,
    onManualCheckIn: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        RetroDoubleBorderContainer(borderColor = NES_GOLD) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PixelSprite(spriteColors = heroSprite, pixelSize = 2.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SAGES' SCROLL OF STATS",
                    color = NES_GOLD,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111111))
                        .border(width = 1.dp, color = NES_GREY)
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "STATUS: ACTIVE RUNNER",
                            color = NES_GREEN_LIGHT,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "- QUESTS: $habitsCompleted/$totalHabits COMPLETED",
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "- TOTAL WISDOM XP: $xp",
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "- DAILY RUNS STREAK: $streak",
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                onManualCheckIn()
                                onDismiss()
                            }
                            .border(width = 1.dp, color = NES_GOLD)
                            .background(Color(0xFF332200))
                            .padding(8.dp)
                            .testTag("gamepad_menu_checkin"),
                    ) {
                        Text(
                            text = "REPLAY CHECKIN",
                            color = NES_GOLD,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .border(width = 1.dp, color = NES_WHITE)
                            .background(NES_BLACK)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "RESUME",
                            color = NES_WHITE,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterCreatorDialog(
    profile: ProfileEntity,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var selectedSpriteType by remember { mutableStateOf(profile.spriteType) }
    var selectedPrimaryColor by remember { mutableStateOf(profile.primaryColorName) }
    var selectedSecondaryColor by remember { mutableStateOf(profile.secondaryColorName) }
    var selectedAccessoryColor by remember { mutableStateOf(profile.accessoryColorName) }

    val previewSprite = remember(selectedSpriteType, selectedPrimaryColor, selectedSecondaryColor, selectedAccessoryColor) {
        ZeldaSprites.getCustomHeroSprite(
            spriteType = selectedSpriteType,
            primaryColorName = selectedPrimaryColor,
            secondaryColorName = selectedSecondaryColor,
            accessoryColorName = selectedAccessoryColor
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        RetroDoubleBorderContainer(borderColor = NES_GOLD) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HERO ASSEMBLY CHAMBER",
                    color = NES_GOLD,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Large Avatar Live Preview with double border container
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(width = 3.dp, color = NES_WHITE)
                        .background(NES_BLACK)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PixelSprite(spriteColors = previewSprite, pixelSize = 6.dp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. CLASS OPTIONS
                Text(
                    text = "SELECT HERO CLASS:",
                    color = NES_GREY,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("HERO", "SAGE", "KNIGHT", "FAIRY").forEach { type ->
                        val isSelected = selectedSpriteType.lowercase() == type.lowercase()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSpriteType = type.lowercase() }
                                .border(width = 1.dp, color = if (isSelected) NES_GOLD else NES_GREY)
                                .background(if (isSelected) Color(0xFF332200) else NES_BLACK)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) NES_GOLD else NES_WHITE,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. PRIMARY OUTFIT COLOR
                Text(
                    text = "OUTFIT PRIMARY COLOR:",
                    color = NES_GREY,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val colorsList = listOf("green", "red", "blue", "gold", "white")
                    colorsList.forEach { colName ->
                        val isSelected = selectedPrimaryColor.lowercase() == colName.lowercase()
                        val colHex = when (colName) {
                            "green" -> NES_GREEN_LIGHT
                            "red" -> NES_RED
                            "blue" -> NES_BLUE_LIGHT
                            "gold" -> NES_GOLD
                            else -> NES_WHITE
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPrimaryColor = colName }
                                .border(width = 1.dp, color = if (isSelected) NES_GOLD else NES_GREY)
                                .background(if (isSelected) Color(0xFF331100) else NES_BLACK)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(10.dp).background(colHex).border(1.dp, NES_WHITE))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = colName.uppercase().take(5),
                                    color = if (isSelected) NES_GOLD else NES_WHITE,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. SECONDARY OUTFIT COLOR
                Text(
                    text = "OUTFIT SHADOW/ACCENT:",
                    color = NES_GREY,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val secondaryColors = listOf("dark_green", "dark_red", "dark_blue", "brown", "grey")
                    secondaryColors.forEach { colName ->
                        val isSelected = selectedSecondaryColor.lowercase() == colName.lowercase()
                        val colHex = when (colName) {
                            "dark_green" -> NES_GREEN_DARK
                            "dark_red" -> Color(0xFF801800)
                            "dark_blue" -> NES_BLUE_DARK
                            "brown" -> NES_BROWN
                            else -> NES_GREY
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSecondaryColor = colName }
                                .border(width = 1.dp, color = if (isSelected) NES_GOLD else NES_GREY)
                                .background(if (isSelected) Color(0xFF330011) else NES_BLACK)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(10.dp).background(colHex).border(1.dp, NES_WHITE))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = colName.replace("dark_", "D_").uppercase().take(4),
                                    color = if (isSelected) NES_GOLD else NES_WHITE,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 7.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. ACCESSORY / HAIR COLOR
                Text(
                    text = "ACCESSORY/HAIR DETAIL:",
                    color = NES_GREY,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val accessoryColors = listOf("gold", "white", "peach", "pink")
                    accessoryColors.forEach { colName ->
                        val isSelected = selectedAccessoryColor.lowercase() == colName.lowercase()
                        val colHex = when (colName) {
                            "gold" -> NES_GOLD
                            "white" -> NES_WHITE
                            "peach" -> NES_PEACH
                            else -> Color(0xFFFC80A0)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedAccessoryColor = colName }
                                .border(width = 1.dp, color = if (isSelected) NES_GOLD else NES_GREY)
                                .background(if (isSelected) Color(0xFF112211) else NES_BLACK)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(10.dp).background(colHex).border(1.dp, NES_WHITE))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = colName.uppercase(),
                                    color = if (isSelected) NES_GOLD else NES_WHITE,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .border(width = 1.dp, color = NES_GREY)
                            .background(NES_BLACK)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "ABORT",
                            color = NES_GREY,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clickable {
                                onSave(
                                    selectedSpriteType,
                                    selectedPrimaryColor,
                                    selectedSecondaryColor,
                                    selectedAccessoryColor
                                )
                            }
                            .border(width = 1.dp, color = NES_GOLD)
                            .background(Color(0xFF332200))
                            .padding(8.dp)
                            .testTag("save_custom_hero_btn")
                    ) {
                        Text(
                            text = "EQUIP HERO",
                            color = NES_GOLD,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

