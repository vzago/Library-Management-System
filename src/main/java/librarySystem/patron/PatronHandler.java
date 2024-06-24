package librarySystem.patron;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Handles the CRUD operations for the patron class.
 * The data is stored in a binary file named "patron.dat" with a fixed record size of 2048 bytes.
 * The index is stored in a binary file named "patronIndex.dat" with a variable record size.
 * The title index is stored in a binary file named "name_index.dat" with a variable record size.
 * The deleted records spaces are stored in a list.
 */

public class PatronHandler {

    private static final String DATA_FILE = "patron.dat";
    private static final String INDEX_FILE = "patronIndex.dat";
    private static final String TEMP_FILE = "temp.dat";
    private static final String LAST_NAME_INDEX_FILE = "lastName_index.dat";
    private static final String NAME_INDEX_FILE = "name_index.dat";
    private static final int RECORD_SIZE = 2048;
    private final List<Long> deletedRecordsSpaces;

    private PatronSecondaryIndexManager nameIndexManager;
    public PatronSecondaryIndexManager lastNameIndexManager;

    private static final int NOT_FOUND = -1;

    /**
     * Creates a new MangaHandler object.
     * The constructor initializes the list of deleted records spaces by calling the loadDeletedRecordsSpaces method.
     */
    public PatronHandler() {
        deletedRecordsSpaces = new ArrayList<>();
        lastNameIndexManager = new PatronSecondaryIndexManager(LAST_NAME_INDEX_FILE);
        nameIndexManager = new PatronSecondaryIndexManager(NAME_INDEX_FILE);
        loadDeletedRecordsSpaces();
    }

