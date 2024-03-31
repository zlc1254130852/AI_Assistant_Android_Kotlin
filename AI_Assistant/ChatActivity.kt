package com.example.AI_Assistant

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.concurrent.TimeUnit

class ChatActivity : Activity(), View.OnClickListener {
    private lateinit var data: MutableList<ChatMessage>
    private var chatItemAdapter: ChatItemAdapter? = null
    private lateinit var asrSendBtn: ImageButton
    private lateinit var chatSpeakBtn: Button
    private var chatInputEt: EditText? = null
    private lateinit var btnSend: Button
    private var chatAsrUtil: ChatAsrUtil? = null
    private var chatTtsUtil: ChatTtsUtil? = null
    private lateinit var connection:Connection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_layout)
        btnSend = findViewById(R.id.btn_send)
        btnSend.setOnClickListener(this)
        asrSendBtn = findViewById(R.id.btn_asr_send)
        asrSendBtn.setOnClickListener(this)
        chatSpeakBtn = findViewById(R.id.btn_chat_speak)
        chatSpeakBtn.setOnClickListener(this)
        chatSpeakBtn.setOnTouchListener(touchListener)
        chatInputEt = findViewById(R.id.et_chat_input)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_chat)
        data = ArrayList()
        chatItemAdapter = ChatItemAdapter(data)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatItemAdapter
        chatAsrUtil = ChatAsrUtil(this)
        chatTtsUtil = ChatTtsUtil(this)

