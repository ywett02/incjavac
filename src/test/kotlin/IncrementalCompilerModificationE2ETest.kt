package com.example.javac.incremental

import com.example.javac.incremental.entity.ExitCode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalCompilationModificationE2ETest : IncrementalCompilerBaseE2ETest() {

    @Test
    fun `when first run, non-incremental mode is triggered`() {
        // act
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        // assert
        assertEquals(ExitCode.OK, compilationResult)
        assertTrue(
            eventRecorder.events.any { it.contains("Non-incremental compilation will be performed") })
    }

    @Test
    fun `when changing a file with no dependencies then only that file should be recompiled`() {
        // arrange
        val testClass = testSourceFiles.first { file -> file.name == "TestClass.java" }
        testClass.writeText(
            """
                    package testData.src;
                    
                    public class TestClass extends AbstractClassImpInterface {
                    
                        @Override
                        void abstractClassMethod() {
                        }
                    
                        @Override
                        public void interfaceMethod() {
                        }
                    
                        public void callDependentMethod() {
                            new IndependentClass().method();
                        }
                    
                        public void printConstantValue() {
                            System.out.println(ConstantOwner.CONSTANT_VALUE);
                        }
                        
                        public void addedMethod() {
                            //no-op
                        }
                    }
        """
        )

        //act
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        // assert
        assertEquals(ExitCode.OK, compilationResult)
        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("TestClass.java") }
        testSourceFileNames.filter { name -> name != "TestClass" }
            .forEach { name -> assertFalse { dirtyFileMessage.contains(name) } }

        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("TestClass.class") }
        testSourceFileNames.filter { name -> name != "TestClass" }
            .forEach { name -> assertFalse { dirtyClassMessage.contains(name) } }
    }

    @Test
    fun `when changing a file with dependencies then the file and its dependencies should be recompiled`() {
        // arrange
        val independentClass = testSourceFiles.first { file -> file.name == "IndependentClass.java" }
        independentClass.writeText(
            """
                    package testData.src;
                    
                    public class IndependentClass {
                        public void method() {
                            System.out.println("IndependentClass.method()");
                        }
                        
                        public void addedMethod() {
                            //no-op
                        }
                    }
        """
        )

        // act
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        // assert
        assertEquals(ExitCode.OK, compilationResult)
        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("IndependentClass.java") }
        assertTrue { dirtyFileMessage.contains("TestClass.java") }
        testSourceFileNames.filter { name -> name != "TestClass" && name != "IndependentClass" }
            .forEach { name -> assertFalse { dirtyFileMessage.contains(name) } }

        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("IndependentClass.class") }
        assertTrue { dirtyClassMessage.contains("TestClass.class") }
        testSourceFileNames.filter { name -> name != "TestClass" && name != "IndependentClass" }
            .forEach { name -> assertFalse { dirtyClassMessage.contains(name) } }
    }

    @Test
    fun `when removing dependency from file then the dependency is not recompiled after its change`() {
        // arrange
        val testClass = testSourceFiles.first { file -> file.name == "TestClass.java" }
        testClass.writeText(
            """
                    package testData.src;
                    
                    public class TestClass extends AbstractClassImpInterface {
                    
                        @Override
                        void abstractClassMethod() {
                        }
                    
                        @Override
                        public void interfaceMethod() {
                        }

                        public void printConstantValue() {
                            System.out.println(ConstantOwner.CONSTANT_VALUE);
                        }
                    }
        """
        )
        incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())
        eventRecorder.clear()

        val independentClass = testSourceFiles.first { file -> file.name == "IndependentClass.java" }
        independentClass.writeText(
            """
                    package testData.src;
                    
                    public class IndependentClass {
                        public void method() {
                            System.out.println("IndependentClass.method()");
                        }
                        
                        public void addedMethod() {
                            //no-op
                        }
                    }
        """
        )

        // act
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        // assert
        assertEquals(ExitCode.OK, compilationResult)
        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("IndependentClass.java") }
        testSourceFileNames.filter { name -> name != "IndependentClass" }
            .forEach { name -> assertFalse { dirtyFileMessage.contains(name) } }

        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("IndependentClass.class") }
        testSourceFileNames.filter { name -> name != "IndependentClass" }
            .forEach { name -> assertFalse { dirtyClassMessage.contains(name) } }
    }
}
