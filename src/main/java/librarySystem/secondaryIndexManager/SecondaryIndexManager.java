package librarySystem.secondaryIndexManager;

import java.io.IOException;
import java.util.List;

public abstract class SecondaryIndexManager {

    protected static final String TEMP_FILE = "temp.dat";
    protected String filename;

    public SecondaryIndexManager(String filename){
        this.filename = filename;
    }

    public abstract void addSecondaryIndex(String secKey, String primKey) throws IOException;
    

    public abstract List<String> getPrimKeyBySecKey(String key) throws IOException;


    public abstract void removeSecondaryIndex(String primKey_rem) throws IOException;

        
}
