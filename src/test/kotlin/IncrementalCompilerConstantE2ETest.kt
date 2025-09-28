package com.example.javac.incremental

import com.example.javac.incremental.entity.ExitCode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalCompilationConstantE2ETest : IncrementalCompilerBaseE2ETest() {

    @Test
    fun `when constant is changed its user is recompiled`() {
        // arrange
        val constantOwner = testSourceFiles.first { file -> file.name == "ConstantOwner.java" }
        constantOwner.writeText(
            """
           package testData.src;

            public class ConstantOwner {
                public static final String CONSTANT_VALUE = "constant_value";
            }
        """
        )

        // act
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        // assert
        assertEquals(ExitCode.OK, compilationResult)

        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("ConstantOwner.java") }
        assertTrue { dirtyFileMessage.contains("TestClass.java") }
        testSourceFileNames.filter { fileName -> fileName != "ConstantOwner" && fileName != "TestClass" }
            .forEach { fileName -> assertFalse { dirtyFileMessage.contains(fileName) } }

        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("ConstantOwner.class") }
        assertTrue { dirtyClassMessage.contains("TestClass.class") }
        testSourceFileNames.filter { fileName -> fileName != "ConstantOwner" && fileName != "TestClass" }
            .forEach { fileName -> assertFalse { dirtyClassMessage.contains(fileName) } }
    }
}
