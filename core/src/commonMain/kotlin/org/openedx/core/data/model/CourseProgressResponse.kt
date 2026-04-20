package org.openedx.core.data.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.room.CertificateDataDb
import org.openedx.core.data.model.room.CompletionSummaryDb
import org.openedx.core.data.model.room.CourseGradeDb
import org.openedx.core.data.model.room.CourseProgressEntity
import org.openedx.core.data.model.room.GradingPolicyDb
import org.openedx.core.data.model.room.SectionScoreDb
import org.openedx.core.data.model.room.VerificationDataDb
import org.openedx.core.domain.model.CourseProgress

@Serializable
data class CourseProgressResponse(
    @SerialName("verified_mode") val verifiedMode: String? = null,
    @SerialName("access_expiration") val accessExpiration: String? = null,
    @SerialName("certificate_data") val certificateData: CertificateData? = null,
    @SerialName("completion_summary") val completionSummary: CompletionSummary? = null,
    @SerialName("course_grade") val courseGrade: CourseGrade? = null,
    @SerialName("credit_course_requirements") val creditCourseRequirements: String? = null,
    @SerialName("end") val end: String? = null,
    @SerialName("enrollment_mode") val enrollmentMode: String? = null,
    @SerialName("grading_policy") val gradingPolicy: GradingPolicy? = null,
    @SerialName("has_scheduled_content") val hasScheduledContent: Boolean? = null,
    @SerialName("section_scores") val sectionScores: List<SectionScore>? = null,
    @SerialName("studio_url") val studioUrl: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("user_has_passing_grade") val userHasPassingGrade: Boolean? = null,
    @SerialName("verification_data") val verificationData: VerificationData? = null,
    @SerialName("disable_progress_graph") val disableProgressGraph: Boolean? = null,
) {
    @Serializable
    data class CertificateData(
        @SerialName("cert_status") val certStatus: String? = null,
        @SerialName("cert_web_view_url") val certWebViewUrl: String? = null,
        @SerialName("download_url") val downloadUrl: String? = null,
        @SerialName("certificate_available_date") val certificateAvailableDate: String? = null
    ) {
        fun mapToRoomEntity() = CertificateDataDb(
            certStatus = certStatus.orEmpty(),
            certWebViewUrl = certWebViewUrl.orEmpty(),
            downloadUrl = downloadUrl.orEmpty(),
            certificateAvailableDate = certificateAvailableDate.orEmpty()
        )

        fun mapToDomain() = CourseProgress.CertificateData(
            certStatus = certStatus ?: "",
            certWebViewUrl = certWebViewUrl ?: "",
            downloadUrl = downloadUrl ?: "",
            certificateAvailableDate = certificateAvailableDate ?: ""
        )
    }

    @Serializable
    data class CompletionSummary(
        @SerialName("complete_count") val completeCount: Int? = null,
        @SerialName("incomplete_count") val incompleteCount: Int? = null,
        @SerialName("locked_count") val lockedCount: Int? = null
    ) {
        fun mapToRoomEntity() = CompletionSummaryDb(
            completeCount = completeCount ?: 0,
            incompleteCount = incompleteCount ?: 0,
            lockedCount = lockedCount ?: 0
        )

        fun mapToDomain() = CourseProgress.CompletionSummary(
            completeCount = completeCount ?: 0,
            incompleteCount = incompleteCount ?: 0,
            lockedCount = lockedCount ?: 0
        )
    }

    @Serializable
    data class CourseGrade(
        @SerialName("letter_grade") val letterGrade: String? = null,
        @SerialName("percent") val percent: Double? = null,
        @SerialName("is_passing") val isPassing: Boolean? = null
    ) {
        fun mapToRoomEntity() = CourseGradeDb(
            letterGrade = letterGrade.orEmpty(),
            percent = percent ?: 0.0,
            isPassing = isPassing ?: false
        )

        fun mapToDomain() = CourseProgress.CourseGrade(
            letterGrade = letterGrade ?: "",
            percent = percent ?: 0.0,
            isPassing = isPassing ?: false
        )
    }

    @Serializable
    data class GradingPolicy(
        @SerialName("assignment_policies") val assignmentPolicies: List<AssignmentPolicy>? = null,
        @SerialName("grade_range") val gradeRange: Map<String, Float>? = null,
        @SerialName("assignment_colors") val assignmentColors: List<String>? = null
    ) {
        // TODO Temporary solution. Backend will returns color list later
        companion object {
            val DEFAULT_COLORS = listOf(
                "#D24242",
                "#7B9645",
                "#5A5AD8",
                "#B0842C",
                "#2E90C2",
                "#D13F88",
                "#36A17D",
                "#AE5AD8",
                "#3BA03B"
            )
        }

        fun mapToRoomEntity() = GradingPolicyDb(
            assignmentPolicies = assignmentPolicies?.map { it.mapToRoomEntity() } ?: emptyList(),
            gradeRange = gradeRange ?: emptyMap(),
            assignmentColors = assignmentColors ?: DEFAULT_COLORS
        )

        fun mapToDomain() = CourseProgress.GradingPolicy(
            assignmentPolicies = assignmentPolicies?.map { it.mapToDomain() } ?: emptyList(),
            gradeRange = gradeRange ?: emptyMap(),
            assignmentColors = assignmentColors?.map { colorString ->
                Color(parseColorString(colorString))
            } ?: DEFAULT_COLORS.map { Color(parseColorString(it)) }
        )

        @Serializable
        data class AssignmentPolicy(
            @SerialName("num_droppable") val numDroppable: Double? = null,
            @SerialName("num_total") val numTotal: Double? = null,
            @SerialName("short_label") val shortLabel: String? = null,
            @SerialName("type") val type: String? = null,
            @SerialName("weight") val weight: Double? = null
        ) {
            fun mapToRoomEntity() = GradingPolicyDb.AssignmentPolicyDb(
                numDroppable = numDroppable?.toInt() ?: 0,
                numTotal = numTotal?.toInt() ?: 0,
                shortLabel = shortLabel.orEmpty(),
                type = type.orEmpty(),
                weight = weight ?: 0.0
            )

            fun mapToDomain() = CourseProgress.GradingPolicy.AssignmentPolicy(
                numDroppable = numDroppable?.toInt() ?: 0,
                numTotal = numTotal?.toInt() ?: 0,
                shortLabel = shortLabel ?: "",
                type = type ?: "",
                weight = weight ?: 0.0
            )
        }
    }

    @Serializable
    data class SectionScore(
        @SerialName("display_name") val displayName: String? = null,
        @SerialName("subsections") val subsections: List<Subsection>? = null
    ) {
        fun mapToRoomEntity() = SectionScoreDb(
            displayName = displayName.orEmpty(),
            subsections = subsections?.map { it.mapToRoomEntity() } ?: emptyList()
        )

        fun mapToDomain() = CourseProgress.SectionScore(
            displayName = displayName ?: "",
            subsections = subsections?.map { it.mapToDomain() } ?: emptyList()
        )

        @Serializable
        data class Subsection(
            @SerialName("assignment_type") val assignmentType: String? = null,
            @SerialName("block_key") val blockKey: String? = null,
            @SerialName("display_name") val displayName: String? = null,
            @SerialName("has_graded_assignment") val hasGradedAssignment: Boolean? = null,
            @SerialName("override") val override: String? = null,
            @SerialName("learner_has_access") val learnerHasAccess: Boolean? = null,
            @SerialName("num_points_earned") val numPointsEarned: Float? = null,
            @SerialName("num_points_possible") val numPointsPossible: Float? = null,
            @SerialName("percent_graded") val percentGraded: Double? = null,
            @SerialName("problem_scores") val problemScores: List<ProblemScore>? = null,
            @SerialName("show_correctness") val showCorrectness: String? = null,
            @SerialName("show_grades") val showGrades: Boolean? = null,
            @SerialName("url") val url: String? = null
        ) {
            fun mapToRoomEntity() = SectionScoreDb.SubsectionDb(
                assignmentType = assignmentType.orEmpty(),
                blockKey = blockKey.orEmpty(),
                displayName = displayName.orEmpty(),
                hasGradedAssignment = hasGradedAssignment ?: false,
                override = override.orEmpty(),
                learnerHasAccess = learnerHasAccess ?: false,
                numPointsEarned = numPointsEarned ?: 0f,
                numPointsPossible = numPointsPossible ?: 0f,
                percentGraded = percentGraded ?: 0.0,
                problemScores = problemScores?.map { it.mapToRoomEntity() } ?: emptyList(),
                showCorrectness = showCorrectness.orEmpty(),
                showGrades = showGrades ?: false,
                url = url.orEmpty()
            )

            fun mapToDomain() = CourseProgress.SectionScore.Subsection(
                assignmentType = assignmentType ?: "",
                blockKey = blockKey ?: "",
                displayName = displayName ?: "",
                hasGradedAssignment = hasGradedAssignment ?: false,
                override = override ?: "",
                learnerHasAccess = learnerHasAccess ?: false,
                numPointsEarned = numPointsEarned ?: 0f,
                numPointsPossible = numPointsPossible ?: 0f,
                percentGraded = percentGraded ?: 0.0,
                problemScores = problemScores?.map { it.mapToDomain() } ?: emptyList(),
                showCorrectness = showCorrectness ?: "",
                showGrades = showGrades ?: false,
                url = url ?: ""
            )

            @Serializable
            data class ProblemScore(
                @SerialName("earned") val earned: Double? = null,
                @SerialName("possible") val possible: Double? = null
            ) {
                fun mapToRoomEntity() = SectionScoreDb.SubsectionDb.ProblemScoreDb(
                    earned = earned ?: 0.0,
                    possible = possible ?: 0.0
                )

                fun mapToDomain() = CourseProgress.SectionScore.Subsection.ProblemScore(
                    earned = earned ?: 0.0,
                    possible = possible ?: 0.0
                )
            }
        }
    }

    @Serializable
    data class VerificationData(
        @SerialName("link") val link: String? = null,
        @SerialName("status") val status: String? = null,
        @SerialName("status_date") val statusDate: String? = null
    ) {
        fun mapToRoomEntity() = VerificationDataDb(
            link = link.orEmpty(),
            status = status.orEmpty(),
            statusDate = statusDate.orEmpty()
        )

        fun mapToDomain() = CourseProgress.VerificationData(
            link = link ?: "",
            status = status ?: "",
            statusDate = statusDate ?: ""
        )
    }

    fun mapToDomain(): CourseProgress {
        return CourseProgress(
            verifiedMode = verifiedMode ?: "",
            accessExpiration = accessExpiration ?: "",
            certificateData = certificateData?.mapToDomain(),
            completionSummary = completionSummary?.mapToDomain(),
            courseGrade = courseGrade?.mapToDomain(),
            creditCourseRequirements = creditCourseRequirements ?: "",
            end = end ?: "",
            enrollmentMode = enrollmentMode ?: "",
            gradingPolicy = gradingPolicy?.mapToDomain(),
            hasScheduledContent = hasScheduledContent ?: false,
            sectionScores = sectionScores?.map { it.mapToDomain() } ?: emptyList(),
            studioUrl = studioUrl ?: "",
            username = username ?: "",
            userHasPassingGrade = userHasPassingGrade ?: false,
            verificationData = verificationData?.mapToDomain(),
            disableProgressGraph = disableProgressGraph ?: false,
        )
    }

    fun mapToRoomEntity(courseId: String): CourseProgressEntity {
        return CourseProgressEntity(
            courseId = courseId,
            verifiedMode = verifiedMode.orEmpty(),
            accessExpiration = accessExpiration.orEmpty(),
            certificateData = certificateData?.mapToRoomEntity(),
            completionSummary = completionSummary?.mapToRoomEntity(),
            courseGrade = courseGrade?.mapToRoomEntity(),
            creditCourseRequirements = creditCourseRequirements.orEmpty(),
            end = end.orEmpty(),
            enrollmentMode = enrollmentMode.orEmpty(),
            gradingPolicy = gradingPolicy?.mapToRoomEntity(),
            hasScheduledContent = hasScheduledContent ?: false,
            sectionScores = sectionScores?.map { it.mapToRoomEntity() } ?: emptyList(),
            studioUrl = studioUrl.orEmpty(),
            username = username.orEmpty(),
            userHasPassingGrade = userHasPassingGrade ?: false,
            verificationData = verificationData?.mapToRoomEntity(),
            disableProgressGraph = disableProgressGraph ?: false,
        )
    }
}

private fun parseColorString(colorString: String): Int {
    val hex = colorString.removePrefix("#")
    val value = hex.toLong(16)
    return if (hex.length <= 6) (value or 0xFF000000).toInt() else value.toInt()
}
