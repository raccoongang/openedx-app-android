package org.openedx.profile.presentation.settings

interface SettingsScreenAction {
    object AppVersionClick : SettingsScreenAction
    object LogoutClick : SettingsScreenAction
    object PrivacyPolicyClick : SettingsScreenAction
    object CookiePolicyClick : SettingsScreenAction
    object DataSellClick : SettingsScreenAction
    object FaqClick : SettingsScreenAction
    object TermsClick : SettingsScreenAction
    object SupportClick : SettingsScreenAction
    object VideoSettingsClick : SettingsScreenAction
    object ManageAccountClick : SettingsScreenAction
    object CalendarSettingsClick : SettingsScreenAction
}
