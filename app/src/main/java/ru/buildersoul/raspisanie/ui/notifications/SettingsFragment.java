package ru.buildersoul.raspisanie.ui.notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import ru.buildersoul.raspisanie.HelloActivity;
import ru.buildersoul.raspisanie.MainActivity;
import ru.buildersoul.raspisanie.R;

public class SettingsFragment extends Fragment {

    SharedPreferences myPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final Button button = root.findViewById(R.id.button_clear);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // Обработчик нажтия на кнопку сброса настроек
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().clear().apply();
                Intent intent = new Intent(getContext(), HelloActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        return root;
    }



}