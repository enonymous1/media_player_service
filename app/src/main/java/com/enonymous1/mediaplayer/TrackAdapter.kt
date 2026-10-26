package com.enonymous1.mediaplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enonymous1.mediaplayer.databinding.ItemTrackBinding

/** Simple list of tracks. The selected row is highlighted; taps play that track. */
class TrackAdapter(
    private val onTrackClick: (Int) -> Unit,
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    var tracks: List<Track> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedIndex: Int = -1
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = tracks.size

    inner class TrackViewHolder(private val binding: ItemTrackBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track, isSelected: Boolean) {
            binding.trackTitle.text = track.title
            binding.trackArtist.text = track.artist
            binding.trackDuration.text = formatDuration(track.durationMs)
            binding.root.isSelected = isSelected
            binding.root.alpha = if (isSelected) 1.0f else 0.85f
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onTrackClick(position)
            }
        }
    }
}
