package org.libreoffice.sdk

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.libreoffice.kit.DirectBufferAllocator
import org.libreoffice.kit.Document
import org.libreoffice.kit.LibreOfficeKit
import org.libreoffice.kit.Office
import java.io.File
import java.nio.ByteBuffer

/**
 * LibreOffice Android SDK - Main entry point
 *
 * Provides document conversion and rendering capabilities for:
 * - Word documents (.docx, .doc, .odt)
 * - Spreadsheets (.xlsx, .xls, .ods)
 * - Presentations (.pptx, .ppt, .odp)
 * - PDF export
 */
object LibreOfficeSDK {

    private var office: Office? = null
    private var isInitialized = false

    /**
     * Document types supported by LibreOffice
     */
    enum class DocumentType(val value: Int) {
        TEXT(Document.DOCTYPE_TEXT),
        SPREADSHEET(Document.DOCTYPE_SPREADSHEET),
        PRESENTATION(Document.DOCTYPE_PRESENTATION),
        DRAWING(Document.DOCTYPE_DRAWING),
        OTHER(Document.DOCTYPE_OTHER)
    }

    /**
     * Export formats supported
     */
    enum class ExportFormat(val extension: String, val filter: String) {
        PDF("pdf", "pdf"),
        DOCX("docx", "MS Word 2007 XML"),
        XLSX("xlsx", "Calc MS Excel 2007 XML"),
        PPTX("pptx", "Impress MS PowerPoint 2007 XML"),
        ODT("odt", "writer8"),
        ODS("ods", "calc8"),
        ODP("odp", "impress8"),
        PNG("png", "png"),
        JPG("jpg", "jpg")
    }

    /**
     * Initialize the SDK. Must be called before any other operations.
     * Call this from your Application class or main Activity.
     *
     * @param activity The activity context for initialization
     */
    @JvmStatic
    fun initialize(activity: Activity) {
        if (isInitialized) return

        LibreOfficeKit.init(activity)
        val handle = LibreOfficeKit.getLibreOfficeKitHandle()
        office = Office(handle)
        isInitialized = true
    }

    /**
     * Check if SDK is initialized
     */
    @JvmStatic
    fun isReady(): Boolean = isInitialized

    /**
     * Open a document for processing
     *
     * @param filePath Absolute path to the document
     * @return LODocument wrapper or null if failed
     */
    @JvmStatic
    @WorkerThread
    fun openDocument(filePath: String): LODocument? {
        checkInitialized()
        val doc = office?.documentLoad(filePath) ?: return null
        return LODocument(doc, filePath)
    }

    /**
     * Open a document asynchronously
     */
    @JvmStatic
    suspend fun openDocumentAsync(filePath: String): LODocument? = withContext(Dispatchers.IO) {
        openDocument(filePath)
    }

    /**
     * Convert a document to another format
     *
     * @param inputPath Path to source document
     * @param outputPath Path for converted document
     * @param format Target format
     * @return true if conversion succeeded
     */
    @JvmStatic
    @WorkerThread
    fun convert(inputPath: String, outputPath: String, format: ExportFormat): Boolean {
        val doc = openDocument(inputPath) ?: return false
        return try {
            doc.saveAs(outputPath, format)
            true
        } catch (e: Exception) {
            false
        } finally {
            doc.close()
        }
    }

    /**
     * Convert document asynchronously
     */
    @JvmStatic
    suspend fun convertAsync(inputPath: String, outputPath: String, format: ExportFormat): Boolean =
        withContext(Dispatchers.IO) {
            convert(inputPath, outputPath, format)
        }

    /**
     * Quick convert to PDF
     */
    @JvmStatic
    @WorkerThread
    fun convertToPdf(inputPath: String, outputPath: String): Boolean {
        return convert(inputPath, outputPath, ExportFormat.PDF)
    }

    /**
     * Get last error message
     */
    @JvmStatic
    fun getLastError(): String? = office?.error

    /**
     * Clean up resources. Call when done with the SDK.
     */
    @JvmStatic
    fun destroy() {
        office?.destroy()
        office = null
        isInitialized = false
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("LibreOfficeSDK not initialized. Call initialize() first.")
        }
    }
}

/**
 * Wrapper for a loaded LibreOffice document
 */
class LODocument internal constructor(
    private val document: Document,
    val filePath: String
) {

    /**
     * Get document type
     */
    val type: LibreOfficeSDK.DocumentType
        get() = LibreOfficeSDK.DocumentType.entries.find {
            it.value == document.documentType
        } ?: LibreOfficeSDK.DocumentType.OTHER

    /**
     * Get document width in twips
     */
    val width: Long
        get() = document.documentWidth

    /**
     * Get document height in twips
     */
    val height: Long
        get() = document.documentHeight

    /**
     * Get number of pages/sheets/slides
     */
    val pageCount: Int
        get() = document.parts

    /**
     * Get current page index
     */
    var currentPage: Int
        get() = document.part
        set(value) { document.part = value }

    /**
     * Get page name
     */
    fun getPageName(index: Int): String = document.getPartName(index)

    /**
     * Save document to a new format
     *
     * @param outputPath Output file path
     * @param format Export format
     */
    @WorkerThread
    fun saveAs(outputPath: String, format: LibreOfficeSDK.ExportFormat) {
        document.saveAs(outputPath, format.extension, "")
    }

    /**
     * Render a page/tile to a Bitmap
     *
     * @param width Bitmap width in pixels
     * @param height Bitmap height in pixels
     * @param pageIndex Page to render (0-indexed)
     * @return Rendered bitmap
     */
    @WorkerThread
    fun renderPage(width: Int, height: Int, pageIndex: Int = 0): Bitmap {
        document.part = pageIndex
        document.initializeForRendering()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val buffer = DirectBufferAllocator.allocate(width * height * 4)

        document.paintTile(
            buffer,
            width, height,
            0, 0,
            document.documentWidth.toInt(),
            document.documentHeight.toInt()
        )

        buffer.rewind()
        bitmap.copyPixelsFromBuffer(buffer)
        DirectBufferAllocator.free(buffer)

        return bitmap
    }

    /**
     * Render page asynchronously
     */
    suspend fun renderPageAsync(width: Int, height: Int, pageIndex: Int = 0): Bitmap =
        withContext(Dispatchers.IO) {
            renderPage(width, height, pageIndex)
        }

    /**
     * Get selected text
     */
    fun getSelectedText(): String? = document.getTextSelection("text/plain")

    /**
     * Execute a UNO command
     */
    fun executeCommand(command: String, args: String = "") {
        document.postUnoCommand(command, args, false)
    }

    /**
     * Close the document and release resources
     */
    fun close() {
        document.destroy()
    }
}
