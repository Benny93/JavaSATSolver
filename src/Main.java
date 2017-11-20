import dataStructure.CDCL;
import dataStructure.ClauseSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class
 */
public class Main {
    private static final String DIMACS_FILE_EXTENSION = ".cnf";
    private static final String PRINT_SEPERATOR = "-----------------------------------------" +
            "---------------------------------------";


    /**
     * Main function
     *
     * @param args Input arguments
     */
    public static void main(String[] args) {
        List<String> filePaths = getParams(args);


        if (filePaths == null) {
            System.err.print("No file paths");
            System.exit(1);
        }

        for (String filePath : filePaths) {
            System.out.println("Processing: " + filePath);
            ClauseSet clauseSet = new ClauseSet(filePath);
            CDCL cdcl = new CDCL(clauseSet);
            cdcl.solve();
            System.out.println(PRINT_SEPERATOR);
        }

    }

    private static List<String> getParams(String[] args) {
        List<String> listOfFiles = new ArrayList<String>();
        if (args.length != 0) {
            listOfFiles.add(args[0]);
            return  listOfFiles;
        } else {
            listOfFiles = findInputFiles();
            if (listOfFiles.size() != 0) {
                return listOfFiles;
            } else {
                String USAGE_INSTRUCTION = "USAGE: \n" +
                        "Main <Path to File> : " +
                        "CDCL on single file\n" +
                        "OR\n" +
                        "Main <No Parameter>: program searches for folder small_aim and tests all contained files.\n";
                System.out.print(USAGE_INSTRUCTION);
                System.exit(1);
            }
        }
        return null;
    }

    private static ArrayList<String> findInputFiles() {
        //search current working directory
        ArrayList<String> result = new ArrayList<String>();
        String smallAimFolderYes = "small_aim/yes/";
        String smallAimFolderNo = "small_aim/no/";
        try {
            File folder = new File(smallAimFolderYes);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    String fname = file.getName();
                    if (fname.contains(".")) {
                        if (fname.substring(fname.lastIndexOf(".")).equals(DIMACS_FILE_EXTENSION)) {
                            result.add(file.getPath());
                        }
                    }
                }
            }
            folder = new File(smallAimFolderNo);
            listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    String fname = file.getName();
                    if (fname.contains(".")) {
                        if (fname.substring(fname.lastIndexOf(".")).equals(DIMACS_FILE_EXTENSION)) {
                            result.add(file.getPath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
