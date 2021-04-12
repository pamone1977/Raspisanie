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

    List<FacultetList> facultet_item= new ArrayList<>();
    List<KafedraList> cafedra_item = new ArrayList<>();
    List<SpecialList> specialLists = new ArrayList<>();
    List<GroupList> groupLists = new ArrayList<>();

    SharedPreferences myPreferences;
    SharedPreferences.Editor myEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        try
        {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch (ClassNotFoundException | IllegalAccessException | java.lang.InstantiationException e)
        {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
        }

        myPreferences = PreferenceManager.getDefaultSharedPreferences(HelloActivity.this);
        myEditor = myPreferences.edit();

        new LoadFacultet().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hello_menu, menu);
        return true;
    }


    public void onClickMenu(MenuItem item)
    {
        if(groupLists.size() > 0) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Хорошо", Toast.LENGTH_SHORT);
            toast.show();

            myEditor.commit();

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
            pDialog = new ProgressDialog(HelloActivity.this);
            pDialog.setMessage("Загрузка. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args)
        {
            facultet_item.clear();
            cafedra_item.clear();
            specialLists.clear();
            groupLists.clear();
            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {
                PreparedStatement selectStatement = conn.prepareStatement("select * from facyltet");
                ResultSet rs = selectStatement.executeQuery();

                while (rs.next()) { // will traverse through all rows
                    facultet_item.add(new FacultetList(rs.getInt("id")+"", rs.getString("facyltet_name")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String file_url)
        {
            pDialog.dismiss();

            Spinner spinner = (Spinner) findViewById(R.id.spinner2);
            spinner.setPrompt("Title");
            ArrayAdapter<String> adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.select_dialog_item, facultet_item);
            adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
            spinner.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
                {
                    myEditor.putString("facultet_id", facultet_item.get(pos).id);
                    myEditor.putString("facultet_name", facultet_item.get(pos).name);
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
            pDialog = new ProgressDialog(HelloActivity.this);
            pDialog.setMessage("Загрузка. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args)
        {
            cafedra_item.clear();
            specialLists.clear();
            groupLists.clear();
            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {
                PreparedStatement selectStatement = conn.prepareStatement("select * from kafedra WHERE facyltet_id LIKE " + id);
                ResultSet rs = selectStatement.executeQuery();
                while (rs.next())
                { // will traverse through all rows
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
            if(facultet_item.size() > 0)
            {
                spinner.setVisibility(View.VISIBLE);
                findViewById(R.id.textView3).setVisibility(View.VISIBLE);
                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.select_dialog_item, cafedra_item);
                adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
                spinner.setAdapter(adapter);

                adapter.notifyDataSetChanged();
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        myEditor.putString("cafedra_id", cafedra_item.get(pos).id);
                        myEditor.putString("cafedra_name", cafedra_item.get(pos).name);
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
                while (rs.next()) {
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
                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.select_dialog_item, specialLists);
                adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
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
                while (rs.next()) {
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
                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.select_dialog_item, groupLists);
                adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
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