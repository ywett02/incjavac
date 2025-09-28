package com.example.javac.incremental

import org.kohsuke.args4j.CmdLineException
import kotlin.system.exitProcess

fun main(array: Array<String>) {
    try {
        val exitCode = IncrementalJavaCompilerCommand.run(array)
        println("Compilation result code: ${exitCode.code}")
        exitProcess(0)
    } catch (e: CmdLineException) {
        exitProcess(2)
    } catch (e: Throwable) {
        println("incjavac tool failed: ${e.localizedMessage}")
        exitProcess(1)
    }
}