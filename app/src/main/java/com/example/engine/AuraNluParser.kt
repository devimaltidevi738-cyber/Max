package com.example.engine

import com.example.model.ActionCategory
import com.example.model.CommandIntent
import com.example.model.ConfirmationLevel
import java.util.Locale
import java.util.regex.Pattern

class AuraNluParser {

    // Context Memory across conversational turns
    data class ConversationContext(
        var lastOpenedApp: String? = null,
        var lastTargetPackage: String? = null,
        var lastSearchedQuery: String? = null,
        var lastContactTarget: String? = null,
        var lastCategory: ActionCategory = ActionCategory.UNKNOWN,
        var pendingConfirmationAction: CommandIntent? = null
    )

    private val context = ConversationContext()

    fun getContext(): ConversationContext = context

    fun resetContext() {
        context.lastOpenedApp = null
        context.lastTargetPackage = null
        context.lastSearchedQuery = null
        context.lastContactTarget = null
        context.lastCategory = ActionCategory.UNKNOWN
        context.pendingConfirmationAction = null
    }

    fun parse(rawInput: String): CommandIntent {
        val cleanInput = rawInput.trim()
        val lower = cleanInput.lowercase(Locale.ROOT)

        // 1. Check for Confirmation Responses if pending
        if (context.pendingConfirmationAction != null) {
            if (isAffirmative(lower)) {
                val pending = context.pendingConfirmationAction!!
                context.pendingConfirmationAction = null
                return pending.copy(
                    actionName = "CONFIRMED_EXECUTION",
                    spokenResponseHindi = "Ji, confirm ho gaya. Action execute kar raha hoon.",
                    spokenResponseEnglish = "Confirmed. Executing action."
                )
            } else if (isNegative(lower)) {
                context.pendingConfirmationAction = null
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.SYSTEM_AUTOMATION,
                    actionName = "CANCELLED",
                    spokenResponseHindi = "Theek hai, action cancel kar diya gaya hai.",
                    spokenResponseEnglish = "Action cancelled.",
                    confirmationLevel = ConfirmationLevel.LEVEL_1_SAFE
                )
            }
        }

