package org.openedx.shared.ui

/**
 * Platform-specific image picker.
 * Android: launches ActivityResult for gallery
 * iOS: shows PHPickerViewController
 */
expect fun showImagePicker(onImageSelected: (ByteArray, String) -> Unit)
