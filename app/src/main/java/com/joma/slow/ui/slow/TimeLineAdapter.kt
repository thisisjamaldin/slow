package com.joma.slow.ui.slow

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.joma.slow.databinding.ItemTimeLineBinding
import com.joma.slow.model.MVideo
import com.joma.slow.ui.utils.AdapterListener
import com.joma.slow.ui.utils.millisecondsToTime

class TimeLineAdapter(private val listener: AdapterListener) :
    RecyclerView.Adapter<TimeLineAdapter.ViewHolder>() {

    private val list: MutableList<MVideo> = ArrayList()

    fun setList(lst: List<MVideo>) {
        Log.e("--------erer", "$lst")
        this.list.clear()
        this.list.addAll(lst)
        notifyDataSetChanged()
    }
    fun getSize(): Int {
        return list.size
    }

    fun getVideo(pos: Int): MVideo {
        return list[pos]
    }

    fun remove(pos: Int) {
        list.removeAt(pos)
        notifyItemRemoved(pos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimeLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(
        private val binding: ItemTimeLineBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(pos: Int) {
            val context = binding.root.context
            Glide.with(context).load(list[pos].thumb).into(binding.thumbnail)
            binding.time.text = millisecondsToTime(list[pos].duration)
        }
    }
}