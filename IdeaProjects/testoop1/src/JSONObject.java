//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

public class JSONObject {
    static final Pattern NUMBER_PATTERN = Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");
    private final Map<String, Object> map;
    public static final Object NULL = new Null();

    public Class<? extends Map> getMapType() {
        return this.map.getClass();
    }

    public JSONObject() {
        this.map = new HashMap();
    }

    public JSONObject(JSONObject jo, String... names) {
        this(names.length);

        for(int i = 0; i < names.length; ++i) {
            try {
                this.putOnce(names[i], jo.opt(names[i]));
            } catch (Exception var5) {
            }
        }

    }

    public JSONObject(JSONTokener x) throws JSONException {
        this();
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
                            if (this.opt(key) != null) {
                                throw x.syntaxError("Duplicate key \"" + key + "\"");
                            }

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
                    testValidity(value);
                    this.map.put(String.valueOf(e.getKey()), wrap(value));
                }
            }
        }

    }

    public JSONObject(Object bean) {
        this();
        this.populateMap(bean);
    }

    private JSONObject(Object bean, Set<Object> objectsRecord) {
        this();
        this.populateMap(bean, objectsRecord);
    }

    public JSONObject(Object object, String... names) {
        this(names.length);
        Class<?> c = object.getClass();

        for(int i = 0; i < names.length; ++i) {
            String name = names[i];

            try {
                this.putOpt(name, c.getField(name).get(object));
            } catch (Exception var7) {
            }
        }

    }

    public JSONObject(String source) throws JSONException {
        this(new JSONTokener(source));
    }

