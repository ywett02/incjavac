package com.example.assignment.entity

import java.io.File

data class FileChanges(
    val addedAndModifiedFiles: List<File>,
    val removedFiles: List<File>
)