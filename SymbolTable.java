import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.Map;
import java.util.HashMap;

public class SymbolTable{

    Map<String, Integer> symbolTable;
    int freeAddress;

    public SymbolTable(){
        this.symbolTable = new HashMap<String, Integer>();
        this.freeAddress = 16;
        loadPredefinedSymbols();
    }

    public void loadPredefinedSymbols(){
        this.addEntry("SP", 0);
        this.addEntry("LCL", 1);
        this.addEntry("ARG", 2);
        this.addEntry("THIS", 3);
        this.addEntry("THAT", 4);
        this.addEntry("SCREEN", 16384);
        this.addEntry("KBD", 24576);
    }

    public void addEntry(String symbol, int address){
        this.symbolTable.put(symbol, address);
    }

    public boolean contains(String symbol){
        return this.symbolTable.keySet().contains(symbol);
    }

    public int getAddress(String symbol){
        return this.symbolTable.get(symbol);
    }

    public void readFile(String fileName){
        fileName = fileName + ".asm";

        // March through file and add necessary symbols and variables
        try {
            File file = new File(fileName);
            parseSymbols(new Scanner(file));
            parseVariables(new Scanner(file));
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
    }

    public void parseSymbols(Scanner scanner){
        int currCommandAddress = 0;
        String curr = "";
        while (scanner.hasNextLine()){
            curr = Assembler.sanitizeCommand(scanner.nextLine());
            if (!Assembler.shouldAddCommand(curr)){
                continue;
            }

            if (curr.matches("\\(.*\\)")){
                String label = curr.substring(1, curr.length() - 1);
                if (!this.contains(label)){
                    this.addEntry(label, currCommandAddress);
                }
            } else {
                currCommandAddress++;
            }
        }

        scanner.close();
    }

    public void parseVariables(Scanner scanner){
        int freeAddress = 16;
        String curr = "";
        while (scanner.hasNextLine()){
            curr = Assembler.sanitizeCommand(scanner.nextLine());

            if (!Assembler.shouldAddCommand(curr)){
                continue;
            }

            if (curr.matches("@\\D+")){
                String symbol = curr.substring(1);
                if (!this.contains(symbol)){
                    this.addEntry(symbol, freeAddress);
                    freeAddress++;
                }
            }
        }
        scanner.close();
    }

    public void addVariable(String symbol){
        if (!this.contains(symbol))
            this.addEntry(symbol, this.freeAddress++);
    }

}