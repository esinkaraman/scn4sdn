package tr.edu.boun.cmpe.scn.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by esinka on 3/12/2017.
 */
public class CpuReader {

    public static String getCpuTime(String cpuResource) throws IOException {
        File file = new File(cpuResource);
        if (!file.exists()) {
            System.out.println(cpuResource + " does not exist!");
            return null;
        }
        String line = null;
        BufferedReader br = null;
        try {
            br = java.nio.file.Files.newBufferedReader(Paths.get(file.getPath()));
            line = br.readLine();
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return line;
    }
}
