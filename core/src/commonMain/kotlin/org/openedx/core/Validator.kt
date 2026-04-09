package org.openedx.core

class Validator {

    fun isEmailOrUserNameValid(input: String): Boolean {
        return if (input.contains("@")) {
            val validEmailAddressRegex = Regex(
                "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                RegexOption.IGNORE_CASE
            )
            validEmailAddressRegex.containsMatchIn(input)
        } else {
            input.isNotBlank() && input.contains(" ").not()
        }
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 2
    }
}
