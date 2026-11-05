package edu.bu.terriertrade.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.components.TTBottomActions
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTRuleCard
import edu.bu.terriertrade.ui.components.TTScreenHeader
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** One rule on the Terms screen. */
data class CommunityRule(val icon: ImageVector, val title: String, val body: String)

/** Everything a student agrees to when they create an account. Kept in one place so it's easy to update. */
object CommunityRules {
    const val LAST_UPDATED = "November 2026"

    val all = listOf(
        CommunityRule(
            Icons.Outlined.VerifiedUser, "BU students only",
            "Only verified @bu.edu accounts can join. Use your real name and keep to one account.",
        ),
        CommunityRule(
            Icons.Outlined.Handshake, "No money in the app",
            "Terrier Trade has no fees and never handles payments. You pay or get paid in person at the meetup.",
        ),
        CommunityRule(
            Icons.Outlined.Place, "Meet on campus, in public",
            "Meet in busy public spots like GSU or Mugar Library, in daylight when you can. " +
                "Don't share your room number or go somewhere private with someone you just met.",
        ),
        CommunityRule(
            Icons.Outlined.SwapHoriz, "Skill swaps",
            "You can trade teaching for teaching with no money. Agree in chat on what each person " +
                "teaches and for how long before you meet.",
        ),
        CommunityRule(
            Icons.Outlined.ConfirmationNumber, "How ticket handoffs work",
            "1. Agree on a deal in chat. 2. Send the tickets with the ticket app's Transfer button. " +
                "3. You tap Sent, they tap Received. Screenshots, paper or PDF tickets don't count.",
        ),
        CommunityRule(
            Icons.Outlined.Block, "What you can't post",
            "No alcohol, drugs, weapons, fake IDs, stolen items, or exam answers and other people's graded work. " +
                "Nothing that breaks BU's Code of Student Conduct.",
        ),
        CommunityRule(
            Icons.Outlined.ThumbUp, "Be honest and respectful",
            "Describe things truthfully, show up when you say you will, and keep chats friendly. " +
                "Leave a fair review after each trade.",
        ),
        CommunityRule(
            Icons.Outlined.ReportProblem, "If something feels wrong",
            "Stop the trade and leave. In an emergency, call 911 or BU Police.",
        ),
        CommunityRule(
            Icons.Outlined.School, "A student project",
            "Terrier Trade is built by BU students and isn't run by Boston University. " +
                "Trades are between you and the other student.",
        ),
    )
}

/**
 * The Terms & community rules. Opened from the checkbox on Create account.
 * With [onAgree], the bottom button ticks the box and goes back. Without it, the screen is read-only.
 */
@Composable
fun CommunityRulesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAgree: (() -> Unit)? = null,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TTScreenHeader(
                title = "Community rules",
                subtitle = "Terms of use · Updated ${CommunityRules.LAST_UPDATED}",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text(
                    text = "Terrier Trade only works if everyone can trust each other. By creating an account you agree to these rules.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CommunityRules.all.forEach { rule ->
                    TTRuleCard(icon = rule.icon, title = rule.title, body = rule.body)
                }
            }
            if (onAgree != null) {
                TTBottomActions {
                    TTPrimaryButton(text = "I agree", onClick = onAgree, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CommunityRulesScreenPreview() {
    TerrierTradeTheme {
        CommunityRulesScreen(onBack = {}, onAgree = {})
    }
}
