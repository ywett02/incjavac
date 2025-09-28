package com.example.javac.incremental.entity

import java.io.File

data class FileChanges(
    val addedAndModifiedFiles: Set<File>,
    val removedFiles: Set<File>
)