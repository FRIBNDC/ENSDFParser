package ensdfparser.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomMessage {

	/*
	 * example use
	 * CustomMessage msg = new CustomMessage(
                               "File not found: data.txt",
                               "file_reader",     // label
                               "io_error"         // type
                             );
                             
       // Add additional metadata
       msg.addTag("filename", "data.txt");
       msg.addTag("line_number", "42");
       msg.addTag("timestamp", "2024-01-15T10:30:00");
       msg.addTag("user_id", "john_doe");
       msg.addTag("severity", "high");
	 */
	
	private int id=-1;       
	
	private String body="";
	private String label="";//given label for this message for identification, can be empty
	private String type="";//specified type for this message, can be empty
	
    private Map<String, String> additionalTags = new HashMap<>();//for metadata
	
	public CustomMessage(String body,String label,String type) {
		this.body=body;
		this.label=label;
		this.type=type;
	}
	
	public void setID(int id) {this.id=id;}
	public void setBody(String body) {this.body=body;}
	public void setLabel(String label) {this.label=label;}
	public void setType(String type) {this.type=type;}
	
	public int id() { return id; }
	public String body() {return body;}
	public String label() {return label;}
	public String type() {return type;}
	
    public Map<String, String> tags() { return new HashMap<>(additionalTags); }
    
    // Add optional tags
    public void addTag(String key, String value) {
        additionalTags.put(key, value);
    }
    
    public String getTag(String key) {
        return additionalTags.get(key);
    }
    
    public List<String> getBodyLinesAsList() {
        List<String> lines = new ArrayList<>();
        if (body != null && !body.isEmpty()) {
            // Handles Windows (\r\n), Unix (\n), and old Mac (\r)
            lines = Arrays.asList(body.split("[\\r?\\n|\\r]+"));
        }
        return lines;
    }
    
	public String toString() {return body;}
}
