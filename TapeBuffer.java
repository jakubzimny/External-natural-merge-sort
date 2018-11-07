import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TapeBuffer {

    private int size;
    private int currentRecord;
    private int counter;
    private int writeCount;
    private int readCount;
    private boolean isReading; //if true buffer is reading records, when false its saving
    private TapeFile tape;
    private ArrayList<Record> recordsBuffer = new ArrayList<>();
    public static boolean verbosity;

    public int getWriteCount() {
        return writeCount;
    }

    public int getReadCount() {
        return readCount;
    }

    TapeBuffer(int size, String tapeFilePath, boolean isReading) {
        this.size = size;
        if (isReading) this.currentRecord = size;
        else this.currentRecord = 0;
        this.counter = 0;
        this.tape = new TapeFile(tapeFilePath);
        this.writeCount = 0;
        this.readCount = 0;
    }

    public TapeFile getTapeFile() {
        return tape;
    }

    public Record getRecord(TapeFile file) {
        String line;
        if (currentRecord >= size) { // if whole buffer read, read from disk
            readCount++;
            recordsBuffer.clear();
            counter = 0;
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()))) {
                reader.skip(file.getOffset());
                while ((counter < size) && (!file.isEndOfFile())) { // while buffer not full and not eof, fill the buffer
                    line = reader.readLine();
                    if (line == null) file.setEndOfFile(true);
                    else {
                        recordsBuffer.add(new Record(line.toCharArray()));
                        counter++;
                        file.setOffset(file.getOffset() + line.length() + 2);
                    }
                }
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
            currentRecord = 0;
        }
        if (currentRecord >= counter && file.isEndOfFile()) {
            return null;
        }
        currentRecord++;
        return recordsBuffer.get(currentRecord - 1);
    }

    public void saveRecord(Record record) {
        if (currentRecord >= size) { //when whole buffer full, save to disk
            writeCount++;
            try (FileWriter fw = new FileWriter(tape.getPath(), true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                for (Record r : recordsBuffer) {
                    String line = new String(r.getCharset());
                    bw.write(line + "\r\n");
                }
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
            recordsBuffer.clear();
            currentRecord = 0;
        }
        if (record == null) return;
        recordsBuffer.add(record);
        currentRecord++;
    }

    public void saveBuffer() { //save records remaining in buffer after the end of phase
        if (currentRecord != 0) writeCount++;
        try (FileWriter fw = new FileWriter(tape.getPath(), true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i = 0; i < currentRecord; i++) {
                String line = new String(recordsBuffer.get(i).getCharset());
                bw.write(line + "\r\n");
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        recordsBuffer.clear();
        currentRecord = 0;
    }

}
