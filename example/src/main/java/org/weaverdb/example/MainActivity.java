/*-------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2024, Myron Scott  <myron@weaverdb.org>
 *
 * All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 *
 *-------------------------------------------------------------------------
 */

package org.weaverdb.example;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.weaverdb.DBReference;
import org.weaverdb.ExecutionException;
import org.weaverdb.FetchSet;
import org.weaverdb.android.DBHome;

import java.util.Date;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
    long clicks = 0;
    DBReference c;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.main);
        layout.setClickable(true);
        layout.setOnTouchListener(this::onTouch);

        Button push = (Button)findViewById(R.id.button);
        push.setOnClickListener(messageClickedHandler);

        try {
            DBHome.startInstance(getApplicationContext().getFilesDir().toPath());
            c = DBReference.connect("uitest");
            if (c == null) {
                DBHome.createDB("uitest");
                c = DBReference.connect("uitest");
                c.execute("create table clickcounter (x int4, y int4, moment timestamp)");
            }

            try (Stream<FetchSet.Row> r = FetchSet.builder(c).parse("select x,y,moment from clickcounter order by moment")
                    .output(1, Integer.class)
                    .output(2, Integer.class)
                    .output(3, Date.class)
                    .execute()) {
                clicks = r.peek(row->Log.d("INIT", row.toString())).count();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final View.OnClickListener messageClickedHandler = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        public void onClick(View v) {
            System.out.println("clicked");
            TextView text = (TextView)findViewById(R.id.textView);
            text.setText("click count:" + insertClick(0,0));
        }
    };

    private String insertClick(int x, int y) {
        try {
            FetchSet.builder(c).parse("insert into clickcounter (x,y,moment) values ($x,$y,$time)")
                    .input("x", x)
                    .input("y", y)
                    .input("time", new Date()).execute().close();
            clicks += 1;
        } catch (ExecutionException ee) {
            Log.e("EXAMPLE", "an error accessing database", ee);
        }
        return Long.toString(clicks);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            c.close();
        } catch (Exception ee) {
            Log.e("EXAMPLE", "an error closing reference", ee);
        }
    }


    private boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            insertClick(x, y);
        }
        return true;
    }
}