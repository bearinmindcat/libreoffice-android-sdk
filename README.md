# LibreOffice Android SDK

[![Release](https://jitpack.io/v/bearinmindcat/libreoffice-android-sdk.svg)](https://jitpack.io/#bearinmindcat/libreoffice-android-sdk)
[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)
[![API](https://img.shields.io/badge/API-21%2B-blue.svg)](https://android-arsenal.com/api?level=21)

A powerful document processing SDK for Android that enables offline document conversion, rendering, and manipulation. Built on LibreOffice core, it supports a wide range of office document formats.

## Features

- **Document Conversion** - Convert between formats (DOCX → PDF, XLSX → PDF, etc.)
- **Page Rendering** - Render document pages to Bitmap images
- **Text Editing** - Basic text manipulation via keyboard/mouse events
- **Text Selection** - Select, copy, and paste text
- **Multi-format Support** - DOC, DOCX, XLS, XLSX, PPT, PPTX, ODT, ODS, ODP, RTF, and more
- **Password Protected Files** - Open encrypted documents
- **No Server Required** - All processing happens on-device

## Supported Formats

| Type | Formats |
|------|---------|
| **Documents** | DOC, DOCX, ODT, RTF, TXT |
| **Spreadsheets** | XLS, XLSX, ODS, CSV |
| **Presentations** | PPT, PPTX, ODP |
| **Export** | PDF, PNG, JPG |

## Requirements

- Android API 21+ (Android 5.0 Lollipop)
- ARM64 devices only (arm64-v8a)

## Installation

Add JitPack repository to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.bearinmindcat:libreoffice-android-sdk:v0.0.1-beta")
}
```

## Quick Start

### Initialize the SDK

Initialize in your `Application` class or main `Activity`:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SDK (required before any operations)
        LibreOfficeSDK.initialize(this)
    }
}
```

### Convert Document to PDF

```kotlin
// Synchronous (call from background thread)
val success = LibreOfficeSDK.convertToPdf(
    inputPath = "/path/to/document.docx",
    outputPath = "/path/to/output.pdf"
)

// Asynchronous with coroutines
lifecycleScope.launch {
    val success = LibreOfficeSDK.convertAsync(
        inputPath = "/path/to/document.docx",
        outputPath = "/path/to/output.pdf",
        format = LibreOfficeSDK.ExportFormat.PDF
    )
}
```

### Render Document Page to Bitmap

```kotlin
lifecycleScope.launch {
    val document = LibreOfficeSDK.openDocumentAsync("/path/to/file.docx")

    document?.let { doc ->
        // Get document info
        val pageCount = doc.pageCount
        val docType = doc.type

        // Render first page
        val bitmap = doc.renderPageAsync(
            width = 1080,
            height = 1920,
            pageIndex = 0
        )

        imageView.setImageBitmap(bitmap)

        // Don't forget to close
        doc.close()
    }
}
```

### Convert to Other Formats

```kotlin
// Convert to DOCX
LibreOfficeSDK.convert(input, output, LibreOfficeSDK.ExportFormat.DOCX)

// Convert to XLSX
LibreOfficeSDK.convert(input, output, LibreOfficeSDK.ExportFormat.XLSX)

// Convert to PNG image
LibreOfficeSDK.convert(input, output, LibreOfficeSDK.ExportFormat.PNG)
```

## Advanced Usage

### Access Low-Level Document API

For more control, use the `LODocument` wrapper directly:

```kotlin
val doc = LibreOfficeSDK.openDocument("/path/to/file.docx")

doc?.let {
    // Document properties
    println("Type: ${it.type}")
    println("Pages: ${it.pageCount}")
    println("Width: ${it.width}")
    println("Height: ${it.height}")

    // Navigate pages
    it.currentPage = 2

    // Get page name (for spreadsheets: sheet name)
    val pageName = it.getPageName(0)

    // Execute UNO commands
    it.executeCommand(".uno:Bold")
    it.executeCommand(".uno:Italic")

    // Get selected text
    val selectedText = it.getSelectedText()

    it.close()
}
```

### Available Export Formats

```kotlin
enum class ExportFormat {
    PDF,    // Portable Document Format
    DOCX,   // Microsoft Word
    XLSX,   // Microsoft Excel
    PPTX,   // Microsoft PowerPoint
    ODT,    // OpenDocument Text
    ODS,    // OpenDocument Spreadsheet
    ODP,    // OpenDocument Presentation
    PNG,    // PNG Image
    JPG     // JPEG Image
}
```

### UNO Commands

Execute LibreOffice UNO commands for text formatting:

```kotlin
// Text formatting
doc.executeCommand(".uno:Bold")
doc.executeCommand(".uno:Italic")
doc.executeCommand(".uno:Underline")
doc.executeCommand(".uno:Strikeout")

// Alignment
doc.executeCommand(".uno:LeftPara")
doc.executeCommand(".uno:CenterPara")
doc.executeCommand(".uno:RightPara")
doc.executeCommand(".uno:JustifyPara")

// Lists
doc.executeCommand(".uno:DefaultBullet")
doc.executeCommand(".uno:DefaultNumbering")

// Clipboard
doc.executeCommand(".uno:Copy")
doc.executeCommand(".uno:Cut")
doc.executeCommand(".uno:Paste")

// Undo/Redo
doc.executeCommand(".uno:Undo")
doc.executeCommand(".uno:Redo")
```

## Proguard / R8

If using code shrinking, add these rules:

```proguard
-keep class org.libreoffice.kit.** { *; }
-keep class org.libreoffice.sdk.** { *; }
```

## Size Considerations

The SDK includes native LibreOffice libraries:

| Component | Size |
|-----------|------|
| AAR (compressed) | ~74 MB |
| Native libs (uncompressed) | ~203 MB |

The large size is due to bundling the full LibreOffice engine. Consider using [App Bundles](https://developer.android.com/guide/app-bundle) to optimize delivery.

## Limitations

- **ARM64 only** - x86/x86_64 not currently supported
- **No PDF annotations** - Cannot add highlights, notes, or drawings
- **No digital signatures** - Cannot sign PDFs
- **No PDF forms** - Cannot fill interactive forms
- **Basic editing only** - Full WYSIWYG editing not exposed

## Troubleshooting

### OutOfMemoryError

Large documents may cause memory issues. Increase heap size:

```xml
<!-- AndroidManifest.xml -->
<application android:largeHeap="true" ... >
```

### Initialization Crash

Ensure you initialize before any SDK calls:

```kotlin
if (!LibreOfficeSDK.isReady()) {
    LibreOfficeSDK.initialize(activity)
}
```

### Document Won't Open

Check the error message:

```kotlin
val doc = LibreOfficeSDK.openDocument(path)
if (doc == null) {
    val error = LibreOfficeSDK.getLastError()
    Log.e("SDK", "Failed to open: $error")
}
```

## License

This project is licensed under the [Mozilla Public License 2.0](https://www.mozilla.org/en-US/MPL/2.0/).

LibreOffice is a trademark of The Document Foundation. This project is not affiliated with or endorsed by The Document Foundation.

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## Acknowledgments

- [LibreOffice](https://www.libreoffice.org/) - The Document Foundation
- [LibreOffice Core](https://github.com/LibreOffice/core) - Source code
