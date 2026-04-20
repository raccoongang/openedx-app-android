package org.openedx.shared.ui

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.CoreGraphics.CGSizeMake
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

private var currentDelegate: NSObject? = null

private const val MAX_IMAGE_WIDTH = 500.0
private const val JPEG_QUALITY = 0.9

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.downscaleIfNeeded(maxWidth: Double): UIImage {
    val (width, height) = size.useContents { width to height }
    if (width <= maxWidth) return this
    val ratio = maxWidth / width
    val newWidth = maxWidth
    val newHeight = height * ratio
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(newWidth, newHeight), false, 1.0)
    drawInRect(CGRectMake(0.0, 0.0, newWidth, newHeight))
    val resized = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return resized ?: this
}

actual fun showImagePicker(onImageSelected: (ByteArray, String, String) -> Unit) {
    val config = PHPickerConfiguration().apply {
        filter = PHPickerFilter.imagesFilter
        selectionLimit = 1
    }

    val picker = PHPickerViewController(configuration = config)

    val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
        override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
            picker.dismissViewControllerAnimated(true, null)

            val result = didFinishPicking.firstOrNull() as? PHPickerResult
            if (result == null) {
                currentDelegate = null
                return
            }

            val provider = result.itemProvider
            provider.loadDataRepresentationForTypeIdentifier(
                typeIdentifier = UTTypeImage.identifier
            ) { data, error ->
                currentDelegate = null
                if (error != null || data == null) return@loadDataRepresentationForTypeIdentifier
                val originalImage = UIImage.imageWithData(data) ?: return@loadDataRepresentationForTypeIdentifier
                val image = originalImage.downscaleIfNeeded(MAX_IMAGE_WIDTH)
                val jpegData = UIImageJPEGRepresentation(image, JPEG_QUALITY) ?: return@loadDataRepresentationForTypeIdentifier
                val bytes = jpegData.toByteArray()

                // Save to temp file for preview display
                val fileName = "${NSUUID().UUIDString}.jpg"
                val tempPath = NSTemporaryDirectory() + fileName
                jpegData.writeToFile(tempPath, true)
                val previewUri = "file://$tempPath"

                dispatch_async(dispatch_get_main_queue()) {
                    onImageSelected(bytes, "jpg", previewUri)
                }
            }
        }
    }

    currentDelegate = delegate
    picker.delegate = delegate

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
