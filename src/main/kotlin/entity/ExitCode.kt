package com.example.assignment.entity

enum class ExitCode(val code: Int) {
    OK(0),
    COMPILATION_ERROR(1),
    INTERNAL_ERROR(2)
}