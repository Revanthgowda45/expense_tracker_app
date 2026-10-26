package com.example.studentexpensetracker.ui.reports.mood

import androidx.compose.ui.graphics.Color
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

data class MoodInsight(
    val title: String,
    val description: String,
    val emoji: String = "💡"
)

data class MoodStat(
    val emoji: String,
    val mood: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val avgAmount: Double,
    val percentageOfMoodSpend: Float,
    val topCategory: String?,
    val color: Color
)

data class TimeOfDayMoodPattern(
    val timeSlot: String,
    val timeRange: String,
    val dominantMood: String,
    val dominantEmoji: String,
    val totalAmount: Double,
    val moodColor: Color
)

data class MoodAnalytics(
    val totalMoodTransactions: Int,
    val totalMoodSpend: Double,
    val moodStats: List<MoodStat>,
    val highestSpendMood: MoodStat?,
    val highestAvgMood: MoodStat?,
    val insights: List<MoodInsight>,
    val timeOfDayPatterns: List<TimeOfDayMoodPattern>,
    val isThresholdReached: Boolean,
    val thresholdCount: Int = 5,
    val remainingToThreshold: Int = 0
) {
    companion object {
        val EMPTY = MoodAnalytics(
            totalMoodTransactions = 0,
            totalMoodSpend = 0.0,
            moodStats = emptyList(),
            highestSpendMood = null,
            highestAvgMood = null,
            insights = emptyList(),
            timeOfDayPatterns = emptyList(),
            isThresholdReached = false,
            remainingToThreshold = 5
        )
    }
}

data class WeeklyMoodSummary(
    val dominantMood: String,
    val dominantEmoji: String,
    val dominantColor: Color,
    val totalWeeklySpend: Double,
    val moodTransactionCount: Int,
    val positivePercentage: Int,
    val headline: String,
    val summaryInsight: String
)

object MoodInsightsGenerator {

    val MOOD_EMOJIS = mapOf(
        "Happy" to "😊",
        "Neutral" to "😐",
        "Stressed" to "😟",
        "Bored" to "🥱",
        "Sad" to "😢",
        "Celebrating" to "🎉"
    )

    fun getMoodEmoji(mood: String?): String {
        if (mood == null) return "💭"
        return MOOD_EMOJIS.entries.firstOrNull { it.key.equals(mood, ignoreCase = true) }?.value
            ?: when (mood.lowercase()) {
                "happy" -> "😊"
                "neutral" -> "😐"
                "stressed" -> "😟"
                "bored" -> "🥱"
                "sad" -> "😢"
                "celebrating" -> "🎉"
                else -> "💭"
            }
    }

    fun getMoodColor(mood: String?): Color {
        return when (mood?.lowercase()) {
            "happy" -> Color(0xFF10B981) // Emerald Green
            "celebrating" -> Color(0xFF8B5CF6) // Purple
            "neutral" -> Color(0xFF64748B) // Slate Blue
            "bored" -> Color(0xFFF59E0B) // Amber
            "stressed" -> Color(0xFFEF4444) // Rose Red
            "sad" -> Color(0xFF3B82F6) // Blue
            else -> Color(0xFF94A3B8)
        }
    }

    /**
     * Backward-compatible generator for existing unit tests and callers
     */
    fun generate(transactions: List<TransactionEntity>): List<MoodInsight> {
        val moodTransactions = transactions.filter { it.type == "EXPENSE" && !it.mood.isNullOrBlank() }
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        if (moodTransactions.size < 5) {
            return emptyList()
        }

        val insights = mutableListOf<MoodInsight>()

        // 1. Total spent by mood
        val spentByMood = moodTransactions.groupBy { it.mood!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val topMood = spentByMood.maxByOrNull { it.value }
        if (topMood != null && topMood.value > 0) {
            insights.add(
                MoodInsight(
                    title = "Highest Spending Mood",
                    description = "You spend the most money when you are feeling ${topMood.key}.",
                    emoji = getMoodEmoji(topMood.key)
                )
            )
        }

        // 2. Average spend by mood
        val avgSpendByMood = moodTransactions.groupBy { it.mood!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } / entry.value.size }

        val highestAvgMood = avgSpendByMood.maxByOrNull { it.value }
        if (highestAvgMood != null && highestAvgMood.value > 0) {
            insights.add(
                MoodInsight(
                    title = "Most Expensive Habit",
                    description = "Your average transaction is highest (${formatter.format(highestAvgMood.value)}) when you are ${highestAvgMood.key}.",
                    emoji = getMoodEmoji(highestAvgMood.key)
                )
            )
        }

        // 3. Category per mood (e.g., Stressed = Food)
        val stressedTransactions = moodTransactions.filter { it.mood.equals("Stressed", ignoreCase = true) }
        if (stressedTransactions.isNotEmpty()) {
            val topStressedCategory = stressedTransactions
                .groupBy {
                    val cat = it.note?.substringBefore(":")?.trim()
                    if (!cat.isNullOrBlank()) cat else "Other Expense"
                }
                .maxByOrNull { it.value.size }?.key

            if (topStressedCategory != null) {
                insights.add(
                    MoodInsight(
                        title = "Stress Spending",
                        description = "When stressed, you frequently spend on $topStressedCategory.",
                        emoji = "😟"
                    )
                )
            }
        }

        return insights
    }

