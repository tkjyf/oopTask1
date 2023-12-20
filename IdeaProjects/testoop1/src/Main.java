
public class Main {
    public static void main(String[] args) {

        String jsonString = "{\"name\":\"Google\",\"employees\":140000," +
                "\"offices\":[\"Mountain View\",\"Los Angeles\",\"New York\"]}";

        try {
            JSONObject json = new JSONObject(jsonString);
            String prettyJsonString = json.toString(4);
            System.out.println(prettyJsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}