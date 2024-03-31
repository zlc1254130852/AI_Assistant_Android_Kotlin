package com.example.AI_Assistant

class ChatMessage(var content: String, var sendType: Int) {

    companion object {
        var ME_SEND = 1
        var CHATGPT_SEND = 2
    }
}
