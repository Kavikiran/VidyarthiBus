package com.kavikiran.vidyarthibus.watcher

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kavikiran.vidyarthibus.R
import com.kavikiran.vidyarthibus.model.AutoContact

class AutoAdapter(
    private val contacts: List<AutoContact>
) : RecyclerView.Adapter<AutoAdapter.AutoViewHolder>() {

    inner class AutoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvAutoName: TextView = itemView.findViewById(R.id.tvAutoName)
        val tvAutoArea: TextView = itemView.findViewById(R.id.tvAutoArea)
        val tvAutoPhone: TextView = itemView.findViewById(R.id.tvAutoPhone)
        val btnCall: MaterialButton = itemView.findViewById(R.id.btnCall)

        fun bind(contact: AutoContact) {
            // Set contact details
            tvAutoName.text = contact.name
            tvAutoArea.text = "📍 ${contact.area}"
            tvAutoPhone.text = "📞 ${contact.phone}"

            // Call button click
            btnCall.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:${contact.phone}")
                )
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auto_contact, parent, false)
        return AutoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AutoViewHolder,
        position: Int
    ) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int {
        return contacts.size
    }
}