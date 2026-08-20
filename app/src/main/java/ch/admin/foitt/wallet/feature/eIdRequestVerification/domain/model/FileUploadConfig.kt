package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model

import io.ktor.http.ContentType

data class FileUploadConfig(
    val fileName: String,
    val contentType: ContentType,
    val serverFileName: String,
    val isMandatory: Boolean
) {
    companion object {
        val filesToUpload: List<FileUploadConfig> by lazy {
            listOf(
                FileUploadConfig(FIRST_PAGE, ContentType.Image.PNG, FIRST_PAGE, true),
                FileUploadConfig(SECOND_PAGE, ContentType.Image.PNG, SECOND_PAGE, true),
                FileUploadConfig(VIDEO, ContentType.Video.MP4, VIDEO, true),
                // gyroscope data
                FileUploadConfig(METADATA, ContentType.Application.OctetStream, METADATA, false),
                // optional unless docVideoRequired was true during case creation
                FileUploadConfig(DOCUMENT, ContentType.Video.MP4, DOCUMENT_SERVER_NAME, false),
                FileUploadConfig(MOBILE_RESULT_XML, ContentType.Application.Xml, MOBILE_RESULT_XML_SERVER_NAME, true),
                FileUploadConfig(MOBILE_RESULT_JSON, ContentType.Application.Json, MOBILE_RESULT_JSON_SERVER_NAME, true),
            )
        }

        private const val FIRST_PAGE = "fullFrameFirstPage.png"
        private const val SECOND_PAGE = "fullFrameSecondPage.png"
        private const val VIDEO = "video.mp4"
        private const val METADATA = "metadata.bin"
        private const val DOCUMENT = "docRecVideo.mp4"
        private const val DOCUMENT_SERVER_NAME = "document.mp4"
        private const val MOBILE_RESULT_JSON = "result.json"
        private const val MOBILE_RESULT_JSON_SERVER_NAME = "mobile-result.json"
        private const val MOBILE_RESULT_XML = "result.xml"
        private const val MOBILE_RESULT_XML_SERVER_NAME = "mobile-result.xml"
    }
}
