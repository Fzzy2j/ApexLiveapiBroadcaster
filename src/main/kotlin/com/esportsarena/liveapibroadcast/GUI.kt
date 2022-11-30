package com.esportsarena.liveapibroadcast

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import java.awt.*
import java.io.File
import java.io.FileWriter
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class GUI {

    private val gson = Gson()
    private val configFile: LinkedTreeMap<String, String>
    private val textFields = hashMapOf<String, JTextField>()
    private val checkBoxes = hashMapOf<String, JCheckBox>()
    private var lines = 0
    private val frame = JFrame("Liveapi Broadcaster")

    private val statusLabel = JLabel("")

    var isConnected: Boolean = false
        set(value) {
            if (value) {
                statusLabel.text = "CONNECTED"
            } else {
                statusLabel.text = "NOT CONNECTED"
            }
            field = value
        }

    val currentSpec: String
        get() = textFields["Apex Account Name"]!!.text

    val liveApiDirectory: String
        get() = textFields["Liveapi Directory"]!!.text

    init {
        val f = File("config.json")
        if (!f.exists()) f.createNewFile()
        val text = f.readLines().joinToString("")
        configFile = if (text.length < 2)
            LinkedTreeMap<String, String>()
        else
            gson.fromJson(text, object : TypeToken<Map<String, String>>() {}.type)

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.preferredSize = Dimension(580, 140)
        frame.isResizable = false

        //initTextField("Apex Account Name")
        initTextField("Liveapi Directory")

        statusLabel.font = Font(statusLabel.font.name, Font.PLAIN, 26)
        statusLabel.bounds = Rectangle(5, 5 + (lines++ * 25), 400, 45)
        frame.add(statusLabel)

        frame.layout = null

        frame.setLocationRelativeTo(null)
        frame.pack()
        frame.isVisible = true
    }

    private fun initTextField(key: String) {
        val label = JLabel("$key: ")
        label.bounds = Rectangle(5, 5 + (lines * 25), 150, 25)
        frame.add(label)
        textFields[key] = JTextField(configFile.getOrDefault(key, ""))
        textFields[key]!!.bounds = Rectangle(155, 5 + (lines++ * 25), 400, 25)

        textFields[key]!!.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                saveToFile()
            }

            override fun removeUpdate(e: DocumentEvent) {
                saveToFile()
            }

            override fun changedUpdate(e: DocumentEvent) {
            }
        })

        frame.add(textFields[key])
    }

    private fun initCheckBox(key: String): JPanel {
        val panel = JPanel()
        panel.layout = FlowLayout(FlowLayout.LEFT)
        panel.add(JLabel("$key: "))
        checkBoxes[key] = JCheckBox()
        checkBoxes[key]!!.isSelected = configFile.getOrDefault(key, "False") == "True"

        checkBoxes[key]!!.addItemListener { saveToFile() }

        panel.add(checkBoxes[key])
        return panel
    }

    private fun saveToFile() {
        val save = hashMapOf<String, String>()
        for ((key, textfield) in textFields) {
            save[key] = textfield.text
        }
        for ((key, checkbox) in checkBoxes) {
            save[key] = if (checkbox.isSelected) "True" else "False"
        }
        val writer = FileWriter("config.json")
        writer.write(gson.toJson(save))
        writer.close()
    }
}