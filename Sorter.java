import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Sorter {
    public static final String TAPE_A_PATH = "./tapeA.txt";
    private static final String TAPE_B_PATH = "./tapeB.txt";
    private static final String TAPE_C_PATH = "./tapeC.txt";
    private int totalReadCount;
    private int totalWriteCount;

    private String inputPath;
    private int bufferSize;

    Sorter(int bufferSize, String inputPath){
        this.bufferSize = bufferSize;
        this.inputPath = inputPath;
        this.totalReadCount = 0;
        this.totalWriteCount = 0;
    }

    private boolean distribute(boolean first) {
        TapeFile inputFile = new TapeFile(inputPath);
        TapeBuffer tapeC = new TapeBuffer(bufferSize, TAPE_C_PATH, true);
        TapeBuffer tapeA = new TapeBuffer(bufferSize, TAPE_A_PATH, false);
        TapeBuffer tapeB = new TapeBuffer(bufferSize, TAPE_B_PATH, false);
        TapeBuffer currentTape = tapeA;
        boolean sorted = true;
        Record record;
        Record lastRecord = null;
        while (true) {
            if (first) record = tapeC.getRecord(inputFile);
            else record = tapeC.getRecord(tapeC.getTapeFile());
            if (record == null) break;
            if (lastRecord != null && lastRecord.compareTo(record) < 0) {//if new run
                sorted = false; // more than 1 run so not yet sorted
                currentTape = currentTape == tapeA ? tapeB : tapeA; //switch tapes
            }
            currentTape.saveRecord(record);
            lastRecord = record;
        }
        tapeA.saveBuffer();
        if(!sorted)tapeB.saveBuffer();
        totalReadCount += tapeC.getReadCount();
        totalWriteCount += tapeA.getWriteCount();
        totalWriteCount += tapeB.getWriteCount();
        if (!first) {
            try {
                Files.delete(Paths.get(tapeC.getTapeFile().getPath()));
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
        }
        return sorted;
    }

    private void merge() {
        TapeBuffer tapeC = new TapeBuffer(bufferSize, TAPE_C_PATH, false);
        TapeBuffer tapeA = new TapeBuffer(bufferSize, TAPE_A_PATH, true);
        TapeBuffer tapeB = new TapeBuffer(bufferSize, TAPE_B_PATH, true);
        Record lastRA = null;
        Record lastRB = null;
        Record rA = tapeA.getRecord(tapeA.getTapeFile());
        Record rB = tapeB.getRecord(tapeB.getTapeFile());
        while (true) {
            if((rA != null )&&(rB != null)){ // none of tapes are empty
                if(lastRA != null && lastRA.compareTo(rA) < 0) { // if new run on tape A
                    while((rB!=null) && (lastRB == null || lastRB.compareTo(rB) >= 0)){// copy rest of the run on tapeB
                        tapeC.saveRecord(rB);
                        if(TapeBuffer.verbosity)System.out.println(new String(rB.getCharset()));
                        lastRB = rB;
                        rB = tapeB.getRecord(tapeB.getTapeFile());
                    }
                    lastRA = null;
                    lastRB = null;
                }
                else if(lastRB != null && lastRB.compareTo(rB) < 0) { // if new run on tape B
                    while((rA!=null) && (lastRA == null || lastRA.compareTo(rA) >= 0)){// copy rest of the run on tapeA
                        tapeC.saveRecord(rA);
                        if(TapeBuffer.verbosity)System.out.println(new String(rA.getCharset()));
                        lastRA = rA;
                        rA = tapeA.getRecord(tapeA.getTapeFile());
                    }
                    lastRA = null;
                    lastRB = null;
                }
                else{ // No new runs so take smaller
                    if(rA.compareTo(rB) > 0) { // if record A is smaller than B
                        tapeC.saveRecord(rA);
                        if(TapeBuffer.verbosity)System.out.println(new String(rA.getCharset()));
                        lastRA = rA;
                        rA = tapeA.getRecord(tapeA.getTapeFile());
                    }
                    else{ // rB is smaller or equal to rA
                        tapeC.saveRecord(rB);
                        if(TapeBuffer.verbosity)System.out.println(new String(rB.getCharset()));
                        lastRB = rB;
                        rB = tapeB.getRecord(tapeB.getTapeFile());
                    }
                }
            }
            else if (rA == null){ // tape A is empty
                while(rB!=null){ // so copy rest of tape B and finish
                    tapeC.saveRecord(rB);
                    if(TapeBuffer.verbosity)System.out.println(new String(rB.getCharset()));
                    rB = tapeB.getRecord(tapeB.getTapeFile());
                }
                break;
            }
            else if (rB == null){ // tape B is empty
                while(rA!=null){ //so copy rest of tape A and finish
                    tapeC.saveRecord(rA);
                    if(TapeBuffer.verbosity)System.out.println(new String(rA.getCharset()));
                    rA = tapeA.getRecord(tapeA.getTapeFile());
                }
                break;
            }
        }
        tapeC.saveBuffer();
        totalReadCount += tapeA.getReadCount();
        totalReadCount += tapeB.getReadCount();
        totalWriteCount += tapeC.getWriteCount();
        try {
            Files.delete(Paths.get(tapeA.getTapeFile().getPath()));
            Files.delete(Paths.get(tapeB.getTapeFile().getPath()));
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    public void sort( ){
        System.out.println("Sorting...");
        int phaseCount = 0;
        boolean sorted;
        System.out.println("\nPhase " + ++phaseCount + "\n");
        sorted = distribute(true); //first phase from input file
        if(!sorted) {
            merge();
            while (true) {
                sorted = distribute(false);
                if (sorted) break;
                System.out.println("\nPhase " + ++phaseCount + "\n");
                merge();
            }
        }
        System.out.println("\nSorting done.");
        System.out.println("Phases: " + phaseCount);
        System.out.println("Total read count: " + totalReadCount);
        System.out.println("Total write count: " + totalWriteCount);

    }

}
