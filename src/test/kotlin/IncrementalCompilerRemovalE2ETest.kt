package com.example.javac.incremental

import com.example.javac.incremental.entity.ExitCode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalCompilationRemovalE2ETest : IncrementalCompilerBaseE2ETest() {

    @Test
    fun `when file with dependant is removed dependant is recompiled`() {
        // arrange
        val independentClass = testSourceFiles.first { file -> file.name == "IndependentClass.java" }
        independentClass.delete()

        // act
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        // assert
        assertEquals(ExitCode.COMPILATION_ERROR, compilationResult)

        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("TestClass") }
        testSourceFileNames.filter { name -> name != "TestClass" }
            .forEach { name -> assertFalse { dirtyFileMessage.contains(name) } }

        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("TestClass") }
        assertTrue { dirtyClassMessage.contains("IndependentClass") }
        testSourceFileNames.filter { name -> name != "TestClass" && name != "IndependentClass" }
            .forEach { name -> assertFalse { dirtyClassMessage.contains(name) } }
    }

    @Test
    fun `when file with no dependant is nothing is recompiled`() {
        // arrange
        val testClass = testSourceFiles.first { file -> file.name == "TestClass.java" }
        testClass.delete()

        // act
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        // assert
        assertEquals(ExitCode.OK, compilationResult)
        eventRecorder.events.first { message -> message.contains("Dirty source files: []") }

        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("TestClass.class") }
        testSourceFileNames.filter { name -> name != "TestClass" }
            .forEach { name -> assertFalse { dirtyClassMessage.contains(name) } }
    }
}
