package org.weaverdb.android;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.weaverdb.Connection;
import org.weaverdb.ResultSet;
import org.weaverdb.Statement;
import org.weaverdb.WeaverCmdLine;
import org.weaverdb.WeaverInitializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.Comparator;
import java.util.Properties;

public class DBHome {
    public static void eraseDB(Path home) throws Exception {
        Path dbhome = home.resolve("dbhome");
        if (!Files.exists(dbhome)) FileUtils.deleteDirectory(dbhome.toFile());
    }

    public static void startDB(Path home) throws Exception {
        if (!Files.exists(home.resolve("dbhome"))) {
            unpackDBHome(home);
        }
        Properties prop = new Properties();
        prop.setProperty("datadir", String.valueOf(home.resolve("dbhome")));
        prop.setProperty("allow_anonymous", "true");
        prop.setProperty("start_delay", "10");
        prop.setProperty("debuglevel", "DEBUG");
        prop.setProperty("stdlog", "TRUE");
        prop.setProperty("disable_crc", "TRUE");

        WeaverInitializer.initialize(prop);
    }

    public static void initdb(Path home) throws Exception {
        int val = WeaverCmdLine.cmd( new String[]{"-boot","-x","-C","-F","-D" + home.toString(), "-Q"});
        try (InputStream is = DBHome.class.getResourceAsStream("/template1.bki.source")) {
                pipe(is, System.out);
        }
        if (val != 0) throw new RuntimeException();

        val = WeaverCmdLine.cmd( new String[]{"-boot","-C","-F","-D" + home.toString(), "-Q"});

        try (InputStream is = DBHome.class.getResourceAsStream("/global1.bki.source")) {
                pipe(is, System.out);
        }
        if (val != 0) throw new RuntimeException();

        val = WeaverCmdLine.cmd( new String[]{"-boot","-C","-F","-D" + home.toString(), "-Q"});

            OutputStream os = System.out;
                os.write("open pg_database\n".getBytes());
                os.write("insert (template1 anonymous 0 template1)\n".getBytes());
                os.write("close pg_database\n".getBytes());

        if (val != 0) throw new RuntimeException();

    }

    private static void pipe(InputStream is, OutputStream os) throws IOException {
        int read = is.read();
        while (read >= 0) {
            os.write(read);
            read = is.read();
        }
    }

    public static void unpackDBHome(Path home) throws Exception {
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
}
