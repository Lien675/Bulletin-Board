package Client;

import javax.crypto.SecretKey;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBeheer {
    public static boolean bestaatFile(String naam) throws IOException { //return true als file al bestond

        String s = naam +".txt";
        File file = new File(s);

        boolean exists= file.exists();
        if(exists){
            System.out.println("file bestond al");
            return true;
        }
        else{
            boolean result = file.createNewFile();

            if(result) System.out.println("File created");

            else System.out.println("file already existed");

            return false;
        }
    }

    public static List<String> readFromFile(String naam) throws IOException {
        List<String > waarden = new ArrayList<>();

        File file = new File(naam+".txt");

        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;

        while ((st = br.readLine()) != null) waarden.add(st);

        return waarden;
    }

    public static void writeToFile(List<String> waarden,String naam) throws IOException {
        System.out.println("in write to file");

        StringBuilder sb = new StringBuilder();

        for(String s: waarden){
            sb.append(s);
            sb.append("\n");
        }

        String waard = sb.toString();

        FileWriter myWriter = new FileWriter(naam+".txt");
        myWriter.flush();

        myWriter.write(waard);
//        for(String value: waarden){
//            System.out.println(value);
//            myWriter.append(value);
//            myWriter.append("\n");
//        }

        myWriter.close();
    }

}
