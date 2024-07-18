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

import android.util.Log;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.weaverdb.DBReference;
import org.weaverdb.ExecutionException;
import org.weaverdb.WeaverInitializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class DBHome {
    private static final AtomicReference<Path> singleInstance = new AtomicReference<>();

    /**
     * Delete the files of the entire database install.  This cannot be reversed.
     * @param home Absolute path to where the database will be removed
     * @throws Exception
     */
    public static void eraseInstance(Path home) throws Exception {
        Path dbhome = home.resolve("dbhome");
        if (singleInstance.get() != null) {
            throw new ExecutionException("db still running, close it first");
        }
        if (Files.exists(dbhome)) {
            FileUtils.deleteDirectory(dbhome.toFile());
        }
    }

    /**
     * connect to a database namespace in the installation.  If the database does not
     * exist, it will be created
     * @param db name of the database
     * @return reference to the database
     */
    public static DBReference connect(String db) {
        checkIfStarted();
        if (!dbExists(db)) {
            if (!createDB(db)) {
                throw new RuntimeException("unable to create database " + db);
            }
        }
        return DBReference.connect(db);
    }

    /**
     * Check if the desired database exists
     * @param name name of the target database
     * @return true if the database exists
     */
    public static boolean dbExists(String name) {
        Path db = singleInstance.get();
        if (db != null) {
            return Files.exists(db.resolve("base").resolve(name));
        }
        return false;
    }

    /**
     * Create a database in the current database installation
     * @param name create a database to be accessed on connect
     * @return true if the database was created
     */
    public static boolean createDB(String name) {
        checkIfStarted();
        if (!dbExists(name)) {
            try (DBReference c = org.weaverdb.DBReference.connect("template1")) {
                c.execute("create database " + name);
                return true;
            } catch (ExecutionException ee) {
                Log.e("DBHOME", "unable to create database", ee);
                return false;
            }
        }
        return false;
    }

    /**
     * drop a database from the installation
     * @param name name of the database to drop
     * @return true if the database was successfully dropped
     */
    public static boolean dropDB(String name) {
        checkIfStarted();
        if (dbExists(name)) {
            try (DBReference c = org.weaverdb.DBReference.connect("template1")) {
                c.execute("drop database " + name);
                return true;
            } catch (ExecutionException ee) {
                Log.e("DBHOME", "unable to drop database", ee);
                return false;
            }
        }
        return false;
    }

    /**
     * Start the single database instance at the supplied installation
     * path.  If the install does not exist at the supplied path, the
     * the root database will attempt to be exploded in place before starting
     * @param home absolute path location of the database
     * @return true if the instance was successfully started
     * @throws IOException
     */
    public static boolean startInstance(Path home) throws IOException {
        boolean created = false;
        if (!home.isAbsolute()) {
            throw new RuntimeException("the path to the database instance must be an absolute path");
        }
        Path dbhome = home.resolve("dbhome");
        if (singleInstance.compareAndSet(null, dbhome)) {
            if (!Files.exists(dbhome)) {
                unpackDBHome(home);
                created = true;
            }
            Properties prop = new Properties();
            prop.setProperty("datadir", String.valueOf(home.resolve("dbhome")));
            prop.setProperty("buffercount", "128");

            WeaverInitializer.initialize(prop);
            Runtime.getRuntime().addShutdownHook(new Thread(DBHome::close));
        } else if (!dbhome.equals(singleInstance.get())) {
            throw new IOException("instance already exists at " + singleInstance.get());
        }
        return created;
    }

    /**
     * Attempt to shutdown and close the single instance of Weaver currently running
     */
    public static void close() {
        Path home = singleInstance.get();
        if (home != null && singleInstance.compareAndSet(home, null)) {
            WeaverInitializer.forceShutdown();
            singleInstance.set(null);
        }
    }

    private static void unpackDBHome(Path home) throws IOException {
        try (InputStream is = DBHome.class.getResourceAsStream("/dbhome.tar")) {
            TarArchiveInputStream tar = new TarArchiveInputStream(is);
            ArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                Path extractTo = home.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(extractTo);
                } else {
                    Files.copy(tar, extractTo);
                }
            }
        }
    }

    private static void checkIfStarted() {
        if (singleInstance.get() == null) {
            throw new RuntimeException("weaver intance not started");
        }
    }
}
