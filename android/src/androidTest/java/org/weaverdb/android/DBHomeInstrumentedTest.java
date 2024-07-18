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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.weaverdb.DBReference;
import org.weaverdb.FetchSet;
import org.weaverdb.Statement;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DBHomeInstrumentedTest {
    @BeforeClass
    public static void setupDB() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Path dbhome = appContext.getFilesDir().toPath();
        System.out.println("placing test home:" + dbhome);
        if (Files.exists(dbhome.resolve("dbhome"))) {
            DBHome.eraseInstance(dbhome);
        }
        DBHome.startInstance(dbhome);
    }

    @AfterClass
    public static void shutdownDB() {
        DBHome.close();
    }

    @Test
    public void testDBExists() {
        assertFalse(DBHome.dbExists("seeit"));
        assertTrue(DBHome.createDB("seeit"));
        assertTrue(DBHome.dbExists("seeit"));
        assertTrue(DBHome.dropDB("seeit"));
        assertFalse(DBHome.dbExists("seeit"));
    }

    @Test
    public void testDBCreateOnConnect() throws Exception {
        assertFalse(DBHome.dbExists("connect"));
        try (DBReference ref = DBHome.connect("connect")) {
            assertNotNull(ref);
        }
        assertTrue(DBHome.dbExists("connect"));
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("org.weaverdb.android.test", appContext.getPackageName());

        try (DBReference conn = DBReference.connect("template1")) {
            try (Statement s = conn.statement("select * from pg_type where oid = 16")) {
                FetchSet.stream(s).flatMap(FetchSet.Row::stream).forEach(System.out::println);
            }
        }
    }
}