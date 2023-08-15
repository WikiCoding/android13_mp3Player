package com.wikicoding.musicplayer.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wikicoding.musicplayer.databinding.RvItemBinding
import com.wikicoding.musicplayer.models.Song

class MainActivityAdapter(private val items: ArrayList<Song>): RecyclerView.Adapter<MainActivityAdapter.ViewHolder>() {
    private var onClicked: OnClickList? = null

    class ViewHolder(binding: RvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvMusic: TextView = binding.tvMusic
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = items[position]
        holder.tvMusic.text = model.title

        holder.tvMusic.setOnClickListener {
            onClicked?.onClick(position, model)
        }

        if (model.isPlaying) {
            holder.tvMusic.setTypeface(null, Typeface.BOLD)
        } else {
            holder.tvMusic.setTypeface(null, Typeface.NORMAL)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnClickList {
        fun onClick(position: Int, model: Song)
    }

    fun setOnClick(onClick: OnClickList) {
        this.onClicked = onClick
    }
}