package edu.bu.terriertrade.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.MoreHoriz
import edu.bu.terriertrade.data.model.Category

/** The icon for each category: the round buttons on Browse, listing cards and the New listing form. */
val Category.icon: ImageVector
    get() = when (this) {
        Category.BOOKS -> Icons.Outlined.Book
        Category.FURNITURE -> Icons.Outlined.Chair
        Category.TUTORING -> Icons.Outlined.School
        Category.TICKETS -> Icons.Outlined.ConfirmationNumber
        Category.NOTES -> Icons.Outlined.Description
        Category.TECH -> Icons.Outlined.Computer
        Category.OTHER -> Icons.Outlined.MoreHoriz
    }