    /**
     * Loads the deleted records spaces from the data file.
     * The method reads the data file and adds the file pointers of the deleted records to the deletedRecordsSpaces list.
     */
    private void loadDeletedRecordsSpaces(){

        char status;

        try (RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "rw")){
            long pointer = 0;
            while (pointer < dataFile.length()) {
                dataFile.seek(pointer);
                status = dataFile.readChar();
                if (status == '*') {
                    deletedRecordsSpaces.add(pointer);
                }
                pointer += RECORD_SIZE;
            }
        } catch (IOException e) {
            System.out.println("Error in loadDeletedRecordsSpaces : " + e.getMessage());
        }
    }


    /**
     * Adds a new patron to the data file.
     * The method writes the patron object to the data file and adds an index entry to the index file.
     * The method also adds a name index entry to the title index file.
     * @param patron The patron object to be added.
     * @throws IOException If an I/O error occurs.
     */
    public void addPatron(Patron patron) throws IOException {

        long filePointer;

        if(getPositionFromIndexFile(patron.getCpf()) != NOT_FOUND){
            throw new IOException("patron already exists");
        }

        if (!deletedRecordsSpaces.isEmpty()) {
            filePointer = deletedRecordsSpaces.remove(0);
        } else {
            try (RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "rw")) {
                filePointer = dataFile.length();
            }catch(FileNotFoundException e){
                System.out.println("File not found, creating new file...");
                filePointer = 0;
            }
        }

        try (RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "rw")) {
            dataFile.seek(filePointer);
            writePatron(dataFile, patron);
            addIndex(patron.getCpf(), filePointer);
            lastNameIndexManager.addSecondaryIndex(patron.getLastName(),patron.getCpf());
            nameIndexManager.addSecondaryIndex(patron.getName(),patron.getCpf());
        }catch(FileNotFoundException e){
            System.out.println("FIle not found, creating new file...");
        }
    }

    /**
     * Retrieves a patron from the data file by its CPF.
     * The method reads the data file using the file pointer from the index file.
     * @param cpf The CPF of the patron to retrieve.
     * @return The patron object with the specified CPF, or null if not found.
     * @throws IOException If an I/O error occurs.
     */
    public Patron getPatron(String cpf) throws IOException{

        long filePointer = getPositionFromIndexFile(cpf);

        if (filePointer == NOT_FOUND){
            System.out.println("patron not found");
            return null;
        }
        try (RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "r")) {
            dataFile.seek(filePointer);
            return readPatron(dataFile);
        }catch (FileNotFoundException e){
            System.out.println("File not found");
            return null;
        }
    }

    /**
     * Updates a patron in the data file by its CPF.
     * The method reads the data file using the file pointer from the index file and updates the patron object.
     * @param cpf The CPF of the patron to update.
     * @param updatedPatron The updated patron object.
     * @throws IOException If an I/O error occurs.
     */
    public void updatePatron(String cpf, Patron updatedPatron) throws IOException {

        long filePointer = getPositionFromIndexFile(cpf);

        if (filePointer == NOT_FOUND){
            throw new IOException("patron not found");
        }
        try (RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "rw")) {
            dataFile.seek(filePointer);
            // Check if the title has changed and update the title index
            Patron tempPatron = getPatron(cpf);
            if(!tempPatron.getLastName().equals(updatedPatron.getLastName())){
                lastNameIndexManager.removeSecondaryIndex(cpf);
                lastNameIndexManager.addSecondaryIndex(updatedPatron.getLastName(),cpf);
            }
            writePatron(dataFile, updatedPatron);
        }catch(FileNotFoundException e){
            System.out.println("File not found");
        }
    }

    /**
     * Deletes a patron from the data file by its CPF.
     * The method reads the data file using the file pointer from the index file and marks the record as deleted.
     * The method also removes the index entry and the last name index entry.
     * @param cpf The CPF of the patron to delete.
     * @throws IOException If an I/O error occurs.
     */
    public void deletePatron(String cpf) throws IOException {

        long filePointer = getPositionFromIndexFile(cpf);

        if (filePointer == NOT_FOUND){
            throw new IOException("patron not found");
        }

        try (RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "rw")) {
            dataFile.seek(filePointer);
            //Mark register as deleted
            dataFile.writeChar('*');
            deletedRecordsSpaces.add(filePointer);
        }
        removeIndex(cpf);
        lastNameIndexManager.removeSecondaryIndex(cpf);
        nameIndexManager.removeSecondaryIndex(cpf);
    }


    /**
     * Searches for patrons by last name.
     * The method searches the title index file for all lastNames that match the specified title.
     * For each title found, the method retrieves the CPF from the last name index file and retrieves the patrons from the data file.
     * @param lastName The title to search for.
     * @return A list of patron objects with lastNames that match the specified title.
     * @throws IOException If an I/O error occurs.
     */
    public List<Patron> searchPatronsByLastName(String lastName) throws IOException {

        List<String> cpfs = lastNameIndexManager.getCpfsByKey(lastName);
        List<Patron> patrons = new ArrayList<>();
        Patron patron;

        for (String cpf : cpfs) {
            patron = getPatron(cpf);
            if (patron != null) {
                patrons.add(patron);
            }
        }
        return patrons;
    }

    /**
     * Searches for a patron by its CPF.
     * The method retrieves the patron from the data file using the getManga method.
     * @param cpf The CPF of the patron to search for.
     * @return A list with the patron object with the specified CPF, or an empty list if not found.
     * @throws IOException If an I/O error occurs.
     */
    public List<Patron> searchPatronByCpf(String cpf) throws IOException {

        List<Patron> patrons = new ArrayList<>();
        Patron patron = getPatron(cpf);

        if (patron != null) {
            patrons.add(patron);
        }
        return patrons;
    }


    public List<Patron> searchPatronByName(String name) throws IOException {

        List<String> cpfs = nameIndexManager.getCpfsByKey(name);
        List<Patron> patrons = new ArrayList<>();
        Patron patron;

        for (String cpf : cpfs) {
            patron = getPatron(cpf);
            if (patron != null) {
                patrons.add(patron);
            }
        }
        return patrons;
    }

    /**
     * Writes a patron object to the data file.
     * The method writes the patron object to the data file using the specified record size.
     * The method fills the remaining space with blank bytes to complete the record size.
     * @param data_file The RandomAccessFile object for the data file.
     * @param patron The patron object to write.
     * @throws IOException If an I/O error occurs.
     */
    private void writePatron(RandomAccessFile data_file, Patron patron) throws IOException {

        long start_Pointer = data_file.getFilePointer();
        data_file.writeUTF(patron.getCpf());
        data_file.writeUTF(patron.getName());
        data_file.writeUTF(patron.getLastName());
        data_file.writeUTF(patron.getPassword());
        data_file.writeUTF(patron.getEmail());
        data_file.writeUTF(patron.getPhoneNumber());
        long end_Pointer = data_file.getFilePointer();
        // Fill the remaining space with blank bytes to complete the record size
        long remainingBytes = RECORD_SIZE - (end_Pointer - start_Pointer);
        if (remainingBytes > 0) {
            data_file.write(new byte[(int) remainingBytes]);
        }
    }

    /**
     * Reads a patron object from the data file.
     * The method reads the patron object from the data file using the specified record size.
     * @param file The RandomAccessFile object for the data file.
     * @return The patron object read from the data file.
     * @throws IOException If an I/O error occurs.
     */
    private Patron readPatron(RandomAccessFile file) throws IOException {

        String cpf = file.readUTF();
        String name = file.readUTF();
        String lastName = file.readUTF();
        String password = file.readUTF();
        String email = file.readUTF();
        String phoneNumber = file.readUTF();
        file.seek(file.getFilePointer() + (RECORD_SIZE - (file.getFilePointer() % RECORD_SIZE)));
        return new Patron(name,lastName,cpf,password,email,phoneNumber);
    }

    /**
     * Adds an index entry to the index file.
     * The method reads the current index entries from the index file and adds the new index entry.
     * The method sorts the index entries by CPF and writes them back to the index file.
     * @param cpf The CPF of the  to add to the index file.
     * @param filePointer The file pointer of the  in the data file.
     * @throws IOException If an I/O error occurs.
     */
    private void addIndex(String cpf, long filePointer) throws IOException {

        List<PatronIndexEntry> indexEntries = new ArrayList<>();
        String currentCpf;
        long currentPointer;

        // Read all actual index
        try (RandomAccessFile patronIndexFile = new RandomAccessFile(INDEX_FILE, "r")) {
            while (patronIndexFile.getFilePointer() < patronIndexFile.length()) {
                currentCpf = patronIndexFile.readUTF();
                currentPointer = patronIndexFile.readLong();
                indexEntries.add(new PatronIndexEntry(currentCpf, currentPointer));
            }
        }catch(FileNotFoundException e){
            System.out.println("File not found, creating new file...");
        }catch(IOException e){
            System.out.println("I/O Error in addIndex method");
        }catch(Exception e){
            System.out.println("Error in addIndex method");
        }

        indexEntries.add(new PatronIndexEntry(cpf, filePointer));

        indexEntries.sort(Comparator.comparing(PatronIndexEntry::getCpf));

        try (RandomAccessFile patronIndexFile = new RandomAccessFile(INDEX_FILE, "rw")) {
            patronIndexFile.setLength(0); //Clear the file
            for (PatronIndexEntry entry : indexEntries) {
                patronIndexFile.writeUTF(entry.getCpf());
                patronIndexFile.writeLong(entry.getFilePointer());
            }
        }
    }

    /**
     * Retrieves the file pointer of a  in the data file by its CPF.
     * The method reads the index file and uses binary search to find the index entry with the specified CPF.
     * @param cpf The CPF of the  to find.
     * @return The file pointer of the patron in the data file, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    private long getPositionFromIndexFile(String cpf) throws IOException {

        List<PatronIndexEntry> indexEntries = new ArrayList<>();
        String currentCpf;
        long filePointer;

        // Read all current indexes
        try (RandomAccessFile patronIndexFile = new RandomAccessFile(INDEX_FILE, "r")) {
            while (patronIndexFile.getFilePointer() < patronIndexFile.length()) {
                currentCpf = patronIndexFile.readUTF();
                filePointer = patronIndexFile.readLong();
                indexEntries.add(new PatronIndexEntry(currentCpf, filePointer));
            }
        }catch(FileNotFoundException e){
            System.out.println("File not found in getPositionFromIndexFile method");
        }

        // Use binary search to find the index
        PatronIndexEntry midEntry;
        int mid;
        int cmp;
        int left = 0;
        int right = indexEntries.size() - 1;

        while (left <= right) {
            mid = (left + right) / 2;
            midEntry = indexEntries.get(mid);
            cmp = midEntry.getCpf().compareTo(cpf);
            if (cmp < 0) {
                left = mid + 1;
            } else if (cmp > 0) {
                right = mid - 1;
            } else {
                return midEntry.getFilePointer();
            }
        }
        return NOT_FOUND;
    }

    /**
     * Removes an index entry from the index file.
     * The method reads the current index entries from the index file and writes them back to a temporary file.
     * The method skips the index entry with the specified CPF and renames the temporary file to the index file.
     * @param cpf_rem The CPF of the patron to remove from the index file.
     * @throws IOException If an I/O error occurs.
     */
    private void removeIndex(String cpf_rem) throws IOException {

        File tempFile = new File(TEMP_FILE);
        String currentCpf;
        long filePointer;

        try (RandomAccessFile patronIndexFile = new RandomAccessFile(INDEX_FILE, "rw");
             RandomAccessFile tempIndexFile = new RandomAccessFile(tempFile, "rw")) {
            // Besides storing the PatronIndexEntry in a list, we write it to a temporary file to avoid reading the whole index file again
            while (patronIndexFile.getFilePointer() < patronIndexFile.length()) {
                currentCpf = patronIndexFile.readUTF();
                filePointer = patronIndexFile.readLong();
                if (!currentCpf.equals(cpf_rem)) {
                    tempIndexFile.writeUTF(currentCpf);
                    tempIndexFile.writeLong(filePointer);
                }
            }
        }catch(FileNotFoundException e){
            System.out.println("File not found in removeIndex method");
        }
        tempFile.renameTo(new File(INDEX_FILE));
    }

    /**
     * Retrieves all the last names of the patrons.
     * The method reads the last name index file and retrieves all the last names.
     * @return A list of all the lastNames of the patrons.
     * @throws IOException If an I/O error occurs.
     */
    public List<String> getAllPatronsLastNames() throws IOException {
        List<String> lastNames = new ArrayList<>();
        String lastName;

        try (RandomAccessFile patronIndexFile = new RandomAccessFile(LAST_NAME_INDEX_FILE, "rw")) {

            patronIndexFile.seek(0);
            while (patronIndexFile.getFilePointer() < patronIndexFile.length()) {
                lastName = patronIndexFile.readUTF();
                patronIndexFile.readUTF(); // Skip the CPF
                lastNames.add(lastName);
            }
        }

        return lastNames;
    }
}





