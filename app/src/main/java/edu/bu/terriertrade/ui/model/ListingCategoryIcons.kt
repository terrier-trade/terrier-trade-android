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

/** The icon for each category, used on the round category buttons and listing pictures. */
val ListingCategory.icon: ImageVector
    get() = when (this) {
        ListingCategory.BOOKS -> Icons.Outlined.Book
        ListingCategory.FURNITURE -> Icons.Outlined.Chair
        ListingCategory.TUTORING -> Icons.Outlined.School
        ListingCategory.TICKETS -> Icons.Outlined.ConfirmationNumber
        ListingCategory.NOTES -> Icons.Outlined.Description
        ListingCategory.TECH -> Icons.Outlined.Computer
        ListingCategory.OTHER -> Icons.Outlined.MoreHoriz
    }