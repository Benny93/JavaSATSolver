package dataStructure;


import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class CDCLTest {

    /**
     * Tests all given files in small_aim
     * @throws Exception
     */
    @Test
    public void testSolve() throws Exception {
        //test yes files
        File folder = new File("small_aim/yes");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                System.out.println("Testing: " + file.getName());
                testYesFile(file.getPath());
            }
        }
        //test no files
        folder = new File("small_aim/no");
        listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                System.out.println("Testing: " + file.getName());
                testNoFile(file.getPath());
            }
        }
    }

    private void testYesFile(String filePath) {
        ClauseSet clauseSet = new ClauseSet(filePath);
        CDCL cdcl = new CDCL(clauseSet);
        cdcl.verboseOutput = false;
        assertNotNull(cdcl);
        boolean sat = cdcl.solve();
        assertTrue(sat);
    }

    private void testNoFile(String filePath) {
        ClauseSet clauseSet = new ClauseSet(filePath);
        //System.out.print(clauseSet.clausesToString());
        CDCL cdcl = new CDCL(clauseSet);
        assertNotNull(cdcl);
        boolean sat = cdcl.solve();
        assertFalse(sat);
    }

    @Test
    public void testA10_1(){
        ClauseSet clauseSet = new ClauseSet("exercise10/a10_1.cnf");
        //System.out.print(clauseSet.clausesToString());
        int[ ] selectionOrder = { 1, 2, 3, 5, 6, 4, 7, 8};
        CDCL cdcl = new CDCLEx10(clauseSet,selectionOrder);
        assertNotNull(cdcl);
        cdcl.verboseOutput = true;
        cdcl.setDEFAULT_ASSING(true);
        boolean sat = cdcl.solve();
        System.out.print("Is Sat " + sat);
    }

    @Test
    public void testA10_2(){
        ClauseSet clauseSet = new ClauseSet("exercise10/a10_2.cnf");
        //System.out.print(clauseSet.clausesToString());
        int[ ] selectionOrder = { 4, 1, 2, 3, 5};
        CDCL cdcl = new CDCLEx10(clauseSet,selectionOrder);
        assertNotNull(cdcl);
        cdcl.verboseOutput = true;
        boolean sat = cdcl.solve();
        System.out.print("Is Sat " + sat);
    }


}