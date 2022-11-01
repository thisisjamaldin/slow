package com.joma.slow.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.joma.slow.databinding.ItemGalleryBinding
import com.joma.slow.model.MVideo
import com.joma.slow.ui.utils.dpToPx
import com.joma.slow.ui.utils.AdapterListener
import com.joma.slow.ui.utils.millisecondsToTime


class GalleryAdapter(private val listener: AdapterListener) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val list: MutableList<MVideo> = ArrayList()

    fun setList(lst: List<MVideo>) {
        this.list.clear()
        this.list.addAll(lst)
        notifyDataSetChanged()
    }

    fun remove(pos: Int) {
        list.removeAt(pos)
        notifyItemRemoved(pos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(
        private val binding: ItemGalleryBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(pos: Int) {
            val context = binding.root.context
            Glide.with(context).load(list[pos].thumb).into(binding.thumbnail)
            val params = binding.thumbnail.layoutParams
            params.height = context.resources.displayMetrics.widthPixels / 3 - dpToPx(context, 24)
            binding.thumbnail.layoutParams = params
            binding.duration.text = millisecondsToTime(list[pos].duration)
        }
    }
}