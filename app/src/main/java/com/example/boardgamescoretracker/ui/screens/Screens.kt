package com.example.boardgamescoretracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.boardgamescoretracker.data.db.GameEntity
import com.example.boardgamescoretracker.viewmodel.GameViewModel
import com.example.boardgamescoretracker.viewmodel.GameViewModelFactory
import com.example.boardgamescoretracker.viewmodel.PlayerViewModel
import com.example.boardgamescoretracker.viewmodel.PlayerViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@Composable
fun GameListScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).gameRepository,
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).playerRepository,
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).scoreRepository
        )
    )
) {
    val games by gameViewModel.allGames.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Board Game Score Tracker",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { navController.navigate("new_game") },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New Game")
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Game")
        }

        Text(
            text = "Recent Games",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(games) { game ->
                GameListItem(
                    game = game,
                    onGameClick = { gameId ->
                        navController.navigate("active_game/$gameId")
                    },
                    onDeleteClick = { gameId ->
                        gameViewModel.deleteGame(gameId)
                    },
                    onDuplicateClick = { sourceGame ->
                        gameViewModel.duplicateGame(sourceGame) { newGameId ->
                            navController.navigate("active_game/$newGameId")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GameListItem(
    game: GameEntity,
    onGameClick: (Int) -> Unit,
    onDeleteClick: ((Int) -> Unit)? = null,
    onDuplicateClick: ((GameEntity) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGameClick(game.gameId) }
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = game.gameName, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (game.finishedAt == null) "Ongoing" else "Created: ${SimpleDateFormat("MMM dd").format(game.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (game.finishedAt == null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${if (game.gameType == "Wingspan") game.currentRound.coerceAtMost(4) else game.currentRound} rnds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                game.winnerNames?.let {
                    Text(
                        text = "Winner: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
            Row {
                if (onDuplicateClick != null) {
                    IconButton(onClick = { onDuplicateClick(game) }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Settings",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (onDeleteClick != null) {
                    IconButton(onClick = { onDeleteClick(game.gameId) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete Game",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewGameScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).gameRepository,
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).playerRepository,
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).scoreRepository
        )
    )
) {
    var gameName by remember { mutableStateOf("") }
    var winningScoreStr by remember { mutableStateOf("") }
    var maxRoundsStr by remember { mutableStateOf("") }
    var selectedGameType by remember { mutableStateOf("Generic") }
    var wingspanGoalSide by remember { mutableStateOf("Green") }
    var isExpanded by remember { mutableStateOf(false) }

    val onStartGame = {
        if (gameName.isNotBlank()) {
            val winningScore = winningScoreStr.toIntOrNull()
            val maxRounds = if (selectedGameType == "Wingspan") 4 else maxRoundsStr.toIntOrNull()
            gameViewModel.startNewGame(
                gameName = gameName,
                winningScore = winningScore,
                maxRounds = maxRounds,
                gameType = selectedGameType,
                gameConfig = if (selectedGameType == "Wingspan") wingspanGoalSide else null
            ) { newGameId ->
                navController.navigate("active_game/$newGameId") {
                    popUpTo("game_list")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create New Game",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = gameName,
            onValueChange = { gameName = it },
            label = { Text("Game Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        // Game Type Selector
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            OutlinedTextField(
                value = selectedGameType,
                onValueChange = { },
                label = { Text("Select Game (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = true },
                readOnly = true,
                enabled = false, // Use enabled=false + clickable to intercept clicks on the whole field
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            )
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                DropdownMenuItem(
                    text = { Text("Generic Game") },
                    onClick = {
                        selectedGameType = "Generic"
                        isExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Wingspan") },
                    onClick = {
                        selectedGameType = "Wingspan"
                        if (gameName.isBlank()) gameName = "Wingspan"
                        isExpanded = false
                    }
                )
            }
        }

        if (selectedGameType == "Wingspan") {
            Text(
                text = "Wingspan Goal Board Side",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = wingspanGoalSide == "Green",
                    onClick = { wingspanGoalSide = "Green" }
                )
                Text("Green (Competitive)")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = wingspanGoalSide == "Blue",
                    onClick = { wingspanGoalSide = "Blue" }
                )
                Text("Blue (Non-competitive)")
            }
        }

        if (selectedGameType == "Generic") {
            OutlinedTextField(
                value = winningScoreStr,
                onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) winningScoreStr = it },
                label = { Text("Winning Score (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = maxRoundsStr,
                onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) maxRoundsStr = it },
                label = { Text("Max Rounds (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { onStartGame() }
                )
            )
        } else if (selectedGameType == "Wingspan") {
            Text(
                text = "Wingspan uses 4 rounds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = onStartGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Game")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun ActiveGameScreen(
    gameId: Int,
    navController: NavHostController,
    gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).gameRepository,
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).playerRepository,
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).scoreRepository
        )
    ),
    playerViewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).playerRepository,
            (navController.context.applicationContext as com.example.boardgamescoretracker.ScoreTrackerApplication).scoreRepository
        )
    )
) {
    val game by gameViewModel.getGameById(gameId).collectAsState(initial = null)
    val playersWithScores by playerViewModel.getPlayersWithScoresForGame(gameId).collectAsState(initial = emptyList())
    val roundHistory by playerViewModel.getRoundHistory(gameId).collectAsState(initial = emptyList())
    
    val scope = rememberCoroutineScope()
    var animatingStartingPlayerId by remember { mutableStateOf<Int?>(null) }
    val focusManager = LocalFocusManager.current
    val firstPlayerFocusRequester = remember { FocusRequester() }

    // Wingspan local state
    val wingspanInputs = remember { mutableStateMapOf<Int, Int>() }

    val isGameOver = game?.finishedAt != null
    val isWingspan = game?.gameType == "Wingspan"
    val isWingspanGoals = isWingspan && (game?.currentRound ?: 1) <= 4
    val isWingspanCategories = isWingspan && (game?.currentRound ?: 1) in 5..9
    
    val categoryNames = listOf("Birds", "Bonus", "Eggs", "Food", "Tucked")
    val currentCategory = if (isWingspanCategories) categoryNames.getOrNull((game?.currentRound ?: 5) - 5) ?: "" else ""
    val wingspanStageLabel = if (isWingspanGoals) "Round ${game?.currentRound} Goal" else if (isWingspanCategories) "$currentCategory Score" else ""

    // Auto-fill wingspanInputs from history if we are resuming or advancing
    LaunchedEffect(game?.currentRound) {
        if (isWingspan) {
            // When round changes, clear focus to ensure it goes back to top or moves correctly
            focusManager.clearFocus()
            
            val currentRoundNum = game?.currentRound ?: 1
            val existing = roundHistory.filter { it.round == currentRoundNum }
            if (existing.isNotEmpty()) {
                existing.forEach { 
                    wingspanInputs[it.playerId] = it.rawWingspanValue ?: 0
                }
            } else {
                wingspanInputs.clear()
            }
            
            // Try to focus the first box for categories
            if (isWingspanCategories || isWingspanGoals) {
                delay(100) // Brief delay to ensure UI is ready
                try { firstPlayerFocusRequester.requestFocus() } catch (e: Exception) {}
            }
        }
    }

    val deltas = remember(playersWithScores, roundHistory, game?.currentRound) {
        playersWithScores.associate { player ->
            val previousTotal = roundHistory.filter { it.playerId == player.playerId && it.round < (game?.currentRound ?: 1) }
                .sumOf { it.scoreIncrement }
            player.playerId to (player.score - previousTotal)
        }
    }

    val hasMetCondition = (!isWingspan && game?.winningScore != null && playersWithScores.any { it.score >= game!!.winningScore!! }) || 
                          (!isWingspan && game?.maxRounds != null && (game?.currentRound ?: 1) > (game?.maxRounds ?: 0)) ||
                          (isWingspan && (game?.currentRound ?: 1) > 9)
    
    // Live wingspan point calculation for goals (Competitive Green)
    val wingspanGoalPoints = remember(wingspanInputs.toMap(), game?.currentRound, game?.gameConfig, playersWithScores) {
        if (isWingspanGoals && game?.gameConfig == "Green") {
            val round = game?.currentRound ?: 1
            val roundPointsTable = when (round) {
                1 -> listOf(4, 1, 0, 0)
                2 -> listOf(5, 2, 1, 0)
                3 -> listOf(6, 3, 2, 0)
                4 -> listOf(7, 4, 3, 0)
                else -> listOf(0, 0, 0, 0)
            }
            
            playersWithScores.associate { player ->
                val rank = wingspanInputs[player.playerId] ?: 0
                var points = 0
                if (rank in 1..4) {
                    val playersAtSameRank = playersWithScores.count { (wingspanInputs[it.playerId] ?: 0) == rank }
                    val playersAtHigherRanks = playersWithScores.count { (wingspanInputs[it.playerId] ?: 0) in 1 until rank }
                    
                    val startIndex = playersAtHigherRanks
                    val endIndex = (startIndex + playersAtSameRank - 1).coerceAtMost(3)
                    
                    if (startIndex <= 3) {
                        val totalPointsForTie = roundPointsTable.slice(startIndex..endIndex).sum()
                        points = totalPointsForTie / playersAtSameRank
                    }
                }
                player.playerId to points
            }
        } else if (isWingspanGoals && game?.gameConfig == "Blue") {
            playersWithScores.associate { it.playerId to (wingspanInputs[it.playerId] ?: 0).coerceAtMost(5) }
        } else {
            emptyMap()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isGameOver && hasMetCondition) {
            val winners = game?.winnerNames ?: playersWithScores.maxByOrNull { it.score }?.playerName
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🏁 GAME OVER 🏁", style = MaterialTheme.typography.titleLarge)
                    winners?.let {
                        Text(
                            text = if (it.contains(",")) "Winners: $it!" else "Winner: $it!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game?.gameName ?: "Game $gameId",
                    style = MaterialTheme.typography.headlineLarge
                )
                if (isWingspanCategories) {
                    Text(
                        text = "Final Scoring: $currentCategory",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (isWingspanGoals) {
                    Text(
                        text = "Goal Scoring Phase",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    game?.winningScore?.let {
                        Text(
                            text = "Target: $it points",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isWingspanCategories) "Category" else "Round",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val displayRound = game?.currentRound ?: 1
                    val isWingspanActive = game?.gameType == "Wingspan"
                    Text(
                        text = if (isWingspanActive) displayRound.coerceAtMost(4).toString() else "$displayRound",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    val max = if (isWingspanActive) 4 else game?.maxRounds
                    max?.let {
                        Text(
                            text = " / $it",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isGameOver && !isWingspanCategories) {
            var showAddPlayer by remember { mutableStateOf(false) }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showAddPlayer = !showAddPlayer }) {
                        Icon(
                            if (showAddPlayer) Icons.Default.Delete else Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showAddPlayer) "Cancel" else "Add Player")
                    }
                }

                if (showAddPlayer) {
                    var newPlayerName by remember { mutableStateOf("") }
                    val onAddPlayer = {
                        if (newPlayerName.isNotBlank()) {
                            playerViewModel.addPlayerToGame(gameId, newPlayerName)
                            newPlayerName = ""
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newPlayerName,
                            onValueChange = { newPlayerName = it },
                            label = { Text("Player Name") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = ImeAction.Done,
                                capitalization = KeyboardCapitalization.Words
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onDone = { onAddPlayer() }
                            )
                        )
                        Button(onClick = {
                            onAddPlayer()
                            showAddPlayer = false
                        }) {
                            Text("Add Player")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (playersWithScores.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Starting Player",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        TextButton(
                            onClick = { 
                                if (playersWithScores.isNotEmpty() && animatingStartingPlayerId == null) {
                                    scope.launch {
                                        var delayTime = 40L
                                        val iterations = 15 + (0..10).random()
                                        repeat(iterations) {
                                            animatingStartingPlayerId = playersWithScores.random().playerId
                                            delay(delayTime)
                                            delayTime = (delayTime * 1.2f).toLong().coerceAtMost(400L)
                                        }
                                        val finalWinner = playersWithScores.random().playerId
                                        animatingStartingPlayerId = finalWinner
                                        delay(600)
                                        gameViewModel.setStartingPlayer(gameId, finalWinner)
                                        animatingStartingPlayerId = null
                                    }
                                }
                            },
                            enabled = animatingStartingPlayerId == null && !isGameOver
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pick Random", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        Text(
            text = if (isGameOver) "Final Standings" else (if (isWingspanCategories) "Current Totals" else "Score Board"),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(if (playersWithScores.size > 5) 4.dp else 8.dp)
        ) {
            items(playersWithScores) { playerScore ->
                val wingspanIncrement = if (isWingspanGoals) (wingspanGoalPoints[playerScore.playerId] ?: 0) else if (isWingspanCategories) (wingspanInputs[playerScore.playerId] ?: 0) else 0
                val displayedScore = playerScore.score + wingspanIncrement
                
                val highestScore = playersWithScores.maxOfOrNull { it.score + (if (isWingspanGoals) (wingspanGoalPoints[it.playerId] ?: 0) else if (isWingspanCategories) (wingspanInputs[it.playerId] ?: 0) else 0) } ?: 0
                val isLeading = highestScore > 0 && displayedScore == highestScore
                val shouldHighlight = isGameOver && hasMetCondition && isLeading || (!isGameOver && !isWingspan && game?.winningScore != null && playerScore.score >= (game?.winningScore ?: Int.MAX_VALUE))

                ScoreBoardRow(
                    playerName = playerScore.playerName,
                    score = displayedScore,
                    delta = if (isWingspanGoals || isWingspanCategories) wingspanIncrement else (deltas[playerScore.playerId] ?: 0),
                    isHighlighted = shouldHighlight,
                    isCompact = playersWithScores.size > 5,
                    isStartingPlayer = game?.startingPlayerId == playerScore.playerId,
                    isAnimatingStart = animatingStartingPlayerId == playerScore.playerId,
                    wingspanMode = if (isWingspanGoals) (game?.gameConfig ?: "Green") else null,
                    wingspanInputValue = wingspanInputs[playerScore.playerId] ?: 0,
                    isCategoryMode = isWingspanCategories,
                    wingspanStageLabel = wingspanStageLabel,
                    numPlayers = playersWithScores.size,
                    isWingspan = isWingspan,
                    isGameOver = isGameOver,
                    focusRequester = if (playersWithScores.indexOf(playerScore) == 0) firstPlayerFocusRequester else null,
                    onWingspanInput = { valToSet ->
                        wingspanInputs[playerScore.playerId] = valToSet
                        // Persist immediately in the current round
                        gameViewModel.updateWingspanRawValue(gameId, game?.currentRound ?: 1, playerScore.playerId, valToSet)
                    },
                    onSetStarting = { 
                        if (!isGameOver && animatingStartingPlayerId == null) {
                            gameViewModel.setStartingPlayer(gameId, if (game?.startingPlayerId == playerScore.playerId) null else playerScore.playerId)
                        }
                    },
                    onUpdateScore = { delta ->
                        if (!isGameOver && !isWingspan) {
                            playerViewModel.updateScore(gameId, playerScore.playerId, playerScore.score + delta)
                        }
                    },
                    onNextFocus = { focusManager.moveFocus(FocusDirection.Down) }
                )
            }
            
            item {
                if (!isGameOver) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        if ((game?.currentRound ?: 1) <= 9) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isWingspan && (game?.currentRound ?: 1) > 1) {
                                    OutlinedButton(
                                        onClick = { 
                                            val lastRound = roundHistory.maxOfOrNull { it.round } ?: 1
                                            gameViewModel.undoLastRound(gameId, lastRound)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Undo, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Undo Step")
                                    }
                                }
                                Button(
                                    onClick = { 
                                        if (isWingspanGoals) {
                                            gameViewModel.incrementRound(gameId, playersWithScores, game?.currentRound ?: 1, game?.winningScore, game?.maxRounds, game?.startingPlayerId, wingspanInputs.toMap(), "Wingspan", game?.gameConfig)
                                            wingspanInputs.clear()
                                        } else if (isWingspanCategories) {
                                            gameViewModel.incrementRound(gameId, playersWithScores, game?.currentRound ?: 1, game?.winningScore, game?.maxRounds, game?.startingPlayerId, wingspanInputs.toMap(), "Wingspan")
                                            wingspanInputs.clear()
                                        } else {
                                            gameViewModel.incrementRound(gameId, playersWithScores, game?.currentRound ?: 1, game?.winningScore, game?.maxRounds, game?.startingPlayerId) 
                                        }
                                    },
                                    modifier = if (isWingspan && (game?.currentRound ?: 1) > 1) Modifier.weight(1f) else Modifier.fillMaxWidth()
                                ) {
                                    Text(if (isWingspanCategories) "Next Category" else "Update Round")
                                }
                            }
                        }
                        
                        if (isWingspan && (game?.currentRound ?: 1) > 9) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { 
                                        val lastRound = roundHistory.maxOfOrNull { it.round } ?: 1
                                        gameViewModel.undoLastRound(gameId, lastRound)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Undo, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Undo Last Step")
                                }
                                Button(
                                    onClick = { gameViewModel.finishWingspanGame(gameId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Text("Finish Game")
                                }
                            }
                        }
                    }
                }

                if (roundHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Round History",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!isGameOver) {
                            TextButton(onClick = { 
                                val lastRound = roundHistory.maxOf { it.round }
                                gameViewModel.undoLastRound(gameId, lastRound)
                            }) {
                                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Undo Last", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RoundHistoryTable(
                        roundHistory = roundHistory, 
                        players = playersWithScores,
                        isGameOver = isGameOver,
                        isWingspan = isWingspan,
                        onEditIncrement = { round, playerId, newInc ->
                            playerViewModel.updateRoundScore(gameId, round, playerId, newInc)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close Game")
        }
    }
}

@Composable
fun WingspanScoringDialog(
    playerName: String,
    currentScores: List<com.example.boardgamescoretracker.data.db.CategoryScoreEntity>,
    onDismiss: () -> Unit,
    onUpdateCategory: (String, Int) -> Unit
) {
    val categories = listOf(
        "Bird Cards", "Bonus Cards", "Eggs", "Food on Cards", "Tucked Cards",
        "Round 1 Goal", "Round 2 Goal", "Round 3 Goal", "Round 4 Goal"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wingspan Scoring: $playerName") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                categories.forEach { category ->
                    val score = currentScores.find { it.categoryName == category }?.scoreValue ?: 0
                    var textValue by remember(score) { mutableStateOf(score.toString()) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = category, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { 
                                textValue = it
                                it.toIntOrNull()?.let { v -> onUpdateCategory(category, v) }
                            },
                            modifier = Modifier.width(80.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun RoundHistoryTable(
    roundHistory: List<com.example.boardgamescoretracker.data.db.RoundScoreEntity>,
    players: List<com.example.boardgamescoretracker.data.db.PlayerScore>,
    isGameOver: Boolean,
    isWingspan: Boolean = false,
    onEditIncrement: (Int, Int, Int) -> Unit
) {
    var editingCell by remember { mutableStateOf<Pair<Int, Int>?>(null) } // Round, PlayerId
    val categoryNames = listOf("Bird", "Bonus", "Eggs", "Food", "Tucked")

    // Calculate totals on the fly
    val totals = remember(roundHistory) {
        val t = mutableMapOf<Pair<Int, Int>, Int>()
        players.forEach { player ->
            var sum = 0
            roundHistory.filter { it.playerId == player.playerId }
                .sortedBy { it.round }
                .forEach { score ->
                    sum += score.scoreIncrement
                    t[score.round to player.playerId] = sum
                }
        }
        t
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Text(
                    text = if (isWingspan) "Phase" else "Rnd", 
                    modifier = Modifier.width(if (isWingspan) { if (roundHistory.any { it.round > 4 }) 50.dp else 35.dp } else 35.dp), 
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, 
                    style = MaterialTheme.typography.labelSmall
                )
                players.forEach { player ->
                    Text(
                        text = player.playerName.take(5), 
                        modifier = Modifier.weight(1f), 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            // Rows
            val rounds = roundHistory.groupBy { it.round }.toSortedMap()
            rounds.forEach { (round, scores) ->
                androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 2.dp), thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = if (isWingspan && round > 4) categoryNames.getOrNull(round - 5) ?: "$round" else "$round"
                    Text(
                        text = label, 
                        modifier = Modifier.width(if (isWingspan) { if (roundHistory.any { it.round > 4 }) 50.dp else 35.dp } else 35.dp), 
                        style = MaterialTheme.typography.bodySmall
                    )
                    players.forEach { player ->
                        val roundScore = scores.find { it.playerId == player.playerId }
                        val inc = roundScore?.scoreIncrement ?: 0
                        val total = totals[round to player.playerId] ?: 0
                        val isStarting = roundScore?.wasStartingPlayer ?: false
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isGameOver) {
                                    editingCell = round to player.playerId
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (editingCell?.first == round && editingCell?.second == player.playerId) {
                                var tempInc by remember { mutableStateOf(inc.toString()) }
                                val onSave = {
                                    tempInc.toIntOrNull()?.let {
                                        onEditIncrement(round, player.playerId, it)
                                    }
                                    editingCell = null
                                }

                                OutlinedTextField(
                                    value = tempInc,
                                    onValueChange = { tempInc = it },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                        onDone = { onSave() }
                                    ),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = { onSave() }) {
                                            Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(12.dp))
                                        }
                                    }
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isStarting) {
                                        Icon(
                                            Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(10.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = "$total",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "(${if (inc >= 0) "+$inc" else "$inc"})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (inc >= 0) MaterialTheme.colorScheme.primary else Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBoardRow(
    playerName: String,
    score: Int,
    delta: Int,
    isHighlighted: Boolean,
    isCompact: Boolean = false,
    isStartingPlayer: Boolean = false,
    isAnimatingStart: Boolean = false,
    wingspanMode: String? = null, // "Green" or "Blue"
    wingspanInputValue: Int = 0,
    isCategoryMode: Boolean = false,
    wingspanStageLabel: String = "",
    numPlayers: Int = 1,
    isWingspan: Boolean = false,
    isGameOver: Boolean = false,
    focusRequester: FocusRequester? = null,
    onWingspanInput: (Int) -> Unit = {},
    onSetStarting: () -> Unit = {},
    onUpdateScore: (Int) -> Unit,
    onNextFocus: () -> Unit = {}
) {
    val cardColors = if (isHighlighted) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else if (isAnimatingStart) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 2.dp else 4.dp),
        colors = cardColors,
        elevation = if (isHighlighted || isAnimatingStart) CardDefaults.cardElevation(defaultElevation = 8.dp) else CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 8.dp else 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = playerName,
                    style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = if (isHighlighted) androidx.compose.ui.text.font.FontWeight.ExtraBold else androidx.compose.ui.text.font.FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onSetStarting,
                    modifier = Modifier.size(if (isCompact) 24.dp else 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Starting Player",
                        tint = if (isStartingPlayer || isAnimatingStart) {
                            if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        } else {
                            if (isHighlighted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (wingspanMode != null || isCategoryMode) {
                    // Wingspan Input (Rank or Count or Category)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                        Text(
                            text = wingspanStageLabel,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        if (wingspanMode == "Green") {
                            // Competitive: Rank Dropdown
                            var isRankExpanded by remember { mutableStateOf(false) }
                            val rankLabels = listOf("First", "Second", "Third", "Fourth")
                            Box {
                                OutlinedButton(
                                    onClick = { isRankExpanded = true },
                                    modifier = Modifier.width(110.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(text = if (wingspanInputValue == 0) "-" else (rankLabels.getOrNull(wingspanInputValue - 1) ?: "$wingspanInputValue"), style = MaterialTheme.typography.bodySmall)
                                }
                                DropdownMenu(expanded = isRankExpanded, onDismissRequest = { isRankExpanded = false }) {
                                    DropdownMenuItem(text = { Text("None") }, onClick = { onWingspanInput(0); isRankExpanded = false })
                                    repeat(numPlayers.coerceAtMost(4)) { i ->
                                        val rank = i + 1
                                        val label = rankLabels.getOrNull(i) ?: "Rank $rank"
                                        DropdownMenuItem(text = { Text(label) }, onClick = { onWingspanInput(rank); isRankExpanded = false })
                                    }
                                }
                            }
                        } else {
                            // Blue or Category Mode: Numeric Input
                            var textValue by remember(wingspanInputValue) { mutableStateOf(wingspanInputValue.toString()) }
                            
                            OutlinedTextField(
                                value = textValue,
                                onValueChange = { input ->
                                    val digitsOnly = input.filter { it.isDigit() }
                                    if (digitsOnly.isEmpty()) {
                                        textValue = ""
                                        onWingspanInput(0)
                                    } else {
                                        var newValue = digitsOnly
                                        // Robust replacement: if we had a "0" and typed a digit, remove the "0"
                                        // regardless of where the cursor was (handling both "02" and "20")
                                        if (textValue == "0" && digitsOnly.length > 1) {
                                            newValue = digitsOnly.replaceFirst("0", "")
                                        }
                                        
                                        val parsed = newValue.toIntOrNull() ?: 0
                                        textValue = parsed.toString()
                                        onWingspanInput(parsed)
                                    }
                                },
                                modifier = Modifier
                                    .width(80.dp)
                                    .let { if (focusRequester != null) it.focusRequester(focusRequester) else it },
                                textStyle = MaterialTheme.typography.bodySmall,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onNext = { onNextFocus() }
                                ),
                                singleLine = true
                            )
                        }
                    }
                } else {
                    if (!(isWingspan && (numPlayers > 0 && wingspanStageLabel == "" && score > 0))) { // Hide buttons in final summary if wingspan
                        // Decrement buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { onUpdateScore(-10) },
                                colors = if (isHighlighted) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary, contentColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(if (isCompact) 32.dp else 36.dp).widthIn(min = 48.dp)
                            ) {
                                Text("-10", style = MaterialTheme.typography.labelMedium)
                            }
                            Button(
                                onClick = { onUpdateScore(-1) },
                                colors = if (isHighlighted) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary, contentColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(if (isCompact) 32.dp else 36.dp).widthIn(min = 44.dp)
                            ) {
                                Text("-1", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Current Score + Delta
                Row(verticalAlignment = Alignment.CenterVertically, modifier = if (wingspanMode != null || isCategoryMode) Modifier.padding(start = 16.dp) else Modifier) {
                    Text(
                        text = score.toString(),
                        style = if (isCompact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    if (!isGameOver && wingspanStageLabel != "") {
                        Text(
                            text = "(${if (delta >= 0) "+$delta" else "$delta"})",
                            style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            color = if (isHighlighted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else (if (delta >= 0) MaterialTheme.colorScheme.secondary else Color.Red)
                        )
                    }
                }

                if (wingspanMode == null && !isCategoryMode) {
                    if (!(isWingspan && (numPlayers > 0 && wingspanStageLabel == "" && score > 0))) {
                        // Increment buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { onUpdateScore(1) },
                                colors = if (isHighlighted) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary, contentColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(if (isCompact) 32.dp else 36.dp).widthIn(min = 44.dp)
                            ) {
                                Text("+1", style = MaterialTheme.typography.labelMedium)
                            }
                            Button(
                                onClick = { onUpdateScore(10) },
                                colors = if (isHighlighted) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary, contentColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(if (isCompact) 32.dp else 36.dp).widthIn(min = 48.dp)
                            ) {
                                Text("+10", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
