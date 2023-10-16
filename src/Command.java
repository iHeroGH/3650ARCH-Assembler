import java.util.Map;
import java.util.HashMap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Command {

    private static Pattern cPattern = Pattern.compile(
        "^((?<dest>[AMD]{1,3})=)?" + // Matches an optional named group 'dest'
        "(?<comp>[01\\-+ADM&|!]{1,3})" + // Matches a required named group 'comp'
        "(;(?<jump>J[GTEQLNMP]{2}))?$" // Matches an optional named group 'jump'

        // Additional logic will be required to ensure only one of 'dest'
        // or 'jump' is omitted. The pattern already deals with omitting
        // '=' and ';' respectively. Finally, a check must be made to ensure
        // the 'dest', 'comp', and 'jump' values exists within the mnemonic map.
    );

    private static Map<String, Integer> compMnemonic = loadComp();
    private static Map<String, Integer> destMnemonic = loadDest();
    private static Map<String, Integer> jumpMnemonic = loadJump();

    private String instruction;
    private SymbolTable symbolTable;

    private CommandType commandType;
    private String symbol;
    private String comp;
    private String dest;
    private String jump;

    public Command(String instruction, SymbolTable symbolTable){
        this.instruction = instruction;
        this.symbolTable = symbolTable;

        decodeInstruction();
    }

    public String decodeInstruction(){

        String fullValue;
        if (instruction.startsWith("@")){
            commandType = CommandType.A_COMMAND;
            fullValue = zeroPad(decodeAInstruction(instruction));
        } else if (instruction.matches("\\(.*\\)")){
            commandType = CommandType.L_COMMAND;
            fullValue = zeroPad("0");
        } else {
            commandType = CommandType.C_COMMAND;
            fullValue = decodeCInstruction(instruction);
        }

        this.comp = fullValue.substring(0, 10);
        this.dest = fullValue.substring(10, 13);
        this.jump = fullValue.substring(13);

        String decoded = getFullInstruction();
        if (decoded.length() != 16){
            throw new RuntimeException(
                "Something went wrong decoding instruction '" +
                instruction + "'. The decoded '" +
                decoded + "' length does not match the intended size '16'."
            );
        }

        return decoded;
    }

    public String decodeAInstruction(String instruction){
        if (!instruction.startsWith("@")){
            throw new RuntimeException(
                "Instruction incorrectly identified as A-Instruction"
            );
        }

        this.symbol = instruction.substring(1);
        // A register address
        if (this.symbol.matches("R\\d+")){
            try{
                int rValue = Integer.parseInt(
                    instruction.substring(2)
                );
                if (0 <= rValue && rValue <= 15){
                    return Integer.toBinaryString(rValue);
                }
            } catch (Exception e){
                System.out.println(
                    "Something went wrong extracting R value from '" +
                    instruction + "''."
                );
                e.printStackTrace();
            }
        }

        // Any number
        if (this.symbol.matches("\\d+")){
            try{
                int value = Integer.parseInt(this.symbol);
                if (value < 0){
                    throw new RuntimeException(
                        "Constants must be non-negative"
                    );
                }
                return Integer.toBinaryString(value);
            } catch (Exception e){
                System.out.println(
                    "Something went wrong extracting value from '" +
                    instruction + "''."
                );
                e.printStackTrace();
            }
        }

        // Try adding the symbol (addVariable does the necessary checks)
        this.symbolTable.addVariable(symbol);

        // See if an address is found
        if (symbolTable.contains(this.symbol)){
            return Integer.toBinaryString(symbolTable.getAddress(this.symbol));
        }

        // If we end up here, something went wrong
        throw new RuntimeException(
            "The label found in instruction '" + instruction + "' was " +
            "problematic. It was either not defined or not formatted."
        );
    }

    public String decodeCInstruction(String instruction){
        instruction = instruction.toUpperCase();

        Matcher m = cPattern.matcher(instruction);
        if (!m.matches()){
            throw new RuntimeException(
                "Found C-Instruction '" + instruction +
                "' but had trouble parsing it. " +
                "It may not match the required format"
            );
        }

        String destValueString = m.group("dest");
        String compValueString = m.group("comp");
        String jumpValueString = m.group("jump");

        if (compValueString == null || // If a comp value was not found
            // If both 'dest' and 'jump' were omitted
            (destValueString == null && jumpValueString == null)){
                throw new RuntimeException(
                "Found C-Instruction '" + instruction +
                "' but had trouble parsing it. " +
                "Both optional fields may have been omitted."
            );
        }

        int aValue = 0;
        if (compValueString.contains("M")){
            aValue = 1;
            compValueString = compValueString.replaceAll("M", "A");
        }

        String destValue = zeroPad(decodeDestValue(destValueString), 3);
        String compValue = zeroPad(decodeCompValue(compValueString), 6);
        String jumpValue = zeroPad(decodeJumpValue(jumpValueString), 3);

        return "11" + aValue + compValue + destValue + jumpValue;
    }

    public String decodeDestValue(String destValueString){
        if (!destMnemonic.containsKey(destValueString)){
            throw new RuntimeException(
                "The dest value '" + destValueString + "' was not found."
            );
        }
        return Integer.toBinaryString(destMnemonic.get(destValueString));
    }

    public String decodeCompValue(String compValueString){
        if (!compMnemonic.containsKey(compValueString)){
            throw new RuntimeException(
                "The comp value '" + compValueString + "' was not found."
            );
        }
        return Integer.toBinaryString(compMnemonic.get(compValueString));
    }

    public String decodeJumpValue(String jumpValueString){
        if (!jumpMnemonic.containsKey(jumpValueString)){
            throw new RuntimeException(
                "The jump value '" + jumpValueString + "' was not found."
            );
        }
        return Integer.toBinaryString(jumpMnemonic.get(jumpValueString));
    }

    private static Map<String, Integer> loadComp(){
        Map<String, Integer> mnemonic = new HashMap<String, Integer>();
        mnemonic.put("0", 42);
        mnemonic.put("1", 63);
        mnemonic.put("-1", 58);
        mnemonic.put("D", 12);
        mnemonic.put("A", 48);
        mnemonic.put("!D", 13);
        mnemonic.put("!A", 49);
        mnemonic.put("-D", 15);
        mnemonic.put("-A", 51);
        mnemonic.put("D+1", 31);
        mnemonic.put("A+1", 55);
        mnemonic.put("D-1", 14);
        mnemonic.put("A-1", 50);
        mnemonic.put("D+A", 2);
        mnemonic.put("D-A", 19);
        mnemonic.put("A-D", 7);
        mnemonic.put("D&A", 0);
        mnemonic.put("D|A", 21);

        return mnemonic;
    }

    private static Map<String, Integer> loadDest(){
        Map<String, Integer> mnemonic = new HashMap<String, Integer>();
        mnemonic.put(null, 0);
        mnemonic.put("M", 1);
        mnemonic.put("D", 2);
        mnemonic.put("MD", 3);
        mnemonic.put("A", 4);
        mnemonic.put("AM", 5);
        mnemonic.put("AD", 6);
        mnemonic.put("AMD", 7);

        return mnemonic;
    }

    private static Map<String, Integer> loadJump(){
        Map<String, Integer> mnemonic = new HashMap<String, Integer>();
        mnemonic.put(null, 0);
        mnemonic.put("JGT", 1);
        mnemonic.put("JEQ", 2);
        mnemonic.put("JGE", 3);
        mnemonic.put("JLT", 4);
        mnemonic.put("JNE", 5);
        mnemonic.put("JLE", 6);
        mnemonic.put("JMP", 7);

        return mnemonic;
    }

    @Override
    public String toString(){
        return getFullInstruction();
    }

    public static String zeroPad(String value){
        return zeroPad(value, 15);
    }

    public static String zeroPad(String value, int target){
        int needed = target - value.length();
        if(needed < 0){
            return value;
        }
        return "0".repeat(needed) + value;
    }

    public static String zeroPad(int value){
        return zeroPad(Integer.toString(value), 15);
    }

    public static String zeroPad(int value, int target){
        return zeroPad(Integer.toString(value), target);
    }

    public CommandType getCommandType(){
        return this.commandType;
    }

    public String getComp(){
        return this.comp;
    }
    public String getDest(){
        return this.dest;
    }
    public String getJump(){
        return this.jump;
    }

    public String getFullInstruction(){
        String instruction = this.comp + this.dest + this.jump;
        if (commandType == CommandType.C_COMMAND){
            return "1" + instruction;
        } else {
            return "0" + instruction;
        }
    }

}

enum CommandType {
    A_COMMAND,
    C_COMMAND,
    L_COMMAND
}