//    public JSONObject(String baseName, Locale locale) throws JSONException {
//        this();
//        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, Thread.currentThread().getContextClassLoader());
//        Enumeration<String> keys = bundle.getKeys();
//
//        while(true) {
//            Object key;
//            do {
//                if (!keys.hasMoreElements()) {
//                    return;
//                }
//
//                key = keys.nextElement();
//            } while(key == null);
//
//            String[] path = ((String)key).split("\\.");
//            int last = path.length - 1;
//            JSONObject target = this;
//
//            for(int i = 0; i < last; ++i) {
//                String segment = path[i];
//                JSONObject nextTarget = target.optJSONObject(segment);
//                if (nextTarget == null) {
//                    nextTarget = new JSONObject();
//                    target.put(segment, (Object)nextTarget);
//                }
//
//                target = nextTarget;
//            }
//
//            target.put(path[last], (Object)bundle.getString((String)key));
//        }
//    }

    protected JSONObject(int initialCapacity) {
        this.map = new HashMap(initialCapacity);
    }

    public JSONObject accumulate(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, value instanceof JSONArray ? (new JSONArray()).put(value) : value);
        } else if (object instanceof JSONArray) {
            ((JSONArray)object).put(value);
        } else {
            this.put(key, (Object)(new JSONArray()).put(object).put(value));
        }

        return this;
    }

    public JSONObject append(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, (Object)(new JSONArray()).put(value));
        } else {
            if (!(object instanceof JSONArray)) {
                throw wrongValueFormatException(key, "JSONArray", (Object)null, (Throwable)null);
            }

            this.put(key, (Object)((JSONArray)object).put(value));
        }

        return this;
    }

    public static String doubleToString(double d) {
        if (!Double.isInfinite(d) && !Double.isNaN(d)) {
            String string = Double.toString(d);
            if (string.indexOf(46) > 0 && string.indexOf(101) < 0 && string.indexOf(69) < 0) {
                while(string.endsWith("0")) {
                    string = string.substring(0, string.length() - 1);
                }

                if (string.endsWith(".")) {
                    string = string.substring(0, string.length() - 1);
                }
            }

            return string;
        } else {
            return "null";
        }
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

    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws JSONException {
        E val = this.optEnum(clazz, key);
        if (val == null) {
            throw wrongValueFormatException(key, "enum of type " + quote(clazz.getSimpleName()), this.opt(key), (Throwable)null);
        } else {
            return val;
        }
    }

    public boolean getBoolean(String key) throws JSONException {
        Object object = this.get(key);
        if (!object.equals(Boolean.FALSE) && (!(object instanceof String) || !((String)object).equalsIgnoreCase("false"))) {
            if (!object.equals(Boolean.TRUE) && (!(object instanceof String) || !((String)object).equalsIgnoreCase("true"))) {
                throw wrongValueFormatException(key, "Boolean", object, (Throwable)null);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public BigInteger getBigInteger(String key) throws JSONException {
        Object object = this.get(key);
        BigInteger ret = objectToBigInteger(object, (BigInteger)null);
        if (ret != null) {
            return ret;
        } else {
            throw wrongValueFormatException(key, "BigInteger", object, (Throwable)null);
        }
    }

    public BigDecimal getBigDecimal(String key) throws JSONException {
        Object object = this.get(key);
        BigDecimal ret = objectToBigDecimal(object, (BigDecimal)null);
        if (ret != null) {
            return ret;
        } else {
            throw wrongValueFormatException(key, "BigDecimal", object, (Throwable)null);
        }
    }

    public double getDouble(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number)object).doubleValue();
        } else {
            try {
                return Double.parseDouble(object.toString());
            } catch (Exception var4) {
                throw wrongValueFormatException(key, "double", object, var4);
            }
        }
    }

    public float getFloat(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number)object).floatValue();
        } else {
            try {
                return Float.parseFloat(object.toString());
            } catch (Exception var4) {
                throw wrongValueFormatException(key, "float", object, var4);
            }
        }
    }

    public Number getNumber(String key) throws JSONException {
        Object object = this.get(key);

        try {
            return object instanceof Number ? (Number)object : stringToNumber(object.toString());
        } catch (Exception var4) {
            throw wrongValueFormatException(key, "number", object, var4);
        }
    }

    public int getInt(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number)object).intValue();
        } else {
            try {
                return Integer.parseInt(object.toString());
            } catch (Exception var4) {
                throw wrongValueFormatException(key, "int", object, var4);
            }
        }
    }

    public JSONArray getJSONArray(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONArray) {
            return (JSONArray)object;
        } else {
            throw wrongValueFormatException(key, "JSONArray", object, (Throwable)null);
        }
    }

    public JSONObject getJSONObject(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONObject) {
            return (JSONObject)object;
        } else {
            throw wrongValueFormatException(key, "JSONObject", object, (Throwable)null);
        }
    }

    public long getLong(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number)object).longValue();
        } else {
            try {
                return Long.parseLong(object.toString());
            } catch (Exception var4) {
                throw wrongValueFormatException(key, "long", object, var4);
            }
        }
    }

    public static String[] getNames(JSONObject jo) {
        return jo.isEmpty() ? null : (String[])jo.keySet().toArray(new String[jo.length()]);
    }

    public static String[] getNames(Object object) {
        if (object == null) {
            return null;
        } else {
            Class<?> klass = object.getClass();
            Field[] fields = klass.getFields();
            int length = fields.length;
            if (length == 0) {
                return null;
            } else {
                String[] names = new String[length];

                for(int i = 0; i < length; ++i) {
                    names[i] = fields[i].getName();
                }

                return names;
            }
        }
    }

    public String getString(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof String) {
            return (String)object;
        } else {
            throw wrongValueFormatException(key, "string", object, (Throwable)null);
        }
    }

    public boolean has(String key) {
        return this.map.containsKey(key);
    }

    public JSONObject increment(String key) throws JSONException {
        Object value = this.opt(key);
        if (value == null) {
            this.put(key, 1);
        } else if (value instanceof Integer) {
            this.put(key, (Integer)value + 1);
        } else if (value instanceof Long) {
            this.put(key, (Long)value + 1L);
        } else if (value instanceof BigInteger) {
            this.put(key, (Object)((BigInteger)value).add(BigInteger.ONE));
        } else if (value instanceof Float) {
            this.put(key, (Float)value + 1.0F);
        } else if (value instanceof Double) {
            this.put(key, (Double)value + 1.0);
        } else {
            if (!(value instanceof BigDecimal)) {
                throw new JSONException("Unable to increment [" + quote(key) + "].");
            }

            this.put(key, (Object)((BigDecimal)value).add(BigDecimal.ONE));
        }

        return this;
    }

    public boolean isNull(String key) {
        return NULL.equals(this.opt(key));
    }

    public Iterator<String> keys() {
        return this.keySet().iterator();
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    protected Set<Map.Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    public int length() {
        return this.map.size();
    }

    public void clear() {
        this.map.clear();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public JSONArray names() {
        return this.map.isEmpty() ? null : new JSONArray(this.map.keySet());
    }

    public static String numberToString(Number number) throws JSONException {
        if (number == null) {
            throw new JSONException("Null pointer");
        } else {
            testValidity(number);
            String string = number.toString();
            if (string.indexOf(46) > 0 && string.indexOf(101) < 0 && string.indexOf(69) < 0) {
                while(string.endsWith("0")) {
                    string = string.substring(0, string.length() - 1);
                }

                if (string.endsWith(".")) {
                    string = string.substring(0, string.length() - 1);
                }
            }

            return string;
        }
    }

    public Object opt(String key) {
        return key == null ? null : this.map.get(key);
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key) {
        return (E) this.optEnum(clazz, key, (Enum)null);
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key, E defaultValue) {
        try {
            Object val = this.opt(key);
            if (NULL.equals(val)) {
                return defaultValue;
            } else if (clazz.isAssignableFrom(val.getClass())) {
                E myE = (E) val;
                return myE;
            } else {
                return Enum.valueOf(clazz, val.toString());
            }
        } catch (IllegalArgumentException var6) {
            return defaultValue;
        } catch (NullPointerException var7) {
            return defaultValue;
        }
    }

    public boolean optBoolean(String key) {
        return this.optBoolean(key, false);
    }

    public boolean optBoolean(String key, boolean defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        } else if (val instanceof Boolean) {
            return (Boolean)val;
        } else {
            try {
                return this.getBoolean(key);
            } catch (Exception var5) {
                return defaultValue;
            }
        }
    }

    public Boolean optBooleanObject(String key) {
        return this.optBooleanObject(key, false);
    }

    public Boolean optBooleanObject(String key, Boolean defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        } else if (val instanceof Boolean) {
            return (Boolean)val;
        } else {
            try {
                return this.getBoolean(key);
            } catch (Exception var5) {
                return defaultValue;
            }
        }
    }

    public BigDecimal optBigDecimal(String key, BigDecimal defaultValue) {
        Object val = this.opt(key);
        return objectToBigDecimal(val, defaultValue);
    }

    static BigDecimal objectToBigDecimal(Object val, BigDecimal defaultValue) {
        return objectToBigDecimal(val, defaultValue, true);
    }

    static BigDecimal objectToBigDecimal(Object val, BigDecimal defaultValue, boolean exact) {
        if (NULL.equals(val)) {
            return defaultValue;
        } else if (val instanceof BigDecimal) {
            return (BigDecimal)val;
        } else if (val instanceof BigInteger) {
            return new BigDecimal((BigInteger)val);
        } else if (!(val instanceof Double) && !(val instanceof Float)) {
            if (!(val instanceof Long) && !(val instanceof Integer) && !(val instanceof Short) && !(val instanceof Byte)) {
                try {
                    return new BigDecimal(val.toString());
                } catch (Exception var4) {
                    return defaultValue;
                }
            } else {
                return new BigDecimal(((Number)val).longValue());
            }
        } else if (!numberIsFinite((Number)val)) {
            return defaultValue;
        } else {
            return exact ? new BigDecimal(((Number)val).doubleValue()) : new BigDecimal(val.toString());
        }
    }

    public BigInteger optBigInteger(String key, BigInteger defaultValue) {
        Object val = this.opt(key);
        return objectToBigInteger(val, defaultValue);
    }

    static BigInteger objectToBigInteger(Object val, BigInteger defaultValue) {
        if (NULL.equals(val)) {
            return defaultValue;
        } else if (val instanceof BigInteger) {
            return (BigInteger)val;
        } else if (val instanceof BigDecimal) {
            return ((BigDecimal)val).toBigInteger();
        } else if (!(val instanceof Double) && !(val instanceof Float)) {
            if (!(val instanceof Long) && !(val instanceof Integer) && !(val instanceof Short) && !(val instanceof Byte)) {
                try {
                    String valStr = val.toString();
                    return isDecimalNotation(valStr) ? (new BigDecimal(valStr)).toBigInteger() : new BigInteger(valStr);
                } catch (Exception var3) {
                    return defaultValue;
                }
            } else {
                return BigInteger.valueOf(((Number)val).longValue());
            }
        } else {
            return !numberIsFinite((Number)val) ? defaultValue : (new BigDecimal(((Number)val).doubleValue())).toBigInteger();
        }
    }

    public double optDouble(String key) {
        return this.optDouble(key, Double.NaN);
    }

    public double optDouble(String key, double defaultValue) {
        Number val = this.optNumber(key);
        return val == null ? defaultValue : val.doubleValue();
    }

    public Double optDoubleObject(String key) {
        return this.optDoubleObject(key, Double.NaN);
    }

    public Double optDoubleObject(String key, Double defaultValue) {
        Number val = this.optNumber(key);
        return val == null ? defaultValue : val.doubleValue();
    }

    public float optFloat(String key) {
        return this.optFloat(key, Float.NaN);
    }

    public float optFloat(String key, float defaultValue) {
        Number val = this.optNumber(key);
        if (val == null) {
            return defaultValue;
        } else {
            float floatValue = val.floatValue();
            return floatValue;
        }
    }

    public Float optFloatObject(String key) {
        return this.optFloatObject(key, Float.NaN);
    }

    public Float optFloatObject(String key, Float defaultValue) {
        Number val = this.optNumber(key);
        if (val == null) {
            return defaultValue;
        } else {
            Float floatValue = val.floatValue();
            return floatValue;
        }
    }

    public int optInt(String key) {
        return this.optInt(key, 0);
    }

    public int optInt(String key, int defaultValue) {
        Number val = this.optNumber(key, (Number)null);
        return val == null ? defaultValue : val.intValue();
    }

    public Integer optIntegerObject(String key) {
        return this.optIntegerObject(key, 0);
    }

    public Integer optIntegerObject(String key, Integer defaultValue) {
        Number val = this.optNumber(key, (Number)null);
        return val == null ? defaultValue : val.intValue();
    }

    public JSONArray optJSONArray(String key) {
        return this.optJSONArray(key, (JSONArray)null);
    }

    public JSONArray optJSONArray(String key, JSONArray defaultValue) {
        Object object = this.opt(key);
        return object instanceof JSONArray ? (JSONArray)object : defaultValue;
    }

    public JSONObject optJSONObject(String key) {
        return this.optJSONObject(key, (JSONObject)null);
    }

    public JSONObject optJSONObject(String key, JSONObject defaultValue) {
        Object object = this.opt(key);
        return object instanceof JSONObject ? (JSONObject)object : defaultValue;
    }

    public long optLong(String key) {
        return this.optLong(key, 0L);
    }

    public long optLong(String key, long defaultValue) {
        Number val = this.optNumber(key, (Number)null);
        return val == null ? defaultValue : val.longValue();
    }

    public Long optLongObject(String key) {
        return this.optLongObject(key, 0L);
    }

    public Long optLongObject(String key, Long defaultValue) {
        Number val = this.optNumber(key, (Number)null);
        return val == null ? defaultValue : val.longValue();
    }

    public Number optNumber(String key) {
        return this.optNumber(key, (Number)null);
    }

    public Number optNumber(String key, Number defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        } else if (val instanceof Number) {
            return (Number)val;
        } else {
            try {
                return stringToNumber(val.toString());
            } catch (Exception var5) {
                return defaultValue;
            }
        }
    }

    public String optString(String key) {
        return this.optString(key, "");
    }

    public String optString(String key, String defaultValue) {
        Object object = this.opt(key);
        return NULL.equals(object) ? defaultValue : object.toString();
    }

    private void populateMap(Object bean) {
        this.populateMap(bean, Collections.newSetFromMap(new IdentityHashMap()));
    }

    private void populateMap(Object bean, Set<Object> objectsRecord) {
        Class<?> klass = bean.getClass();
        boolean includeSuperClass = klass.getClassLoader() != null;
        Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
        Method[] var6 = methods;
        int var7 = methods.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            Method method = var6[var8];
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && method.getParameterTypes().length == 0 && !method.isBridge() && method.getReturnType() != Void.TYPE && isValidMethodName(method.getName())) {
                String key = getKeyNameFromMethod(method);
                if (key != null && !key.isEmpty()) {
                    try {
                        Object result = method.invoke(bean);
                        if (result != null) {
                            if (objectsRecord.contains(result)) {
                                throw recursivelyDefinedObjectException(key);
                            }

                            objectsRecord.add(result);
                            testValidity(result);
                            this.map.put(key, wrap(result, objectsRecord));
                            objectsRecord.remove(result);
                            if (result instanceof Closeable) {
                                try {
                                    ((Closeable)result).close();
                                } catch (IOException var14) {
                                }
                            }
                        }
                    } catch (IllegalAccessException var15) {
                    } catch (IllegalArgumentException var16) {
                    } catch (InvocationTargetException var17) {
                    }
                }
            }
        }

    }

    private static boolean isValidMethodName(String name) {
        return !"getClass".equals(name) && !"getDeclaringClass".equals(name);
    }

    private static String getKeyNameFromMethod(Method method) {
        int ignoreDepth = getAnnotationDepth(method, JSONPropertyIgnore.class);
        if (ignoreDepth > 0) {
            int forcedNameDepth = getAnnotationDepth(method, JSONPropertyName.class);
            if (forcedNameDepth < 0 || ignoreDepth <= forcedNameDepth) {
                return null;
            }
        }

        JSONPropertyName annotation = (JSONPropertyName)getAnnotation(method, JSONPropertyName.class);
        if (annotation != null && annotation.value() != null && !annotation.value().isEmpty()) {
            return annotation.value();
        } else {
            String name = method.getName();
            String key;
            if (name.startsWith("get") && name.length() > 3) {
                key = name.substring(3);
            } else {
                if (!name.startsWith("is") || name.length() <= 2) {
                    return null;
                }

                key = name.substring(2);
            }

            if (key.length() != 0 && !Character.isLowerCase(key.charAt(0))) {
                if (key.length() == 1) {
                    key = key.toLowerCase(Locale.ROOT);
                } else if (!Character.isUpperCase(key.charAt(1))) {
                    key = key.substring(0, 1).toLowerCase(Locale.ROOT) + key.substring(1);
                }

                return key;
            } else {
                return null;
            }
        }
    }

    private static <A extends Annotation> A getAnnotation(Method m, Class<A> annotationClass) {
        if (m != null && annotationClass != null) {
            if (m.isAnnotationPresent(annotationClass)) {
                return m.getAnnotation(annotationClass);
            } else {
                Class<?> c = m.getDeclaringClass();
                if (c.getSuperclass() == null) {
                    return null;
                } else {
                    Class[] var3 = c.getInterfaces();
                    int var4 = var3.length;

                    for(int var5 = 0; var5 < var4; ++var5) {
                        Class<?> i = var3[var5];

                        try {
                            Method im = i.getMethod(m.getName(), m.getParameterTypes());
                            return getAnnotation(im, annotationClass);
                        } catch (SecurityException var10) {
                        } catch (NoSuchMethodException var11) {
                        }
                    }

                    try {
                        return getAnnotation(c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
                    } catch (SecurityException var8) {
                        return null;
                    } catch (NoSuchMethodException var9) {
                        return null;
                    }
                }
            }
        } else {
            return null;
        }
    }

    private static int getAnnotationDepth(Method m, Class<? extends Annotation> annotationClass) {
        if (m != null && annotationClass != null) {
            if (m.isAnnotationPresent(annotationClass)) {
                return 1;
            } else {
                Class<?> c = m.getDeclaringClass();
                if (c.getSuperclass() == null) {
                    return -1;
                } else {
                    Class[] var3 = c.getInterfaces();
                    int var4 = var3.length;

                    for(int var5 = 0; var5 < var4; ++var5) {
                        Class<?> i = var3[var5];

                        try {
                            Method im = i.getMethod(m.getName(), m.getParameterTypes());
                            int d = getAnnotationDepth(im, annotationClass);
                            if (d > 0) {
                                return d + 1;
                            }
                        } catch (SecurityException var11) {
                        } catch (NoSuchMethodException var12) {
                        }
                    }

                    try {
                        int d = getAnnotationDepth(c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
                        return d > 0 ? d + 1 : -1;
                    } catch (SecurityException var9) {
                        return -1;
                    } catch (NoSuchMethodException var10) {
                        return -1;
                    }
                }
            }
        } else {
            return -1;
        }
    }

    public JSONObject put(String key, boolean value) throws JSONException {
        return this.put(key, (Object)(value ? Boolean.TRUE : Boolean.FALSE));
    }

    public JSONObject put(String key, Collection<?> value) throws JSONException {
        return this.put(key, (Object)(new JSONArray(value)));
    }

    public JSONObject put(String key, double value) throws JSONException {
        return this.put(key, (Object)value);
    }

    public JSONObject put(String key, float value) throws JSONException {
        return this.put(key, (Object)value);
    }

    public JSONObject put(String key, int value) throws JSONException {
        return this.put(key, (Object)value);
    }

    public JSONObject put(String key, long value) throws JSONException {
        return this.put(key, (Object)value);
    }

    public JSONObject put(String key, Map<?, ?> value) throws JSONException {
        return this.put(key, (Object)(new JSONObject(value)));
    }

    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new NullPointerException("Null key.");
        } else {
            if (value != null) {
                testValidity(value);
                this.map.put(key, value);
            } else {
                this.remove(key);
            }

            return this;
        }
    }

    public JSONObject putOnce(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            if (this.opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            } else {
                return this.put(key, value);
            }
        } else {
            return this;
        }
    }

    public JSONObject putOpt(String key, Object value) throws JSONException {
        return key != null && value != null ? this.put(key, value) : this;
    }

    public Object query(String jsonPointer) {
        return this.query(new JSONPointer(jsonPointer));
    }

    public Object query(JSONPointer jsonPointer) {
        return jsonPointer.queryFrom(this);
    }

    public Object optQuery(JSONPointer jsonPointer) {
        try {
            return jsonPointer.queryFrom(this);
        } catch (JSONPointerException var3) {
            return null;
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

    public Object remove(String key) {
        return this.map.remove(key);
    }

    public boolean similar(Object other) {
        try {
            if (!(other instanceof JSONObject)) {
                return false;
            } else if (!this.keySet().equals(((JSONObject)other).keySet())) {
                return false;
            } else {
                Iterator var2 = this.entrySet().iterator();

                Object valueThis;
                Object valueOther;
                do {
                    while(true) {
                        do {
                            if (!var2.hasNext()) {
                                return true;
                            }

                            Map.Entry<String, ?> entry = (Map.Entry)var2.next();
                            String name = (String)entry.getKey();
                            valueThis = entry.getValue();
                            valueOther = ((JSONObject)other).get(name);
                        } while(valueThis == valueOther);

                        if (valueThis == null) {
                            return false;
                        }

                        if (valueThis instanceof JSONObject) {
                            break;
                        }

                        if (valueThis instanceof JSONArray) {
                            if (!((JSONArray)valueThis).similar(valueOther)) {
                                return false;
                            }
                        } else if (valueThis instanceof Number && valueOther instanceof Number) {
                            if (!isNumberSimilar((Number)valueThis, (Number)valueOther)) {
                                return false;
                            }
                        } else if (valueThis instanceof JSONString && valueOther instanceof JSONString) {
                            if (!((JSONString)valueThis).toJSONString().equals(((JSONString)valueOther).toJSONString())) {
                                return false;
                            }
                        } else if (!valueThis.equals(valueOther)) {
                            return false;
                        }
                    }
                } while(((JSONObject)valueThis).similar(valueOther));

                return false;
            }
        } catch (Throwable var7) {
            return false;
        }
    }

    static boolean isNumberSimilar(Number l, Number r) {
        if (numberIsFinite(l) && numberIsFinite(r)) {
            if (l.getClass().equals(r.getClass()) && l instanceof Comparable) {
                int compareTo = ((Comparable)l).compareTo(r);
                return compareTo == 0;
            } else {
                BigDecimal lBigDecimal = objectToBigDecimal(l, (BigDecimal)null, false);
                BigDecimal rBigDecimal = objectToBigDecimal(r, (BigDecimal)null, false);
                if (lBigDecimal != null && rBigDecimal != null) {
                    return lBigDecimal.compareTo(rBigDecimal) == 0;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static boolean numberIsFinite(Number n) {
        if (!(n instanceof Double) || !((Double)n).isInfinite() && !((Double)n).isNaN()) {
            return !(n instanceof Float) || !((Float)n).isInfinite() && !((Float)n).isNaN();
        } else {
            return false;
        }
    }

    protected static boolean isDecimalNotation(String val) {
        return val.indexOf(46) > -1 || val.indexOf(101) > -1 || val.indexOf(69) > -1 || "-0".equals(val);
    }

    protected static Number stringToNumber(String input) throws NumberFormatException {
        String val = input;
        if (input.startsWith(".")) {
            val = "0" + input;
        }

        if (val.startsWith("-.")) {
            val = "-0." + val.substring(2);
        }

        char initial = val.charAt(0);
        if ((initial < '0' || initial > '9') && initial != '-') {
            throw new NumberFormatException("val [" + input + "] is not a valid number.");
        } else if (isDecimalNotation(val)) {
            try {
                BigDecimal bd = new BigDecimal(val);
                return (Number)(initial == '-' && BigDecimal.ZERO.compareTo(bd) == 0 ? -0.0 : bd);
            } catch (NumberFormatException var6) {
                try {
                    Double d = Double.valueOf(val);
                    if (!d.isNaN() && !d.isInfinite()) {
                        return d;
                    } else {
                        throw new NumberFormatException("val [" + input + "] is not a valid number.");
                    }
                } catch (NumberFormatException var5) {
                    throw new NumberFormatException("val [" + input + "] is not a valid number.");
                }
            }
        } else {
            val = removeLeadingZerosOfNumber(input);
            initial = val.charAt(0);
            char at1;
            if (initial == '0' && val.length() > 1) {
                at1 = val.charAt(1);
                if (at1 >= '0' && at1 <= '9') {
                    throw new NumberFormatException("val [" + input + "] is not a valid number.");
                }
            } else if (initial == '-' && val.length() > 2) {
                at1 = val.charAt(1);
                char at2 = val.charAt(2);
                if (at1 == '0' && at2 >= '0' && at2 <= '9') {
                    throw new NumberFormatException("val [" + input + "] is not a valid number.");
                }
            }

            BigInteger bi = new BigInteger(val);
            if (bi.bitLength() <= 31) {
                return bi.intValue();
            } else {
                return (Number)(bi.bitLength() <= 63 ? bi.longValue() : bi);
            }
        }
    }

    public static Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        } else if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        } else if ("null".equalsIgnoreCase(string)) {
            return NULL;
        } else {
            if (potentialNumber(string)) {
                try {
                    return stringToNumber(string);
                } catch (Exception var2) {
                }
            }

            return string;
        }
    }

    private static boolean potentialNumber(String value) {
        return value != null && !value.isEmpty() ? potentialPositiveNumberStartingAtIndex(value, value.charAt(0) == '-' ? 1 : 0) : false;
    }

    private static boolean potentialPositiveNumberStartingAtIndex(String value, int index) {
        return index >= value.length() ? false : digitAtIndex(value, value.charAt(index) == '.' ? index + 1 : index);
    }

    private static boolean digitAtIndex(String value, int index) {
        if (index >= value.length()) {
            return false;
        } else {
            return value.charAt(index) >= '0' && value.charAt(index) <= '9';
        }
    }

    public static void testValidity(Object o) throws JSONException {
        if (o instanceof Number && !numberIsFinite((Number)o)) {
            throw new JSONException("JSON does not allow non-finite numbers.");
        }
    }

    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        if (names != null && !names.isEmpty()) {
            JSONArray ja = new JSONArray();

            for(int i = 0; i < names.length(); ++i) {
                ja.put(this.opt(names.getString(i)));
            }

            return ja;
        } else {
            return null;
        }
    }

    public String toString() {
        try {
            return this.toString(0);
        } catch (Exception var2) {
            return null;
        }
    }

    public String toString(int indentFactor) throws JSONException {
        StringWriter w = new StringWriter();
        return this.write(w, indentFactor, 0).toString();
    }

    public static String valueToString(Object value) throws JSONException {
        return JSONWriter.valueToString(value);
    }

    public static Object wrap(Object object) {
        return wrap(object, (Set)null);
    }

    private static Object wrap(Object object, Set<Object> objectsRecord) {
        try {
            if (NULL.equals(object)) {
                return NULL;
            } else if (!(object instanceof JSONObject) && !(object instanceof JSONArray) && !NULL.equals(object) && !(object instanceof JSONString) && !(object instanceof Byte) && !(object instanceof Character) && !(object instanceof Short) && !(object instanceof Integer) && !(object instanceof Long) && !(object instanceof Boolean) && !(object instanceof Float) && !(object instanceof Double) && !(object instanceof String) && !(object instanceof BigInteger) && !(object instanceof BigDecimal) && !(object instanceof Enum)) {
                if (object instanceof Collection) {
                    Collection<?> coll = (Collection)object;
                    return new JSONArray(coll);
                } else if (object.getClass().isArray()) {
                    return new JSONArray(object);
                } else if (object instanceof Map) {
                    Map<?, ?> map = (Map)object;
                    return new JSONObject(map);
                } else {
                    Package objectPackage = object.getClass().getPackage();
                    String objectPackageName = objectPackage != null ? objectPackage.getName() : "";
                    if (!objectPackageName.startsWith("java.") && !objectPackageName.startsWith("javax.") && object.getClass().getClassLoader() != null) {
                        return objectsRecord != null ? new JSONObject(object, objectsRecord) : new JSONObject(object);
                    } else {
                        return object.toString();
                    }
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

    public Writer write(Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }

    static final Writer writeValue(Writer writer, Object value, int indentFactor, int indent) throws JSONException, IOException {
        if (value != null && !value.equals((Object)null)) {
            String numberAsString;
            if (value instanceof JSONString) {
                try {
                    numberAsString = ((JSONString)value).toJSONString();
                } catch (Exception var6) {
                    throw new JSONException(var6);
                }

                writer.write(numberAsString != null ? numberAsString.toString() : quote(value.toString()));
            } else if (value instanceof Number) {
                numberAsString = numberToString((Number)value);
                if (NUMBER_PATTERN.matcher(numberAsString).matches()) {
                    writer.write(numberAsString);
                } else {
                    quote(numberAsString, writer);
                }
            } else if (value instanceof Boolean) {
                writer.write(value.toString());
            } else if (value instanceof Enum) {
                writer.write(quote(((Enum)value).name()));
            } else if (value instanceof JSONObject) {
                ((JSONObject)value).write(writer, indentFactor, indent);
            } else if (value instanceof JSONArray) {
                ((JSONArray)value).write(writer, indentFactor, indent);
            } else if (value instanceof Map) {
                Map<?, ?> map = (Map)value;
                (new JSONObject(map)).write(writer, indentFactor, indent);
            } else if (value instanceof Collection) {
                Collection<?> coll = (Collection)value;
                (new JSONArray(coll)).write(writer, indentFactor, indent);
            } else if (value.getClass().isArray()) {
                (new JSONArray(value)).write(writer, indentFactor, indent);
            } else {
                quote(value.toString(), writer);
            }
        } else {
            writer.write("null");
        }

        return writer;
    }

    static final void indent(Writer writer, int indent) throws IOException {
        for(int i = 0; i < indent; ++i) {
            writer.write(32);
        }

    }

    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        try {
            boolean needsComma = false;
            int length = this.length();
            writer.write(123);
            if (length == 1) {
                Map.Entry<String, ?> entry = (Map.Entry)this.entrySet().iterator().next();
                String key = (String)entry.getKey();
                writer.write(quote(key));
                writer.write(58);
                if (indentFactor > 0) {
                    writer.write(32);
                }

                try {
                    writeValue(writer, entry.getValue(), indentFactor, indent);
                } catch (Exception var12) {
                    throw new JSONException("Unable to write JSONObject value for key: " + key, var12);
                }
            } else if (length != 0) {
                int newIndent = indent + indentFactor;

                for(Iterator var15 = this.entrySet().iterator(); var15.hasNext(); needsComma = true) {
                    Map.Entry<String, ?> entry = (Map.Entry)var15.next();
                    if (needsComma) {
                        writer.write(44);
                    }

                    if (indentFactor > 0) {
                        writer.write(10);
                    }

                    indent(writer, newIndent);
                    String key = (String)entry.getKey();
                    writer.write(quote(key));
                    writer.write(58);
                    if (indentFactor > 0) {
                        writer.write(32);
                    }

                    try {
                        writeValue(writer, entry.getValue(), indentFactor, newIndent);
                    } catch (Exception var11) {
                        throw new JSONException("Unable to write JSONObject value for key: " + key, var11);
                    }
                }

                if (indentFactor > 0) {
                    writer.write(10);
                }

                indent(writer, indent);
            }

            writer.write(125);
            return writer;
        } catch (IOException var13) {
            throw new JSONException(var13);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> results = new HashMap();

        Map.Entry entry;
        Object value;
        for(Iterator var2 = this.entrySet().iterator(); var2.hasNext(); results.put(entry.getKey(), value)) {
            entry = (Map.Entry)var2.next();
            if (entry.getValue() != null && !NULL.equals(entry.getValue())) {
                if (entry.getValue() instanceof JSONObject) {
                    value = ((JSONObject)entry.getValue()).toMap();
                } else if (entry.getValue() instanceof JSONArray) {
                    value = ((JSONArray)entry.getValue()).toList();
                } else {
                    value = entry.getValue();
                }
            } else {
                value = null;
            }
        }

        return results;
    }

    private static JSONException wrongValueFormatException(String key, String valueType, Object value, Throwable cause) {
        if (value == null) {
            return new JSONException("JSONObject[" + quote(key) + "] is not a " + valueType + " (null).", cause);
        } else {
            return !(value instanceof Map) && !(value instanceof Iterable) && !(value instanceof JSONObject) ? new JSONException("JSONObject[" + quote(key) + "] is not a " + valueType + " (" + value.getClass() + " : " + value + ").", cause) : new JSONException("JSONObject[" + quote(key) + "] is not a " + valueType + " (" + value.getClass() + ").", cause);
        }
    }

    private static JSONException recursivelyDefinedObjectException(String key) {
        return new JSONException("JavaBean object contains recursively defined member variable of key " + quote(key));
    }

    private static String removeLeadingZerosOfNumber(String value) {
        if (value.equals("-")) {
            return value;
        } else {
            boolean negativeFirstChar = value.charAt(0) == '-';

            for(int counter = negativeFirstChar ? 1 : 0; counter < value.length(); ++counter) {
                if (value.charAt(counter) != '0') {
                    if (negativeFirstChar) {
                        return "-".concat(value.substring(counter));
                    }

                    return value.substring(counter);
                }
            }

            if (negativeFirstChar) {
                return "-0";
            } else {
                return "0";
            }
        }
    }

    private static final class Null {
        private Null() {
        }

        protected final Object clone() {
            return this;
        }

        public boolean equals(Object object) {
            return object == null || object == this;
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            return "null";
        }
    }
}
