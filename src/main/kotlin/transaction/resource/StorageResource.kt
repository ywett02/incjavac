package com.example.javac.incremental.transaction.resource

import com.example.javac.incremental.storage.Storage
import com.example.javac.incremental.transaction.CompilationResource

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
