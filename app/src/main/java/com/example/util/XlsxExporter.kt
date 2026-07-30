package com.example.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.AccountEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object XlsxExporter {

    fun exportToXlsx(context: Context, accounts: List<AccountEntity>): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "accounts_$timeStamp.xlsx"

        val xlsxBytes = buildXlsxZip(accounts)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IllegalStateException("Failed to create MediaStore entry")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(xlsxBytes)
            }
            "Downloads/$fileName"
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(xlsxBytes)
            }
            file.absolutePath
        }
    }

    private fun buildXlsxZip(accounts: List<AccountEntity>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            // 1. [Content_Types].xml
            addZipEntry(zos, "[Content_Types].xml", CONTENT_TYPES_XML)

            // 2. _rels/.rels
            addZipEntry(zos, "_rels/.rels", RELS_XML)

            // 3. xl/workbook.xml
            addZipEntry(zos, "xl/workbook.xml", WORKBOOK_XML)

            // 4. xl/_rels/workbook.xml.rels
            addZipEntry(zos, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS_XML)

            // 5. xl/worksheets/sheet1.xml
            addZipEntry(zos, "xl/worksheets/sheet1.xml", buildSheetXml(accounts))
        }
        return baos.toByteArray()
    }

    private fun addZipEntry(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    private fun buildSheetXml(accounts: List<AccountEntity>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.append("<sheetData>")

        // Row 1: Header (UID, Password, Cookie)
        sb.append("""<row r="1">""")
        sb.append("""<c r="A1" t="inlineStr"><is><t>UID</t></is></c>""")
        sb.append("""<c r="B1" t="inlineStr"><is><t>Password</t></is></c>""")
        sb.append("""<c r="C1" t="inlineStr"><is><t>Cookie</t></is></c>""")
        sb.append("</row>")

        // Data Rows
        accounts.forEachIndexed { index, account ->
            val rowNum = index + 2
            val uidStr = escapeXml(if (account.uid.isNotBlank()) account.uid else account.emailOrPhone)
            val passStr = escapeXml(account.password)
            val cookieStr = escapeXml(account.cookie)

            sb.append("""<row r="$rowNum">""")
            sb.append("""<c r="A$rowNum" t="inlineStr"><is><t>$uidStr</t></is></c>""")
            sb.append("""<c r="B$rowNum" t="inlineStr"><is><t>$passStr</t></is></c>""")
            sb.append("""<c r="C$rowNum" t="inlineStr"><is><t>$cookieStr</t></is></c>""")
            sb.append("</row>")
        }

        sb.append("</sheetData>")
        sb.append("</worksheet>")
        return sb.toString()
    }

    private fun escapeXml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
            .filter { c -> c.code in 0x20..0xD7FF || c.code in 0xE000..0xFFFD || c.code == 0x9 || c.code == 0xA || c.code == 0xD }
    }

    private const val CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

    private const val RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private const val WORKBOOK_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Accounts" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

    private const val WORKBOOK_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
}
