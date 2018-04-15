package com.joaomariodev.rmsfsensoractuationapp.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.joaomariodev.rmsfsensoractuationapp.R
import com.joaomariodev.rmsfsensoractuationapp.Services.UserDataService


class AppsAndDevicesAdapter(val context: Context) : BaseExpandableListAdapter() {


    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }


    //GROUPS
    override fun getGroup(p0: Int): Any {
        return UserDataService.appsList.get(p0).devicesList
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(position: Int, expanded: Boolean, convertView: View?, parent: ViewGroup?): View {

        val categoryView: View
        val holder : ViewHolderGroup

        if (convertView == null) {
            holder = ViewHolderGroup()

            categoryView = LayoutInflater.from(context).inflate(R.layout.list_group, null)
            holder.name = categoryView.findViewById(R.id.lblListHeader)

            categoryView.tag = holder
        } else{
            holder = convertView.tag as ViewHolderGroup
            categoryView = convertView
        }

        holder.name?.text = UserDataService.appsList[position].appID

        return categoryView
    }

    override fun getGroupId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getGroupCount(): Int {
        return UserDataService.appsList.size
    }

    //CHILDS
    override fun getChild(p0: Int, p1: Int): Any {
        return UserDataService.appsList[p0].devicesList[p1]
    }


    @SuppressLint("InflateParams")
    override fun getChildView(groupPosition: Int, childPosition: Int, LastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val categoryView: View
        val holder : ViewHolderChild

        if (convertView == null) {
            holder = ViewHolderChild()

            categoryView = LayoutInflater.from(context).inflate(R.layout.list_item, null)
            holder.name = categoryView.findViewById(R.id.lblListItem)

            categoryView.tag = holder
        } else{
            holder = convertView.tag as ViewHolderChild
            categoryView = convertView
        }

        holder.name?.text = UserDataService.appsList[groupPosition].devicesList[childPosition].deviceID

        return categoryView
    }

    override fun getChildId(p0: Int, p1: Int): Long {
        return p1.toLong()
    }

    override fun getChildrenCount(p0: Int): Int {
        return UserDataService.appsList[p0].devicesList.size
    }

    private class ViewHolderGroup {
        var name : TextView? = null
    }

    private class ViewHolderChild {
        var name : TextView? = null
    }


}