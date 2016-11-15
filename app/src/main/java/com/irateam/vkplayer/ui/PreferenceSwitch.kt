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
        get() = switch.isChecked
        set(value) {
            switch.isChecked = value
        }

    var text: CharSequence
        get() = label.text
        set(value) {
            label.text = value
        }

    override fun isEnabled(): Boolean {
        return super.isEnabled()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        label.isEnabled = enabled
        switch.isEnabled = enabled
    }

    private lateinit var label: TextView
    private lateinit var switch: SwitchCompat

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
        label = getViewById(R.id.label)
        switch = getViewById(R.id.control_switch)

        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.PreferenceSwitch,
                    defStyleAttr,
                    defStyleRes)

            if (typedArray.hasValue(R.styleable.PreferenceSwitch_checked)) {
                isChecked = typedArray.getBoolean(R.styleable.PreferenceSwitch_checked, false)
            }

            if (typedArray.hasValue(R.styleable.PreferenceSwitch_enabled)) {
                isEnabled = typedArray.getBoolean(R.styleable.PreferenceSwitch_enabled, true)
            }

            if (typedArray.hasValue(R.styleable.PreferenceSwitch_text)) {
                text = typedArray.getString(R.styleable.PreferenceSwitch_text)
            }

            typedArray.recycle()
        }

        setOnClickListener {
            switch.toggle()
            assigner?.invoke()
        }
    }

    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        switch.setOnCheckedChangeListener { compoundButton, checked -> listener(checked) }
    }

    fun assignToPreferences(settingsService: SettingsService,
                            property: KMutableProperty1<SettingsService, Boolean>) {

        isChecked = property.get(settingsService)
        assigner = { property.set(settingsService, isChecked) }
    }
}