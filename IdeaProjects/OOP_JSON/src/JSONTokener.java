//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JSONTokener {
    private long character;
    private boolean eof;
    private long index;
    private long line;
    private char previous;
    private final Reader reader;
    private boolean usePrevious;
    private long characterPreviousLine;

    public JSONTokener(Reader reader) {
        this.reader = (Reader)(reader.markSupported() ? reader : new BufferedReader(reader));
        this.eof = false;
        this.usePrevious = false;
        this.previous = 0;
        this.index = 0L;
        this.character = 1L;
        this.characterPreviousLine = 0L;
        this.line = 1L;
    }

    public JSONTokener(String s) {
        this((new StringReader(s)));
    }

    public void back() throws JSONException {
        if (!this.usePrevious && this.index > 0L) {
            this.decrementIndexes();
            this.usePrevious = true;
            this.eof = false;
        } else {
            throw new JSONException("Stepping back two steps is not supported");
        }
    }

    private void decrementIndexes() {
        --this.index;
        if (this.previous != '\r' && this.previous != '\n') {
            if (this.character > 0L) {
                --this.character;
            }
        } else {
            --this.line;
            this.character = this.characterPreviousLine;
        }

    }

    public boolean end() {
        return this.eof && !this.usePrevious;
    }

    public char next() throws JSONException {
        int c;
        if (this.usePrevious) {
            this.usePrevious = false;
            c = this.previous;
        } else {
            try {
                c = this.reader.read();
            } catch (IOException var3) {
                throw new JSONException(var3);
            }
        }

        if (c <= 0) {
            this.eof = true;
            return '\u0000';
        } else {
            this.incrementIndexes(c);
            this.previous = (char)c;
            return this.previous;
        }
    }

    private void incrementIndexes(int c) {
        if (c > 0) {
            ++this.index;
            if (c == 13) {
                ++this.line;
                this.characterPreviousLine = this.character;
                this.character = 0L;
            } else if (c == 10) {
                if (this.previous != '\r') {
                    ++this.line;
                    this.characterPreviousLine = this.character;
                }

                this.character = 0L;
            } else {
                ++this.character;
            }
        }

    }

    public String next(int n) throws JSONException {
        if (n == 0) {
            return "";
        } else {
            char[] chars = new char[n];

            for(int pos = 0; pos < n; ++pos) {
                chars[pos] = this.next();
                if (this.end()) {
                    throw this.syntaxError("Substring bounds error");
                }
            }

            return new String(chars);
        }
    }

    public char nextClean() throws JSONException {
        char c;
        do {
            c = this.next();
        } while(c != 0 && c <= ' ');

        return c;
    }

    public String nextString(char quote) throws JSONException {
        StringBuilder sb = new StringBuilder();

        while(true) {
            char c = this.next();
            switch (c) {
                case '\u0000':
                case '\n':
                case '\r':
                    throw this.syntaxError("Unterminated string");
                case '\\':
                    c = this.next();
                    switch (c) {
                        case '"':
                        case '\'':
                        case '/':
                        case '\\':
                            sb.append(c);
                            continue;
                        case 'b':
                            sb.append('\b');
                            continue;
                        case 'f':
                            sb.append('\f');
                            continue;
                        case 'n':
                            sb.append('\n');
                            continue;
                        case 'r':
                            sb.append('\r');
                            continue;
                        case 't':
                            sb.append('\t');
                            continue;
                        case 'u':
                            try {
                                sb.append((char)Integer.parseInt(this.next(4), 16));
                                continue;
                            } catch (NumberFormatException var5) {
                                throw this.syntaxError("Illegal escape.", var5);
                            }
                        default:
                            throw this.syntaxError("Illegal escape.");
                    }
                default:
                    if (c == quote) {
                        return sb.toString();
                    }

                    sb.append(c);
            }
        }
    }

    public Object nextValue() throws JSONException {
        char c = this.nextClean();
        switch (c) {
            case '[':
                this.back();

                try {
                    return new JSONArray(this);
                } catch (StackOverflowError var4) {
                    throw new JSONException("JSON Array or Object depth too large to process.", var4);
                }
            case '{':
                this.back();

                try {
                    return new JSONObject(this);
                } catch (StackOverflowError var3) {
                    throw new JSONException("JSON Array or Object depth too large to process.", var3);
                }
            default:
                return this.nextSimpleValue(c);
        }
    }

    Object nextSimpleValue(char c) {
        switch (c) {
            case '"':
            case '\'':
                return this.nextString(c);
            default:
                StringBuilder sb;
                for(sb = new StringBuilder(); c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0; c = this.next()) {
                    sb.append(c);
                }

                if (!this.eof) {
                    this.back();
                }

                String string = sb.toString().trim();
                if ("".equals(string)) {
                    throw this.syntaxError("Missing value");
                } else {
                    return string;
                }
        }
    }

    public JSONException syntaxError(String message) {
        return new JSONException(message + this.toString());
    }

    public JSONException syntaxError(String message, Throwable causedBy) {
        return new JSONException(message + this.toString(), causedBy);
    }

    public String toString() {
        return " at " + this.index + " [character " + this.character + " line " + this.line + "]";
    }
}
