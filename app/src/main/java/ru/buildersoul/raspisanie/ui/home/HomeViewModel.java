package ru.buildersoul.raspisanie.ui.home;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ru.buildersoul.raspisanie.MainActivity;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;


    public HomeViewModel() {
        mText = new MutableLiveData<>();


        mText.setValue("Проверка");


    }

    public LiveData<String> getText() {
        return mText;
    }
}