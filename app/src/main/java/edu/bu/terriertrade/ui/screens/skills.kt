package edu.bu.terriertrade.ui.screens.skills

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.components.TTEmptyState

@Composable
fun SkillsScreen(modifier: Modifier = Modifier) {
    TTEmptyState(
        title = "Skill swaps coming soon",
        message = "Teach something you know and learn something back from another Terrier.",
        modifier = modifier,
    )
}