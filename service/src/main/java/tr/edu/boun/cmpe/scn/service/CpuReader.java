package tr.edu.boun.cmpe.scn.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by esinka on 3/12/2017.
 */
public class CpuReader {

    private static final String PROPERTIES_FILE = "service.properties";
    private static final String CPU_FILE_USAGE_KEY = "cpu.usage.file";

    private String cpuUsageFileName;

    private static final CpuReader instance = new CpuReader();

    public static CpuReader getInstance() {
        return instance;
    }

    private CpuReader() {
        init();
    }

    private void init() {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(PROPERTIES_FILE);
        try {
            prop.load(stream);
            Object o = prop.get(CPU_FILE_USAGE_KEY);
            cpuUsageFileName = (String) o;
            System.out.println("CPU usage will be read from " + cpuUsageFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCpuTime() throws IOException {
        if (cpuUsageFileName == null) {
            return null;
        }
        File file = new File(cpuUsageFileName);
        if (!file.exists()) {
            System.out.println(cpuUsageFileName + " does not exist!");
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
