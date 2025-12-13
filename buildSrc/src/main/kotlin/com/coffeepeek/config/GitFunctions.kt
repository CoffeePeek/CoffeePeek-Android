package com.coffeepeek.config

import java.io.BufferedReader
import java.io.InputStreamReader

object GitFunctions {


    fun getGitTimestamp(): Long {
        val process =
            Runtime.getRuntime().exec("git show -s --format=%ct".split(" ").toTypedArray())
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        return reader.readLine().trim().toLong()
    }

    fun getRevision(): String {
        val process = Runtime
            .getRuntime()
            .exec("git rev-parse --short=8 HEAD".split(" ").toTypedArray())
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        return reader.readLine().trim()
    }

    fun getRevisionDate(): String {
        val process = Runtime
            .getRuntime()
            .exec("git show -s --format=%ci HEAD^{commit}".split(" ").toTypedArray())
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        return reader.readLine().trim()
    }

    fun getBrunchName(): String {
        val process = Runtime
            .getRuntime()
            .exec("git rev-parse --abbrev-ref HEAD".split(" ").toTypedArray())
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        return reader.readLine().trim()
    }

    fun getCommitCount(): Int {
        val process = Runtime
            .getRuntime()
            .exec("git rev-list --all --count HEAD".split(" ").toTypedArray())
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        return reader.readLine().trim().toInt()
    }

}
