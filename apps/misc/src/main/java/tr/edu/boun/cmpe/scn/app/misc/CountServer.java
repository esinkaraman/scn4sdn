package tr.edu.boun.cmpe.scn.app.misc;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by esinka on 4/4/2017.
 */
public class CountServer {

    private Map<String, Integer> map = new HashMap();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: <Service log file name>");
            return;
        }
        String fileName = args[0];

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File does not exists");
            return;
        }

        new CountServer().processFile(file);
    }

    private void processFile(File file) {
        String line;
        try {
            BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(file.getPath()));
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
            for (String serverIp : map.keySet()) {
                Integer count = map.get(serverIp);
                System.out.print(serverIp + "=" + count + " ");
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line) {
        if (line == null) {
            return;
        }
        String[] split = line.split(";");
        if (split.length < 3) {
            System.out.println("Size of split line less than 3. Line:" + line);
            return;
        }

        String serverIp = split[1];
        Integer countPerServerIp = map.get(serverIp);
        Integer count = countPerServerIp == null ? 1 : countPerServerIp.intValue() + 1;
        map.put(serverIp, count);
    }
}
