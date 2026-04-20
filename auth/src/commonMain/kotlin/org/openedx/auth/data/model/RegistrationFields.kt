package org.openedx.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.RegistrationField
import org.openedx.core.domain.model.RegistrationFieldType

@Serializable
data class RegistrationFields(
    @SerialName("fields")
    val fields: List<Field>? = null,
) {

    @Serializable
    data class Field(
        @SerialName("name")
        val name: String? = null,
        @SerialName("label")
        val label: String? = null,
        @SerialName("type")
        val type: String? = null,
        @SerialName("placeholder")
        val placeholder: String? = null,
        @SerialName("instructions")
        val instructions: String? = null,
        @SerialName("exposed")
        val exposed: Boolean? = null,
        @SerialName("required")
        val required: Boolean? = null,
        @SerialName("restrictions")
        val restrictions: Restrictions? = null,
        @SerialName("options")
        val options: List<Option>? = null
    ) {
        fun mapToDomain(): RegistrationField {
            return RegistrationField(
                name = name ?: "",
                label = label ?: "",
                type = RegistrationFieldType.returnLocalTypeFromServerType(type),
                placeholder = placeholder ?: "",
                instructions = instructions ?: "",
                exposed = exposed ?: false,
                required = required ?: false,
                restrictions = restrictions?.mapToDomain() ?: RegistrationField.Restrictions(),
                options = options?.map { it.mapToDomain() } ?: emptyList()
            )
        }
    }

    @Serializable
    data class Restrictions(
        @SerialName("max_length")
        val maxLength: Int? = null,
        @SerialName("min_length")
        val minLength: Int? = null
    ) {
        fun mapToDomain(): RegistrationField.Restrictions {
            return RegistrationField.Restrictions(
                maxLength = maxLength ?: 128,
                minLength = minLength ?: 1
            )
        }
    }

    @Serializable
    data class Option(
        @SerialName("value")
        val value: String? = null,
        @SerialName("name")
        val name: String? = null,
        @SerialName("default")
        val default: String? = null
    ) {
        fun mapToDomain(): RegistrationField.Option {
            return RegistrationField.Option(
                value = value ?: "",
                name = name ?: "",
                default = default ?: ""
            )
        }
    }
}
