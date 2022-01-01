package Client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBeheer {
    public static boolean bestaatFile(String naam, int poort) throws IOException { //return true als file al bestond
        String s = naam + poort + ".txt";
        File file = new File(s);

        boolean exists = file.exists();
        if (exists) {
            return true;
        } else {
            return file.createNewFile();
        }
    }

    public static List<String> readFromFile(String naam, int poort) throws IOException {
        List<String> waarden = new ArrayList<>();

        File file = new File(naam + poort + ".txt");

        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;

        while ((st = br.readLine()) != null) waarden.add(st);

        return waarden;
    }

    public static void writeToFile(List<String> waarden, String naam, int poort) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (String s : waarden) {
            sb.append(s);
            sb.append("\n");
        }

        String waard = sb.toString();

        FileWriter myWriter = new FileWriter(naam + poort + ".txt");
        myWriter.flush();
        myWriter.write(waard);
        myWriter.close();
    }

}
