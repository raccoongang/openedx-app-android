package org.openedx.core.utils

import org.openedx.core.domain.model.RegistrationField

expect object LocaleUtils {
    fun getBirthYearsRange(): List<RegistrationField.Option>
    fun isProfileLimited(inputYear: String?): Boolean
    fun getCountries(): List<RegistrationField.Option>
    fun getLanguages(): List<RegistrationField.Option>
    fun getLanguages(languages: List<String>): List<RegistrationField.Option>
    fun getCountryByCountryCode(code: String): String?
    fun getLanguageByLanguageCode(code: String): String?
    fun getDisplayLanguage(languageCode: String): String
}
