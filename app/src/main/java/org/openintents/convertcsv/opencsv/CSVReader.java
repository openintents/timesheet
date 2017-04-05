package org.openintents.convertcsv.opencsv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    public static final char DEFAULT_QUOTE_CHARACTER = '\"';
    public static final char DEFAULT_SEPARATOR = ',';
    public static final int DEFAULT_SKIP_LINES = 0;
    private BufferedReader br;
    private boolean hasNext;
    private boolean linesSkiped;
    private char quotechar;
    private char separator;
    private int skipLines;

    public CSVReader(Reader reader) {
        this(reader, DEFAULT_SEPARATOR);
    }

    public CSVReader(Reader reader, char separator) {
        this(reader, separator, DEFAULT_QUOTE_CHARACTER);
    }

    public CSVReader(Reader reader, char separator, char quotechar) {
        this(reader, separator, quotechar, 0);
    }

    public CSVReader(Reader reader, char separator, char quotechar, int line) {
        this.hasNext = true;
        this.br = new BufferedReader(reader);
        this.separator = separator;
        this.quotechar = quotechar;
        this.skipLines = line;
    }

    public List readAll() throws IOException {
        List allElements = new ArrayList();
        while (this.hasNext) {
            String[] nextLineAsTokens = readNext();
            if (nextLineAsTokens != null) {
                allElements.add(nextLineAsTokens);
            }
        }
        return allElements;
    }

    public String[] readNext() throws IOException {
        return this.hasNext ? parseLine(getNextLine()) : null;
    }

    private String getNextLine() throws IOException {
        if (!this.linesSkiped) {
            for (int i = 0; i < this.skipLines; i++) {
                this.br.readLine();
            }
            this.linesSkiped = true;
        }
        String nextLine = this.br.readLine();
        if (nextLine == null) {
            this.hasNext = false;
        }
        return this.hasNext ? nextLine : null;
    }

    private String[] parseLine(String nextLine) throws IOException {
        if (nextLine == null) {
            return null;
        }
        List tokensOnThisLine = new ArrayList();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        do {
            if (inQuotes) {
                sb.append(CSVWriter.DEFAULT_LINE_END);
                nextLine = getNextLine();
                if (nextLine == null) {
                    break;
                }
            }
            int i = 0;
            while (i < nextLine.length()) {
                char c = nextLine.charAt(i);
                if (c == this.quotechar) {
                    if (inQuotes && nextLine.length() > i + 1 && nextLine.charAt(i + 1) == this.quotechar) {
                        sb.append(nextLine.charAt(i + 1));
                        i++;
                    } else {
                        inQuotes = !inQuotes;
                        if (i > 2 && nextLine.charAt(i - 1) != this.separator && nextLine.length() > i + 1 && nextLine.charAt(i + 1) != this.separator) {
                            sb.append(c);
                        }
                    }
                } else if (c != this.separator || inQuotes) {
                    sb.append(c);
                } else {
                    tokensOnThisLine.add(sb.toString());
                    sb = new StringBuffer();
                }
                i++;
            }
        } while (inQuotes);
        tokensOnThisLine.add(sb.toString());
        return (String[]) tokensOnThisLine.toArray(new String[0]);
    }

    public void close() throws IOException {
        this.br.close();
    }
}