//        window.setBackgroundDrawableResource(R.color.colorPrimary)
//        val nightModeSwitch = NightModeSwitch(this,window)

        Thread(Runnable {

//            val sql = "INSERT INTO chat (user, message) VALUES (?, ?)"

            try {
                connection = establishConnection()
//                    executeInsert(connection, sql, "me", "hello")
                executeQuery(connection, "SELECT * FROM chat")
                Log.d("SQL Success","连接数据库成功。")
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e("SQL Error","连接数据库时发生错误。")
            }
        }).start()
    }

    private fun establishConnection(): Connection {
        Class.forName("com.mysql.jdbc.Driver")
        return DriverManager.getConnection(
            "jdbc:mysql://sql5.freemysqlhosting.net:3306/sql5667235",
            "sql5667235", "viLq8NBU13"
        )
    }

    fun executeInsert(connection: Connection, sql: String, user: String, message: String) {
        val preparedStatement: PreparedStatement = connection.prepareStatement(sql)
        preparedStatement.setString(1, user)
        preparedStatement.setString(2, message)
        preparedStatement.executeUpdate()
        preparedStatement.close()
    }

    private fun executeQuery(connection: Connection, sql: String) {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(sql)

        val resultStringBuilder = StringBuilder()

        while (resultSet.next()) {

            if ("${resultSet.getString("user")}"=="me")
            {
                addChatMessage(ChatMessage("${resultSet.getString("message")}", ChatMessage.ME_SEND))
            }
            else {
                addChatMessage(ChatMessage("${resultSet.getString("message")}", ChatMessage.CHATGPT_SEND))
            }
        }

        resultSet.close()
        statement.close()
        connection.close()

        runOnUiThread {
            Log.d("Print messages", resultStringBuilder.toString())
        }
    }

    private val touchListener = OnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            chatAsrUtil!!.onClick(MotionEvent.ACTION_DOWN)
            chatSpeakBtn!!.text = "release to send"
        } else if (event.action == MotionEvent.ACTION_UP) {
            chatAsrUtil!!.onClick(MotionEvent.ACTION_UP)
            chatSpeakBtn!!.text = "press to speak"
            sendToChatGPTAudio()
        }
        false
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onClick(v: View) {
        if (v.id == R.id.btn_send) {
            Log.d("", "clicked")
            val chatStr = chatInputEt!!.text.toString()
            val chatMessage = ChatMessage("" + chatStr, ChatMessage.ME_SEND)
            addChatMessage(chatMessage)
            chatInputEt!!.setText("")
            sendToChatGPT(chatStr)

            Thread(Runnable {

            val sql = "INSERT INTO chat (user, message) VALUES (?, ?)"

                try {
                    connection = establishConnection()
                    executeInsert(connection, sql, "me", chatStr)
                    Log.d("SQL Success","连接数据库成功。")
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e("SQL Error","连接数据库时发生错误。")
                }
            }).start()

        } else if (v.id == R.id.btn_asr_send) {
            if (btnSend!!.visibility != View.GONE) {
                asrSendBtn!!.setImageResource(R.drawable.keyboard)
                chatSpeakBtn!!.visibility = View.VISIBLE
                chatInputEt!!.visibility = View.GONE
                btnSend!!.visibility = View.GONE
            } else {
                asrSendBtn!!.setImageResource(R.drawable.mic)
                chatSpeakBtn!!.visibility = View.GONE
                chatInputEt!!.visibility = View.VISIBLE
                btnSend!!.visibility = View.VISIBLE
            }
        } else if (v.id == R.id.btn_chat_speak) {
        }
    }

    private fun addChatMessage(chatMessage: ChatMessage) {
        runOnUiThread {
            data!!.add(chatMessage)
            chatItemAdapter!!.notifyDataSetChanged()
        }
    }

    private fun addChatMessageStream(chatMessage: ChatMessage) {
        runOnUiThread {
            data!![data!!.size - 1] = chatMessage
            Log.d("size", Integer.toString(data!!.size))
            chatItemAdapter!!.notifyDataSetChanged()
        }
    }

    private fun sendToChatGPT(chatStr: String) {
        ChatGptUtil.sendHttpRequest(chatStr, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("result", e.toString())
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val inputStream = response.body()?.byteStream()
                val reader: Reader = InputStreamReader(inputStream, "utf-8")
                val bufferedReader = BufferedReader(reader)
                bufferedReader.readLine()
                var str: String
                var strTotal = ""
                val chatMessageGpt = ChatMessage("", ChatMessage.CHATGPT_SEND)
                addChatMessage(chatMessageGpt)
                try {
                    while (bufferedReader.readLine().also { str = it } != null) {
                        if (str.length > 6 && str != "data: [DONE]") {
//                            Log.d("chunk",str);
                            val parseResult = ChatGptUtil.parseStream(str.substring(6))
                            strTotal += parseResult
                            chatMessageGpt.content = "" + strTotal
                            addChatMessageStream(chatMessageGpt)
                        }
                    }
                    Thread(Runnable {

                        val sql = "INSERT INTO chat (user, message) VALUES (?, ?)"

                        try {
                            connection = establishConnection()
                            executeInsert(connection, sql, "chatGPT", strTotal)
                            Log.d("SQL Success","连接数据库成功。")
                        } catch (e: SQLException) {
                            e.printStackTrace()
                            Log.e("SQL Error","连接数据库时发生错误。")
                        }
                    }).start()

                    chatTtsUtil!!.ttsStart(strTotal)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {

                    inputStream?.close()
                }
            }
        })
    }

    private fun sendToChatGPTAudio() {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).build()
        val file = File(
            ContextCompat.getExternalFilesDirs(
                applicationContext,
                Environment.DIRECTORY_DCIM
            )[0].absolutePath + "/openai.mp3"
        )
        val fileBody = RequestBody.create(MediaType.parse("audio/mp3"), file)
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "openai.mp3", fileBody)
            .addFormDataPart("model", "whisper-1")
            .build()
        val request = Request.Builder()
            .addHeader("Content-Type", "application/form-data")
            .addHeader("Authorization", "")
            .url("https://api.openai.com/v1/audio/transcriptions")
            .post(requestBody)
            .build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG-FAILED：", e.toString() + "")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val string = response.body()?.string()
                Log.d("TAG-SUCCESS：", string + "")
                try {
                    val jsonObject = JSONObject(string)
                    val parseString = jsonObject.getString("text")
                    val chatMessageGpt = ChatMessage("" + parseString + "", ChatMessage.ME_SEND)
                    addChatMessage(chatMessageGpt)
                    Log.d("check", parseString)
                    sendToChatGPT(parseString)

                    Thread(Runnable {

                        val sql = "INSERT INTO chat (user, message) VALUES (?, ?)"

                        try {
                            connection = establishConnection()
                            executeInsert(connection, sql, "me", parseString)
                            Log.d("SQL Success","连接数据库成功。")
                        } catch (e: SQLException) {
                            e.printStackTrace()
                            Log.e("SQL Error","连接数据库时发生错误。")
                        }
                    }).start()

                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        chatAsrUtil!!.destroy()
    }
}
