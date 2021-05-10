package ru.buildersoul.raspisanie.ui.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.buildersoul.raspisanie.MainActivity;
import ru.buildersoul.raspisanie.R;
import ru.buildersoul.raspisanie.ui.dashboard.DashboardFragment;
import ru.buildersoul.raspisanie.util.BD;
import ru.buildersoul.raspisanie.util.PassisanieList;
import ru.buildersoul.raspisanie.util.ZamenaList;

public class HomeFragment extends Fragment {
    // Инициализация постоянного хранилища
    SharedPreferences myPreferences;
    SharedPreferences.Editor myEditor;
    private ProgressDialog pDialog;
    // Массив хранящий данные о расписании
    List<PassisanieList> paspisanieList_item = new ArrayList<>();
    // Создание основной таблицы
    TableLayout table;

    View root;
    // Проверкаа интернет соединения
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Получаем номер (имя) группы
        FloatingActionButton fab = root.findViewById(R.id.floatingActionButton);
        String special_name = myPreferences.getString("groups_name", "unknown");

        Spinner spinner = (Spinner) root.findViewById(R.id.spinner_dened);
        // Добавляем выпадающий список дни недели
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.my_spinner, new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"});
        adapter.setDropDownViewResource(R.layout.my_spinner);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = firstUpperCase(sdf.format(d));

        int position = adapter.getPosition(dayOfTheWeek);
        spinner.setSelection(position, true);


