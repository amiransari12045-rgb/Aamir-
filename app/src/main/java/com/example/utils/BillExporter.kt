package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ui.CalculationResult
import java.io.File
import java.io.FileOutputStream

object BillExporter {

    fun shareAsText(context: Context, result: CalculationResult) {
        val reportText = buildString {
            append("⚡ ELECTRICITY BILL REPORT ⚡\n")
            append("Period: ${result.billMonth} ${result.billYear}\n")
            append("===============================\n\n")
            
            append("📊 OVERALL BILL DETAILS:\n")
            append("• Total Amount Payable: ₹${String.format("%.2f", result.totalAmount)}\n")
            append("• Total Consumption: ${String.format("%.2f", result.finalConsumption)} Units\n")
            append("• Rate Per Unit: ₹${String.format("%.4f", result.perUnitRate)}\n")
            append("• Readings Period: ${result.previousMonth} to ${result.currentMonth}\n\n")
            
            append("👤 SUBMETER SPLITS:\n")
            result.submeters.forEachIndexed { idx, item ->
                append("${idx + 1}. ${item.name}\n")
                append("   - Previous Reading: ${item.previousReading}\n")
                append("   - Current Reading: ${item.currentReading}\n")
                append("   - Original Units: ${String.format("%.2f", item.rawUnits)}\n")
                append("   - Adjusted Units: ${String.format("%.2f", item.finalUnits)}\n")
                append("   - Amount Owed: ₹${String.format("%.2f", item.calculatedAmount)}\n\n")
            }
            
            append("===============================\n")
            append("✅ Sum of Adjusted Units: ${String.format("%.2f", result.totalSubmeterUnits)}\n")
            append("✅ Sum of Calculated Amounts: ₹${String.format("%.2f", result.totalSubmeterAmount)}\n\n")
            append("Generated via Electric Bill Calculator Pro app ⚡")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Electric Bill ${result.billMonth} ${result.billYear}")
            putExtra(Intent.EXTRA_TEXT, reportText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Bill Details"))
    }

    fun shareAsPdf(context: Context, result: CalculationResult) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#4F46E5") // Indigo
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#06B6D4") // Cyan
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#F3F4F6")
            style = Paint.Style.FILL
        }

        var yPosition = 40f

        // Title ⚡
        canvas.drawText("Electric Bill Calculator ⚡", 40f, yPosition, titlePaint)
        yPosition += 25f

        canvas.drawText("Bill Period: ${result.billMonth} ${result.billYear}", 40f, yPosition, subTitlePaint)
        yPosition += 35f

        // Summary Table Header Background
        canvas.drawRect(40f, yPosition, 555f, yPosition + 25f, headerBgPaint)
        canvas.drawRect(40f, yPosition, 555f, yPosition + 25f, borderPaint)

        // Table headers
        canvas.drawText("Total Consumption Unit", 50f, yPosition + 17f, boldTextPaint)
        canvas.drawText("Total Bill Amount", 240f, yPosition + 17f, boldTextPaint)
        canvas.drawText("Per Unit Rate", 410f, yPosition + 17f, boldTextPaint)
        yPosition += 25f

        // Table values
        canvas.drawRect(40f, yPosition, 555f, yPosition + 25f, borderPaint)
        canvas.drawText(String.format("%.2f", result.finalConsumption), 50f, yPosition + 17f, textPaint)
        canvas.drawText("₹" + String.format("%.2f", result.totalAmount), 240f, yPosition + 17f, textPaint)
        canvas.drawText("₹" + String.format("%.4f", result.perUnitRate), 410f, yPosition + 17f, textPaint)
        yPosition += 45f

        // Submeters Heading
        canvas.drawText("Submeter Splits (Readings: ${result.previousMonth} to ${result.currentMonth})", 40f, yPosition, subTitlePaint)
        yPosition += 20f

        // Submeters Grid Header
        canvas.drawRect(40f, yPosition, 555f, yPosition + 25f, headerBgPaint)
        canvas.drawRect(40f, yPosition, 555f, yPosition + 25f, borderPaint)
        canvas.drawText("Room / Name", 45f, yPosition + 17f, boldTextPaint)
        canvas.drawText("Prev", 160f, yPosition + 17f, boldTextPaint)
        canvas.drawText("Curr", 230f, yPosition + 17f, boldTextPaint)
        canvas.drawText("Raw Units", 300f, yPosition + 17f, boldTextPaint)
        canvas.drawText("Adj Units", 380f, yPosition + 17f, boldTextPaint)
        canvas.drawText("Amount", 470f, yPosition + 17f, boldTextPaint)
        yPosition += 25f

        // Submeters Rows
        result.submeters.forEach { item ->
            canvas.drawRect(40f, yPosition, 555f, yPosition + 25f, borderPaint)
            
            // Text truncation to fit column
            val displayName = if (item.name.length > 16) item.name.substring(0, 14) + ".." else item.name
            canvas.drawText(displayName, 45f, yPosition + 17f, textPaint)
            
            canvas.drawText(item.previousReading.toString(), 160f, yPosition + 17f, textPaint)
            canvas.drawText(item.currentReading.toString(), 230f, yPosition + 17f, textPaint)
            canvas.drawText(String.format("%.1f", item.rawUnits), 300f, yPosition + 17f, textPaint)
            canvas.drawText(String.format("%.2f", item.finalUnits), 380f, yPosition + 17f, textPaint)
            canvas.drawText("₹" + String.format("%.2f", item.calculatedAmount), 470f, yPosition + 17f, boldTextPaint)
            yPosition += 25f
        }

        yPosition += 20f

        // Summary box
        canvas.drawRect(40f, yPosition, 555f, yPosition + 60f, headerBgPaint)
        canvas.drawRect(40f, yPosition, 555f, yPosition + 60f, borderPaint)
        canvas.drawText("SUMMARY:", 50f, yPosition + 20f, boldTextPaint)
        canvas.drawText("Sum of Adjusted Units: ${String.format("%.2f", result.totalSubmeterUnits)} Units", 50f, yPosition + 35f, textPaint)
        canvas.drawText("Sum of Splitted Amounts: ₹${String.format("%.2f", result.totalSubmeterAmount)}", 50f, yPosition + 50f, textPaint)
        
        yPosition += 90f

        // Footer
        canvas.drawText("Generated using Electric Bill Calculator Pro.", 40f, yPosition, textPaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache and share
        val cacheDir = context.cacheDir
        val pdfFile = File(cacheDir, "electric_bill_${result.billMonth}_${result.billYear}.pdf")
        
        try {
            FileOutputStream(pdfFile).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()

            // Get share URI
            val authority = "com.aistudio.electricbillcalculator.wovgzn.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "Electric Bill ${result.billMonth} ${result.billYear}")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Bill PDF"))
        } catch (e: Exception) {
            pdfDocument.close()
            Toast.makeText(context, "Error creating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
