package org.openedx.core.utils

import org.openedx.core.domain.model.RegistrationField
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.currentLocale
import platform.Foundation.ISOCountryCodes
import platform.Foundation.ISOLanguageCodes

private val englishLocale = NSLocale("en")

actual object LocaleUtils {

    private const val MIN_USER_AGE = 13

    actual fun getBirthYearsRange(): List<RegistrationField.Option> {
        val calendar = platform.Foundation.NSCalendar.currentCalendar
        val currentYear = calendar.component(
            platform.Foundation.NSCalendarUnitYear,
            fromDate = platform.Foundation.NSDate()
        ).toInt()
        return (currentYear - 120..currentYear).reversed().map {
            RegistrationField.Option(it.toString(), it.toString(), "")
        }
    }

    actual fun isProfileLimited(inputYear: String?): Boolean {
        if (inputYear.isNullOrEmpty()) return true
        val calendar = platform.Foundation.NSCalendar.currentCalendar
        val currentYear = calendar.component(
            platform.Foundation.NSCalendarUnitYear,
            fromDate = platform.Foundation.NSDate()
        ).toInt()
        return currentYear - inputYear.toInt() < MIN_USER_AGE
    }

    actual fun getCountries(): List<RegistrationField.Option> {
        return NSLocale.ISOCountryCodes.mapNotNull { code ->
            val countryCode = code as String
            val name = englishLocale.displayNameForKey(NSLocaleCountryCode, countryCode)
            if (name != null) RegistrationField.Option(countryCode, name, "") else null
        }.sortedBy { it.name }
    }

    actual fun getLanguages(): List<RegistrationField.Option> {
        return NSLocale.ISOLanguageCodes
            .mapNotNull { code ->
                val langCode = code as String
                if (langCode.length != 2) return@mapNotNull null
                val name = englishLocale.displayNameForKey(NSLocaleLanguageCode, langCode)
                if (name != null) RegistrationField.Option(langCode, name, "") else null
            }.sortedBy { it.name }
    }

    actual fun getLanguages(languages: List<String>): List<RegistrationField.Option> {
        return getLanguages().filter { languages.contains(it.value) }
    }

    actual fun getCountryByCountryCode(code: String): String? {
        return englishLocale.displayNameForKey(NSLocaleCountryCode, code)
    }

    actual fun getLanguageByLanguageCode(code: String): String? {
        return englishLocale.displayNameForKey(NSLocaleLanguageCode, code)
    }

    actual fun getDisplayLanguage(languageCode: String): String {
        return englishLocale.displayNameForKey(NSLocaleLanguageCode, languageCode) ?: languageCode
    }
}
