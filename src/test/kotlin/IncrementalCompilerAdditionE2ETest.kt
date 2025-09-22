package com.example.assignment

import com.example.assignment.entity.ExitCode
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncrementalCompilationAdditionE2ETest : IncrementalCompilerBaseE2ETest() {

    @Test
    fun `when file added it is recompiled`() {
        createJavaFile(
            srcDir, "IndependentClass2.java", """
            package com.example.test;

            public class IndependentClass2 {
                public void method2() {
                    System.out.println("IndependentClass2.method2()");
                }
            }
        """
        )

        val compilationResult = incrementalJavaCompilerRunner.compile(incrementalJavaCompilerContext)

        assertEquals(ExitCode.OK, compilationResult)
        assertTrue { eventRecorder.events.contains("Dirty source files: [IndependentClass2.java]") }
        assertTrue { eventRecorder.events.contains("Dirty source files: [IndependentClass2.class]") }
    }

    private fun createJavaFile(srcDir: File, name: String, content: String): File {
        val packageDir = File(srcDir, "com/example/test")
        packageDir.mkdirs()

        val file = File(packageDir, name).apply {
            createNewFile()
        }
        file.writeText(content)
        return file
    }
}
