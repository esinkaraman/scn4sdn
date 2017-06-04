package tr.edu.boun.cmpe.scn.app.misc;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Paths;

/**
 * Created by esinka on 4/4/2017.
 */
public class CountClient {

    private long totalElapsed = 0;
    private int itemCount = 0;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: <Client log file name>");
            return;
        }
        String fileName = args[0];

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File does not exists");
            return;
        }

        new CountClient().processFile(file);
    }

    private void processFile(File file) {
        String line = null;
        try {
            BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(file.getPath()));
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
            double average = totalElapsed * 1.0 / itemCount * 1.0;
            System.out.println("Total elapsed=" + totalElapsed + " Item Count=" + itemCount + " Average=" + average);
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
        long elapsed = Long.parseLong(split[2]);
        totalElapsed = totalElapsed + elapsed;
        itemCount++;
    }
}
