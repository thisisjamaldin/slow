package com.joma.slow.ui.gallery

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.joma.slow.databinding.ItemGalleryBinding
import com.joma.slow.ui.utils.AdapterListener

class GalleryAdapter(private val listener: AdapterListener) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val list: MutableList<Uri> = ArrayList()

    fun setList(lst: List<Uri>) {
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
            Log.e("-------$pos", "${list[pos]}")
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(binding.duration.context, list[pos])
                val bitmap1 = retriever.getFrameAtTime(1)
                Glide.with(binding.root.context).load(bitmap1).into(binding.thumbnail)
            } catch (e: Exception){
                Log.e("--------234234", "${e.message}")}
//            binding.duration.text = "${list[pos]}"
        }
    }
}