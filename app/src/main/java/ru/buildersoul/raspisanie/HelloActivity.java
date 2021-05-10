package ru.buildersoul.raspisanie;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.navigation.NavigationView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ru.buildersoul.raspisanie.util.BD;
import ru.buildersoul.raspisanie.util.FacultetList;
import ru.buildersoul.raspisanie.util.GroupList;
import ru.buildersoul.raspisanie.util.KafedraList;
import ru.buildersoul.raspisanie.util.SpecialList;

public class HelloActivity extends AppCompatActivity
{
    private ProgressDialog pDialog;

    /** Массивы хранящие данные о
     * Факультетах
     * Кафедрах
     * Специальностях
     * Группах
     */
    List<FacultetList> facultet_item= new ArrayList<>();
    List<KafedraList> cafedra_item = new ArrayList<>();
    List<SpecialList> specialLists = new ArrayList<>();
    List<GroupList> groupLists = new ArrayList<>();

    // Инициализация постоянного хранилища
    SharedPreferences myPreferences;
    SharedPreferences.Editor myEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        // Инициализация работы с БД
        try
        {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch (ClassNotFoundException | IllegalAccessException | java.lang.InstantiationException e)
        {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
        }
        ////

        // Получаем общедоступную настройку, предоставляемую Android
        myPreferences = PreferenceManager.getDefaultSharedPreferences(HelloActivity.this);
        myEditor = myPreferences.edit();
        ////

        // Загружаем список факультетов из БД
        new LoadFacultet().execute();
    }

