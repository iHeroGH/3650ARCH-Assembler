import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;

public class Assembler{

    private SymbolTable symbolTable;
    private String fileName;
    private Scanner scanner;
    private String currentCommand;

    public Assembler(String fileName){
        this.fileName = fileName;
        this.symbolTable = new SymbolTable();
        this.currentCommand = null;

        try {
            File file = new File(this.fileName + ".asm");
            this.scanner = new Scanner(file);
        } catch (FileNotFoundException e){
            System.out.println(
                "The provided input file (" + fileName + ".asm) was not found."
            );
        }

        writeToFile(readFile());
    }

    public boolean hasMoreCommands(){
        return this.scanner.hasNextLine();
    }

    public void advance(){
        this.currentCommand = scanner.nextLine();
    }

    public List<Command> readFile(){
        List<Command> content = new ArrayList<Command>();

        // Write all the data from the input file to the contents list
        try {
            String curr = "";
            while (hasMoreCommands()){
                advance();
                curr = currentCommand
                .replaceAll("\\s", "")
                .replaceAll("//.*", "");

                if (curr.startsWith("//") || curr.length() == 0){
                    continue;
                }

                content.add(new Command(curr, this.symbolTable));
            }

            scanner.close();
        } catch (Exception e){ // An unexpected error
            System.out.println(
                "Something went wrong reading the input file (" + fileName + ")."
            );
            e.printStackTrace();
        }

        return content;
    }

    public void writeToFile(List<Command> content){
        try {
            PrintWriter writer = new PrintWriter(fileName + ".hack");

            // Write all the data from the contents list to the file
            for(Command command : content){
                writer.println(command.getFullInstruction());
            }

            writer.close();
        } catch (FileNotFoundException e){
            System.out.println(
                "The provided output file (" + fileName + ".hack) could not be created."
            );
        } catch (Exception e){ // An unexpected error
            System.out.println(
                "Something went wrong writing to the output file (" + fileName + ".hack)."
            );
            e.printStackTrace();
        }
    }
}