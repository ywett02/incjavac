package com.example.javac.incremental

import com.example.javac.incremental.entity.ExitCode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalCompilationSuperTypesE2ETest : IncrementalCompilerBaseE2ETest() {

    @Test
    fun `when supertype is changed all transitive children are recompiled`() {
        // arrange
        val interfaceFile = testSourceFiles.first { file -> file.name == "InterfaceClass.java" }
        interfaceFile.writeText(
            """
                package testData.src;
                
                public interface InterfaceClass {
                    void interfaceMethod();
                    void addedInterfaceMethod();
                }
        """
        )

        // act
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        // assert
        assertEquals(ExitCode.COMPILATION_ERROR, compilationResult)

        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("InterfaceClass") }
        assertTrue { dirtyFileMessage.contains("AbstractClassImpInterface") }
        assertTrue { dirtyFileMessage.contains("TestClass") }
        testSourceFileNames.filter { name -> name != "InterfaceClass" && name != "AbstractClassImpInterface" && name != "TestClass" }
            .forEach { name -> assertFalse { dirtyFileMessage.contains(name) } }

        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("InterfaceClass") }
        assertTrue { dirtyClassMessage.contains("AbstractClassImpInterface") }
        assertTrue { dirtyClassMessage.contains("TestClass") }
        testSourceFileNames.filter { name -> name != "InterfaceClass" && name != "AbstractClassImpInterface" && name != "TestClass" }
            .forEach { name -> assertFalse { dirtyClassMessage.contains(name) } }
    }
}
