package com.example.assignment.analysis.constant

import com.example.assignment.entity.FqName

class ConstantDependencyContext {
    private val _dependencyMap: MutableMap<FqName, MutableSet<FqName>> = mutableMapOf()
    val dependencyMap: Map<FqName, Set<FqName>>
        get() = _dependencyMap.mapValues { it.value.toSet() }

    private var currentClass: FqName? = null
    fun setCurrentClass(classFqName: FqName) {
        currentClass = classFqName
        _dependencyMap.putIfAbsent(classFqName, mutableSetOf())
    }

    fun addConstantDependency(constantOwnerFqName: FqName) {
        val current = currentClass ?: throw IllegalStateException("No current class set. Call setCurrentClass() first.")

        if (current != constantOwnerFqName) {
            _dependencyMap.getOrPut(current) { mutableSetOf() }.add(constantOwnerFqName)
        }
    }
}
