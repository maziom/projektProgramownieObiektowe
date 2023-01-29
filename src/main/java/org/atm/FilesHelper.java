package org.atm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FilesHelper {

    public static BufferedReader getFile(String filename) {
        final var file = System.getProperty("user.dir")+"/src/main/resources/" + filename;
        try {
            return new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist");
            throw new RuntimeException(e);
        }
    }
}
