
public class Main {
    public static void main(String[] args) {

        String jsonString = "{\"name\":\"Google\",\"employees\":14000," +
                "\"offices\":[\"Mountain View\",\"Los Angeles\",\"New York\"]}";

        JSONObject json = new JSONObject(jsonString);
        JsonPrinter printer = new JsonPrinter();
        printer.printObject(json, 0);
    }
}