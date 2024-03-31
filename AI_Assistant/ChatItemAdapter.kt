package com.example.AI_Assistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.AI_Assistant.ChatItemAdapter.ChatItemViewHolder

class ChatItemAdapter(private val data: List<ChatMessage>) :
    RecyclerView.Adapter<ChatItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val chatView = layoutInflater.inflate(R.layout.rv_item, null)
        val chatItemViewHolder = ChatItemViewHolder(chatView)
        chatItemViewHolder.setIsRecyclable(false)
        return chatItemViewHolder
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        val chatMessage = data[position]
        if (chatMessage.sendType == ChatMessage.CHATGPT_SEND) {
            holder.tv_item_chatgpt.text = chatMessage.content
            holder.tv_item_me.visibility = View.GONE
            holder.tv_item_me_border.visibility = View.GONE
            holder.tv_item_me_header.visibility = View.GONE
            holder.tv_item_me_outer_border.visibility = View.GONE
        } else {
            holder.tv_item_me.text = chatMessage.content
            holder.tv_item_chatgpt.visibility = View.GONE
            holder.tv_item_chatgpt_border.visibility = View.GONE
            holder.tv_item_chatgpt_header.visibility = View.GONE
            holder.tv_item_chatgpt_outer_border.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ChatItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_item_chatgpt: TextView
        val tv_item_me: TextView
        val tv_item_chatgpt_border: RelativeLayout
        val tv_item_me_border: RelativeLayout
        val tv_item_chatgpt_outer_border: RelativeLayout
        val tv_item_me_outer_border: RelativeLayout
        val tv_item_chatgpt_header: TextView
        val tv_item_me_header: TextView

        init {
            tv_item_chatgpt = itemView.findViewById(R.id.tv_item_chatgpt)
            tv_item_me = itemView.findViewById(R.id.tv_item_me)
            tv_item_chatgpt_header = itemView.findViewById(R.id.tv_item_chatgpt_header)
            tv_item_me_header = itemView.findViewById(R.id.tv_item_me_header)
            tv_item_chatgpt_outer_border = itemView.findViewById(R.id.tv_item_chatgpt_outer_border)
            tv_item_me_outer_border = itemView.findViewById(R.id.tv_item_me_outer_border)
            tv_item_chatgpt_border = itemView.findViewById(R.id.tv_item_chatgpt_border)
            tv_item_me_border = itemView.findViewById(R.id.tv_item_me_border)
        }
    }
}
