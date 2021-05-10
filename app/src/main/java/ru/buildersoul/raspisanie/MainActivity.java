package ru.buildersoul.raspisanie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity
{
    // Инициализация постоянного хранилища
    SharedPreferences myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
// Получаем из насторек группу
        String name = myPreferences.getString("groups_name", "unknown");

// Проверяем, что в настройкак сожержатся название группы
        if(name.equalsIgnoreCase("unknown")) {
            Intent intent = new Intent(MainActivity.this, HelloActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        //Передача каждого идентификатора меню как набора идентификаторов, поскольку каждое
        //меню следует рассматривать как пункты назначения верхнего уровня.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }


}