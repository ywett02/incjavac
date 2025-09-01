package com.example.assignment.analysis

import com.example.assignment.EventReporter
import com.example.assignment.entity.FqName
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import com.example.assignment.util.joinToString
import com.sun.source.util.TaskEvent
import com.sun.source.util.TaskListener
import java.io.File
import javax.lang.model.util.Elements
import javax.tools.JavaFileObject

class FileToFqnMapCollector(
    private val elements: Elements,
    private val fqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val eventReporter: EventReporter
) : TaskListener {

    private val fileToFqnMap: MutableMap<File, MutableSet<FqName>> = mutableMapOf()

    override fun started(e: TaskEvent) {
        if (e.kind != TaskEvent.Kind.GENERATE) {
            return
        }

        val sourceFile = e.sourceFile ?: return
        if (sourceFile.kind != JavaFileObject.Kind.SOURCE) return

        fileToFqnMap.computeIfAbsent(File(sourceFile.toUri())) { mutableSetOf() }
            .add(FqName(elements.getBinaryName(e.typeElement).toString()))
    }

    override fun finished(e: TaskEvent) {
        if (e.kind != TaskEvent.Kind.GENERATE) {
            return
        }

        eventReporter.reportEvent(
            """File to FQN map created: [
                |${fileToFqnMap.joinToString({ it.absolutePath }, { it.id })}]""".trimMargin()
        )

        fqnMapInMemoryStorage.set(fileToFqnMap)
        fileToFqnMap.clear()
    }
}