package com.yindun.shouhu.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Environment
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PDF生成器
 * 用于生成《诈骗行为记录摘要表》等报案材料
 */
class PdfGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
    private val dateFormatShort = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)

    /**
     * 生成诈骗行为记录摘要表
     * @param caseInfo 案件信息
     * @return PDF文件路径
     */
    fun generateCaseSummaryPdf(caseInfo: CaseInfo): File {
        val document = PdfDocument()

        // 创建第一页 - 封面
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4尺寸
        val page1 = document.startPage(pageInfo1)
        drawCoverPage(page1.canvas, caseInfo)
        document.finishPage(page1)

        // 创建第二页 - 详细信息
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = document.startPage(pageInfo2)
        drawDetailPage(page2.canvas, caseInfo)
        document.finishPage(page2)

        // 创建第三页 - 证据列表
        val pageInfo3 = PdfDocument.PageInfo.Builder(595, 842, 3).create()
        val page3 = document.startPage(pageInfo3)
        drawEvidencePage(page3.canvas, caseInfo)
        document.finishPage(page3)

        // 保存文件
        val fileName = "诈骗记录_${caseInfo.caseNumber}_${dateFormatShort.format(Date())}.pdf"
        val outputFile = File(getOutputDirectory(), fileName)

        FileOutputStream(outputFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return outputFile
    }

    /**
     * 绘制封面页
     */
    private fun drawCoverPage(canvas: Canvas, caseInfo: CaseInfo) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }

        val infoPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            textAlign = Paint.Align.LEFT
        }

        // 标题
        canvas.drawText("诈骗行为记录摘要表", 297f, 200f, titlePaint)

        // 分隔线
        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        }
        canvas.drawLine(100f, 220f, 495f, 220f, linePaint)

        // 基本信息
        var y = 280f
        val lineHeight = 35f

        canvas.drawText("案件编号：${caseInfo.caseNumber}", 100f, y, infoPaint)
        y += lineHeight

        canvas.drawText("生成时间：${dateFormat.format(caseInfo.createdAt)}", 100f, y, infoPaint)
        y += lineHeight

        canvas.drawText("诈骗类型：${caseInfo.fraudType}", 100f, y, infoPaint)
        y += lineHeight

        canvas.drawText("涉及金额：¥${caseInfo.amount}", 100f, y, infoPaint)
        y += lineHeight

        canvas.drawText("风险等级：${caseInfo.riskLevel}", 100f, y, infoPaint)
        y += lineHeight + 20f

        // 警告文字
        val warningPaint = Paint().apply {
            color = Color.RED
            textSize = 16f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("重要提示：请妥善保管此材料，报案时需提供原件", 297f, y, warningPaint)
        y += lineHeight

        canvas.drawText("如有疑问，请拨打全国反诈专线：96110", 297f, y, warningPaint)

        // 页脚
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("银盾守护 - 老年人防诈骗安全守护应用", 297f, 800f, footerPaint)
    }

    /**
     * 绘制详细信息页
     */
    private fun drawDetailPage(canvas: Canvas, caseInfo: CaseInfo) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
        }

        val contentPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        var y = 60f
        val lineHeight = 25f

        // 标题
        canvas.drawText("案件详细信息", 297f, y, titlePaint)
        y += lineHeight * 2

        // 案件基本信息
        canvas.drawText("一、案件基本信息", 50f, y, headerPaint)
        y += lineHeight

        drawInfoRow(canvas, "案件编号", caseInfo.caseNumber, y, contentPaint)
        y += lineHeight
        drawInfoRow(canvas, "报案时间", dateFormat.format(caseInfo.createdAt), y, contentPaint)
        y += lineHeight
        drawInfoRow(canvas, "诈骗类型", caseInfo.fraudType, y, contentPaint)
        y += lineHeight
        drawInfoRow(canvas, "涉及金额", "¥${caseInfo.amount}", y, contentPaint)
        y += lineHeight
        drawInfoRow(canvas, "风险等级", caseInfo.riskLevel, y, contentPaint)
        y += lineHeight * 1.5f

        // 受害人信息
        canvas.drawText("二、受害人信息", 50f, y, headerPaint)
        y += lineHeight

        drawInfoRow(canvas, "姓名", caseInfo.victimName, y, contentPaint)
        y += lineHeight
        drawInfoRow(canvas, "联系电话", caseInfo.victimPhone, y, contentPaint)
        y += lineHeight
        drawInfoRow(canvas, "住址", caseInfo.victimAddress, y, contentPaint)
        y += lineHeight * 1.5f

        // 嫌疑人信息
        canvas.drawText("三、嫌疑人信息", 50f, y, headerPaint)
        y += lineHeight

        drawInfoRow(canvas, "联系电话", caseInfo.suspectPhone, y, contentPaint)
        y += lineHeight
        drawInfoRow(canvas, "其他信息", caseInfo.suspectInfo, y, contentPaint)
        y += lineHeight * 1.5f

        // 案件描述
        canvas.drawText("四、案件描述", 50f, y, headerPaint)
        y += lineHeight

        // 自动换行处理
        val maxWidth = 495f
        val words = caseInfo.description.toCharArray()
        var line = ""
        var currentX = 50f

        for (char in words) {
            val testLine = line + char
            val testWidth = contentPaint.measureText(testLine)
            if (testWidth > maxWidth - 100f) {
                canvas.drawText(line, currentX, y, contentPaint)
                line = char.toString()
                y += lineHeight
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, currentX, y, contentPaint)
        }
    }

    /**
     * 绘制证据列表页
     */
    private fun drawEvidencePage(canvas: Canvas, caseInfo: CaseInfo) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
        }

        val contentPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        var y = 60f
        val lineHeight = 25f

        // 标题
        canvas.drawText("证据材料清单", 297f, y, titlePaint)
        y += lineHeight * 2

        // 证据列表标题
        canvas.drawText("五、已提交证据", 50f, y, headerPaint)
        y += lineHeight * 1.5f

        // 表头
        val headerLinePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }
        canvas.drawLine(50f, y, 545f, y, headerLinePaint)
        y += lineHeight

        canvas.drawText("序号", 60f, y, headerPaint)
        canvas.drawText("证据类型", 120f, y, headerPaint)
        canvas.drawText("描述", 250f, y, headerPaint)
        canvas.drawText("提交时间", 420f, y, headerPaint)
        y += lineHeight

        canvas.drawLine(50f, y, 545f, y, headerLinePaint)
        y += lineHeight

        // 证据列表
        caseInfo.evidences.forEachIndexed { index, evidence ->
            canvas.drawText("${index + 1}", 70f, y, contentPaint)
            canvas.drawText(evidence.type, 130f, y, contentPaint)
            canvas.drawText(evidence.description, 250f, y, contentPaint)
            canvas.drawText(dateFormat.format(evidence.timestamp), 420f, y, contentPaint)
            y += lineHeight
        }

        canvas.drawLine(50f, y, 545f, y, headerLinePaint)
        y += lineHeight * 2

        // 注意事项
        canvas.drawText("六、注意事项", 50f, y, headerPaint)
        y += lineHeight * 1.5f

        val notices = listOf(
            "1. 本材料由“银盾守护”应用自动生成，仅供参考。",
            "2. 请将此材料与原始证据一同提交给公安机关。",
            "3. 如需补充材料，请通过应用的“举报中心”功能整理。",
            "4. 全国反诈专线：96110，如遇紧急情况请立即拨打。",
            "5. 请妥善保管个人信息，防止二次受骗。"
        )

        notices.forEach { notice ->
            canvas.drawText(notice, 60f, y, contentPaint)
            y += lineHeight
        }

        // 页脚
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("本材料生成于 ${dateFormat.format(Date())}", 297f, 800f, footerPaint)
    }

    /**
     * 绘制信息行
     */
    private fun drawInfoRow(canvas: Canvas, label: String, value: String, y: Float, paint: Paint) {
        val labelPaint = Paint(paint).apply {
            isFakeBoldText = true
        }
        canvas.drawText("$label：", 70f, y, labelPaint)
        canvas.drawText(value, 180f, y, paint)
    }

    /**
     * 获取输出目录
     */
    private fun getOutputDirectory(): File {
        val appDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val pdfDir = File(appDir, "PDF")
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }
        return pdfDir
    }

    companion object {
        @Volatile
        private var instance: PdfGenerator? = null

        fun getInstance(context: Context): PdfGenerator {
            return instance ?: synchronized(this) {
                instance ?: PdfGenerator(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 案件信息
 */
data class CaseInfo(
    val caseNumber: String,
    val fraudType: String,
    val amount: Double,
    val riskLevel: String,
    val victimName: String,
    val victimPhone: String,
    val victimAddress: String,
    val suspectPhone: String,
    val suspectInfo: String,
    val description: String,
    val evidences: List<EvidenceInfo>,
    val createdAt: Date = Date()
)

/**
 * 证据信息
 */
data class EvidenceInfo(
    val type: String,
    val description: String,
    val filePath: String? = null,
    val timestamp: Date = Date()
)
