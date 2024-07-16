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



package org.weaverdb.android;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.weaverdb.DBReference;
import org.weaverdb.FetchSet;
import org.weaverdb.Statement;

import java.nio.file.Path;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("org.weaverdb.android.test", appContext.getPackageName());

        Path dbhome = appContext.getFilesDir().toPath();
        DBHome.startInstance(dbhome);

        System.err.println("initialized");
        try (DBReference conn = DBReference.connect("template1")) {
            try (Statement s = conn.statement("select * from pg_type where oid = 16")) {
                FetchSet.stream(s).flatMap(FetchSet.Row::stream).forEach(System.out::println);
            }
        }
    }
}