package com.example.assignment.resource.impl

import com.example.assignment.resource.CompilationResource
import com.example.assignment.storage.Storage

class StorageResource(
    private val storage: Storage
) : CompilationResource {

    override fun onSuccess() {
        storage.flush()
    }

    override fun onFailure() {
    }
}

fun Storage.asResource(): StorageResource = StorageResource(this)
