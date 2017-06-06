package org.openintents.convertcsv.opencsv;

/**
 * Copyright 2005 Bytecode Pty Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * The project is available at http://sourceforge.net/projects/opencsv/
 */

/**
 * The project is available at http://sourceforge.net/projects/opencsv/
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A very simple CSV reader released under a commercial-friendly license.
 *
 * @author Glen Smith
 *
 */
public class CSVReader {

    /** The default separator to use if none is supplied to the constructor. */
    public static final char DEFAULT_SEPARATOR = ',';
    /**
     * The default quote character to use if none is supplied to the
     * constructor.
     */
    public static final char DEFAULT_QUOTE_CHARACTER = '"';
    /**
     * The default line to start reading.
     */
    public static final int DEFAULT_SKIP_LINES = 0;
    private BufferedReader br;
    private boolean hasNext;
    private boolean linesSkiped;
    private char quotechar;
    private char separator;
    private int skipLines;

    /**
     * Constructs CSVReader using a comma for the separator.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     */
    public CSVReader(Reader reader) {
        this(reader, DEFAULT_SEPARATOR);
    }

    /**
     * Constructs CSVReader with supplied separator.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries.
     */
    public CSVReader(Reader reader, char separator) {
        this(reader, separator, DEFAULT_QUOTE_CHARACTER);
    }


    /**
     * Constructs CSVReader with supplied separator and quote char.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     */
    public CSVReader(Reader reader, char separator, char quotechar) {
        this(reader, separator, quotechar, DEFAULT_SKIP_LINES);
    }

    /**
     * Constructs CSVReader with supplied separator and quote char.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param line
     *            the line number to skip for start reading
     */
    public CSVReader(Reader reader, char separator, char quotechar, int line) {
        this.hasNext = true;
        this.br = new BufferedReader(reader);
        this.separator = separator;
        this.quotechar = quotechar;
        this.skipLines = line;
    }

    /**
     * Reads the entire file into a List with each element being a String[] of
     * tokens.
     *
     * @return a List of String[], with each String[] representing a line of the
     *         file.
     *
     * @throws IOException
     *             if bad things happen during the read
     */
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

    /**
     * Reads the next line from the buffer and converts to a string array.
     *
     * @return a string array with each comma-separated element as a separate
     *         entry.
     *
     * @throws IOException
     *             if bad things happen during the read
     */
    public String[] readNext() throws IOException {
        return this.hasNext ? parseLine(getNextLine()) : null;
    }

    /**
     * Reads the next line from the file.
     *
     * @return the next line from the file without trailing newline
     * @throws IOException
     *             if bad things happen during the read
     */
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

    /**
     * Parses an incoming String and returns an array of elements.
     *
     * @param nextLine
     *            the string to parse
     * @return the comma-tokenized list of elements, or null if nextLine is null
     * @throws IOException if bad things happen during the read
     */
    private String[] parseLine(String nextLine) throws IOException {

        if (nextLine == null) {
            return null;
        }

        List tokensOnThisLine = new ArrayList();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        do {
            if (inQuotes) {
                // continuing a quoted section, reappend newline
                sb.append("\n");
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

    /**
     * Closes the underlying reader.
     *
     * @throws IOException if the close fails
     */
    public void close() throws IOException {
        this.br.close();
    }

}
