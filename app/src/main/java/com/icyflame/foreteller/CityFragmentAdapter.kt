package com.icyflame.foreteller

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter

class CityFragmentAdapter(fm: FragmentManager, private var fragmentList : MutableList<CityFragment>) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int = fragmentList.size
    override fun getItem(position: Int): Fragment = fragmentList[position]
    override fun getItemPosition(`object`: Any): Int {
        val index = fragmentList.indexOf(`object`)
        return if (index == -1)
            PagerAdapter.POSITION_NONE
        else
            index
    }

    fun setFragmentLis(fragmentList : MutableList<CityFragment>){
        this.fragmentList = fragmentList
        notifyDataSetChanged()
    }
}