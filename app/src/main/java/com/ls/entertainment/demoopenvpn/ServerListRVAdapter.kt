package com.ls.entertainment.demoopenvpn

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ls.entertainment.demoopenvpn.ServerListRVAdapter.MyViewHolder

class ServerListRVAdapter(
	private val serverLists: ArrayList<Server>,
	private val mContext: Context
) : RecyclerView.Adapter<MyViewHolder>() {
	private val listener: NavItemClickListener = mContext as NavItemClickListener
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
		val view = LayoutInflater.from(mContext).inflate(R.layout.server_list_view, parent, false)
		return MyViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
		holder.serverCountry.text = serverLists[position].country
		Glide.with(mContext).load(serverLists[position].flagUrl).into(holder.serverIcon)
		holder.serverItemLayout.setOnClickListener { listener.clickedItem(position) }
	}
	
	override fun getItemCount(): Int {
		return serverLists.size
	}
	
	inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		var serverItemLayout: LinearLayout
		var serverIcon: ImageView
		var serverCountry: TextView
		
		init {
			serverItemLayout = itemView.findViewById(R.id.serverItemLayout)
			serverIcon = itemView.findViewById(R.id.iconImg)
			serverCountry = itemView.findViewById(R.id.countryTv)
		}
	}
}