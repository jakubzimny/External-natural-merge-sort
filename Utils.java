import com.sun.istack.internal.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class Utils {

    private final static String DEFAULT_INPUT_PATH = "./input.txt";

    public static void renameOutputFile(){
        Path source = Paths.get(Sorter.TAPE_A_PATH);
        int i=-1;
        while(true) { //iterate until available name is found
            try {
                i++;
                if(i==0)Files.move(source, source.resolveSibling("output.txt"));
                else{
                    Files.move(source, source.resolveSibling("output"+Integer.toString(i)+".txt"));
                }
                break; //leave when found
            } catch (FileAlreadyExistsException e) {
                //do nothing when file already exists
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
                break; //leave when another exception
            }
        }
    }

    private static void generateRandomRecords(String fileName) {
        System.out.println("Enter number of records to generate:");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        int number;
        try {
            number = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.err.println("Wrong input format.");
            return;
        }
        try (FileWriter fw = new FileWriter(fileName, false);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i = 0; i < number; i++) {
                Random rand = new Random();
                int length = rand.nextInt(31) + 1;
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < length; j++) {
                    char c = (char) (rand.nextInt(127 - (int) ' ' - 1) + (int) ' ' + 1);
                    sb.append(c);
                }
                bw.write(sb.toString() + "\r\n");
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        System.out.println("Generating done");
    }

    private static void getKeyboardInput(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of records:");
        String input = scanner.nextLine();
        int number;
        try {
            number = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.err.println("Wrong input format.");
            return;
        }
        System.out.println("Enter records (up to 30 ASCII characters without whitespaces) separated by new lines:");
        try (FileWriter fw = new FileWriter(DEFAULT_INPUT_PATH, false);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i=0; i<number; i++){
                   bw.write(scanner.nextLine()+"\r\n");
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    @Nullable
    public static String displayMenu(){
        String filePath = DEFAULT_INPUT_PATH;
        String menu = "Choose source of records to be sorted:\n" +
                "1. From file\n" +
                "2. Randomly generated\n" +
                "3. Keyboard input";
        System.out.println(menu);
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        int option;
        try {
            option = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.err.println("Wrong input format.");
            return null;
        }
        switch (option){
            case 1:
                System.out.println("Enter path to input file:");
                filePath = scanner.nextLine();
                break;
            case 2:
                generateRandomRecords(DEFAULT_INPUT_PATH);
                break;
            case 3:
                getKeyboardInput();
                break;
            default:
                System.out.println("Wrong input.");
                return null;
        }
        System.out.println("Do you want to output file content after each phase? (y/n)");
        input = scanner.nextLine();
        if(input.equals("y") || input.equals("Y"))TapeBuffer.verbosity = true;
        else if (input.equals("n") || input.equals("N"))TapeBuffer.verbosity = false;
        else{
            System.err.println("Wrong input format. Assuming \"no\".");
            TapeBuffer.verbosity = false;
        }
        scanner.close();
        return filePath;
    }
}
