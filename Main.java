
public class Main {
    public static void main(String[] args) {
        String inputFilePath = Utils.displayMenu();
        if (inputFilePath != null) {
            Sorter sorter = new Sorter(500, inputFilePath);
            sorter.sort();
        }
        Utils.renameOutputFile();
    }


}