        // 2. Check for Identity / Self introduction query
        if (matchesAny(lower, "who are you", "tum kaun ho", "aap kaun ho", "tera naam kya hai", "apna naam batao", "what is your name", "max kaun hai", "tumhara naam kya hai")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.SYSTEM_AUTOMATION,
                actionName = "ASSISTANT_INTRO",
                spokenResponseHindi = "Ji, main MAX hoon. Boliye.",
                spokenResponseEnglish = "I am MAX, your personal Android assistant."
            )
        }

        // 3. Check for Multi-step Commands (split by 'aur', 'and', 'then', 'phir', comma)
        if (hasMultiStep(lower)) {
            val subPhrases = splitMultiStep(cleanInput)
            if (subPhrases.size > 1) {
                val subIntents = subPhrases.map { subPhrase -> parseSingle(subPhrase) }
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.SYSTEM_AUTOMATION,
                    actionName = "MULTI_STEP_AUTOMATION",
                    spokenResponseHindi = "Ji, dono actions execute kar raha hoon.",
                    spokenResponseEnglish = "Executing command sequence.",
                    isMultiStep = true,
                    subIntents = subIntents
                )
            }
        }

        return parseSingle(cleanInput)
    }

    private fun parseSingle(rawInput: String): CommandIntent {
        val cleanInput = rawInput.trim()
        val lower = cleanInput.lowercase(Locale.ROOT)

        // Handle Contextual "Ab" / "Then" (e.g. "Ab Shah Rukh Khan search karo" or "Ab search karo...")
        if (lower.startsWith("ab ") || lower.startsWith("then ") || lower.startsWith("phir ")) {
            val remaining = if (lower.startsWith("ab ")) lower.substring(3)
            else if (lower.startsWith("then ")) lower.substring(5)
            else lower.substring(5)

            if (context.lastOpenedApp.equals("YouTube", ignoreCase = true) || context.lastTargetPackage == "com.google.android.youtube") {
                val searchQuery = extractSearchQuery(remaining)
                context.lastSearchedQuery = searchQuery
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.WEB_SEARCH,
                    actionName = "SEARCH_YOUTUBE",
                    primaryParam = searchQuery,
                    targetPackage = "com.google.android.youtube",
                    spokenResponseHindi = "YouTube par $searchQuery search kar raha hoon.",
                    spokenResponseEnglish = "Searching $searchQuery on YouTube."
                )
            } else if (context.lastOpenedApp.equals("Chrome", ignoreCase = true) || context.lastTargetPackage == "com.android.chrome") {
                val searchQuery = extractSearchQuery(remaining)
                context.lastSearchedQuery = searchQuery
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.WEB_SEARCH,
                    actionName = "SEARCH_GOOGLE",
                    primaryParam = searchQuery,
                    targetPackage = "com.android.chrome",
                    spokenResponseHindi = "Chrome mein $searchQuery search kar raha hoon.",
                    spokenResponseEnglish = "Searching $searchQuery in Chrome."
                )
            }
        }

        // ==========================================
        // 1. PHONE NAVIGATION & ACCESSIBILITY
        // ==========================================
        if (matchesAny(lower, "home pe jao", "go home", "home screen", "home screen pe jao", "home jao", "home")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.NAVIGATION,
                actionName = "NAV_HOME",
                requiresAccessibility = true,
                spokenResponseHindi = "Home screen par jaa raha hoon.",
                spokenResponseEnglish = "Going to Home screen."
            )
        }

        if (matchesAny(lower, "back karo", "go back", "piche jao", "peeche jao", "back jao", "back")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.NAVIGATION,
                actionName = "NAV_BACK",
                requiresAccessibility = true,
                spokenResponseHindi = "Back kar raha hoon.",
                spokenResponseEnglish = "Going back."
            )
        }

        if (matchesAny(lower, "recent apps kholo", "recent apps", "recents kholo", "open recent apps", "recent task", "recent app")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.NAVIGATION,
                actionName = "NAV_RECENTS",
                requiresAccessibility = true,
                spokenResponseHindi = "Recent apps khol raha hoon.",
                spokenResponseEnglish = "Opening Recent apps."
            )
        }

        if (matchesAny(lower, "notifications kholo", "notification panel kholo", "open notifications", "notification bar")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.NAVIGATION,
                actionName = "NAV_NOTIFICATIONS",
                requiresAccessibility = true,
                spokenResponseHindi = "Notification panel khol raha hoon.",
                spokenResponseEnglish = "Opening Notifications."
            )
        }

        if (matchesAny(lower, "quick settings kholo", "quick settings", "open quick settings", "control center")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.NAVIGATION,
                actionName = "NAV_QUICK_SETTINGS",
                requiresAccessibility = true,
                spokenResponseHindi = "Quick Settings khol raha hoon.",
                spokenResponseEnglish = "Opening Quick Settings."
            )
        }

        if (matchesAny(lower, "screen lock karo", "phone lock karo", "lock the phone", "lock screen", "phone lock kar do")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.NAVIGATION,
                actionName = "NAV_LOCK_SCREEN",
                requiresAccessibility = true,
                spokenResponseHindi = "Screen lock kar raha hoon.",
                spokenResponseEnglish = "Locking screen."
            )
        }

        if (matchesAny(lower, "scroll down", "niche scroll karo", "neeche scroll karo", "scroll karo", "scroll down karo")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.NAVIGATION,
                actionName = "NAV_SCROLL_DOWN",
                requiresAccessibility = true,
                spokenResponseHindi = "Neeche scroll kar raha hoon.",
                spokenResponseEnglish = "Scrolling down."
            )
        }

        if (matchesAny(lower, "scroll up", "upar scroll karo", "scroll up karo")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.NAVIGATION,
                actionName = "NAV_SCROLL_UP",
                requiresAccessibility = true,
                spokenResponseHindi = "Upar scroll kar raha hoon.",
                spokenResponseEnglish = "Scrolling up."
            )
        }

        // ==========================================
        // 2. NOTIFICATIONS READING
        // ==========================================
        if (matchesAny(lower, "notifications padho", "unread notifications batao", "read notifications", "notification padh ke batao", "kya notification hai")) {
            val filterPkg = if (lower.contains("whatsapp")) "whatsapp" else null
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.NOTIFICATIONS,
                actionName = "READ_NOTIFICATIONS",
                primaryParam = filterPkg,
                spokenResponseHindi = "Notifications check kar raha hoon.",
                spokenResponseEnglish = "Checking notifications."
            )
        }

        // ==========================================
        // 3. WHATSAPP & MESSAGES
        // ==========================================
        if (lower.contains("whatsapp") && (lower.contains("message") || lower.contains("msg") || lower.contains("bhejo") || lower.contains("likho") || lower.contains("chat"))) {
            val contactName = extractTargetContact(lower) ?: "Friend"
            val messageBody = extractMessageBody(cleanInput) ?: "Hello"
            context.lastTargetPackage = "com.whatsapp"
            context.lastContactTarget = contactName
            context.lastCategory = ActionCategory.WHATSAPP

            val intent = CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.WHATSAPP,
                actionName = "SEND_WHATSAPP_MESSAGE",
                primaryParam = contactName,
                secondaryParam = messageBody,
                targetPackage = "com.whatsapp",
                confirmationLevel = ConfirmationLevel.LEVEL_2_IMPORTANT,
                spokenResponseHindi = "$contactName ke liye WhatsApp message: '$messageBody'. Bhej doon?",
                spokenResponseEnglish = "WhatsApp message for $contactName: '$messageBody'. Should I send?"
            )
            context.pendingConfirmationAction = intent
            return intent
        }

        if (matchesAny(lower, "whatsapp kholo", "open whatsapp", "whatsapp open karo")) {
            context.lastOpenedApp = "WhatsApp"
            context.lastTargetPackage = "com.whatsapp"
            context.lastCategory = ActionCategory.APPS
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.APPS,
                actionName = "OPEN_APP",
                primaryParam = "WhatsApp",
                targetPackage = "com.whatsapp",
                spokenResponseHindi = "WhatsApp khol raha hoon.",
                spokenResponseEnglish = "Opening WhatsApp."
            )
        }

        // General SMS
        if (lower.contains("message") || lower.contains("sms") || lower.contains("bolo ") || (lower.contains("ko") && lower.contains("bhejo"))) {
            val contactName = extractTargetContact(lower) ?: "Contact"
            val messageBody = extractMessageBody(cleanInput) ?: "Hello"
            context.lastContactTarget = contactName
            context.lastCategory = ActionCategory.MESSAGES

            val intent = CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.MESSAGES,
                actionName = "SEND_SMS",
                primaryParam = contactName,
                secondaryParam = messageBody,
                confirmationLevel = ConfirmationLevel.LEVEL_2_IMPORTANT,
                spokenResponseHindi = "$contactName ko SMS: '$messageBody'. Send karoon?",
                spokenResponseEnglish = "Send SMS to $contactName: '$messageBody'?"
            )
            context.pendingConfirmationAction = intent
            return intent
        }

        // ==========================================
        // 4. PHONE CALLS & CALL CONTROL
        // ==========================================
        // Answer / Receive Call
        if (matchesAny(lower,
                "call receive karo", "call receive kar do", "call receive kar lo", "call receive",
                "call utha lo", "call uthao", "phone uthao", "phone utha lo",
                "answer call", "answer the call", "receive call", "receive the call",
                "pick up call", "call pick karo", "call pick kar lo", "call pick up karo",
                "call accept karo", "accept call", "accept the call",
                "max call receive karo", "max call utha lo", "max call answer karo", "max call receive kar do"
            )) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CALLS,
                actionName = "ANSWER_CALL",
                confirmationLevel = ConfirmationLevel.LEVEL_1_SAFE,
                spokenResponseHindi = "Ji, call receive kar di.",
                spokenResponseEnglish = "Call answered."
            )
        }

        // Reject Incoming Call
        if (matchesAny(lower,
                "call reject karo", "call reject kar do", "call reject kar lo", "call reject",
                "reject call", "reject the call", "call decline karo", "decline call", "decline the call",
                "call mat uthao", "call mat receive karo", "call mat lo", "call mat pick karo",
                "max call reject karo", "max call reject kar do", "max call decline karo"
            )) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CALLS,
                actionName = "REJECT_CALL",
                confirmationLevel = ConfirmationLevel.LEVEL_1_SAFE,
                spokenResponseHindi = "Ji, call reject kar di.",
                spokenResponseEnglish = "Call rejected."
            )
        }

        // End / Disconnect Active Call
        if (matchesAny(lower,
                "call kaat do", "call kaat do na", "call cut kar do", "call cut karo",
                "call disconnect karo", "call disconnect kar do", "disconnect call",
                "hang up call", "hang up", "end call", "end the call",
                "phone kaat do", "phone disconnect karo", "call band karo", "call band kar do",
                "max call kaat do", "max call disconnect karo"
            )) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CALLS,
                actionName = "END_CALL",
                confirmationLevel = ConfirmationLevel.LEVEL_1_SAFE,
                spokenResponseHindi = "Call disconnect kar di.",
                spokenResponseEnglish = "Call ended."
            )
        }

        // Speaker ON / Loudspeaker ON
        if (matchesAny(lower,
                "speaker on karo", "speaker on kar do", "speaker chalu karo", "speaker chala do",
                "speakerphone on karo", "speakerphone on kar do", "turn on speaker", "turn on speakerphone",
                "enable speaker", "loudspeaker on karo", "loudspeaker chalu karo", "loudspeaker on kar do",
                "speaker on", "loudspeaker on"
            )) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.AUDIO_VOLUME,
                actionName = "SPEAKER_ON",
                confirmationLevel = ConfirmationLevel.LEVEL_1_SAFE,
                spokenResponseHindi = "Speaker on kar diya.",
                spokenResponseEnglish = "Speaker turned on."
            )
        }

        // Speaker OFF / Loudspeaker OFF
        if (matchesAny(lower,
                "speaker off karo", "speaker off kar do", "speaker band karo", "speaker rok do",
                "speakerphone off karo", "speakerphone off kar do", "turn off speaker", "turn off speakerphone",
                "disable speaker", "loudspeaker off karo", "loudspeaker band karo", "loudspeaker off kar do",
                "speaker off", "loudspeaker off"
            )) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.AUDIO_VOLUME,
                actionName = "SPEAKER_OFF",
                confirmationLevel = ConfirmationLevel.LEVEL_1_SAFE,
                spokenResponseHindi = "Speaker off kar diya.",
                spokenResponseEnglish = "Speaker turned off."
            )
        }

        // Making Outgoing Calls
        if (matchesAny(lower, "call", "phone karo", "call lagao", "call laga do", "phone lagao", "dial karo")) {
            if (matchesAny(lower, "last caller ko call karo", "last call", "redial", "pichle number pe call karo")) {
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.CALLS,
                    actionName = "CALL_LAST_CALLER",
                    confirmationLevel = ConfirmationLevel.LEVEL_2_IMPORTANT,
                    spokenResponseHindi = "Last caller ko call lagane ke liye confirmation chahiye.",
                    spokenResponseEnglish = "Confirmation required to call last caller."
                )
            }

            val contactName = extractTargetContact(lower) ?: "Unknown"
            context.lastContactTarget = contactName
            context.lastCategory = ActionCategory.CALLS

            val intent = CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CALLS,
                actionName = "MAKE_CALL",
                primaryParam = contactName,
                confirmationLevel = ConfirmationLevel.LEVEL_2_IMPORTANT,
                spokenResponseHindi = "$contactName ko call laga raha hoon.",
                spokenResponseEnglish = "Calling $contactName."
            )
            context.pendingConfirmationAction = intent
            return intent
        }

        // ==========================================
        // 5. ALARMS AND TIMERS
        // ==========================================
        if (lower.contains("alarm")) {
            if (matchesAny(lower, "alarm band karo", "cancel alarm", "dismiss alarm")) {
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.ALARM_TIMER,
                    actionName = "DISMISS_ALARM",
                    spokenResponseHindi = "Alarm band karne ke liye Clock app khol raha hoon.",
                    spokenResponseEnglish = "Opening Clock to dismiss alarm."
                )
            }

            if (matchesAny(lower, "saare alarms dikhao", "show alarms", "show all alarms", "alarms dikhao")) {
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.ALARM_TIMER,
                    actionName = "SHOW_ALARMS",
                    spokenResponseHindi = "Saare alarms dikha raha hoon.",
                    spokenResponseEnglish = "Showing alarms."
                )
            }

            val (hour, minute, amPm) = extractTime(lower)
            val parsedHour = if (amPm.equals("PM", ignoreCase = true) && hour < 12) hour + 12 else if (amPm.equals("AM", ignoreCase = true) && hour == 12) 0 else hour
            val timeDisplay = String.format(Locale.ROOT, "%02d:%02d %s", if (hour == 0) 12 else if (hour > 12) hour - 12 else hour, minute, amPm)

            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.ALARM_TIMER,
                actionName = "SET_ALARM",
                primaryParam = "$parsedHour:$minute",
                secondaryParam = "MAX Alarm",
                numericValue = parsedHour,
                spokenResponseHindi = "$timeDisplay ka alarm set kar diya.",
                spokenResponseEnglish = "Alarm set for $timeDisplay."
            )
        }

        if (lower.contains("timer")) {
            val minutes = extractNumber(lower) ?: 5
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.ALARM_TIMER,
                actionName = "SET_TIMER",
                numericValue = minutes,
                spokenResponseHindi = "$minutes minute ka timer shuru kar diya.",
                spokenResponseEnglish = "Timer set for $minutes minutes."
            )
        }

        if (lower.contains("reminder") || lower.contains("yaad dilana")) {
            val reminderText = extractReminderText(cleanInput)
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.REMINDER,
                actionName = "SET_REMINDER",
                primaryParam = reminderText,
                spokenResponseHindi = "Reminder set ho gaya: '$reminderText'.",
                spokenResponseEnglish = "Reminder set for '$reminderText'."
            )
        }

        // ==========================================
        // 6. AUDIO & VOLUME CONTROL
        // ==========================================
        if (lower.contains("volume") || lower.contains("aawaz") || lower.contains("awaz") || lower.contains("silent") || lower.contains("vibrate")) {
            if (matchesAny(lower, "silent mode on", "silent karo", "phone silent kar do", "mute karo")) {
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.AUDIO_VOLUME,
                    actionName = "SET_RINGER_SILENT",
                    spokenResponseHindi = "Silent mode on kar diya.",
                    spokenResponseEnglish = "Silent mode enabled."
                )
            }

            if (matchesAny(lower, "vibrate mode on", "vibrate karo", "phone vibrate kar do")) {
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.AUDIO_VOLUME,
                    actionName = "SET_RINGER_VIBRATE",
                    spokenResponseHindi = "Vibrate mode on kar diya.",
                    spokenResponseEnglish = "Vibrate mode enabled."
                )
            }

            if (matchesAny(lower, "volume badhao", "volume up", "increase volume", "aawaz badhao")) {
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.AUDIO_VOLUME,
                    actionName = "VOLUME_UP",
                    spokenResponseHindi = "Volume badha diya.",
                    spokenResponseEnglish = "Volume increased."
                )
            }

            if (matchesAny(lower, "volume kam karo", "volume down", "decrease volume", "aawaz kam karo")) {
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.AUDIO_VOLUME,
                    actionName = "VOLUME_DOWN",
                    spokenResponseHindi = "Volume kam kar diya.",
                    spokenResponseEnglish = "Volume decreased."
                )
            }

            val percent = extractNumber(lower)
            if (percent != null) {
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.AUDIO_VOLUME,
                    actionName = "SET_VOLUME_PERCENT",
                    numericValue = percent.coerceIn(0, 100),
                    spokenResponseHindi = "Volume $percent percent set kar diya.",
                    spokenResponseEnglish = "Volume set to $percent percent."
                )
            }
        }

        // ==========================================
        // 7. CONNECTIVITY & SYSTEM SETTINGS
        // ==========================================
        if (matchesAny(lower, "wifi on kar", "wifi settings kholo", "wifi open karo", "wifi band karo", "wifi settings", "wifi")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CONNECTIVITY_SETTINGS,
                actionName = "OPEN_WIFI_SETTINGS",
                spokenResponseHindi = "WiFi settings khol raha hoon.",
                spokenResponseEnglish = "Opening WiFi settings."
            )
        }

        if (matchesAny(lower, "bluetooth band karo", "bluetooth settings kholo", "bluetooth on karo", "bluetooth open karo", "bluetooth settings", "bluetooth")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CONNECTIVITY_SETTINGS,
                actionName = "OPEN_BLUETOOTH_SETTINGS",
                spokenResponseHindi = "Bluetooth settings khol raha hoon.",
                spokenResponseEnglish = "Opening Bluetooth settings."
            )
        }

        if (matchesAny(lower, "airplane mode settings kholo", "flight mode settings", "airplane mode kholo", "airplane mode")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CONNECTIVITY_SETTINGS,
                actionName = "OPEN_AIRPLANE_SETTINGS",
                spokenResponseHindi = "Airplane mode settings khol raha hoon.",
                spokenResponseEnglish = "Opening Airplane mode settings."
            )
        }

        if (matchesAny(lower, "mobile network settings kholo", "network settings kholo", "data settings kholo", "mobile data")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CONNECTIVITY_SETTINGS,
                actionName = "OPEN_NETWORK_SETTINGS",
                spokenResponseHindi = "Mobile network settings khol raha hoon.",
                spokenResponseEnglish = "Opening Mobile network settings."
            )
        }

        if (matchesAny(lower, "hotspot settings kholo", "hotspot open karo", "hotspot")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CONNECTIVITY_SETTINGS,
                actionName = "OPEN_HOTSPOT_SETTINGS",
                spokenResponseHindi = "Hotspot settings khol raha hoon.",
                spokenResponseEnglish = "Opening Hotspot settings."
            )
        }

        if (matchesAny(lower, "display settings kholo", "brightness settings kholo", "display settings")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CONNECTIVITY_SETTINGS,
                actionName = "OPEN_DISPLAY_SETTINGS",
                spokenResponseHindi = "Display settings khol raha hoon.",
                spokenResponseEnglish = "Opening Display settings."
            )
        }

        if (matchesAny(lower, "settings me jao", "settings kholo", "open settings", "settings")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CONNECTIVITY_SETTINGS,
                actionName = "OPEN_SETTINGS",
                spokenResponseHindi = "Settings khol raha hoon.",
                spokenResponseEnglish = "Opening Settings."
            )
        }

        // ==========================================
        // 8. CAMERA
        // ==========================================
        if (lower.contains("camera") || lower.contains("photo") || lower.contains("video")) {
            if (matchesAny(lower, "video mode kholo", "video record karo", "video camera kholo")) {
                return CommandIntent(
                    rawQuery = cleanInput,
                    category = ActionCategory.CAMERA,
                    actionName = "OPEN_VIDEO_CAMERA",
                    spokenResponseHindi = "Video camera open kar raha hoon.",
                    spokenResponseEnglish = "Opening video camera."
                )
            }

            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.CAMERA,
                actionName = "OPEN_CAMERA",
                spokenResponseHindi = "Camera khol raha hoon.",
                spokenResponseEnglish = "Opening Camera."
            )
        }

        // ==========================================
        // 9. MAPS AND NAVIGATION
        // ==========================================
        if (matchesAny(lower, "maps kholo", "google maps kholo", "open maps", "map kholo")) {
            context.lastOpenedApp = "Maps"
            context.lastTargetPackage = "com.google.android.apps.maps"
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.MAPS_LOCATION,
                actionName = "OPEN_MAPS",
                targetPackage = "com.google.android.apps.maps",
                spokenResponseHindi = "Google Maps khol raha hoon.",
                spokenResponseEnglish = "Opening Google Maps."
            )
        }

        if (lower.contains("ka map") || lower.contains("ka route") || lower.contains("route dikhao") || lower.contains("directions to") || lower.contains("nearest")) {
            val destination = extractLocationTarget(cleanInput)
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.MAPS_LOCATION,
                actionName = "NAVIGATE_TO",
                primaryParam = destination,
                targetPackage = "com.google.android.apps.maps",
                spokenResponseHindi = "$destination ka route Google Maps par dikha raha hoon.",
                spokenResponseEnglish = "Showing route to $destination."
            )
        }

        // ==========================================
        // 10. WEB & SEARCH ENGINES
        // ==========================================
        if (lower.contains("chrome") && lower.contains("youtube")) {
            context.lastOpenedApp = "YouTube"
            context.lastTargetPackage = "com.google.android.youtube"
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.WEB_SEARCH,
                actionName = "SEARCH_YOUTUBE",
                primaryParam = "",
                targetPackage = "com.google.android.youtube",
                spokenResponseHindi = "YouTube khol raha hoon.",
                spokenResponseEnglish = "Opening YouTube."
            )
        }

        if (lower.contains("youtube par search karo") || lower.contains("youtube pe search karo") || (lower.contains("youtube") && lower.contains("search"))) {
            val query = extractSearchQuery(cleanInput.replace("youtube", "", ignoreCase = true))
            context.lastOpenedApp = "YouTube"
            context.lastTargetPackage = "com.google.android.youtube"
            context.lastSearchedQuery = query
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.WEB_SEARCH,
                actionName = "SEARCH_YOUTUBE",
                primaryParam = query,
                targetPackage = "com.google.android.youtube",
                spokenResponseHindi = "YouTube par $query search kar raha hoon.",
                spokenResponseEnglish = "Searching $query on YouTube."
            )
        }

        if (lower.contains("google par search karo") || lower.contains("google search karo") || lower.contains("search karo") || lower.contains("search on google")) {
            val query = extractSearchQuery(cleanInput)
            context.lastOpenedApp = "Chrome"
            context.lastTargetPackage = "com.android.chrome"
            context.lastSearchedQuery = query
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.WEB_SEARCH,
                actionName = "SEARCH_GOOGLE",
                primaryParam = query,
                targetPackage = "com.android.chrome",
                spokenResponseHindi = "Google par $query search kar raha hoon.",
                spokenResponseEnglish = "Searching $query on Google."
            )
        }

        // ==========================================
        // 11. FILES AND DOWNLOADS
        // ==========================================
        if (matchesAny(lower, "downloads folder kholo", "downloads kholo", "open downloads", "recent files dikhao", "file manager kholo", "files kholo")) {
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.FILES,
                actionName = "OPEN_DOWNLOADS",
                spokenResponseHindi = "Downloads folder khol raha hoon.",
                spokenResponseEnglish = "Opening Downloads."
            )
        }

        if (matchesAny(lower, "file delete kar do", "delete this file", "delete karo", "saare files delete karo")) {
            val intent = CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.FILES,
                actionName = "DELETE_FILE",
                confirmationLevel = ConfirmationLevel.LEVEL_3_HIGH_RISK,
                spokenResponseHindi = "File delete karne se pehle confirmation zaroori hai. Kya main delete kar doon?",
                spokenResponseEnglish = "Confirmation required before deleting file. Delete?"
            )
            context.pendingConfirmationAction = intent
            return intent
        }

        // ==========================================
        // 12. GENERAL APP LAUNCHING
        // ==========================================
        val appName = extractAppName(lower)
        if (appName != null) {
            context.lastOpenedApp = appName
            context.lastCategory = ActionCategory.APPS
            return CommandIntent(
                rawQuery = cleanInput,
                category = ActionCategory.APPS,
                actionName = "OPEN_APP",
                primaryParam = appName,
                spokenResponseHindi = "$appName khol raha hoon.",
                spokenResponseEnglish = "Opening $appName."
            )
        }

        // Fallback or Unknown
        return CommandIntent(
            rawQuery = cleanInput,
            category = ActionCategory.UNKNOWN,
            actionName = "UNKNOWN_COMMAND",
            spokenResponseHindi = "Mujhe command clear nahi samajh aayi. Dobara boliye.",
            spokenResponseEnglish = "I did not clearly understand the command. Please repeat."
        )
    }

    private fun matchesAny(text: String, vararg targets: String): Boolean {
        return targets.any { text.contains(it, ignoreCase = true) }
    }

    private fun isAffirmative(text: String): Boolean {
        return text.contains("haan") || text.contains("yes") || text.contains("bhej do") ||
                text.contains("kar do") || text.contains("send") || text.contains("proceed") ||
                text.contains("sure") || text.contains("ok") || text.contains("confirm") ||
                text.contains("call lagao") || text.contains("ha")
    }

    private fun isNegative(text: String): Boolean {
        return text.contains("nahi") || text.contains("no") || text.contains("cancel") ||
                text.contains("mat karo") || text.contains("stop") || text.contains("don't") ||
                text.contains("rehne do")
    }

    private fun hasMultiStep(text: String): Boolean {
        return text.contains(" aur ") || text.contains(" and ") || text.contains(" phir ") || text.contains(" then ")
    }

    private fun splitMultiStep(text: String): List<String> {
        val delimiters = arrayOf(" aur ", " and ", " phir ", " then ", ", ")
        var list = listOf(text)
        for (delimiter in delimiters) {
            list = list.flatMap { it.split(delimiter) }
        }
        return list.map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun extractAppName(text: String): String? {
        val appMap = mapOf(
            "youtube" to "YouTube",
            "chrome" to "Chrome",
            "browser" to "Chrome",
            "instagram" to "Instagram",
            "whatsapp" to "WhatsApp",
            "maps" to "Google Maps",
            "google maps" to "Google Maps",
            "calculator" to "Calculator",
            "camera" to "Camera",
            "gallery" to "Gallery",
            "photos" to "Google Photos",
            "clock" to "Clock",
            "contacts" to "Contacts",
            "dialer" to "Phone",
            "phone" to "Phone",
            "settings" to "Settings",
            "file manager" to "Files",
            "files" to "Files",
            "gmail" to "Gmail",
            "email" to "Gmail",
            "play store" to "Google Play Store"
        )
        for ((key, value) in appMap) {
            if (text.contains(key)) {
                return value
            }
        }
        return null
    }

    private fun extractTargetContact(text: String): String? {
        val common = listOf("papa", "mummy", "mother", "father", "rahul", "amit", "priya", "bhai", "sister", "boss", "doctor", "driver")
        for (c in common) {
            if (text.contains(c)) return c.replaceFirstChar { it.uppercase() }
        }

        if (text.contains(" ko ")) {
            val parts = text.substringBefore(" ko ").trim().split(" ")
            val lastWord = parts.lastOrNull()
            if (!lastWord.isNullOrBlank() && !listOf("aur", "and", "whatsapp", "par").contains(lastWord)) {
                return lastWord.replaceFirstChar { it.uppercase() }
            }
        }
        return null
    }

    private fun extractMessageBody(text: String): String? {
        if (text.contains(":")) {
            return text.substring(text.indexOf(":") + 1).trim()
        }
        val lower = text.lowercase(Locale.ROOT)
        if (lower.contains("bolo")) {
            val idx = lower.indexOf("bolo") + 4
            return text.substring(idx).trim()
        }
        if (lower.contains("likho")) {
            val idx = lower.indexOf("likho") + 5
            return text.substring(idx).trim()
        }
        if (lower.contains("ki ")) {
            val idx = lower.indexOf("ki ") + 3
            return text.substring(idx).trim()
        }
        return null
    }

    private fun extractSearchQuery(text: String): String {
        return text
            .replace("google par search karo", "", ignoreCase = true)
            .replace("search on google", "", ignoreCase = true)
            .replace("search karo", "", ignoreCase = true)
            .replace("search", "", ignoreCase = true)
            .replace("latest news search karo", "latest news", ignoreCase = true)
            .replace("chrome me", "", ignoreCase = true)
            .replace("aur", "", ignoreCase = true)
            .replace("ab", "", ignoreCase = true)
            .replace("'", "")
            .replace("\"", "")
            .trim()
    }

    private fun extractLocationTarget(text: String): String {
        return text
            .replace("ka map kholo", "", ignoreCase = true)
            .replace("ka map", "", ignoreCase = true)
            .replace("ka route dikhao", "", ignoreCase = true)
            .replace("ka route", "", ignoreCase = true)
            .replace("google maps kholo", "", ignoreCase = true)
            .replace("route dikhao", "", ignoreCase = true)
            .replace("directions to", "", ignoreCase = true)
            .trim()
    }

    private fun extractReminderText(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        return if (lower.contains("ki ")) {
            val idx = lower.indexOf("ki ") + 3
            text.substring(idx).trim()
        } else if (lower.contains("remind me to")) {
            val idx = lower.indexOf("remind me to") + 12
            text.substring(idx).trim()
        } else {
            text
        }
    }

    private fun extractNumber(text: String): Int? {
        val matcher = Pattern.compile("(\\d+)").matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.toIntOrNull()
        } else null
    }

    private fun extractTime(text: String): Triple<Int, Int, String> {
        var hour = 7
        var minute = 0
        var amPm = if (text.contains("subah") || text.contains("morning") || text.contains("am")) "AM"
        else if (text.contains("shaam") || text.contains("evening") || text.contains("raat") || text.contains("night") || text.contains("pm")) "PM"
        else "AM"

        val colonMatcher = Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(text)
        if (colonMatcher.find()) {
            hour = colonMatcher.group(1)?.toIntOrNull() ?: 7
            minute = colonMatcher.group(2)?.toIntOrNull() ?: 0
        } else {
            val hourMatcher = Pattern.compile("(\\d{1,2})\\s*(?:baje|am|pm|o'clock)?").matcher(text)
            if (hourMatcher.find()) {
                hour = hourMatcher.group(1)?.toIntOrNull() ?: 7
            }
        }
        return Triple(hour, minute, amPm)
    }
}
