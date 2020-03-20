package com.illill.phairy.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView


class CodiPagerAdapter() : PagerAdapter() {
    var items = listOf<Any>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val iv = PhotoView(container.context)

        Glide.with(container.context)
                .load(items[position])
                .thumbnail(0.1f)
                .into(iv)

        container.addView(iv)
        return iv
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View)
    }
    override fun isViewFromObject(view: View, `object`: Any) = view == `object` as View
    override fun getCount() = items.size
}