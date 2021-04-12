package ru.buildersoul.raspisanie.ui.home;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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

    SharedPreferences myPreferences;
    private ProgressDialog pDialog;

    List<PassisanieList> paspisanieList_item = new ArrayList<>();
    TableLayout table;

    View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String special_name = myPreferences.getString("groups_name", "unknown");

        Spinner spinner = (Spinner) root.findViewById(R.id.spinner_dened);
        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.select_dialog_item, new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"});
        adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = firstUpperCase(sdf.format(d));

        int position = adapter.getPosition(dayOfTheWeek);
        spinner.setSelection(position, true);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                table = (TableLayout) root.findViewById(R.id.tablezamena);
                table.setColumnShrinkable(0, true);
                table.removeAllViews();
                paspisanieList_item.clear();
                String selected = spinner.getSelectedItem().toString();
                new LoadPaspisanie(selected).execute();


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
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
        return root;
    }
    public String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return "";
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }


    @Override
    public void onResume() {
        super.onResume();
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String special_name = myPreferences.getString("groups_name", "unknown");

        Spinner spinner = (Spinner) root.findViewById(R.id.spinner_dened);
        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.select_dialog_item, new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"});
        adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = firstUpperCase(sdf.format(d));

        int position = adapter.getPosition(dayOfTheWeek);
        spinner.setSelection(position, true);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                table = (TableLayout) root.findViewById(R.id.tablezamena);
                table.setColumnShrinkable(0, true);
                table.removeAllViews();
                paspisanieList_item.clear();
                String selected = spinner.getSelectedItem().toString();
                new LoadPaspisanie(selected).execute();


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
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
    }

    class LoadPaspisanie extends AsyncTask<String, String, String>
    {
        String den;
        public LoadPaspisanie(String den){this.den = den;}

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
            paspisanieList_item.clear();
            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {

                PreparedStatement selectStatement = conn.prepareStatement("select * from passisanie, dic, prepodav WHERE passisanie.dic = dic.id AND passisanie.prepodavatel = prepodav.id ORDER BY TIME(passisanie.time_start), passisanie.nedel");

                ResultSet rs = selectStatement.executeQuery();
                String groups_id  = myPreferences.getString("groups_id", "");


                while (rs.next())
                {
                    if(groups_id.equalsIgnoreCase(rs.getString("groups_id")) && den.equalsIgnoreCase(rs.getString("den_nedel"))) {
                        String strings = (rs.getString("familia")+" " + rs.getString("name")+" " + rs.getString("otcestvo"));
                        //String name = strings[0] + " " + strings[1].charAt(0) + ". " + strings[2].charAt(0) + ".";

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
            pDialog.dismiss();
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
                    tableRow.addView(textView, j);
                }

                table.addView(tableRow);
            }
        }
    }
}