package org.openedx.core.domain.helper

import org.openedx.core.domain.model.Block
import org.openedx.core.utils.VideoPreview

/**
 * Helper class for handling video preview generation.
 * Platform-specific implementations handle the actual preview retrieval.
 */
expect class VideoPreviewHelper {

    /**
     * Gets video preview for a single block
     * @param block The block to get video preview for
     * @param offlineUrl Optional offline URL for the video
     * @return VideoPreview object or null if no preview available
     */
    fun getVideoPreview(block: Block, offlineUrl: String? = null): VideoPreview?

    /**
     * Gets video previews for multiple blocks
     * @param blocks List of blocks to get video previews for
     * @param offlineUrls Optional map of block IDs to offline URLs
     * @return Map of block IDs to VideoPreview objects
     */
    fun getVideoPreviews(
        blocks: List<Block>,
        offlineUrls: Map<String, String>? = null,
    ): Map<String, VideoPreview?>

    /**
     * Gets video preview for a single block with a specific offline URL
     * @param blockId The ID of the block
     * @param block The block to get video preview for
     * @param offlineUrl Optional offline URL for the video
     * @return Pair of block ID and VideoPreview object or null
     */
    fun getVideoPreviewWithId(
        blockId: String,
        block: Block,
        offlineUrl: String? = null,
    ): Pair<String, VideoPreview?>
}
