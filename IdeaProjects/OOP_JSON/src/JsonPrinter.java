public class JsonPrinter {

    private void printIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.printf("    ");
        }
    }

    public void printObject(JSONObject obj, int indent) {
        System.out.printf("{\n");

        for (Object keyObject : obj.keySet()) {
            String key = (String) keyObject;
            Object val = obj.get(key);

            printIndent(indent+1);
            System.out.printf("%s: ", key);
            if (val instanceof JSONObject) {
                this.printObject((JSONObject) val, indent+1);
            } else if (val instanceof  JSONArray) {
                printArray((JSONArray) val, indent+1);
            } else {
                System.out.printf("%s\n", val);
            }
        }

        printIndent(indent);
        System.out.printf("}\n");
    }

    public void printArray(JSONArray array, int indent) {
        System.out.printf("[\n");
        for (Object o : array) {
            printIndent(indent+1);
            System.out.printf("%s\n", o);
        }
        printIndent(indent);
        System.out.printf("]\n");
    }
}
