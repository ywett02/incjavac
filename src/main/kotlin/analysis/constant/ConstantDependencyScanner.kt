package com.example.assignment.analysis.constant

import com.example.assignment.entity.FqName
import com.sun.source.tree.ClassTree
import com.sun.source.tree.CompilationUnitTree
import com.sun.source.tree.IdentifierTree
import com.sun.source.tree.MemberSelectTree
import com.sun.source.util.TreePathScanner
import com.sun.source.util.Trees
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

class ConstantDependencyScanner(
    private val elements: Elements,
    private val trees: Trees
) : TreePathScanner<ConstantDependencyContext, ConstantDependencyContext>() {

    override fun visitCompilationUnit(
        node: CompilationUnitTree?,
        context: ConstantDependencyContext?
    ): ConstantDependencyContext {
        val nonNullContext = context ?: ConstantDependencyContext()

        super.visitCompilationUnit(node, nonNullContext)
        return nonNullContext
    }

    override fun visitClass(
        node: ClassTree?,
        context: ConstantDependencyContext
    ): ConstantDependencyContext {
        val element = trees.getElement(this.currentPath) as TypeElement
        context.setCurrentClass(FqName(elements.getBinaryName(element).toString()))

        super.visitClass(node, context)
        return context
    }

    override fun visitIdentifier(
        node: IdentifierTree?,
        context: ConstantDependencyContext
    ): ConstantDependencyContext {
        val element = trees.getElement(this.currentPath)

        if (isCompileTimeConstants(element)) {
            val enclosingElement = element.enclosingElement as TypeElement
            val binaryName = elements.getBinaryName(enclosingElement).toString()
            context.addConstantDependency(FqName(binaryName))
        }

        super.visitIdentifier(node, context)
        return context
    }

    override fun visitMemberSelect(
        node: MemberSelectTree?,
        context: ConstantDependencyContext
    ): ConstantDependencyContext {
        val element = trees.getElement(this.currentPath)

        if (isCompileTimeConstants(element)) {
            val enclosingElement = element.enclosingElement as TypeElement
            val binaryName = elements.getBinaryName(enclosingElement).toString()
            context.addConstantDependency(FqName(binaryName))
        }

        super.visitMemberSelect(node, context)
        return context
    }

    private fun isCompileTimeConstants(element: Element): Boolean {
        val variableElement = element as? VariableElement ?: return false
        return variableElement.constantValue != null
    }
}