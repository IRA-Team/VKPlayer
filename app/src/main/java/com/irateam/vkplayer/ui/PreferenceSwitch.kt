package com.irateam.vkplayer.ui

import android.content.Context
import android.support.v7.widget.SwitchCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.api.service.SettingsService
import com.irateam.vkplayer.util.extension.getViewById
import kotlin.reflect.KMutableProperty1

class PreferenceSwitch : RelativeLayout {

    private var assigner: (() -> Unit)? = null

    var isChecked: Boolean
        get() = switchView.isChecked
        set(value) {
            switchView.isChecked = value
        }

    var title: CharSequence
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    override fun isEnabled(): Boolean {
        return super.isEnabled()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        titleView.isEnabled = enabled
        switchView.isEnabled = enabled
    }

    private lateinit var titleView: TextView
    private lateinit var switchView: SwitchCompat

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context,
                attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context,
                attrs: AttributeSet,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    constructor(context: Context,
                attrs: AttributeSet,
                defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    fun init(context: Context,
             attrs: AttributeSet? = null,
             defStyleAttr: Int = 0,
             defStyleRes: Int = 0) {

        LayoutInflater.from(context).inflate(R.layout.content_preference_switch, this)
        titleView = getViewById(R.id.title)
        switchView = getViewById(R.id.control_switch)

        if (attrs != null) {
            val attrArray = intArrayOf(
                    android.R.attr.checked,
                    android.R.attr.enabled,
                    android.R.attr.title)

            val typedArray = context.theme.obtainStyledAttributes(
                    attrs,
                    attrArray,
                    defStyleAttr,
                    defStyleRes)

            if (typedArray.hasValue(0)) {
                isChecked = typedArray.getBoolean(0, false)
            }

            if (typedArray.hasValue(1)) {
                isEnabled = typedArray.getBoolean(1, true)
            }

            if (typedArray.hasValue(2)) {
                title = typedArray.getString(2)
            }

            typedArray.recycle()
        }

        setOnClickListener {
            switchView.toggle()
            assigner?.invoke()
        }
    }

    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        switchView.setOnCheckedChangeListener { compoundButton, checked -> listener(checked) }
    }

    fun assignToPreferences(settingsService: SettingsService,
                            property: KMutableProperty1<SettingsService, Boolean>) {

        isChecked = property.get(settingsService)
        assigner = { property.set(settingsService, isChecked) }
    }
}