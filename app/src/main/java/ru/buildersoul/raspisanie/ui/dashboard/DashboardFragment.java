package ru.buildersoul.raspisanie.ui.dashboard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ru.buildersoul.raspisanie.HelloActivity;
import ru.buildersoul.raspisanie.MainActivity;
import ru.buildersoul.raspisanie.R;
import ru.buildersoul.raspisanie.util.BD;
import ru.buildersoul.raspisanie.util.FacultetList;
import ru.buildersoul.raspisanie.util.ZamenaList;

import static androidx.core.content.ContextCompat.getSystemService;

public class DashboardFragment extends Fragment
{

    SharedPreferences myPreferences;
    private ProgressDialog pDialog;

    List<ZamenaList> zamenaList_item = new ArrayList<>();
    TableLayout table;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String special_name = myPreferences.getString("groups_name", "unknown");

        Spinner spinner = (Spinner) root.findViewById(R.id.spinner_dened);
        ArrayAdapter<String> adapter = new ArrayAdapter(root.getContext(), android.R.layout.select_dialog_item, new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"});
        adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                table = (TableLayout) root.findViewById(R.id.tablezamena);
                table.setColumnShrinkable(0, true);
                table.removeAllViews();
                zamenaList_item.clear();
                String selected = spinner.getSelectedItem().toString();
                new LoadZamena(selected).execute();


                TableRow tableRow = new TableRow(root.getContext());
                tableRow.setGravity(Gravity.CENTER);

                TextView textView = new TextView(root.getContext());
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

    class LoadZamena extends AsyncTask<String, String, String>
    {
        String den;
        public LoadZamena(String den){this.den = den;}

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
            zamenaList_item.clear();
            try (Connection conn = DriverManager.getConnection(BD.domes, BD.user, BD.password)) {

                PreparedStatement selectStatement = conn.prepareStatement("select * from zamenu, dic, prepodav WHERE zamenu.dic = dic.id AND zamenu.prepodavatel = prepodav.id ORDER BY TIME(zamenu.time_start)");

                ResultSet rs = selectStatement.executeQuery();
                String groups_id  = myPreferences.getString("groups_id", "");
                while (rs.next())
                {
                    if(groups_id.equalsIgnoreCase(rs.getString("groups_id")) && den.equalsIgnoreCase(rs.getString("den_nedel"))) {
                        String strings = (rs.getString("familia")+" " + rs.getString("name")+" " + rs.getString("otcestvo"));
                        //String name = strings[0] + " " + strings[1].charAt(0) + ". " + strings[2].charAt(0) + ".";

                        zamenaList_item.add(new ZamenaList(rs.getString("time_start"),
                                rs.getString("time_end"),
                                rs.getString("dic_name"),
                                rs.getString("type_zan"),
                                strings,
                                rs.getString("korpys"),
                                rs.getString("ayditoria")
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

            for(int i = 1; i < zamenaList_item.size() + 1; ++i)
            {
                TableRow tableRow = new TableRow(getContext());
                tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                for (int j = 0; j < 1; j++) {
                    TextView textView = new TextView(getContext());
                    textView.setText(Html.fromHtml(zamenaList_item.get(i - 1).toString()));
                    tableRow.addView(textView, j);
                }

                table.addView(tableRow);
            }

        }
    }
}