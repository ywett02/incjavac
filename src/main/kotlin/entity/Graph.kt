package com.example.assignment.entity


data class Node<T>(val value: T) {
    val children = mutableSetOf<Node<T>>()
    val parents = mutableSetOf<Node<T>>()
}

class Graph<T>(private val _nodes: MutableMap<T, Node<T>> = mutableMapOf()) {

    val nodes: Map<T, Node<T>>
        get() = _nodes

    fun add(value: T) {
        _nodes.computeIfAbsent(value) { Node(value) }
    }

    fun addEdge(sourceValue: T, destinationValues: Set<T>) {
        for (destination in destinationValues) {
            addEdge(sourceValue, destination)
        }
    }

    fun addEdge(sourceValue: T, destinationValue: T) {
        val sourceNode = _nodes.computeIfAbsent(sourceValue) { Node(sourceValue) }
        val destinationNode = _nodes.computeIfAbsent(destinationValue) { Node(destinationValue) }

        addEdge(sourceNode, destinationNode)
    }

    private fun addEdge(sourceNode: Node<T>, destinationNode: Node<T>) {
        sourceNode.children.add(destinationNode)
        destinationNode.parents.add(sourceNode)
    }

    fun remove(key: T) {
        val node = _nodes[key] ?: return

        for (parent in node.parents) {
            parent.children.remove(node)
        }

        for (child in node.children) {
            child.parents.remove(node)
        }

        _nodes.remove(key)
    }

    operator fun contains(key: T): Boolean =
        _nodes.contains(key)

    operator fun get(key: T): Node<T>? =
        _nodes[key]

    operator fun plus(graph: Graph<T>): Graph<T> {
        val nodes: Map<T, Node<T>> = _nodes + graph.nodes
        return Graph(nodes.toMutableMap())
    }

    operator fun minus(keys: Set<T>): Graph<T> {
        val nodes: Map<T, Node<T>> = _nodes - keys
        return Graph<T>(nodes.toMutableMap())
    }
}