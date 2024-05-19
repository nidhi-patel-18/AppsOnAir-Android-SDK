package com.appsonair

import android.content.Context
import android.graphics.Color

class ShakeBugService {
    companion object {
        private const val PAGE_BACKGROUND_COLOR: String = "#E8F1FF"
        private const val APP_BAR_BACKGROUND_COLOR: String = "#E8F1FF"
        private const val APP_BAR_TITLE_TEXT: String = "New Ticket"
        private const val APP_BAR_TITLE_COLOR: String = "#000000"
        private const val TICKET_TYPE_LABEL_TEXT: String = "Ticket Type"
        private const val TICKET_TYPE_LABEL_COLOR: String = "#B1B1B3"
        private const val DESCRIPTION_LABEL_TEXT: String = "Description"
        private const val DESCRIPTION_LABEL_COLOR: String = "#B1B1B3"
        private const val DESCRIPTION_HINT_TEXT: String = "Add description here…"
        private const val DESCRIPTION_HINT_COLOR: String = "#B1B1B3"
        private const val DESCRIPTION_MAX_LENGTH: Int = 255
        private const val DESCRIPTION_COUNTER_TEXT_COLOR: String = "#B1B1B3"
        private const val EMAIL_LABEL_TEXT: String = "Email"
        private const val EMAIL_LABEL_COLOR: String = "#B1B1B3"
        private const val EMAIL_HINT_TEXT: String = "Add email here…"
        private const val EMAIL_HINT_COLOR: String = "#B1B1B3"
        private const val BUTTON_TEXT: String = "Submit"
        private const val BUTTON_TEXT_COLOR: String = "#FFFFFF"
        private const val BUTTON_BACKGROUND_COLOR: String = "#007AFF"

        var pageBackgroundColor: String = PAGE_BACKGROUND_COLOR
        var appbarBackgroundColor: String = APP_BAR_BACKGROUND_COLOR
        var appbarTitleText: String = APP_BAR_TITLE_TEXT
        var appbarTitleColor: String = APP_BAR_TITLE_COLOR
        var ticketTypeLabelText: String = TICKET_TYPE_LABEL_TEXT
        var ticketTypeLabelColor: String = TICKET_TYPE_LABEL_COLOR
        var descriptionLabelText: String = DESCRIPTION_LABEL_TEXT
        var descriptionLabelColor: String = DESCRIPTION_LABEL_COLOR
        var descriptionHintText: String = DESCRIPTION_HINT_TEXT
        var descriptionHintColor: String = DESCRIPTION_HINT_COLOR
        var descriptionMaxLength: Int = DESCRIPTION_MAX_LENGTH
        var descriptionCounterTextColor: String = DESCRIPTION_COUNTER_TEXT_COLOR
        var emailLabelText: String = EMAIL_LABEL_TEXT
        var emailLabelColor: String = EMAIL_LABEL_COLOR
        var emailHintText: String = EMAIL_HINT_TEXT
        var emailHintColor: String = EMAIL_HINT_COLOR
        var buttonText: String = BUTTON_TEXT
        var buttonTextColor: String = BUTTON_TEXT_COLOR
        var buttonBackgroundColor: String = BUTTON_BACKGROUND_COLOR

        fun isValidColorHex(colorHex: String): Boolean {
            return try {
                Color.parseColor(colorHex)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }
        @JvmStatic
        @JvmOverloads
        fun shakeBug(
            context: Context,
            pageBackgroundColor: String = PAGE_BACKGROUND_COLOR,
            appbarBackgroundColor: String = APP_BAR_BACKGROUND_COLOR,
            appbarTitleText: String = APP_BAR_TITLE_TEXT,
            appbarTitleColor: String = APP_BAR_TITLE_COLOR,
            ticketTypeLabelText: String = TICKET_TYPE_LABEL_TEXT,
            ticketTypeLabelColor: String = TICKET_TYPE_LABEL_COLOR,
            descriptionLabelText: String = DESCRIPTION_LABEL_TEXT,
            descriptionLabelColor: String = DESCRIPTION_LABEL_COLOR,
            descriptionHintText: String = DESCRIPTION_HINT_TEXT,
            descriptionHintColor: String = DESCRIPTION_HINT_COLOR,
            descriptionMaxLength: Int = DESCRIPTION_MAX_LENGTH,
            descriptionCounterTextColor: String = DESCRIPTION_COUNTER_TEXT_COLOR,
            emailLabelText: String = EMAIL_LABEL_TEXT,
            emailLabelColor: String = EMAIL_LABEL_COLOR,
            emailHintText: String = EMAIL_HINT_TEXT,
            emailHintColor: String = EMAIL_HINT_COLOR,
            buttonText: String = BUTTON_TEXT,
            buttonTextColor: String = BUTTON_TEXT_COLOR,
            buttonBackgroundColor: String = BUTTON_BACKGROUND_COLOR
        ) {
            ShakeBugService.pageBackgroundColor =
                if (isValidColorHex(pageBackgroundColor)) pageBackgroundColor else PAGE_BACKGROUND_COLOR
            ShakeBugService.appbarBackgroundColor =
                if (isValidColorHex(appbarBackgroundColor)) appbarBackgroundColor else APP_BAR_BACKGROUND_COLOR
            ShakeBugService.appbarTitleText = appbarTitleText
            ShakeBugService.appbarTitleColor =
                if (isValidColorHex(appbarTitleColor)) appbarTitleColor else APP_BAR_TITLE_COLOR
            ShakeBugService.ticketTypeLabelText = ticketTypeLabelText
            ShakeBugService.ticketTypeLabelColor =
                if (isValidColorHex(ticketTypeLabelColor)) ticketTypeLabelColor else TICKET_TYPE_LABEL_COLOR
            ShakeBugService.descriptionLabelText = descriptionLabelText
            ShakeBugService.descriptionLabelColor =
                if (isValidColorHex(descriptionLabelColor)) descriptionLabelColor else DESCRIPTION_LABEL_COLOR
            ShakeBugService.descriptionHintText = descriptionHintText
            ShakeBugService.descriptionHintColor =
                if (isValidColorHex(descriptionHintColor)) descriptionHintColor else DESCRIPTION_HINT_COLOR
            ShakeBugService.descriptionMaxLength = descriptionMaxLength
            ShakeBugService.descriptionCounterTextColor =
                if (isValidColorHex(descriptionCounterTextColor)) descriptionCounterTextColor else DESCRIPTION_COUNTER_TEXT_COLOR
            ShakeBugService.emailLabelText = emailLabelText
            ShakeBugService.emailLabelColor =
                if (isValidColorHex(emailLabelColor)) emailLabelColor else EMAIL_LABEL_COLOR
            ShakeBugService.emailHintText = emailHintText
            ShakeBugService.emailHintColor =
                if (isValidColorHex(emailHintColor)) emailHintColor else EMAIL_HINT_COLOR
            ShakeBugService.buttonText = buttonText
            ShakeBugService.buttonTextColor =
                if (isValidColorHex(buttonTextColor)) buttonTextColor else BUTTON_TEXT_COLOR
            ShakeBugService.buttonBackgroundColor =
                if (isValidColorHex(buttonBackgroundColor)) buttonBackgroundColor else BUTTON_BACKGROUND_COLOR
            AppsOnAirServices.shakeBug(context)
        }
    }
}
