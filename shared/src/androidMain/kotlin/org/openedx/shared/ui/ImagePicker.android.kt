package org.openedx.shared.ui

/**
 * Android stub — image picking is handled via ActivityResult in the Activity layer,
 * not through this commonMain interface. This actual exists only to satisfy the
 * expect declaration for compilation.
 */
actual fun showImagePicker(onImageSelected: (ByteArray, String) -> Unit) {
    // Android uses ActivityResult launcher from Fragment/Activity — not called from here
}
