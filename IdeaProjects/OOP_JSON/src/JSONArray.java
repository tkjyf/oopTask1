//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONArray implements Iterable<Object> {
    private final ArrayList<Object> myArrayList;

    public JSONArray() {
        this.myArrayList = new ArrayList();
    }

    public JSONArray(JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        } else {
            char nextChar = x.nextClean();
            if (nextChar == 0) {
                throw x.syntaxError("Expected a ',' or ']'");
            } else if (nextChar != ']') {
                x.back();

                while(true) {
                    if (x.nextClean() == ',') {
                        x.back();
                        this.myArrayList.add(null);
                    } else {
                        x.back();
                        this.myArrayList.add(x.nextValue());
                    }

                    switch (x.nextClean()) {
                        case ',':
                            nextChar = x.nextClean();
                            if (nextChar == 0) {
                                throw x.syntaxError("Expected a ',' or ']'");
                            }

                            if (nextChar == ']') {
                                return;
                            }

                            x.back();
                            break;
                        case ']':
                            return;
                        case '\u0000':
                        default:
                            throw x.syntaxError("Expected a ',' or ']'");
                    }
                }
            }
        }
    }


    public JSONArray(Object array) throws JSONException {
        this();
        if (!array.getClass().isArray()) {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        } else {
            this.addAll(array, true);
        }
    }

    public Iterator<Object> iterator() {
        return this.myArrayList.iterator();
    }


    public JSONArray put(Object value) {
        this.myArrayList.add(value);
        return this;
    }

    private void addAll(Collection<?> collection, boolean wrap) {
        this.myArrayList.ensureCapacity(this.myArrayList.size() + collection.size());
        Iterator var3;
        Object o;
        if (wrap) {
            var3 = collection.iterator();

            while(var3.hasNext()) {
                o = var3.next();
                this.put(JSONObject.wrap(o));
            }
        } else {
            var3 = collection.iterator();

            while(var3.hasNext()) {
                o = var3.next();
                this.put(o);
            }
        }

    }

    private void addAll(Iterable<?> iter, boolean wrap) {
        Iterator var3;
        Object o;
        if (wrap) {
            var3 = iter.iterator();

            while(var3.hasNext()) {
                o = var3.next();
                this.put(JSONObject.wrap(o));
            }
        } else {
            var3 = iter.iterator();

            while(var3.hasNext()) {
                o = var3.next();
                this.put(o);
            }
        }

    }

    private void addAll(Object array, boolean wrap) throws JSONException {
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            this.myArrayList.ensureCapacity(this.myArrayList.size() + length);
            int i;
            if (wrap) {
                for(i = 0; i < length; ++i) {
                    this.put(JSONObject.wrap(Array.get(array, i)));
                }
            } else {
                for(i = 0; i < length; ++i) {
                    this.put(Array.get(array, i));
                }
            }
        } else if (array instanceof JSONArray) {
            this.myArrayList.addAll(((JSONArray)array).myArrayList);
        } else if (array instanceof Collection) {
            this.addAll((Collection)array, wrap);
        } else {
            if (!(array instanceof Iterable)) {
                throw new JSONException("JSONArray initial value should be a string or collection or array.");
            }

            this.addAll((Iterable)array, wrap);
        }

    }

}
