package com.example.assignment.transaction.impl

import com.example.assignment.storage.Storage
import com.example.assignment.transaction.CompilationResource

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
