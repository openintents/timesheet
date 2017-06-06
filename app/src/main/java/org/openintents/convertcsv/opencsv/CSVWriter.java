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
 * <p>
 * Modifications:
 * - Peli: Dec 2, 2008: Add possibility to write mixed output through new functions
 * write() and writeNewline().
 * - Peli: Dec 3, 2008: Add writeValue() function.
 */

/**
 * The project is available at http://sourceforge.net/projects/opencsv/
 */

/**
 * Modifications:
 *   - Peli: Dec 2, 2008: Add possibility to write mixed output through new functions
 *     write() and writeNewline().
 *   - Peli: Dec 3, 2008: Add writeValue() function.
 */

import org.openintents.timesheet.activity.JobListItemView;
import org.openintents.util.DurationFormater;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class CSVWriter<T> {
    public static final char DEFAULT_ESCAPE_CHARACTER = '\"';
    public static final String DEFAULT_LINE_END = "\n";
    public static final char DEFAULT_QUOTE_CHARACTER = '\"';
    public static final char DEFAULT_SEPARATOR = ',';
    public static final char NO_ESCAPE_CHARACTER = '\u0000';
    public static final char NO_QUOTE_CHARACTER = '\u0000';
    private static final SimpleDateFormat DATE_FORMATTER;
    private static final SimpleDateFormat TIMESTAMP_FORMATTER;

    static {
        TIMESTAMP_FORMATTER = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yyyy");
    }

    public T extras;
    private int currentColumn;
    private char escapechar;
    private String lineEnd;
    private PrintWriter pw;
    private char quotechar;
    private Writer rawWriter;
    private char separator;
    private StringBuffer stringBuffer;

    public CSVWriter(Writer writer) {
        this(writer, DEFAULT_SEPARATOR);
    }

    public CSVWriter(Writer writer, char separator) {
        this(writer, separator, DEFAULT_QUOTE_CHARACTER);
    }

    public CSVWriter(Writer writer, char separator, char quotechar) {
        this(writer, separator, quotechar, (char) DEFAULT_QUOTE_CHARACTER);
    }

    public CSVWriter(Writer writer, char separator, char quotechar, char escapechar) {
        this(writer, separator, quotechar, escapechar, DEFAULT_LINE_END);
    }

    public CSVWriter(Writer writer, char separator, char quotechar, String lineEnd) {
        this(writer, separator, quotechar, DEFAULT_QUOTE_CHARACTER, lineEnd);
    }

    public CSVWriter(Writer writer, char separator, char quotechar, char escapechar, String lineEnd) {
        this.rawWriter = writer;
        this.pw = new PrintWriter(writer);
        this.separator = separator;
        this.quotechar = quotechar;
        this.escapechar = escapechar;
        this.lineEnd = lineEnd;
        this.currentColumn = 0;
        this.stringBuffer = new StringBuffer();
    }

    private static String getColumnValue(ResultSet rs, int colType, int colIndex) throws SQLException, IOException {
        String value = "";
        switch (colType) {
            case -7:
                Object bit = rs.getObject(colIndex);
                if (bit != null) {
                    value = String.valueOf(bit);
                    break;
                }
                break;
            case -6:
            case JobListItemView.STATUS_BREAK /*4*/:
            case JobListItemView.STATUS_BREAK2 /*5*/:
                int intValue = rs.getInt(colIndex);
                if (!rs.wasNull()) {
                    value = String.valueOf(intValue);
                    break;
                }
                break;
            case -5:
            case DurationFormater.TYPE_FORMAT_NICE /*2*/:
            case DurationFormater.TYPE_FORMAT_DONT_UPDATE_INPUT_BOX /*3*/:
            case 6:
            case 7:
            case 8:
                BigDecimal bd = rs.getBigDecimal(colIndex);
                if (bd != null) {
                    value = String.valueOf(bd.doubleValue());
                    break;
                }
                break;
            case -1:
            case DurationFormater.TYPE_FORMAT_SECONDS /*1*/:
            case 12:
                value = rs.getString(colIndex);
                break;
            case 16:
                boolean b = rs.getBoolean(colIndex);
                if (!rs.wasNull()) {
                    value = Boolean.valueOf(b).toString();
                    break;
                }
                break;
            case 91:
                Date date = rs.getDate(colIndex);
                if (date != null) {
                    value = DATE_FORMATTER.format(date);
                    break;
                }
                break;
            case 92:
                Time t = rs.getTime(colIndex);
                if (t != null) {
                    value = t.toString();
                    break;
                }
                break;
            case 93:
                Timestamp tstamp = rs.getTimestamp(colIndex);
                if (tstamp != null) {
                    value = TIMESTAMP_FORMATTER.format(tstamp);
                    break;
                }
                break;
            case 2000:
                Object obj = rs.getObject(colIndex);
                if (obj != null) {
                    value = String.valueOf(obj);
                    break;
                }
                break;
            case 2005:
                Clob c = rs.getClob(colIndex);
                if (c != null) {
                    value = read(c);
                    break;
                }
                break;
            default:
                value = "";
                break;
        }
        if (value == null) {
            return "";
        }
        return value;
    }

    private static String read(Clob c) throws SQLException, IOException {
        StringBuffer sb = new StringBuffer((int) c.length());
        Reader r = c.getCharacterStream();
        char[] cbuf = new char[2048];
        while (true) {
            int n = r.read(cbuf, 0, cbuf.length);
            if (n == -1) {
                return sb.toString();
            }
            if (n > 0) {
                sb.append(cbuf, 0, n);
            }
        }
    }

    public void writeAll(List<String[]> allLines) {
        for (String[] nextLine : allLines) {
            writeNext(nextLine);
        }
    }

    protected void writeColumnNames(ResultSetMetaData metadata) throws SQLException {
        int columnCount = metadata.getColumnCount();
        String[] nextLine = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            nextLine[i] = metadata.getColumnName(i + 1);
        }
        writeNext(nextLine);
    }

    public void writeAll(ResultSet rs, boolean includeColumnNames) throws SQLException, IOException {
        ResultSetMetaData metadata = rs.getMetaData();
        if (includeColumnNames) {
            writeColumnNames(metadata);
        }
        int columnCount = metadata.getColumnCount();
        while (rs.next()) {
            String[] nextLine = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                nextLine[i] = getColumnValue(rs, metadata.getColumnType(i + 1), i + 1);
            }
            writeNext(nextLine);
        }
    }

    public void writeNext(String[] nextLine) {
        if (nextLine != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < nextLine.length; i++) {
                if (i != 0) {
                    sb.append(this.separator);
                }
                String nextElement = nextLine[i];
                if (nextElement != null) {
                    if (this.quotechar != '\u0000') {
                        sb.append(this.quotechar);
                    }
                    for (int j = 0; j < nextElement.length(); j++) {
                        char nextChar = nextElement.charAt(j);
                        if (this.escapechar != '\u0000' && nextChar == this.quotechar) {
                            sb.append(this.escapechar).append(nextChar);
                        } else if (this.escapechar == '\u0000' || nextChar != this.escapechar) {
                            sb.append(nextChar);
                        } else {
                            sb.append(this.escapechar).append(nextChar);
                        }
                    }
                    if (this.quotechar != '\u0000') {
                        sb.append(this.quotechar);
                    }
                }
            }
            sb.append(this.lineEnd);
            this.pw.write(sb.toString());
        }
    }

    public void write(String string, boolean useQuotes) {
        if (this.currentColumn > 0) {
            this.stringBuffer.append(this.separator);
        }
        this.currentColumn++;
        if (string != null) {
            boolean usingQuotes = useQuotes;
            if ((this.quotechar != '\u0000' && string.indexOf(this.quotechar) >= 0)
                    || string.indexOf(this.separator) >= 0
                    || ((this.escapechar != '\u0000' && string.indexOf(this.escapechar) >= 0)
                    || string.contains(this.lineEnd))) {
                usingQuotes = true;
            }
            if (this.quotechar != '\u0000' && usingQuotes) {
                this.stringBuffer.append(this.quotechar);
            }
            for (int j = 0; j < string.length(); j++) {
                char nextChar = string.charAt(j);
                if (this.escapechar != '\u0000' && nextChar == this.quotechar) {
                    this.stringBuffer.append(this.escapechar).append(nextChar);
                } else if (this.escapechar == '\u0000' || nextChar != this.escapechar) {
                    this.stringBuffer.append(nextChar);
                } else {
                    this.stringBuffer.append(this.escapechar).append(nextChar);
                }
            }
            if (this.quotechar != '\u0000' && usingQuotes) {
                this.stringBuffer.append(this.quotechar);
            }
        }
    }

    /**
     * Write a string. Quote chars are used.
     *
     * @param string
     */
    public void write(String string) {
        write(string, true);
    }

    /**
     * Write a value. Quote chars are only used if necessary.
     *
     * @param string
     */
    public void writeValue(String string) {
        write(string, false);
    }

    /**
     * Write an integer value. Quote chars are only used if necessary.
     *
     * @param i
     */
    public void write(int i) {
        write(String.valueOf(i), false);
    }

    /**
     * Write a long value. Quote chars are only used if necessary.
     *
     * @param i
     */
    public void write(long i) {
        write(String.valueOf(i), false);
    }


    public void writePlain(String s) {
        this.stringBuffer.append(s);
    }

    /**
     * End a line of items that have been added through write().
     */
    public void writeNewline() {
        this.stringBuffer.append(this.lineEnd);
        this.pw.write(this.stringBuffer.toString());
        this.stringBuffer.delete(0, this.stringBuffer.length());
        this.currentColumn = 0;
    }

    /**
     * Flush underlying stream to writer.
     *
     * @throws IOException
     *             if bad things happen
     */
    public void flush() throws IOException {
        this.pw.flush();
    }

    /**
     * Close the underlying stream writer flushing any buffered content.
     *
     * @throws IOException
     *             if bad things happen
     *
     */
    public void close() throws IOException {
        this.pw.flush();
        this.pw.close();
        this.rawWriter.close();
    }
}