    /**
     * Comprehensive Mood Analytics calculation for charts, breakdowns, and plain-language insights
     */
    fun analyze(transactions: List<TransactionEntity>): MoodAnalytics {
        val moodTransactions = transactions.filter { it.type == "EXPENSE" && !it.mood.isNullOrBlank() }
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val threshold = 5

        if (moodTransactions.size < threshold) {
            return MoodAnalytics(
                totalMoodTransactions = moodTransactions.size,
                totalMoodSpend = moodTransactions.sumOf { it.amount },
                moodStats = emptyList(),
                highestSpendMood = null,
                highestAvgMood = null,
                insights = emptyList(),
                timeOfDayPatterns = emptyList(),
                isThresholdReached = false,
                thresholdCount = threshold,
                remainingToThreshold = threshold - moodTransactions.size
            )
        }

        val totalMoodSpend = moodTransactions.sumOf { it.amount }
        val overallAvg = if (moodTransactions.isNotEmpty()) totalMoodSpend / moodTransactions.size else 0.0

        // Per-mood breakdown
        val moodGroups = moodTransactions.groupBy { it.mood!! }
        val moodStats = moodGroups.map { (moodName, txs) ->
            val total = txs.sumOf { it.amount }
            val count = txs.size
            val avg = if (count > 0) total / count else 0.0
            val pct = if (totalMoodSpend > 0) ((total / totalMoodSpend) * 100).toFloat() else 0f

            val topCat = txs
                .mapNotNull { it.note?.substringBefore(":")?.trim() }
                .filter { it.isNotBlank() }
                .groupBy { it }
                .maxByOrNull { it.value.size }?.key ?: "General"

            MoodStat(
                emoji = getMoodEmoji(moodName),
                mood = moodName,
                totalAmount = total,
                transactionCount = count,
                avgAmount = avg,
                percentageOfMoodSpend = pct,
                topCategory = topCat,
                color = getMoodColor(moodName)
            )
        }.sortedByDescending { it.totalAmount }

        val highestSpendMood = moodStats.firstOrNull()
        val highestAvgMood = moodStats.maxByOrNull { it.avgAmount }

        // Time of Day Patterns
        val timePatterns = mutableListOf<TimeOfDayMoodPattern>()
        val slots = listOf(
            Triple("Morning", "6 AM - 12 PM") { hr: Int -> hr in 6..11 },
            Triple("Afternoon", "12 PM - 5 PM") { hr: Int -> hr in 12..16 },
            Triple("Evening", "5 PM - 10 PM") { hr: Int -> hr in 17..21 },
            Triple("Late Night", "10 PM - 6 AM") { hr: Int -> hr >= 22 || hr < 6 }
        )

        slots.forEach { (slotName, rangeStr, filterFn) ->
            val slotTxs = moodTransactions.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateTime }
                filterFn(cal.get(Calendar.HOUR_OF_DAY))
            }
            if (slotTxs.isNotEmpty()) {
                val slotTotal = slotTxs.sumOf { it.amount }
                val dominantMoodName = slotTxs.groupBy { it.mood!! }.maxByOrNull { it.value.sumOf { tx -> tx.amount } }?.key ?: "Neutral"
                timePatterns.add(
                    TimeOfDayMoodPattern(
                        timeSlot = slotName,
                        timeRange = rangeStr,
                        dominantMood = dominantMoodName,
                        dominantEmoji = getMoodEmoji(dominantMoodName),
                        totalAmount = slotTotal,
                        moodColor = getMoodColor(dominantMoodName)
                    )
                )
            }
        }

        // Natural Language Insights
        val insights = mutableListOf<MoodInsight>()

        // 1. Highest Spending Mood
        if (highestSpendMood != null && highestSpendMood.totalAmount > 0) {
            insights.add(
                MoodInsight(
                    title = "Highest Spending Driver",
                    description = "You spend the most (${formatter.format(highestSpendMood.totalAmount)}) when feeling ${highestSpendMood.mood}.",
                    emoji = highestSpendMood.emoji
                )
            )
        }

        // 2. Stress Premium (vs overall average)
        val stressedStat = moodStats.find { it.mood.equals("Stressed", ignoreCase = true) }
        if (stressedStat != null && overallAvg > 0 && stressedStat.avgAmount > overallAvg * 1.15) {
            val pct = (((stressedStat.avgAmount - overallAvg) / overallAvg) * 100).toInt()
            insights.add(
                MoodInsight(
                    title = "Stress Premium",
                    description = "You spend $pct% more per purchase when feeling stressed compared to your baseline.",
                    emoji = "😟"
                )
            )
        }

        // 3. Boredom Spending Cost
        val boredStat = moodStats.find { it.mood.equals("Bored", ignoreCase = true) }
        if (boredStat != null && boredStat.totalAmount > 0) {
            insights.add(
                MoodInsight(
                    title = "Boredom Purchases",
                    description = "Bored spending cost you ${formatter.format(boredStat.totalAmount)} across ${boredStat.transactionCount} transactions.",
                    emoji = "🥱"
                )
            )
        }

        // 4. Happiest vs Stressed Days
        val happyStat = moodStats.find { it.mood.equals("Happy", ignoreCase = true) }
        if (happyStat != null && stressedStat != null && happyStat.avgAmount < stressedStat.avgAmount) {
            insights.add(
                MoodInsight(
                    title = "Mindful Spending",
                    description = "Your happiest transactions average ${formatter.format(happyStat.avgAmount)} vs ${formatter.format(stressedStat.avgAmount)} when stressed.",
                    emoji = "😊"
                )
            )
        }

        // 5. Late-Night Spikes
        val lateNightTxs = moodTransactions.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateTime }
            val hr = cal.get(Calendar.HOUR_OF_DAY)
            hr >= 22 || hr < 6
        }
        if (lateNightTxs.isNotEmpty()) {
            val lateNightTotal = lateNightTxs.sumOf { it.amount }
            val lateNightDominant = lateNightTxs.groupBy { it.mood!! }.maxByOrNull { it.value.size }?.key ?: "Bored"
            insights.add(
                MoodInsight(
                    title = "Late-Night Spending",
                    description = "Late night (10 PM - 6 AM) accounts for ${formatter.format(lateNightTotal)} of spending, mostly while feeling $lateNightDominant.",
                    emoji = "🌙"
                )
            )
        }

        // 6. Category Triggers
        if (highestSpendMood != null && !highestSpendMood.topCategory.isNullOrBlank()) {
            insights.add(
                MoodInsight(
                    title = "Category Trigger",
                    description = "When feeling ${highestSpendMood.mood}, your primary spending category is ${highestSpendMood.topCategory}.",
                    emoji = "🎯"
                )
            )
        }

        return MoodAnalytics(
            totalMoodTransactions = moodTransactions.size,
            totalMoodSpend = totalMoodSpend,
            moodStats = moodStats,
            highestSpendMood = highestSpendMood,
            highestAvgMood = highestAvgMood,
            insights = insights,
            timeOfDayPatterns = timePatterns,
            isThresholdReached = true,
            thresholdCount = threshold,
            remainingToThreshold = 0
        )
    }

    /**
     * Weekly mood vs money summary for the Home Dashboard
     */
    fun generateWeeklySummary(transactions: List<TransactionEntity>): WeeklyMoodSummary? {
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        val weeklyTxs = transactions.filter {
            it.type == "EXPENSE" && !it.mood.isNullOrBlank() && it.dateTime >= sevenDaysAgo
        }
        if (weeklyTxs.isEmpty()) return null

        val totalSpend = weeklyTxs.sumOf { it.amount }
        val moodGroups = weeklyTxs.groupBy { it.mood!! }
        val dominantEntry = moodGroups.maxByOrNull { it.value.sumOf { tx -> tx.amount } } ?: return null
        val dominantMood = dominantEntry.key
        val dominantEmoji = getMoodEmoji(dominantMood)
        val dominantColor = getMoodColor(dominantMood)

        val positiveMoods = setOf("happy", "celebrating", "neutral")
        val positiveSpend = weeklyTxs.filter { it.mood!!.lowercase() in positiveMoods }.sumOf { it.amount }
        val positivePercentage = if (totalSpend > 0) ((positiveSpend / totalSpend) * 100).toInt() else 0

        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val headline = "Dominant Mood: $dominantEmoji $dominantMood"
        val summaryInsight = when {
            positivePercentage >= 70 -> "Great emotional balance! $positivePercentage% of this week's spending was positive or mindful."
            dominantMood.equals("Stressed", ignoreCase = true) -> "Stress was your biggest spending trigger this week (${formatter.format(dominantEntry.value.sumOf { it.amount })})."
            dominantMood.equals("Bored", ignoreCase = true) -> "Watch out for boredom snacking/shopping (${formatter.format(dominantEntry.value.sumOf { it.amount })} spent this week)."
            else -> "${formatter.format(totalSpend)} logged across ${weeklyTxs.size} mood-tagged expenses this week."
        }

        return WeeklyMoodSummary(
            dominantMood = dominantMood,
            dominantEmoji = dominantEmoji,
            dominantColor = dominantColor,
            totalWeeklySpend = totalSpend,
            moodTransactionCount = weeklyTxs.size,
            positivePercentage = positivePercentage,
            headline = headline,
            summaryInsight = summaryInsight
        )
    }
}