    // Создание пункта меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hello_menu, menu);
        return true;
    }

    // Нажатие кнопки сохранить
    public void onClickMenu(MenuItem item)
    {
        // Проверка, что список групп не пуст
        if(groupLists.size() > 0) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Хорошо", Toast.LENGTH_SHORT);
            toast.show();

            //Сохранение настроек
            myEditor.commit();
            //Переход на другой экран без возможности возврата обратно
            Intent intent = new Intent(HelloActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }


    public class LoadFacultet extends AsyncTask<String, String, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // Открытие диалогового окна без возможности закрытия
            pDialog = new ProgressDialog(HelloActivity.this);
            pDialog.setMessage("Загрузка. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args)
        {
            //Очистка маасивов (Факультетов, кафедр, спецальностей, групп)
            facultet_item.clear();
            cafedra_item.clear();
            specialLists.clear();
            groupLists.clear();
            //Подключение к БД
            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {
                //Получение всех факультетов с БД
                PreparedStatement selectStatement = conn.prepareStatement("select * from facyltet");
                ResultSet rs = selectStatement.executeQuery();

                while (rs.next()) { // Добавление в  массив всех факультетов
                    facultet_item.add(new FacultetList(rs.getInt("id")+"", rs.getString("facyltet_name")));
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
            ////

            // Добавление выпадающий список всех факультетов из массива
            Spinner spinner = (Spinner) findViewById(R.id.spinner2);
            spinner.setPrompt("Title");
            ArrayAdapter<String> adapter = new ArrayAdapter(getApplicationContext(), R.layout.my_spinner, facultet_item);
            adapter.setDropDownViewResource(R.layout.my_spinner);
            spinner.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
                {
                    // Добавление в насторйки ID факультета и названия факультета
                    myEditor.putString("facultet_id", facultet_item.get(pos).id);
                    myEditor.putString("facultet_name", facultet_item.get(pos).name);
                    ////

                    // Загрузка кафедры пренадлежащая к выбраному факультету
                    new LoadKafedra(Integer.parseInt(facultet_item.get(pos).id)).execute();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) { }
            });
        }
    }

    class LoadKafedra extends AsyncTask<String, String, String>
    {
        int id;
        public LoadKafedra(int facultetID){this.id = facultetID;}

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // Открытие диалогового окна без возможности закрытия
            pDialog = new ProgressDialog(HelloActivity.this);
            pDialog.setMessage("Загрузка. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args)
        {
            //Очистка маасивов (кафедр, спецальностей, групп)
            cafedra_item.clear();
            specialLists.clear();
            groupLists.clear();
            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {
                PreparedStatement selectStatement = conn.prepareStatement("select * from kafedra WHERE facyltet_id LIKE " + id);
                ResultSet rs = selectStatement.executeQuery();
                while (rs.next())
                {
                    cafedra_item.add(new KafedraList(rs.getInt("id")+"", rs.getString("kafedra_name")));
                }

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            Spinner spinner = (Spinner) findViewById(R.id.spinner4);
            // Проверка что в массиве факультетов есть элементы и отображение выбора кафедр
            if(facultet_item.size() > 0)
            {
                spinner.setVisibility(View.VISIBLE);
                findViewById(R.id.textView3).setVisibility(View.VISIBLE);
                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.my_spinner, cafedra_item);
                adapter.setDropDownViewResource(R.layout.my_spinner);
                spinner.setAdapter(adapter);

                adapter.notifyDataSetChanged();
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        // Добавление в насторйки ID кафедр и названия кафедр
                        myEditor.putString("cafedra_id", cafedra_item.get(pos).id);
                        myEditor.putString("cafedra_name", cafedra_item.get(pos).name);

                        // Загрузка спциальностей пренадлежащей к выбраной кафедре
                        new LoadSpecial(Integer.parseInt(cafedra_item.get(pos).id)).execute();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) { }
                });

            }
            else
            {
                spinner.setVisibility(View.INVISIBLE);
                findViewById(R.id.textView3).setVisibility(View.INVISIBLE);
            }
        }
    }

    class LoadSpecial extends AsyncTask<String, String, String>
    {
        int id;
        public LoadSpecial(int facultetID){this.id = facultetID;}

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(HelloActivity.this);
            pDialog.setMessage("Загрузка. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args)
        {
            specialLists.clear();
            groupLists.clear();
            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {
                PreparedStatement selectStatement = conn.prepareStatement("select * from special WHERE kafedra_id LIKE " + id);
                ResultSet rs = selectStatement.executeQuery();
                while (rs.next()) { // Добавление в массив специальностей пренадлежащих к кафедре
                    specialLists.add(new SpecialList(rs.getInt("id")+"", rs.getString("special_name"),rs.getString("obrag_programm") ));
                }

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            Spinner spinner = (Spinner) findViewById(R.id.spinner3);
            if(specialLists.size() > 0)
            {
                spinner.setVisibility(View.VISIBLE);
                findViewById(R.id.textView4).setVisibility(View.VISIBLE);
                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.my_spinner, specialLists);
                adapter.setDropDownViewResource(R.layout.my_spinner);
                spinner.setAdapter(adapter);

                adapter.notifyDataSetChanged();

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        myEditor.putString("special_id", specialLists.get(pos).id);
                        myEditor.putString("special_name", specialLists.get(pos).name);
                        new LoadGroup(Integer.parseInt(specialLists.get(pos).id)).execute();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) { }
                });
            }
            else
            {
                spinner.setVisibility(View.INVISIBLE);
                findViewById(R.id.textView4).setVisibility(View.INVISIBLE);
            }
        }
    }

    class LoadGroup extends AsyncTask<String, String, String>
    {
        int id;
        public LoadGroup(int facultetID){this.id = facultetID;}

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(HelloActivity.this);
            pDialog.setMessage("Загрузка. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
            groupLists.clear();
        }

        protected String doInBackground(String... args)
        {

            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {
                PreparedStatement selectStatement = conn.prepareStatement("select * from groups WHERE special LIKE " + id);
                ResultSet rs = selectStatement.executeQuery();
                while (rs.next()) { // Добавление в массив групп пренадлежащих к специальности
                    groupLists.add(new GroupList(rs.getInt("id")+"", rs.getString("group_name")));
                }

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            Spinner spinner = (Spinner) findViewById(R.id.spinner5);
            if(groupLists.size() > 0)
            {
                spinner.setVisibility(View.VISIBLE);
                findViewById(R.id.textView5).setVisibility(View.VISIBLE);
                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.my_spinner, groupLists);
                adapter.setDropDownViewResource(R.layout.my_spinner);
                spinner.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
                    {
                        myEditor.putString("groups_id", groupLists.get(pos).id);
                        myEditor.putString("groups_name", groupLists.get(pos).name);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) { }
                });
            }
            else
            {
                spinner.setVisibility(View.INVISIBLE);
                findViewById(R.id.textView5).setVisibility(View.INVISIBLE);
            }
        }
    }
}