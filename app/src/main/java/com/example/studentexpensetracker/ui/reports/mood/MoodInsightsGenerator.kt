package com.example.studentexpensetracker.ui.reports.mood

import com.example.studentexpensetracker.data.local.entity.TransactionEntity

data class MoodInsight(
    val title: String,
    val description: String
)

object MoodInsightsGenerator {

    fun generate(transactions: List<TransactionEntity>): List<MoodInsight> {
        val moodTransactions = transactions.filter { it.type == "EXPENSE" && !it.mood.isNullOrBlank() }
        
        if (moodTransactions.size < 5) {
            return emptyList() // Not enough data
        }

        val insights = mutableListOf<MoodInsight>()

        // 1. Total spent by mood
        val spentByMood = moodTransactions.groupBy { it.mood!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val topMood = spentByMood.maxByOrNull { it.value }
        if (topMood != null) {
            insights.add(
                MoodInsight(
                    title = "Highest Spending Mood",
                    description = "You spend the most money when you are feeling ${topMood.key}."
                )
            )
        }

        // 2. Average spend by mood
        val avgSpendByMood = moodTransactions.groupBy { it.mood!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } / entry.value.size }

        val highestAvgMood = avgSpendByMood.maxByOrNull { it.value }
        if (highestAvgMood != null) {
            insights.add(
                MoodInsight(
                    title = "Most Expensive Habit",
                    description = "Your average transaction is highest (₹${String.format("%.2f", highestAvgMood.value)}) when you are ${highestAvgMood.key}."
                )
            )
        }

        // 3. Category per mood (e.g., Stressed = Food)
        val stressedTransactions = moodTransactions.filter { it.mood == "Stressed" }
        if (stressedTransactions.isNotEmpty()) {
            // Category is prepended to the note field in our current DB setup
            val topStressedCategory = stressedTransactions
                .groupBy { it.note?.substringBefore(":")?.trim() ?: "Uncategorized" }
                .maxByOrNull { it.value.size }?.key
            
            if (topStressedCategory != null) {
                insights.add(
                    MoodInsight(
                        title = "Stress Spending",
                        description = "When stressed, you frequently spend on $topStressedCategory."
                    )
                )
            }
        }

        return insights
    }
}
