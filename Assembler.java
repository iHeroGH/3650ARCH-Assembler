import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;

public class Assembler{

    protected static SymbolTable symbolMap = new SymbolTable();
    public static void main(String[] args){
        List<String> instructions = readFile("src/test");
        List<String> content = decodeInstructions(instructions);
        writeToFile("src/test", content);
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

    public static void writeToFile(String fileName, List<String> content){
        fileName = fileName + ".hack";

        try {
            PrintWriter writer = new PrintWriter(fileName);

            // Write all the data from the contents list to the file
            for(String line : content){
                writer.println(line);
            }

            writer.close();
        } catch (FileNotFoundException e){
            System.out.println(
                "The provided output file (" + fileName + ") could not be created."
            );
        } catch (Exception e){ // An unexpected error
            System.out.println(
                "Something went wrong writing to the output file (" + fileName + ")."
            );
            e.printStackTrace();
        }
    }

    public static List<String> decodeInstructions(List<String> instructions){
        List<String> decodedInstructions = new ArrayList<String>();

        for(String instruction : instructions){
            decodedInstructions.add(decodeInstruction(instruction));
        }

        return decodedInstructions;
    }

    public static String decodeInstruction(String instruction){
        String opCode;
        boolean isAInstruction;

        if (instruction.startsWith("@")){
            opCode = "0";
            isAInstruction = true;
        } else {
            opCode = "111";
            isAInstruction = false;
        }

        String decoded = "";
        if (isAInstruction){
            String fullValue = zeroPad(
                Integer.toBinaryString(decodeAInstruction(instruction))
            );
            decoded = opCode + fullValue;

        } else {
            String aValue = "";
            String compValue = "";
            String destValue = "";
            String jumpValue = "";
            decoded = opCode + aValue + compValue + destValue + jumpValue;
        }

        if (decoded.length() != 16){
            throw new RuntimeException(
                "Something went wrong decoding instruction '" +
                instruction + "'. The decoded '" +
                decoded + "' length does not match."
            );
        }

        return decoded;
    }

    public static int decodeAInstruction(String instruction){
        if (!instruction.startsWith("@")){
            throw new RuntimeException(
                "Instruction incorrectly identified as A-Instruction"
            );
        }

        String value = instruction.substring(1);
        if (value.matches("R\\d+")){
            try{
                int rValue = Integer.parseInt(
                    instruction.substring(2)
                );
                if (0 <= rValue && rValue <= 15){
                    return rValue;
                }
            } catch (Exception e){
                System.out.println(
                    "Something went wrong extracting R value from '" +
                    instruction + "''."
                );
                e.printStackTrace();
            }
        }

        if (value.matches("\\d+")){
            try{
                return Integer.parseInt(value);
            } catch (Exception e){
                System.out.println(
                    "Something went wrong extracting value from '" +
                    instruction + "''."
                );
                e.printStackTrace();
            }
        }

        if(symbolMap.contains(value)){
            return symbolMap.getAddress(value);
        }

        throw new RuntimeException(
            "The label found in instruction '" + instruction + "' was not found."
        );
    }

    public static String zeroPad(String value){
        int needed = 15 - value.length();
        if(needed < 0){
            return value;
        }
        return "0".repeat(needed) + value;
    }
}