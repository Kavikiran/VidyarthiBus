package com.kavikiran.vidyarthibus.host

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kavikiran.vidyarthibus.R
import com.kavikiran.vidyarthibus.model.Route

class RouteAdapter(
    private val routes: List<Route>,
    private val onRouteClick: (Route) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    inner class RouteViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvRouteName: TextView = itemView.findViewById(R.id.tvRouteName)
        val tvRouteStops: TextView = itemView.findViewById(R.id.tvRouteStops)

        fun bind(route: Route) {
            // Set route name
            tvRouteName.text = route.name

            // Join stops with arrow separator
            tvRouteStops.text = route.stops.joinToString(" → ")

            // Handle click
            itemView.setOnClickListener {
                onRouteClick(route)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_route, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RouteViewHolder,
        position: Int
    ) {
        holder.bind(routes[position])
    }

    override fun getItemCount(): Int {
        return routes.size
    }
}