import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;

public class Main{

    public static void main(String[] args){
        List<String> content = readFile("max/MaxL");
        System.out.println(content);
    }

    public static List<String> readFile(String fileName){
        fileName = fileName + ".asm";
        List<String> content = new ArrayList<String>();

        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);

            // Write all the data from the input file to the contents list
            String curr = "";
            while (scanner.hasNextLine()){
                curr = scanner.nextLine()
                        .replaceAll("\\s", "")
                        .replaceAll("//.*", "");

                if (curr.startsWith("//") || curr.length() == 0){
                    continue;
                }

                content.add(curr);
            }

            scanner.close();

        } catch (FileNotFoundException e){
            System.out.println(
                "The provided input file (" + fileName + ") was not found."
            );
        } catch (Exception e){ // An unexpected error
            System.out.println(
                "Something went wrong reading the input file (" + fileName + ")."
            );
            e.printStackTrace();
        }

        return content;
    }

    public static void writeToFile(String fileName, ArrayList<String> content){

    }

    public static String decodeInstruction(String instruction){
        return "";
    }
}