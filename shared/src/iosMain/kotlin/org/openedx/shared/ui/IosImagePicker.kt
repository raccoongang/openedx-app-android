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

            val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
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

    picker.delegate = delegate

    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootVC?.presentViewController(picker, animated = true, completion = null)
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
