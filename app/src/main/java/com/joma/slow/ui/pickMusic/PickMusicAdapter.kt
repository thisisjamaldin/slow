package com.joma.slow.ui.pickMusic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.joma.slow.databinding.ItemMusicBinding
import com.joma.slow.model.MAudio
import com.joma.slow.model.MVideo
import com.joma.slow.ui.utils.dpToPx
import com.joma.slow.ui.utils.AdapterListener
import com.joma.slow.ui.utils.millisecondsToTime


class PickMusicAdapter(private val listener: AdapterListener) :
    RecyclerView.Adapter<PickMusicAdapter.ViewHolder>() {

    private val list: MutableList<MAudio> = ArrayList()

    fun setList(lst: List<MAudio>) {
        this.list.clear()
        this.list.addAll(lst)
        notifyDataSetChanged()
    }
    fun getList(): List<MAudio> {
        return list
    }
    fun addAudio(audio: MAudio) {
        list.add(audio)
        notifyDataSetChanged()
    }

    fun get(pos: Int): MAudio {
        return list[pos]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(pos: Int) {
            binding.duration.text = millisecondsToTime(list[pos].duration)
            binding.name.text = list[pos].name
            var size = "${list[pos].size}"
            size = if (size.toInt() < 999) {
                "$size b"
            } else if (size.toInt() < 999999) {
                "${size.toInt() / 1000} kb"
            } else if (size.toInt() < 999999999) {
                "${size.toInt() / 1000000} mb"
            } else {
                "${size.toInt() / 1000000000} gb"
            }
            binding.size.text = size
            binding.root.setOnClickListener { listener.click(absoluteAdapterPosition) }
        }
    }
}