package com.example.assignment.analysis.abi

import org.objectweb.asm.*

class ClassAbiExtractor(private val writer: ClassWriter) : ClassVisitor(Opcodes.ASM9, writer) {

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        return if (access.isAbi()) {
            super.visitMethod(access, name, desc, signature, exceptions)
        } else {
            null
        }
    }

    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor? {
        return if (desc != null) {
            super.visitAnnotation(desc, visible)
        } else {
            null
        }
    }

    override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor? {
        return if (access.isAbi()) {
            super.visitField(access, name, desc, signature, value)
        } else {
            null
        }
    }

    override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
        if (access.isAbi() && outerName != null && innerName != null) {
            super.visitInnerClass(name, outerName, innerName, access)
        }
    }

    fun getBytes(): ByteArray = writer.toByteArray()

    private fun Int.isAbi() = (this and Opcodes.ACC_PRIVATE) == 0
}