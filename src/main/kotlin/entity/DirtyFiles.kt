package com.example.assignment.entity

import java.io.File

class DirtyFiles(
    val dirtySourceFiles: Set<File>,
    val dirtyClassFiles: Set<File>,
)