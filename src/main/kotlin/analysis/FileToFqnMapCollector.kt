package com.example.assignment.analysis

import com.example.assignment.entity.FqName
import com.sun.source.util.TaskEvent
import com.sun.source.util.TaskListener
import java.io.File
import javax.lang.model.util.Elements
import javax.tools.JavaFileObject

class FileToFqnMapCollector(
    private val elements: Elements,
) : TaskListener {

    private val _fileToFqnMap: MutableMap<File, MutableSet<FqName>> = mutableMapOf()
    val fileToFqnMap: Map<File, Set<FqName>>
        get() = _fileToFqnMap

    override fun started(e: TaskEvent) {
        if (e.kind != TaskEvent.Kind.GENERATE) {
            return
        }

        val sourceFile = e.sourceFile ?: return
        if (sourceFile.kind != JavaFileObject.Kind.SOURCE) return

        _fileToFqnMap.computeIfAbsent(File(sourceFile.toUri())) { mutableSetOf() }
            .add(FqName(elements.getBinaryName(e.typeElement).toString()))
    }
}