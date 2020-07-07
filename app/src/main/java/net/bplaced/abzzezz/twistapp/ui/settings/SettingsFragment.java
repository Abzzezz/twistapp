/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import id.ionbit.ionalert.IonAlert;
import net.bplaced.abzzezz.twistapp.R;
import net.bplaced.abzzezz.twistapp.TwistAppMain;


public class SettingsFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_layout, container, false);
        getParentFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragmentInner()).commit();
        return root;
    }

    public static class SettingsFragmentInner extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference clearTrackerButton = findPreference("clear_tracker");
            clearTrackerButton.setOnPreferenceClickListener(preference -> {
                new IonAlert(getActivity(), IonAlert.WARNING_TYPE)
                        .setTitleText("Clear download log?")
                        .setContentText("Download log will not be recoverable")
                        .setConfirmText("Yes, clear log")
                        .setConfirmClickListener(ionAlert -> {
                            TwistAppMain.getINSTANCE().getDownloadTracker().clearTrack();
                            ionAlert.dismissWithAnimation();
                        }).setCancelText("Abort").setCancelClickListener(IonAlert::dismissWithAnimation)
                        .show();
                return true;
            });
        }
    }

}