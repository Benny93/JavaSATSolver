package dimacs.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by Benjamin Vollmer Matr. 3590766 and Kien-Van Quang Matr. 3746183 on 31.10.16.
 * Parser for DIMACS files
 * Opens file at file path on construction
 */
public class DIMACSParser {
    private static final String SPACE_SEPARATOR = "\\s+";
    private final String m_FilePath;
    private BufferedReader m_bufferedReader;

    public DIMACSParser(String filePath) {
        this.m_FilePath = filePath;
        openFile();
    }

    private void openFile() {
        try {
            m_bufferedReader = new BufferedReader(new FileReader(m_FilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get DIMACSStatistics
     *
     * @return Returns Object of Type DIMACSStatistics.
     * This contains the statistics of the input file
     */
    public Vector<Vector<Integer>> parseFile() {
        if (m_bufferedReader == null) throw new NullPointerException();
        String[] tokens;
        String sCurrentLine;
        Vector<Vector<Integer>> formula = new Vector<Vector<Integer>>();
        try {
            while (null != (sCurrentLine = m_bufferedReader.readLine())) {
                //split line string into single tokens
                tokens = sCurrentLine.split(SPACE_SEPARATOR);
                if (tokens.length > 0 && !tokens[0].equals("c")) {
                    if (tokens[0].equals("p")) {
                        //problem line found
                        //Do nothing
                    } else {
                        //line with clause found
                        handleClause(tokens, formula);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return formula;
    }

    private void handleClause(String[] tokens, Vector<Vector<Integer>> formula) {
        Vector<Integer> clauseVector = new Vector<Integer>();
        for (String token : tokens) {
            if (!token.equals("0") && !token.equals("")) {
                try {
                    Integer variable = Integer.parseInt(token);
                    //add to vector
                    clauseVector.add(variable);
                } catch (Exception e) {
                    System.err.println("Could not parse symbol in formula!");
                    System.exit(1);
                }

            }
        }
        if (clauseVector.size() != 0) {
            formula.add(clauseVector);
        }

    }

}
