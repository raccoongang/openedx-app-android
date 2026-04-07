package org.openedx.core.domain.model

import org.openedx.core.data.model.room.discovery.CourseSharingUtmParametersDb

data class CourseSharingUtmParameters(
    val facebook: String,
    val twitter: String
) {

    fun mapToEntity() = CourseSharingUtmParametersDb(
        facebook = facebook,
        twitter = twitter
    )
}
