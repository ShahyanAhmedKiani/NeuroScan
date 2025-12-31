package com.example.neuroscan

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.neuroscan.databinding.ActivityBrainGameBinding
import java.util.*

class BrainGame : AppCompatActivity() {

    private lateinit var binding: ActivityBrainGameBinding
    private lateinit var memoryBoardAdapter: MemoryBoardAdapter
    private val memoryCards = mutableListOf<MemoryCard>()
    private var numPairsFound = 0
    private var numMoves = 0
    private var indexOfSingleSelectedCard: Int? = null
    private var streak = 0
    private var bestScore = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrainGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        loadGameStats()
        setupBoard()
        scheduleStreakNotification()
    }

    private fun loadGameStats() {
        val sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        streak = sharedPreferences.getInt("streak", 0)
        bestScore = sharedPreferences.getInt("best_score", -1)
        binding.tvStreak.text = "Streak: $streak days"
        if (bestScore != -1) {
            binding.tvBestScore.text = "Best Score: $bestScore"
        }
    }

    private fun setupBoard() {
        val imageResources = listOf(
            R.drawable.ic_brain_1, R.drawable.ic_brain_2, R.drawable.ic_brain_3, R.drawable.ic_brain_4,
            R.drawable.ic_brain_5, R.drawable.ic_brain_6, R.drawable.ic_brain_7, R.drawable.ic_brain_8
        )
        val shuffledImages = (imageResources + imageResources).shuffled()

        memoryCards.addAll(shuffledImages.map { MemoryCard(it) })

        memoryBoardAdapter = MemoryBoardAdapter(memoryCards) { position ->
            onCardClicked(position)
        }

        binding.rvBoard.apply {
            adapter = memoryBoardAdapter
            layoutManager = GridLayoutManager(this@BrainGame, 4)
            setHasFixedSize(true)
        }
    }

    private fun onCardClicked(position: Int) {
        val card = memoryCards[position]
        if (card.isFaceUp || card.isMatched) return

        numMoves++
        binding.tvNumMoves.text = "Moves: $numMoves"

        card.isFaceUp = true
        if (indexOfSingleSelectedCard == null) {
            indexOfSingleSelectedCard = position
        } else {
            checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        memoryBoardAdapter.notifyDataSetChanged()
    }

    private fun checkForMatch(position1: Int, position2: Int) {
        if (memoryCards[position1].identifier == memoryCards[position2].identifier) {
            memoryCards[position1].isMatched = true
            memoryCards[position2].isMatched = true
            numPairsFound++
            binding.tvNumPairs.text = "Pairs: $numPairsFound / 8"
            if (numPairsFound == 8) {
                updateBestScore()
                showGameWonDialog()
                updateStreak()
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                memoryCards[position1].isFaceUp = false
                memoryCards[position2].isFaceUp = false
                memoryBoardAdapter.notifyDataSetChanged()
            }, 1000)
        }
    }

    private fun updateBestScore() {
        if (bestScore == -1 || numMoves < bestScore) {
            bestScore = numMoves
            val sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putInt("best_score", bestScore)
                apply()
            }
            binding.tvBestScore.text = "Best Score: $bestScore"
        }
    }

    private fun showGameWonDialog() {
        AlertDialog.Builder(this)
            .setTitle("Congratulations!")
            .setMessage("You've found all the pairs in $numMoves moves. Keep up the great work!")
            .setPositiveButton("Play Again") { _, _ -> resetGame() }
            .setCancelable(false)
            .show()
    }

    private fun resetGame() {
        memoryCards.clear()
        numPairsFound = 0
        numMoves = 0
        indexOfSingleSelectedCard = null
        binding.tvNumMoves.text = "Moves: 0"
        binding.tvNumPairs.text = "Pairs: 0 / 8"
        setupBoard()
        memoryBoardAdapter.notifyDataSetChanged()
    }

    private fun updateStreak() {
        val sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val lastPlayed = sharedPreferences.getLong("last_played", 0)
        val today = Calendar.getInstance()

        if (lastPlayed > 0) {
            val lastPlayedCal = Calendar.getInstance().apply { timeInMillis = lastPlayed }
            if (today.get(Calendar.DAY_OF_YEAR) - lastPlayedCal.get(Calendar.DAY_OF_YEAR) == 1) {
                streak++
            } else if (today.get(Calendar.DAY_OF_YEAR) != lastPlayedCal.get(Calendar.DAY_OF_YEAR)) {
                streak = 1
            }
        } else {
            streak = 1
        }

        with(sharedPreferences.edit()) {
            putInt("streak", streak)
            putLong("last_played", today.timeInMillis)
            apply()
        }
        binding.tvStreak.text = "Streak: $streak days"
    }

    private fun scheduleStreakNotification() {
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_DAY

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_HALF_DAY,
            pendingIntent
        )
    }
}
