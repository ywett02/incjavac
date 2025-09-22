/***
 * ASM examples: examples showing how ASM can be used
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.asm.depend;

import com.example.assignment.entity.FqName;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * DependencyVisitor
 *
 * @author Eugene Kuleshov
 */
public class DependencyVisitor extends ClassVisitor {

    Map<FqName, DependencyAnalysis> data = new HashMap<>();

    FqName className;
    DependencyAnalysis analysis;

    public Map<FqName, DependencyAnalysis> getAnalysis() {
        return data;
    }

    public DependencyVisitor() {
        super(Opcodes.ASM9);
    }

    // ClassVisitor

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        className = binaryName(name);
        analysis = data.computeIfAbsent(className, k -> new DependencyAnalysis());

        if (signature == null) {
            if (superName != null) {
                addInternalName(superName, analysis.superTypes);
            }
            addInternalNames(interfaces, analysis.superTypes);
        } else {
            addSignature(signature);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(
            final String desc,
            final boolean visible) {
        addDesc(desc, analysis.types);
        return new AnnotationDependencyVisitor();
    }

    @Override
    public FieldVisitor visitField(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final Object value) {
        if (signature == null) {
            addDesc(desc, analysis.types);
        } else {
            addTypeSignature(signature);
        }
        if (value instanceof Type) {
            addType((Type) value, analysis.types);
        }
        return new FieldDependencyVisitor();
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final String[] exceptions) {
        if (signature == null) {
            addMethodDesc(desc, analysis.types);
        } else {
            addSignature(signature);
        }
        addInternalNames(exceptions, analysis.types);
        return new MethodDependencyVisitor();
    }

    private void addName(final String name, Set<FqName> collection) {
        if (name == null) {
            return;
        }

        FqName binaryName = binaryName(name);
        if (className.getId().equals(binaryName.getId())) {
            return;
        }

        collection.add(binaryName);
    }

    void addInternalName(final String name, Set<FqName> collection) {
        addType(Type.getObjectType(name), collection);
    }

    private void addInternalNames(final String[] names, Set<FqName> collection) {
        for (int i = 0; names != null && i < names.length; i++) {
            addInternalName(names[i], collection);
        }
    }

    void addDesc(final String desc, Set<FqName> collection) {
        addType(Type.getType(desc), collection);
    }

    // ---------------------------------------------

    void addMethodDesc(final String desc, Set<FqName> collection) {
        addType(Type.getReturnType(desc), collection);
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            addType(types[i], collection);
        }
    }

    private FqName binaryName(String name) {
        return new FqName(name.replaceAll("[/]", "."));
    }

    void addType(final Type t, Set<FqName> collection) {
        switch (t.getSort()) {
            case Type.ARRAY:
                addType(t.getElementType(), collection);
                break;
            case Type.OBJECT:
                addName(t.getInternalName(), collection);
                break;
            case Type.METHOD:
                addMethodDesc(t.getDescriptor(), collection);
                break;
        }
    }

    void addConstant(final Object cst, Set<FqName> collection) {
        if (cst instanceof Type) {
            addType((Type) cst, collection);
        } else if (cst instanceof Handle) {
            Handle h = (Handle) cst;
            addInternalName(h.getOwner(), collection);
            addMethodDesc(h.getDesc(), collection);
        }
    }

    class AnnotationDependencyVisitor extends AnnotationVisitor {

        public AnnotationDependencyVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(final String name, final Object value) {
            if (value instanceof Type) {
                addType((Type) value, analysis.types);
            }
        }

        @Override
        public void visitEnum(
                final String name,
                final String desc,
                final String value) {
            addDesc(desc, analysis.types);
        }

        @Override
        public AnnotationVisitor visitAnnotation(
                final String name,
                final String desc) {
            addDesc(desc, analysis.types);
            return this;
        }

        @Override
        public AnnotationVisitor visitArray(final String name) {
            return this;
        }
    }

    class FieldDependencyVisitor extends FieldVisitor {

        public FieldDependencyVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            addDesc(desc, analysis.types);
            return new AnnotationDependencyVisitor();
        }
    }

    class SignatureDependencyVisitor extends SignatureVisitor {

        String signatureClassName;

        public SignatureDependencyVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visitClassType(final String name) {
            signatureClassName = name;
            addInternalName(name, analysis.types);
        }

        @Override
        public void visitInnerClassType(final String name) {
            signatureClassName = signatureClassName + "$" + name;
            addInternalName(signatureClassName, analysis.types);
        }
    }

    private void addSignature(final String signature) {
        if (signature != null) {
            new SignatureReader(signature).accept(new SignatureDependencyVisitor());
        }
    }

    void addTypeSignature(final String signature) {
        if (signature != null) {
            new SignatureReader(signature).acceptType(new SignatureDependencyVisitor());
        }
    }

    class MethodDependencyVisitor extends MethodVisitor {

        public MethodDependencyVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return new AnnotationDependencyVisitor();
        }

        @Override
        public AnnotationVisitor visitAnnotation(
                final String desc,
                final boolean visible) {
            addDesc(desc, analysis.types);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
                final int parameter,
                final String desc,
                final boolean visible) {
            addDesc(desc, analysis.types);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public void visitTypeInsn(final int opcode, final String type) {
            addType(Type.getObjectType(type), analysis.types);
        }

        @Override
        public void visitFieldInsn(
                final int opcode,
                final String owner,
                final String name,
                final String desc) {
            addInternalName(owner, analysis.types);
            addDesc(desc, analysis.types);
        }

        @Override
        public void visitMethodInsn(
                final int opcode,
                final String owner,
                final String name,
                final String desc,
                final boolean isInterface) {
            addInternalName(owner, analysis.types);
            addMethodDesc(desc, analysis.types);
        }

        @Override
        public void visitInvokeDynamicInsn(
                String name,
                String desc,
                Handle bsm,
                Object... bsmArgs) {
            addMethodDesc(desc, analysis.types);
            addConstant(bsm, analysis.types);
            for (int i = 0; i < bsmArgs.length; i++) {
                addConstant(bsmArgs[i], analysis.types);
            }
        }

        @Override
        public void visitLdcInsn(final Object cst) {
            addConstant(cst, analysis.types);
        }

        @Override
        public void visitMultiANewArrayInsn(final String desc, final int dims) {
            addDesc(desc, analysis.types);
        }

        @Override
        public void visitLocalVariable(
                final String name,
                final String desc,
                final String signature,
                final Label start,
                final Label end,
                final int index) {
            addTypeSignature(signature);
        }

        @Override
        public void visitTryCatchBlock(
                final Label start,
                final Label end,
                final Label handler,
                final String type) {
            if (type != null) {
                addInternalName(type, analysis.types);
            }
        }
    }
}