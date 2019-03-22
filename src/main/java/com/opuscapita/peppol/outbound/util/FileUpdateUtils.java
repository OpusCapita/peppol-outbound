package com.opuscapita.peppol.outbound.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;

public class FileUpdateUtils {

    /**
     * exact search and replace
     */
    public static InputStream searchAndReplace(InputStream source, String search, String replace) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(source))) {
            String line = reader.readLine();
            while (line != null) {
                line = StringUtils.replace(line, search, replace);
                result.write(line.getBytes());
                result.write("\n".getBytes());
                line = reader.readLine();
            }
        }

        return new ByteArrayInputStream(result.toByteArray());
    }

    /**
     * startsWith search and replace
     */
    public static InputStream startAndReplace(InputStream source, String start, String replace) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(source))) {
            String line = reader.readLine();
            while (line != null) {
                line = line.startsWith(start) ? replace : line;
                result.write(line.getBytes());
                result.write("\n".getBytes());
                line = reader.readLine();
            }
        }

        return new ByteArrayInputStream(result.toByteArray());
    }

}
