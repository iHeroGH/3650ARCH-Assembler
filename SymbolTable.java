import java.util.Map;
import java.util.HashMap;

public class SymbolTable{
    Map<String, Integer> symbolMap;

    public SymbolTable(){
        this.symbolMap = new HashMap<String, Integer>();
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
        this.symbolMap.put(symbol, address);
    }

    public boolean contains(String symbol){
        return this.symbolMap.keySet().contains(symbol);
    }

    public int getAddress(String symbol){
        return this.symbolMap.get(symbol);
    }

}