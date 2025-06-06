package com.example.food_ordering_system.Adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.food_ordering_system.Domain.Foods
import com.example.food_ordering_system.databinding.ViewholderBestDealBinding

class BestFoodAdapter(
    val items: MutableList<Foods>
) : RecyclerView.Adapter<BestFoodAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderBestDealBinding) : RecyclerView.ViewHolder(binding.root)  {
        lateinit var titleTxt: TextView
        lateinit var priceTxt: TextView
        lateinit var starTxt: TextView
        lateinit var timeTxt: TextView
        lateinit var pic: ImageView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderBestDealBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvTitle.text=item.Title
        holder.binding.tvPrice.text = "${item.price} ETB"
        holder.binding.tvRating.text=item.star.toString()
        holder.binding.tvTime.text="${item.TimeValue} Min"
        Glide.with(holder.itemView.context)
            .load(item.imagepath)
            .apply(
                RequestOptions()
                .centerCrop()
                .transform(RoundedCorners(30))
            )
            .into(holder.binding.itemImage)


    }

    override fun getItemCount(): Int {
        return items.size
    }
}
