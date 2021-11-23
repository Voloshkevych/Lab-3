package univesity;

import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // System.out.println("Enter file path:");
       // Scanner input = new Scanner(System.in);
        String filePath;
        if (args.length != 0) {
            filePath = args[0];
        }
        else {
            System.out.println("Enter file path:");
            Scanner input = new Scanner(System.in);
            filePath =  input.next();
        }
        try {
            FileReader fr = new FileReader(filePath);
            int i;
            StringBuilder sb = new StringBuilder();
            while ((i=fr.read()) != -1)
                sb.append((char) i);
            String text = sb.toString();
            Automaton l = new Automaton();
            l.initAsCppLexer();
            String str = "";
            for(Lexem lexem:l.recognize(text)) {
                if(lexem.getType() != LexemType.COMMENT && lexem.getType() != LexemType.WHITE) {
                    str = str.concat(lexem.toString());
                    str = str.concat("\n");
                }
            }
            System.out.print(str);
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            writer.write(str);
            writer.close();
        }
        catch (FileNotFoundException e){
            System.out.println("No such file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
