package com.example.myfocusapp.app

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.myfocusapp.R


class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}