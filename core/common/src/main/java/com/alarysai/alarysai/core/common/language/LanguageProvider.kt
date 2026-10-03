package com.alarysai.alarysai.core.common.language

import java.util.Locale
import javax.inject.Inject

/** Language used to resolve [LocalizedText] on screen. */
interface LanguageProvider {
    fun currentLanguage(): Language
}

/**
 * Uses the device language. Once the user profile exists, its `language` field
 * should take precedence over this.
 */
class DeviceLanguageProvider @Inject constructor() : LanguageProvider {
    override fun currentLanguage(): Language = Language.fromCode(Locale.getDefault().language)
}
