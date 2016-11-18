package com.irateam.vkplayer.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.util.extension.getViewById
import com.irateam.vkplayer.util.extension.layoutInflater

class Preference : RelativeLayout {

    var title: String
        get() = titleView.text.toString()
        set(value) {
            titleView.text = value
        }

    var subtitle: String
        get() = subtitleView.text.toString()
        set(value) {
            subtitleView.text = value
        }

    private lateinit var titleView: TextView
    private lateinit var subtitleView: TextView

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context,
                attrs: AttributeSet?) : super(context, attrs) {

        init(context, attrs)
    }

    constructor(context: Context,
                attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        init(context, attrs, defStyleAttr)
    }

    constructor(context: Context,
                attrs: AttributeSet?,
                defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {

        init(context, attrs, defStyleAttr, defStyleRes)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        titleView.isEnabled = enabled
        subtitleView.isEnabled = enabled
    }

    fun init(context: Context,
             attrs: AttributeSet? = null,
             defStyleAttr: Int = 0,
             defStyleRes: Int = 0) {

        context.layoutInflater.inflate(R.layout.content_preference, this)

        titleView = getViewById(R.id.title)
        subtitleView = getViewById(R.id.subtitle)

        if (attrs != null) {
            val attrArray = intArrayOf(
                    android.R.attr.title,
                    android.R.attr.subtitle)

            val typedArray = context.theme.obtainStyledAttributes(
                    attrs,
                    attrArray,
                    defStyleAttr,
                    defStyleRes)

            if (typedArray.hasValue(0)) {
                title = typedArray.getString(0)
            }

            if (typedArray.hasValue(1)) {
                subtitle = typedArray.getString(1)
            }

            typedArray.recycle()
        }
    }


}