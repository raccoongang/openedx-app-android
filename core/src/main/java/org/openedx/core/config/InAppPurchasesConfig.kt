package org.openedx.core.config

import com.google.gson.annotations.SerializedName

data class InAppPurchasesConfig(
    @SerializedName("ENABLED")
    val isEnabled: Boolean = false,
)