        if(isNetworkAvailable())
        {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    table = (TableLayout) root.findViewById(R.id.tablezamena);
                    table.setColumnShrinkable(0, true);
                    table.removeAllViews();
                    // Очистка массива расписаие
                    paspisanieList_item.clear();
                    // Получаем выбранный день недели из списака
                    String selected = spinner.getSelectedItem().toString();
                    // Загрузка расписания из базы данных по выбранному дню недели
                    new LoadPaspisanie(selected).execute();
                    fab.setVisibility(View.VISIBLE);

                    TableRow tableRow = new TableRow(getContext());
                    tableRow.setGravity(Gravity.CENTER);

                    TextView textView = new TextView(getContext());
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    textView.setText(special_name);
                    tableRow.addView(textView);
                    table.addView(tableRow);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            // Сохранение расписания в память
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myEditor = myPreferences.edit();

                    new SavePaspisanie("Понедельник").execute();
                    new SavePaspisanie("Вторник").execute();
                    new SavePaspisanie("Среда").execute();
                    new SavePaspisanie("Четверг").execute();
                    new SavePaspisanie("Пятница").execute();
                    new SavePaspisanie("Суббота").execute();
                    new SavePaspisanie("Воскресенье").execute();

                    Snackbar.make(view, "Сохранено", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    long mills = 200L;
                    Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator.hasVibrator()) {
                        vibrator.vibrate(mills);
                    }
                }
            });
        }
        else
        {
            //Сохраненные данные (Расписание) для просмотра в оффлайн режиме
            fab.setVisibility(View.INVISIBLE);
            Snackbar.make(container, "Нет интернета! Оффлайн содержимое", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            long mills = 200L;
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(mills);
            }
            // Режим без интернета (оффлайн режим), отображение расписания и выбор дня недели
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
                {
                    table = (TableLayout) root.findViewById(R.id.tablezamena);
                    table.setColumnShrinkable(0, true);
                    table.removeAllViews();
                    String selected = spinner.getSelectedItem().toString();
                    String text = myPreferences.getString(selected, "");

                    TableRow tableRow = new TableRow(getContext());
                    tableRow.setGravity(Gravity.CENTER);

                    TextView textView = new TextView(getContext());
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    textView.setText(special_name);
                    tableRow.addView(textView);
                    table.addView(tableRow);
                    TableRow tableRow0 = new TableRow(getContext());

                    tableRow0.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    for (int j = 0; j < 1; j++) {
                        TextView textView1 = new TextView(getContext());
                        textView1.setText(Html.fromHtml(text));
                        textView1.setTextSize(16);
                        tableRow0.addView(textView1, j);
                    }

                    table.addView(tableRow0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {}
            });
        }

        return root;
    }
    // Делает первую букувуу заглавной (дни недели)
    public String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return "";
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    // Метод обновления расписания при разварачивании приложения
    @Override
    public void onResume() {
        super.onResume();
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Получаем номер (имя) группы
        String special_name = myPreferences.getString("groups_name", "unknown");
        FloatingActionButton fab = root.findViewById(R.id.floatingActionButton);

        Spinner spinner = (Spinner) root.findViewById(R.id.spinner_dened);
        // Добавляем выпадающий список дни недели
        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), R.layout.my_spinner, new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"});
        adapter.setDropDownViewResource(R.layout.my_spinner);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        // Получаем текущий день недели
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = firstUpperCase(sdf.format(d));

        int position = adapter.getPosition(dayOfTheWeek);
        spinner.setSelection(position, true);
        // Проверкаа интернет соединения
        if(isNetworkAvailable())
        {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
                {
                    table = (TableLayout) root.findViewById(R.id.tablezamena);
                    table.setColumnShrinkable(0, true);
                    table.removeAllViews();
                    // Очистка массива расписание
                    paspisanieList_item.clear();
                    // Получаем выбранный день недели из списака
                    String selected = spinner.getSelectedItem().toString();
                    // Загрузка замен из базы данных по выбранному дню недели
                    new LoadPaspisanie(selected).execute();
                    fab.setVisibility(View.VISIBLE);

                    TableRow tableRow = new TableRow(getContext());
                    tableRow.setGravity(Gravity.CENTER);

                    TextView textView = new TextView(getContext());
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    textView.setText(special_name);
                    tableRow.addView(textView);
                    table.addView(tableRow);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView)
                {
                }
            });

            // Сохранение расписания в память
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myEditor = myPreferences.edit();

                    new SavePaspisanie("Понедельник").execute();
                    new SavePaspisanie("Вторник").execute();
                    new SavePaspisanie("Среда").execute();
                    new SavePaspisanie("Четверг").execute();
                    new SavePaspisanie("Пятница").execute();
                    new SavePaspisanie("Суббота").execute();
                    new SavePaspisanie("Воскресенье").execute();

                    Snackbar.make(view, "Сохранено", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    long mills = 200L;
                    Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator.hasVibrator()) {
                        vibrator.vibrate(mills);
                    }
                }
            });
        }
        else
        {

            fab.setVisibility(View.INVISIBLE);
            Snackbar.make(root, "Нет интернета! Оффлайн содержимое", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            long mills = 200L;
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(mills);
            }
            // Режим без интернета (оффлайн режим), отображение расписания и выбор дня недели
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
                {
                    table = (TableLayout) root.findViewById(R.id.tablezamena);
                    table.setColumnShrinkable(0, true);
                    table.removeAllViews();
                    String selected = spinner.getSelectedItem().toString();
                    String text = myPreferences.getString(selected, "");

                    TableRow tableRow = new TableRow(getContext());
                    tableRow.setGravity(Gravity.CENTER);

                    TextView textView = new TextView(getContext());
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    textView.setText(special_name);
                    tableRow.addView(textView);
                    table.addView(tableRow);
                    TableRow tableRow0 = new TableRow(getContext());

                    tableRow0.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    for (int j = 0; j < 1; j++) {
                        TextView textView1 = new TextView(getContext());
                        textView1.setText(Html.fromHtml(text));
                        textView1.setTextSize(16);
                        tableRow0.addView(textView1, j);
                    }

                    table.addView(tableRow0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {}
            });
        }
    }

    class LoadPaspisanie extends AsyncTask<String, String, String>
    {
        String den;
        public LoadPaspisanie(String den){ this.den = den; }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Загрузка. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args)
        {
            //Очистка маасива (Расписание)
            paspisanieList_item.clear();
            // Подключение к базе данный
            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {
                // Получение расписание по дню недели
                PreparedStatement selectStatement = conn.prepareStatement("select * from passisanie, dic, prepodav WHERE passisanie.dic = dic.id AND passisanie.prepodavatel = prepodav.id ORDER BY TIME(passisanie.time_start), passisanie.nedel");
                // Получаем груупу id
                ResultSet rs = selectStatement.executeQuery();
                String groups_id  = myPreferences.getString("groups_id", "");


                while (rs.next())
                {
                    // Добавляем в массив только те элементы которые совпадают с группой
                    if(groups_id.equalsIgnoreCase(rs.getString("groups_id")) && den.equalsIgnoreCase(rs.getString("den_nedel"))) {
                        String strings = (rs.getString("familia")+" " + rs.getString("name")+" " + rs.getString("otcestvo"));
                        //String name = strings[0] + " " + strings[1].charAt(0) + ". " + strings[2].charAt(0) + ".";
                        // Добовляем в массив расписание
                        paspisanieList_item.add(new PassisanieList(rs.getString("time_start"),
                                rs.getString("time_end"),
                                rs.getString("dic_name"),
                                rs.getString("type_zan"),
                                strings,
                                rs.getString("korpys"),
                                rs.getString("ayditoria"),
                                rs.getString("nedel")
                        ));
                    }
                }

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String file_url)
        {
            // Закрытие диалогового окна
            pDialog.dismiss();
            table.removeAllViews();
            TableRow tableRow2 = new TableRow(getContext());
            tableRow2.setGravity(Gravity.CENTER);

            if(paspisanieList_item.size() == 0) return;
            // Добавление в таблицу столбцов
            for(int i = 1; i < paspisanieList_item.size() + 1; ++i)
            {
                TableRow tableRow = new TableRow(getContext());
                tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                // Добавление в таблицу строк
                for (int j = 0; j < 1; j++) {
                    TextView textView = new TextView(getContext());
                    textView.setText(Html.fromHtml(paspisanieList_item.get(i - 1).toString()));
                    textView.setTextSize(16);
                    tableRow.addView(textView, j);
                }

                table.addView(tableRow);
            }
        }
    }

    class SavePaspisanie extends AsyncTask<String, String, String>
    {
        String den;
        // Сохранение в оффлайн режиме
        public SavePaspisanie(String den){this.den = den;}

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

        }

        protected String doInBackground(String... args)
        {
            // Подключение к базе данный
            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {
                // Получение расписание по дню недели
                PreparedStatement selectStatement = conn.prepareStatement("select * from passisanie, dic, prepodav WHERE passisanie.dic = dic.id AND passisanie.prepodavatel = prepodav.id ORDER BY TIME(passisanie.time_start), passisanie.nedel");

                ResultSet rs = selectStatement.executeQuery();
                // Получаем груупу id
                String groups_id  = myPreferences.getString("groups_id", "");

                StringBuilder allText = new StringBuilder();

                while (rs.next())
                {
                    // Добавляем в массив только те элементы которые совпадают с группой
                    if(groups_id.equalsIgnoreCase(rs.getString("groups_id")) && den.equalsIgnoreCase(rs.getString("den_nedel"))) {
                        String strings = (rs.getString("familia")+" " + rs.getString("name")+" " + rs.getString("otcestvo"));
                        // Добовляем в строковый массив расписание
                        PassisanieList raspis = new PassisanieList(rs.getString("time_start"),
                                rs.getString("time_end"),
                                rs.getString("dic_name"),
                                rs.getString("type_zan"),
                                strings,
                                rs.getString("korpys"),
                                rs.getString("ayditoria"),
                                rs.getString("nedel")
                        );
                        allText.append(raspis.toString());
                    }
                }
                // Сохранение в память расписание
                myEditor.putString(den, allText.toString());
                myEditor.commit();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String file_url)
        {
            table.removeAllViews();
            TableRow tableRow2 = new TableRow(getContext());
            tableRow2.setGravity(Gravity.CENTER);

            if(paspisanieList_item.size() == 0) return;

            for(int i = 1; i < paspisanieList_item.size() + 1; ++i)
            {
                TableRow tableRow = new TableRow(getContext());
                tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                for (int j = 0; j < 1; j++) {
                    TextView textView = new TextView(getContext());
                    textView.setText(Html.fromHtml(paspisanieList_item.get(i - 1).toString()));
                    textView.setTextSize(16);
                    tableRow.addView(textView, j);
                }

                table.addView(tableRow);
            }
        }
    }
}