package com.example.javac.incremental

import com.example.javac.incremental.entity.ExitCode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalCompilationAdditionE2ETest : IncrementalCompilerBaseE2ETest() {

    @Test
    fun `when file added it is recompiled`() {
        // arrange
        createFile(
            srcDir, "AddedClass.java", """
            package com.example.test;

            public class AddedClass {
                public void method2() {
                    System.out.println("IndependentClass2.method2()");
                }
            }
        """
        )

        // act
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        // assert
        assertEquals(ExitCode.OK, compilationResult)

        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("AddedClass.java") }
        testSourceFileNames.forEach { fileName -> assertFalse { dirtyFileMessage.contains(fileName) } }

        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertFalse { dirtyClassMessage.contains("AddedClass.class") }
        testSourceFileNames.forEach { fileName -> assertFalse { dirtyClassMessage.contains(fileName) } }
    }
}
