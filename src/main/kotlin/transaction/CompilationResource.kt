package com.example.javac.incremental.transaction

interface CompilationResource {

    fun onSuccess()

    fun onFailure()
}