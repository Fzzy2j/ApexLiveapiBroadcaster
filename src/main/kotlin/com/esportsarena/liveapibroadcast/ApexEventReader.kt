package com.esportsarena.liveapibroadcast

import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey

object ApexEventReader {

    class ApexMatch(val filePath: String) {

        private val reader = File(filePath).bufferedReader()
        private var eventIndex = 0L

        fun ingestEvents(live: Boolean) {
            while (true) {
                if (!isSocketConnected) {
                    Thread.sleep(1000)
                    continue
                }
                var line = reader.readLine() ?: break
                //if (!live) continue
                if (line.endsWith(",")) line = line.substring(0, line.length - 1)
                try {
                    val json = JSONObject(line)
                    json.put("matchId", File(filePath).nameWithoutExtension)
                    json.put("eventId", eventIndex++)

                    //println(json.toString())
                    sendEventToServer(json.toString())
                    //broadcastEvent(json.toString())
                    //Thread.sleep(5000)
                } catch (_: JSONException) {
                }
            }
        }
    }

    val liveapiFolder = File("C:\\Users\\admin\\Saved Games\\Respawn\\Apex\\assets\\temp\\live_api")

    val matches = arrayListOf<ApexMatch>()

    val fileSizes = hashMapOf<String, Long>()

    fun start() {
        for (file in liveapiFolder.listFiles()!!) {
            getMatch(file)
        }
        Thread {
            while (true) {
                fun checkFile(file: File) {
                    val length = file.length()
                    val size = fileSizes.getOrDefault(file.absolutePath, length)
                    if (size != length) {
                        val m = getMatch(file)
                        m.ingestEvents(true)
                    }

                    fileSizes[file.absolutePath] = length
                }

                for (file in liveapiFolder.listFiles()!!) {
                    checkFile(file)
                }
            }
        }.start()

        val watchService = FileSystems.getDefault().newWatchService()
        liveapiFolder.toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE)
        var key: WatchKey?
        do {
            key = watchService.take()
            for (event in key.pollEvents()) {
                val file = File(liveapiFolder, event.context().toString())
                if (!matchExists(file)) {
                    println("new match found!")
                    Thread.sleep(100)
                    getMatch(file).ingestEvents(true)
                }
                break
            }
            key.reset()
        } while (key != null)
    }

    fun matchExists(file: File): Boolean {
        for (m in matches) {
            if (m.filePath == file.absolutePath) {
                return true
            }
        }
        return false
    }

    fun getMatch(file: File): ApexMatch {
        for (m in matches) {
            if (m.filePath == file.absolutePath) {
                return m
            }
        }
        val match = ApexMatch(file.absolutePath)
        matches.add(match)
        match.ingestEvents(false)
        println("processed new match: ${file.name}")
        return match
    }
}