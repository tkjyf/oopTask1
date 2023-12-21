//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JSONObject {
    private final Map<String, Object> map;

    public JSONObject(String source) throws JSONException {
        this(new JSONTokener(source));
    }

    public JSONObject(JSONTokener x) throws JSONException {
        this.map = new HashMap();
        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        } else {
            while(true) {
                char c = x.nextClean();
                switch (c) {
                    case '\u0000':
                        throw x.syntaxError("A JSONObject text must end with '}'");
                    case '}':
                        return;
                    default:
                        String key = x.nextSimpleValue(c).toString();
                        c = x.nextClean();
                        if (c != ':') {
                            throw x.syntaxError("Expected a ':' after a key");
                        }

                        if (key != null) {
                            Object value = x.nextValue();
                            if (value != null) {
                                this.put(key, value);
                            }
                        }

                        switch (x.nextClean()) {
                            case ',':
                            case ';':
                                if (x.nextClean() == '}') {
                                    return;
                                }

                                if (x.end()) {
                                    throw x.syntaxError("A JSONObject text must end with '}'");
                                }

                                x.back();
                                break;
                            case '}':
                                return;
                            default:
                                throw x.syntaxError("Expected a ',' or '}'");
                        }
                }
            }
        }
    }

    public JSONObject(Map<?, ?> m) {
        if (m == null) {
            this.map = new HashMap();
        } else {
            this.map = new HashMap(m.size());
            Iterator var2 = m.entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<?, ?> e = (Map.Entry)var2.next();
                if (e.getKey() == null) {
                    throw new NullPointerException("Null key.");
                }

                Object value = e.getValue();
                if (value != null) {
                    this.map.put(String.valueOf(e.getKey()), wrap(value));
                }
            }
        }

    }


    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new NullPointerException("Null key.");
        } else {
            if (value != null) {
//                testValidity(value);
                this.map.put(key, value);
            } else {
                this.map.remove(key);
            }

            return this;
        }
    }


    public static String quote(String string) {
        StringWriter sw = new StringWriter();

        try {
            return quote(string, sw).toString();
        } catch (IOException var3) {
            return "";
        }
    }

    public static Writer quote(String string, Writer w) throws IOException {
        if (string != null && !string.isEmpty()) {
            char c = 0;
            int len = string.length();
            w.write(34);

            for(int i = 0; i < len; ++i) {
                char b = c;
                c = string.charAt(i);
                switch (c) {
                    case '\b':
                        w.write("\\b");
                        continue;
                    case '\t':
                        w.write("\\t");
                        continue;
                    case '\n':
                        w.write("\\n");
                        continue;
                    case '\f':
                        w.write("\\f");
                        continue;
                    case '\r':
                        w.write("\\r");
                        continue;
                    case '"':
                    case '\\':
                        w.write(92);
                        w.write(c);
                        continue;
                    case '/':
                        if (b == '<') {
                            w.write(92);
                        }

                        w.write(c);
                        continue;
                }

                if (c >= ' ' && (c < 128 || c >= 160) && (c < 8192 || c >= 8448)) {
                    w.write(c);
                } else {
                    w.write("\\u");
                    String hhhh = Integer.toHexString(c);
                    w.write("0000", 0, 4 - hhhh.length());
                    w.write(hhhh);
                }
            }

            w.write(34);
            return w;
        } else {
            w.write("\"\"");
            return w;
        }
    }


    protected static Object wrap(Object object) {
        try {
            if (object == null) {
                return null;
            } else if (!(object instanceof JSONObject) && !(object instanceof JSONArray)) { // && !(object instanceof Byte) && !(object instanceof Character) && !(object instanceof Short) && !(object instanceof Integer) && !(object instanceof Long) && !(object instanceof Boolean) && !(object instanceof Float) && !(object instanceof Double) && !(object instanceof String) && !(object instanceof BigInteger) && !(object instanceof BigDecimal) && !(object instanceof Enum)
                if (object.getClass().isArray()) {
                    return new JSONArray(object);
                } else { // if (object instanceof Map)
                    return new JSONObject((Map)object);
                }
            } else {
                return object;
            }
        } catch (JSONException var4) {
            throw var4;
        } catch (Exception var5) {
            return null;
        }
    }


    public Set<String> keySet() {
        return this.map.keySet();
    }

    public Object get(String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        } else {
            Object object = this.opt(key);
            if (object == null) {
                throw new JSONException("JSONObject[" + quote(key) + "] not found.");
            } else {
                return object;
            }
        }
    }
    public Object opt(String key) {
        return key == null ? null : this.map.get(key);
    }

}
