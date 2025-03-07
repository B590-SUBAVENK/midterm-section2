package com.iub.midterm_section2

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.iub.midterm_section2.databinding.PostItemBinding
import com.iub.midterm_section2.model.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import com.iub.midterm_section2.PhotoRepository
import kotlin.math.log


class PostHolder(private val binding: PostItemBinding)
    : RecyclerView.ViewHolder(binding.root) {
    private val TAG = "PostHolder"

    fun bind(post: Post) {
        val username = post.user?.username as String
        binding.tvUsername.text = username
        binding.tvDescription.text = post.description
        Log.d("PostHolder", "Timestamp: ${post.creationTimeMs} -> ${Date(post.creationTimeMs)}")
        binding.tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(
            post.creationTimeMs,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )

        if (post.imageUrl.isNotEmpty()) {
            Log.d("PostHolder", "Fetching image from: ${post.imageUrl}")

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val bitmap = PhotoRepository.get().fetchAndDecodeImage(post.imageUrl)

                    if (bitmap != null) {
                        binding.ivPost.setImageBitmap(bitmap)
                    } else {
                        Log.e("PostHolder", "Failed to decode image")
                        binding.ivPost.setImageResource(R.drawable.flower) // Fallback
                    }
                } catch (e: Exception) {
                    Log.e("PostHolder", "Image load failed: ${e.message}")
                    binding.ivPost.setImageResource(R.drawable.flower) // Fallback
                }
            }
        } else {
            Log.e("PostHolder", "Empty image URL, showing fallback image")
            binding.ivPost.setImageResource(R.drawable.flower)
        }
    }


    class PostsAdapter(
        private val posts: List<Post>
    ) : RecyclerView.Adapter<PostHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = PostItemBinding.inflate(inflater, parent, false)
            return PostHolder(binding)
        }

        override fun getItemCount(): Int {
            return posts.size
        }

        override fun onBindViewHolder(holder: PostHolder, position: Int) {
            val post = posts[position]
            holder.bind(post)
        }
    }
}