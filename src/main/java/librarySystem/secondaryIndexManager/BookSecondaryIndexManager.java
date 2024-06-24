package librarySystem.secondaryIndexManager;

import librarySystem.book.BookKeyIndexEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class    BookSecondaryIndexManager extends SecondaryIndexManager {


    public BookSecondaryIndexManager(String filename){
        super(filename);
    }

    /**
     * Adds a key index entry to the key index file.
     * The method reads the current key index entries from the key index file and adds the new key index entry.
     * The method sorts the key index entries by key and writes them back to the key index file.
     * @param secKey The key of the book to add to the key index file.
     * @param primKey The ISBN of the book to add to the key index file.
     * @throws IOException If an I/O error occurs.
     */

    public void addSecondaryIndex(String secKey, String primKey) throws IOException {
        String temp_currentTitle;
        String temp_currentIsbn;
        List<BookKeyIndexEntry> bookKeyIndexEntry = new ArrayList<>();

        // Read all current title indexes
        try (RandomAccessFile secondaryIndexFile = new RandomAccessFile(filename, "r")) {

            while (secondaryIndexFile.getFilePointer() < secondaryIndexFile.length()) {
                temp_currentTitle = secondaryIndexFile.readUTF();
                temp_currentIsbn = secondaryIndexFile.readUTF();
                bookKeyIndexEntry.add(new BookKeyIndexEntry(temp_currentTitle, temp_currentIsbn));
            }
        }catch(FileNotFoundException e){
            System.out.println("File not found in addTitleIndex method, creating new file...");
        }

        // Add the new title index
        bookKeyIndexEntry.add(new BookKeyIndexEntry(primKey, secKey));

        // Order the title indexes by title
        bookKeyIndexEntry.sort(Comparator.comparing(BookKeyIndexEntry::getKey));

        // Write the title indexes back to the file
        try (RandomAccessFile secondaryIndexFile = new RandomAccessFile(filename, "rw")) {
            // Clear the file
            secondaryIndexFile.setLength(0);
            for (BookKeyIndexEntry entry : bookKeyIndexEntry) {
                secondaryIndexFile.writeUTF(entry.getKey());
                secondaryIndexFile.writeUTF(entry.getIsbn());
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

        List<BookKeyIndexEntry> keyIndexEntry = new ArrayList<>();
        String temp_currentTitle;
        String temp_isbn;

        // Read all current title indexes
        try (RandomAccessFile secondaryIndexFile = new RandomAccessFile(filename, "r")) {
            while (secondaryIndexFile.getFilePointer() < secondaryIndexFile.length()) {
                temp_currentTitle = secondaryIndexFile.readUTF();
                temp_isbn = secondaryIndexFile.readUTF();
                keyIndexEntry.add(new BookKeyIndexEntry(temp_currentTitle, temp_isbn));
            }
        }

        // Use binary search to find the title
        List<String> foundIsbns = new ArrayList<>();
        BookKeyIndexEntry midEntry;
        int mid;
        int cmp;
        int index;
        int left = 0;
        int right = keyIndexEntry.size() - 1;

        while (left <= right) {
            mid = (left + right) / 2;
            midEntry = keyIndexEntry.get(mid);
            cmp = midEntry.getKey().compareTo(key);

            if (cmp < 0) {
                left = mid + 1;
            } else if (cmp > 0) {
                right = mid - 1;
            } else {
                // Found one title, now we need to find all titles with the same name
                // Search to the left first
                index = mid;
                while (index >= 0 && keyIndexEntry.get(index).getKey().equals(key)) {
                    foundIsbns.add(keyIndexEntry.get(index).getIsbn());
                    index--;
                }
                // Now search to the right
                index = mid + 1;
                while (index < keyIndexEntry.size() && keyIndexEntry.get(index).getKey().equals(key)) {
                    foundIsbns.add(keyIndexEntry.get(index).getIsbn());
                    index++;
                }
                break;
            }
        }
        System.out.println(foundIsbns);
        return foundIsbns;
    }

    /**
     * Removes a title index entry from the title index file.
     * The method reads the current title index entries from the title index file and writes them back to a temporary file.
     * The method skips the title index entry with the specified ISBN and renames the temporary file to the title index file.
     * @param primKey_rem The ISBN of the book to remove from the title index file.
     * @throws IOException If an I/O error occurs.
     */
    public void removeSecondaryIndex(String primKey_rem) throws IOException {

        File tempFile = new File(TEMP_FILE);
        String temp_currentTitle;
        String temp_currentIsbn;


        try (RandomAccessFile secondaryIndexFile = new RandomAccessFile(filename, "rw");
             RandomAccessFile tempsecondaryIndexFile = new RandomAccessFile(tempFile, "rw")) {
            while (secondaryIndexFile.getFilePointer() < secondaryIndexFile.length()) {
                temp_currentTitle = secondaryIndexFile.readUTF();
                temp_currentIsbn = secondaryIndexFile.readUTF();
                if (!temp_currentIsbn.equals(primKey_rem)) {
                    tempsecondaryIndexFile.writeUTF(temp_currentTitle);
                    tempsecondaryIndexFile.writeUTF(temp_currentIsbn);
                }
            }
        }
        tempFile.renameTo(new File(filename));
    }


}
