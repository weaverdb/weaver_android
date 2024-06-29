

package org.weaverdb.android;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.weaverdb.Connection;
import org.weaverdb.ResultSet;
import org.weaverdb.Statement;
import org.weaverdb.WeaverInitializer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

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
        DBHome.startDB(dbhome);

        System.err.println("initialized");
        try (Connection conn = Connection.connectAnonymously("template1")) {
            try (Statement s = conn.statement("select * from pg_type where oid = 16")) {
                ResultSet.stream(s).flatMap(ResultSet.Row::stream).forEach(System.out::println);
            }
        }
    }
}