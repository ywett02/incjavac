package com.example.assignment.resource.impl

import com.example.assignment.resource.CompilationResource
import java.io.Closeable

class CloseableResource(
    private val closeable: Closeable
) : CompilationResource {

    override fun onSuccess() {
        closeable.close()
    }

    override fun onFailure() {
        closeable.close()
    }
}

fun Closeable.asResource(): CloseableResource = CloseableResource(this)
