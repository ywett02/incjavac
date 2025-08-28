package com.example.assignment

import java.io.File

data class FileChanges(
    val addedAndModifiedFiles: List<File>,
    val removedFiles: List<File>
)