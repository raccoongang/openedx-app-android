package org.openedx.shared.ui

/**
 * Platform-specific image picker.
 * Android: launches ActivityResult for gallery
 * iOS: shows PHPickerViewController
 */
/**
 * @param onImageSelected callback: (bytes, extension, previewUri)
 *   previewUri is a file:// URL for local display (iOS temp file, Android content URI)
 */
expect fun showImagePicker(onImageSelected: (ByteArray, String, String) -> Unit)
