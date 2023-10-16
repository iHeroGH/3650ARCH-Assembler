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
    private String currentInstruction;
    private Command currentCommand;

    public Assembler(String fileName){
        this.fileName = fileName;
        this.symbolTable = new SymbolTable();
        this.currentCommand = null;

        try {
            File file = new File(this.fileName + ".asm");
            this.scanner = new Scanner(file);
            this.symbolTable.readFile(this.fileName);
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
        this.currentInstruction = scanner.nextLine();
    }

    public List<Command> readFile(){
        List<Command> content = new ArrayList<Command>();

        // Write all the data from the input file to the contents list
        try {
            String curr = "";
            while (hasMoreCommands()){
                advance();
                curr = sanitizeCommand(currentInstruction);

                if (!shouldAddCommand(curr)){
                    continue;
                }

                currentCommand = new Command(curr, this.symbolTable);
                content.add(currentCommand);
            }

            scanner.close();
        } catch (Exception e){ // An unexpected error
            System.out.println(
                "Something went wrong reading the input file (" + fileName + ".asm)."
            );
            e.printStackTrace();
        }

        return content;
    }

    public static String sanitizeCommand(String command){
        return command.replaceAll("\\s", "")
            .replaceAll("//.*", "");
    }

    public static boolean shouldAddCommand(Command command){
        String instruction = command.getFullInstruction();
        return !(
            command.getCommandType() == CommandType.L_COMMAND ||
            !shouldAddCommand(instruction)
        );
    }

    public static boolean shouldAddCommand(String instruction){
        return !(
            instruction.startsWith("//") ||
            instruction.length() == 0
        );
    }

    public void writeToFile(List<Command> content){
        try {
            PrintWriter writer = new PrintWriter(fileName + ".hack");

            // Write all the data from the contents list to the file
            for(Command command : content){
                if (!shouldAddCommand(command)){
                    continue;
                }
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