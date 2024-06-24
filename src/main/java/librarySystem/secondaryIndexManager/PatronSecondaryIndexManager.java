package librarySystem.secondaryIndexManager;

import librarySystem.patron.PatronKeyIndexEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PatronSecondaryIndexManager extends SecondaryIndexManager {


    public PatronSecondaryIndexManager(String filename){
        super(filename);
    }

    /**
     * Adds a key index entry to the key index file.
     * The method reads the current key index entries from the key index file and adds the new key index entry.
     * The method sorts the key index entries by key and writes them back to the key index file.
     * @param secKey The key of the Patron to add to the key index file.
     * @param primKey The CPF of the Patron to add to the key index file.
     * @throws IOException If an I/O error occurs.
     */

    public void addSecondaryIndex(String secKey, String primKey) throws IOException {
        String temp_currentLastName;
        String temp_currentCpf;
        List<PatronKeyIndexEntry> patronIndexEntry = new ArrayList<>();

        // Read all current last name indexes
        try (RandomAccessFile secondaryIndexFile = new RandomAccessFile(filename, "r")) {

            while (secondaryIndexFile.getFilePointer() < secondaryIndexFile.length()) {
                temp_currentLastName = secondaryIndexFile.readUTF();
                temp_currentCpf = secondaryIndexFile.readUTF();
                patronIndexEntry.add(new PatronKeyIndexEntry(temp_currentLastName, temp_currentCpf));
            }
        }catch(FileNotFoundException e){
            System.out.println("File not found in addLasNameIndex method, creating new file...");
        }

        // Add the new las name index
        patronIndexEntry.add(new PatronKeyIndexEntry(primKey, secKey));

        // Order the last name indexes by last name
        patronIndexEntry.sort(Comparator.comparing(PatronKeyIndexEntry::getKey));

        // Write the title indexes back to the file
        try (RandomAccessFile secondaryIndexFile = new RandomAccessFile(filename, "rw")) {
            // Clear the file
            secondaryIndexFile.setLength(0);
            for (PatronKeyIndexEntry entry : patronIndexEntry) {
                secondaryIndexFile.writeUTF(entry.getKey());
                secondaryIndexFile.writeUTF(entry.getCpf());
            }
        }
    }


    /**
     * Retrieves the ISBNs of mangas with the specified title.
     * The method reads the title index file and uses binary search to find the title index entries with the specified title.
     * @param key The title of the mangas to find.
     * @return A list of ISBNs of mangas with the specified title.
     * @throws IOException If an I/O error occurs.
     */
    public List<String> getPrimKeyBySecKey(String key) throws IOException {

        List<PatronKeyIndexEntry> patronIndexEntry = new ArrayList<>();
        String temp_currentLastName;
        String temp_cpf;

        // Read all current title indexes
        try (RandomAccessFile secondaryIndexFile = new RandomAccessFile(filename, "r")) {
            while (secondaryIndexFile.getFilePointer() < secondaryIndexFile.length()) {
                temp_currentLastName = secondaryIndexFile.readUTF();
                temp_cpf = secondaryIndexFile.readUTF();
                patronIndexEntry.add(new PatronKeyIndexEntry(temp_currentLastName, temp_cpf));
            }
        }

        // Use binary search to find the last name
        List<String> foundCpfs = new ArrayList<>();
        PatronKeyIndexEntry midEntry;
        int mid;
        int cmp;
        int index;
        int left = 0;
        int right = patronIndexEntry.size() - 1;

        while (left <= right) {
            mid = (left + right) / 2;
            midEntry = patronIndexEntry.get(mid);
            cmp = midEntry.getKey().compareTo(key);

            if (cmp < 0) {
                left = mid + 1;
            } else if (cmp > 0) {
                right = mid - 1;
            } else {
                // Search to the left first
                index = mid;
                while (index >= 0 && patronIndexEntry.get(index).getKey().equals(key)) {
                    foundCpfs.add(patronIndexEntry.get(index).getCpf());
                    index--;
                }
                // Now search to the right
                index = mid + 1;
                while (index < patronIndexEntry.size() && patronIndexEntry.get(index).getKey().equals(key)) {
                    foundCpfs.add(patronIndexEntry.get(index).getCpf());
                    index++;
                }
                break;
            }
        }
        return foundCpfs;
    }

    /**
     * Removes a title index entry from the title index file.
     * The method reads the current title index entries from the title index file and writes them back to a temporary file.
     * The method skips the title index entry with the specified ISBN and renames the temporary file to the title index file.
     * @param primKey_rem The ISBN of the Patron to remove from the title index file.
     * @throws IOException If an I/O error occurs.
     */
    public void removeSecondaryIndex(String primKey_rem) throws IOException {

        File tempFile = new File(TEMP_FILE);
        String temp_currentLastName;
        String temp_currentCpf;


        try (RandomAccessFile secondaryIndexFile = new RandomAccessFile(filename, "rw");
             RandomAccessFile tempsecondaryIndexFile = new RandomAccessFile(tempFile, "rw")) {
            while (secondaryIndexFile.getFilePointer() < secondaryIndexFile.length()) {
                temp_currentLastName = secondaryIndexFile.readUTF();
                temp_currentCpf = secondaryIndexFile.readUTF();
                if (!temp_currentCpf.equals(primKey_rem)) {
                    tempsecondaryIndexFile.writeUTF(temp_currentLastName);
                    tempsecondaryIndexFile.writeUTF(temp_currentCpf);
                }
            }
        }
        tempFile.renameTo(new File(filename));
    }


}
