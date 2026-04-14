package org.openedx.shared.ui

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * Strong reference to the current picker delegate — prevents Kotlin/Native GC
 * from collecting it while PHPickerViewController is presented. Cleared in
 * didFinishPicking callback.
 */
private var currentDelegate: NSObject? = null

/**
 * Shows iOS PHPickerViewController to select an image from the photo library.
 * Returns the selected image as ByteArray (JPEG) via the callback.
 */
actual fun showImagePicker(onImageSelected: (ByteArray, String) -> Unit) {
    val config = PHPickerConfiguration().apply {
        filter = PHPickerFilter.imagesFilter
        selectionLimit = 1
    }

    val picker = PHPickerViewController(configuration = config)

    val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
        override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
            picker.dismissViewControllerAnimated(true, null)
            currentDelegate = null // Release strong ref

            val result = didFinishPicking.firstOrNull() as? PHPickerResult
            if (result == null) return

            val provider = result.itemProvider
            provider.loadDataRepresentationForTypeIdentifier(
                typeIdentifier = UTTypeImage.identifier
            ) { data, error ->
                if (error != null || data == null) return@loadDataRepresentationForTypeIdentifier
                val image = UIImage.imageWithData(data) ?: return@loadDataRepresentationForTypeIdentifier
                val jpegData = UIImageJPEGRepresentation(image, 0.85) ?: return@loadDataRepresentationForTypeIdentifier
                val bytes = jpegData.toByteArray()
                onImageSelected(bytes, "jpg")
            }
        }
    }

    currentDelegate = delegate // Keep strong reference
    picker.delegate = delegate

    // Find the topmost presented VC to present from
    var topVC = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (topVC?.presentedViewController != null) {
        topVC = topVC.presentedViewController
    }
    topVC?.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        memcpy(bytes.refTo(0), this.bytes, this.length)
    }
    return bytes
}
