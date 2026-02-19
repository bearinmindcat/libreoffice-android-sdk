# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.1-beta] - 2024-02-19

### Added
- Initial release
- Document conversion (DOCX, XLSX, PPTX to PDF)
- Page rendering to Bitmap
- Kotlin wrapper API (`LibreOfficeSDK`)
- Support for ARM64 devices
- Password-protected document support
- Basic text selection and clipboard operations
- UNO command execution for text formatting

### Supported Formats
- **Documents:** DOC, DOCX, ODT, RTF, TXT
- **Spreadsheets:** XLS, XLSX, ODS, CSV
- **Presentations:** PPT, PPTX, ODP
- **Export:** PDF, PNG, JPG

### Known Limitations
- ARM64 only (no x86/x86_64 support)
- Large library size (~74MB AAR)
- No PDF annotations
- No digital signatures
- No form filling
