package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.openedx.core.BlockType
import org.openedx.core.utils.InstantUtils
import org.openedx.core.domain.model.Block as DomainBlock
import org.openedx.core.domain.model.BlockCounts as DomainBlockCounts
import org.openedx.core.domain.model.EncodedVideos as DomainEncodedVideos
import org.openedx.core.domain.model.StudentViewData as DomainStudentViewData
import org.openedx.core.domain.model.VideoInfo as DomainVideoInfo

@Serializable
data class Block(
    @SerialName("id")
    val id: String? = null,
    @SerialName("block_id")
    val blockId: String? = null,
    @SerialName("lms_web_url")
    val lmsWebUrl: String? = null,
    @SerialName("legacy_web_url")
    val legacyWebUrl: String? = null,
    @SerialName("student_view_url")
    val studentViewUrl: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("graded")
    val graded: Boolean? = null,
    @SerialName("descendants")
    val descendants: List<String>? = null,
    @SerialName("student_view_data")
    val studentViewData: StudentViewData? = null,
    @SerialName("student_view_multi_device")
    val studentViewMultiDevice: Boolean? = null,
    @SerialName("block_counts")
    val blockCounts: BlockCounts? = null,
    @SerialName("completion")
    val completion: Double? = null,
    @SerialName("contains_gated_content")
    val containsGatedContent: Boolean? = null,
    @SerialName("assignment_progress")
    val assignmentProgress: AssignmentProgress? = null,
    @SerialName("due")
    val due: String? = null,
    @SerialName("offline_download")
    val offlineDownload: OfflineDownload? = null,
) {
    fun mapToDomain(blockData: Map<String, Block>): DomainBlock {
        val blockType = BlockType.getBlockType(type.orEmpty())
        val descendantsType = determineDescendantsType(blockType, blockData)

        return DomainBlock(
            id = id.orEmpty(),
            blockId = blockId.orEmpty(),
            lmsWebUrl = lmsWebUrl.orEmpty(),
            legacyWebUrl = legacyWebUrl.orEmpty(),
            studentViewUrl = studentViewUrl.orEmpty(),
            type = blockType,
            displayName = displayName.orEmpty(),
            descendants = descendants.orEmpty(),
            descendantsType = descendantsType,
            graded = graded ?: false,
            studentViewData = studentViewData?.mapToDomain(),
            studentViewMultiDevice = studentViewMultiDevice ?: false,
            blockCounts = blockCounts?.mapToDomain()!!,
            completion = completion ?: 0.0,
            containsGatedContent = containsGatedContent ?: false,
            assignmentProgress = assignmentProgress?.mapToDomain(displayName.orEmpty()),
            due = InstantUtils.iso8601ToInstant(due.orEmpty()),
            offlineDownload = offlineDownload?.mapToDomain()
        )
    }

    private fun determineDescendantsType(blockType: BlockType, blockData: Map<String, Block>): BlockType {
        if (blockType != BlockType.VERTICAL) return blockType

        val types = descendants?.map { descendant ->
            BlockType.getBlockType(blockData[descendant]?.type.orEmpty())
        }.orEmpty()

        return BlockType.sortByPriority(types).firstOrNull() ?: blockType
    }
}

@Serializable
data class StudentViewData(
    @SerialName("only_on_web")
    var onlyOnWeb: Boolean? = null,
    @SerialName("duration")
    var duration: JsonElement? = null,
    @SerialName("transcripts")
    var transcripts: HashMap<String, String>? = null,
    @SerialName("encoded_videos")
    var encodedVideos: EncodedVideos? = null,
    @SerialName("all_sources")
    var allSources: List<JsonElement>? = null,
    @SerialName("topic_id")
    val topicId: String? = null
) {
    fun mapToDomain() = DomainStudentViewData(
        onlyOnWeb = onlyOnWeb ?: false,
        duration = duration?.let {
            (it as? JsonPrimitive)?.contentOrNull ?: it.toString()
        } ?: "",
        transcripts = transcripts,
        encodedVideos = encodedVideos?.mapToDomain(),
        topicId = topicId.orEmpty()
    )
}

@Serializable
data class EncodedVideos(
    @SerialName("youtube")
    var videoInfo: VideoInfo? = null,
    @SerialName("hls")
    var hls: VideoInfo? = null,
    @SerialName("fallback")
    var fallback: VideoInfo? = null,
    @SerialName("desktop_mp4")
    var desktopMp4: VideoInfo? = null,
    @SerialName("mobile_high")
    var mobileHigh: VideoInfo? = null,
    @SerialName("mobile_low")
    var mobileLow: VideoInfo? = null
) {
    fun mapToDomain() = DomainEncodedVideos(
        youtube = videoInfo?.mapToDomain(),
        hls = hls?.mapToDomain(),
        fallback = fallback?.mapToDomain(),
        desktopMp4 = desktopMp4?.mapToDomain(),
        mobileHigh = mobileHigh?.mapToDomain(),
        mobileLow = mobileLow?.mapToDomain()
    )
}

@Serializable
data class VideoInfo(
    @SerialName("url")
    var url: String? = null,
    @SerialName("file_size")
    var fileSize: Long? = null
) {
    fun mapToDomain() = DomainVideoInfo(
        url = url
            .orEmpty()
            .trim(),
        fileSize = fileSize ?: 0
    )
}

@Serializable
data class BlockCounts(
    @SerialName("video")
    var video: Int? = null
) {
    fun mapToDomain() = DomainBlockCounts(
        video = video ?: 0
    )
}
