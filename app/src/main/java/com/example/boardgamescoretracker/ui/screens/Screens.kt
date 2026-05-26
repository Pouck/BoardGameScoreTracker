package com.example.boardgamescoretracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
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
                        text = "•  ${game.currentRound} rnds",
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

    val onStartGame = {
        if (gameName.isNotBlank()) {
            val winningScore = winningScoreStr.toIntOrNull()
            val maxRounds = maxRoundsStr.toIntOrNull()
            gameViewModel.startNewGame(gameName, winningScore, maxRounds) { newGameId ->
                navController.navigate("active_game/$newGameId") {
                    popUpTo("game_list")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                imeAction = ImeAction.Next
            )
        )

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

    val deltas = remember(playersWithScores, roundHistory) {
        playersWithScores.associate { player ->
            val previousTotal = roundHistory.filter { it.playerId == player.playerId }
                .sumOf { it.scoreIncrement }
            player.playerId to (player.score - previousTotal)
        }
    }

    val isGameOver = game?.finishedAt != null
    val hasMetCondition = (game?.winningScore != null && playersWithScores.any { it.score >= game!!.winningScore!! }) || 
                          (game?.maxRounds != null && (game?.currentRound ?: 1) > (game?.maxRounds ?: 0))
    
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
                game?.winningScore?.let {
                    Text(
                        text = "Target: $it points",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Round",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val displayRound = game?.currentRound ?: 1
                    Text(
                        text = "$displayRound",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    game?.maxRounds?.let {
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

        if (!isGameOver) {
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
                                imeAction = ImeAction.Done
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
            text = if (isGameOver) "Final Standings" else "Score Board",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(if (playersWithScores.size > 5) 4.dp else 8.dp)
        ) {
            items(playersWithScores) { playerScore ->
                val highestScore = playersWithScores.maxOfOrNull { it.score } ?: 0
                val isLeading = highestScore > 0 && playerScore.score == highestScore
                val shouldHighlight = isGameOver && hasMetCondition && isLeading || (!isGameOver && game?.winningScore != null && playerScore.score >= (game?.winningScore ?: Int.MAX_VALUE))

                ScoreBoardRow(
                    playerName = playerScore.playerName,
                    score = playerScore.score,
                    delta = deltas[playerScore.playerId] ?: 0,
                    isHighlighted = shouldHighlight,
                    isCompact = playersWithScores.size > 5,
                    isStartingPlayer = game?.startingPlayerId == playerScore.playerId,
                    isAnimatingStart = animatingStartingPlayerId == playerScore.playerId,
                    onSetStarting = { 
                        if (!isGameOver && animatingStartingPlayerId == null) {
                            gameViewModel.setStartingPlayer(gameId, if (game?.startingPlayerId == playerScore.playerId) null else playerScore.playerId)
                        }
                    },
                    onUpdateScore = { delta ->
                        if (!isGameOver) {
                            playerViewModel.updateScore(gameId, playerScore.playerId, playerScore.score + delta)
                        }
                    }
                )
            }
            
            item {
                if (!isGameOver) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { gameViewModel.incrementRound(gameId, playersWithScores, game?.currentRound ?: 1, game?.winningScore, game?.maxRounds, game?.startingPlayerId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Round")
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
fun RoundHistoryTable(
    roundHistory: List<com.example.boardgamescoretracker.data.db.RoundScoreEntity>,
    players: List<com.example.boardgamescoretracker.data.db.PlayerScore>,
    isGameOver: Boolean,
    onEditIncrement: (Int, Int, Int) -> Unit
) {
    var editingCell by remember { mutableStateOf<Pair<Int, Int>?>(null) } // Round, PlayerId

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
                Text("Rnd", modifier = Modifier.width(35.dp), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
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
                    Text("$round", modifier = Modifier.width(35.dp), style = MaterialTheme.typography.bodySmall)
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
    onSetStarting: () -> Unit = {},
    onUpdateScore: (Int) -> Unit
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

                // Current Score + Delta
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = score.toString(),
                        style = if (isCompact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "(${if (delta >= 0) "+$delta" else "$delta"})",
                        style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = if (isHighlighted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else (if (delta >= 0) MaterialTheme.colorScheme.secondary else Color.Red)
                    )
                }

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
