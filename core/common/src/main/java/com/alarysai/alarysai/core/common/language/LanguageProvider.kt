package com.alarysai.alarysai.core.common.language

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Language used to resolve [LocalizedText] on screen. */
interface LanguageProvider {
    fun currentLanguage(): Language
}

/**
 * The language chosen in the signed-in user's profile. Set by the app when the profile loads
 * and cleared on sign-out; null means "use the device language".
 */
@Singleton
class PreferredLanguageHolder @Inject constructor() {
    @Volatile
    var language: Language? = null
}

/**
 * Profile language first (see [PreferredLanguageHolder]), then the device language.
 * Only content texts follow it; the app's own strings follow the device locale.
 */
class DeviceLanguageProvider @Inject constructor(
    private val preferred: PreferredLanguageHolder,
) : LanguageProvider {
    override fun currentLanguage(): Language =
        preferred.language ?: Language.fromCode(Locale.getDefault().language)
}
