package org.weaverdb.example;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.weaverdb.Connection;
import org.weaverdb.ExecutionException;
import org.weaverdb.ResultSet;
import org.weaverdb.android.DBHome;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    long clicks = 0;
    Connection c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
/*        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        Button push = (Button)findViewById(R.id.button);
        push.setOnClickListener(messageClickedHandler);

        try {
            boolean created = DBHome.startDB(getApplicationContext().getFilesDir().toPath());
            if (created) {
                try (Connection c = org.weaverdb.Connection.connectAnonymously("template1")) {
                    c.execute("create database uitest");
                }
                c = Connection.connectAnonymously("uitest");
                c.execute("create table clickcounter (x int4, y int4, moment timestamp)");
            } else {
                c = Connection.connectAnonymously("uitest");
                clicks = ResultSet.builder(c).parse("select x,y,moment from clickcounter")
                        .output(1, Integer.class)
                        .output(2, Integer.class)
                        .output(3, Date.class)
                        .execute().peek(r->Log.d("INIT", r.toString())).count();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final View.OnClickListener messageClickedHandler = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        public void onClick(View v) {
            Log.d("BUTTONS", "User tapped the Supabutton");

            System.out.println("clicked");
            TextView text = (TextView)findViewById(R.id.textView);
            text.setText("click count:" + insertClick(new Date()));
        }
    };

    private String insertClick(Date date) {
        try {
            ResultSet.builder(c).parse("insert into clickcounter (x,y,moment) values (0,0,$time)")
                    .input("time", date).execute();
            clicks += 1;
        } catch (ExecutionException ee) {

        }
        return Long.toString(clicks);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            c.close();
            DBHome.close();
        } catch (Exception ee) {

        }
    }
}