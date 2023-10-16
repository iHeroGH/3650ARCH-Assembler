public class Command {

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
        if (instruction.startsWith("@")){
            commandType = CommandType.A_COMMAND;
            String fullValue = zeroPad(
                Integer.toBinaryString(decodeAInstruction(instruction))
            );
            this.comp = fullValue.substring(0, 10);
            this.dest = fullValue.substring(10, 13);
            this.jump = fullValue.substring(13);

        } else if (instruction.matches("\\(.*\\)")){
            commandType = CommandType.L_COMMAND;
            String fullValue = zeroPad("0");
            this.comp = fullValue.substring(0, 10);
            this.dest = fullValue.substring(10, 13);
            this.jump = fullValue.substring(13);
        } else {
            commandType = CommandType.C_COMMAND;
            String aValue = "";
            String compValue = "";
            String destValue = "";
            String jumpValue = "";
            this.comp = aValue + compValue;
            this.dest = destValue;
            this.jump = jumpValue;
        }

        String decoded = getFullInstruction();
        if (decoded.length() != 16){
            throw new RuntimeException(
                "Something went wrong decoding instruction '" +
                instruction + "'. The decoded '" +
                decoded + "' length does not match."
            );
        }

        return decoded;
    }

    public int decodeAInstruction(String instruction){
        if (!instruction.startsWith("@")){
            throw new RuntimeException(
                "Instruction incorrectly identified as A-Instruction"
            );
        }

        this.symbol = instruction.substring(1);
        if (this.symbol.matches("R\\d+")){
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

        if (this.symbol.matches("\\d+")){
            try{
                return Integer.parseInt(this.symbol);
            } catch (Exception e){
                System.out.println(
                    "Something went wrong extracting value from '" +
                    instruction + "''."
                );
                e.printStackTrace();
            }
        }

        if (this.symbol.matches("@\\D+"))
            this.symbolTable.addVariable(symbol);

        if (symbolTable.contains(this.symbol))
            return symbolTable.getAddress(this.symbol);

        throw new RuntimeException(
            "The label found in instruction '" + instruction + "' was not found."
        );
    }

    public int decodeCInstruction(String instruction){
        return 0;
    }

    @Override
    public String toString(){
        return getFullInstruction();
    }

    public static String zeroPad(String value){
        int needed = 15 - value.length();
        if(needed < 0){
            return value;
        }
        return "0".repeat(needed) + value;
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
            return "111" + instruction;
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