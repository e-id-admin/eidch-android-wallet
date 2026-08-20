package project.convention.logic

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Generates the debug-locale "zz" resources from the base strings.xml.
 *
 * Every translatable <string>/<plurals> value is replaced by its own key, so translators
 * can read which key backs which part of the UI. Entries marked translatable="false"
 * are skipped (they fall back to the default value). Output goes to values-zz/strings.xml.
 *
 * lint is suppressed on the generated resources because dropping format placeholders is
 * intentional here (safe at runtime: getString(id, arg) returns the literal key, extra args ignored).
 */
abstract class GenerateDebugLocaleTask : DefaultTask() {

    @get:InputFile
    abstract val baseStrings: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(baseStrings.get().asFile)
        val root = doc.documentElement

        val builder = StringBuilder()
        builder.appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
        builder.appendLine(
            "<resources xmlns:tools=\"http://schemas.android.com/tools\" " +
                "tools:ignore=\"MissingTranslation,ExtraTranslation,StringFormatMatches," +
                "StringFormatCount,PluralsCandidate,UnusedQuantity,Typos,ByteOrderMark\">"
        )

        val children = root.childNodes
        List(children.length) { children.item(it) }
            .filterIsInstance<Element>()
            .filter { it.getAttribute("name").isNotEmpty() && it.getAttribute("translatable") != "false" }
            .forEach { element ->
                val name = element.getAttribute("name")
                when (element.tagName) {
                    "string" -> builder.appendLine("<string name=\"$name\">$name</string>")
                    "plurals" -> {
                        builder.appendLine("<plurals name=\"$name\">")
                        builder.appendLine("<item quantity=\"other\">$name</item>")
                        builder.appendLine("</plurals>")
                    }
                }
            }
        builder.appendLine("</resources>")

        val outFile = outputDir.get().asFile.resolve("values-zz/strings.xml")
        outFile.parentFile.mkdirs()
        outFile.writeText(builder.toString())
    }
}
