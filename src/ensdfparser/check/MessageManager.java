package ensdfparser.check;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MessageManager {
    private List<CustomMessage> messages = new ArrayList<>();
    private CustomMessage currentMessage;
 
    public MessageManager() {
        // Default constructor
    }
    
    public void addMessage(CustomMessage message) {
        messages.add(message);
        currentMessage = message;
    }
    
    public void addMessage(String body, String label, String type) {
        CustomMessage msg = new CustomMessage(body, label, type);
        this.addMessage(msg);
    }
    
    public CustomMessage popCurrentMessage() {
        CustomMessage temp = currentMessage;
        currentMessage = null;
        return temp;
    }
    
    public List<CustomMessage> getAllMessages() {
        return new ArrayList<>(messages); // Return copy
    }
    
    public void clearAll() {
        messages.clear();
        currentMessage = null;
    }
 
    // Get by exact label
    public List<CustomMessage> getByLabel(String label) {
        return filterMessages(m -> label.equals(m.label()));
    }
    
    // Get by exact type  
    public List<CustomMessage> getByType(String type) {
        return filterMessages(m -> type.equals(m.type()));
    }
    
    // Get by label prefix
    public List<CustomMessage> getByLabelPrefix(String prefix) {
        return filterMessages(m -> m.label().startsWith(prefix));
    }
    
    // Get by body containing text
    public List<CustomMessage> getByBodyContaining(String text) {
        return filterMessages(m -> m.body().contains(text));
    }
    
    // Private helper for filtering
    private List<CustomMessage> filterMessages(Predicate<CustomMessage> predicate) {
        return messages.stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }
    
    // Private helper for grouping
    private Map<String, List<CustomMessage>> groupMessages(
            Function<CustomMessage, String> classifier) {
        return messages.stream()
            .collect(Collectors.groupingBy(classifier));
    }
    
    // === Aggregation Methods ===
    
    public Map<String, List<CustomMessage>> groupByLabel() {
        return groupMessages(CustomMessage::label);
    }
    
    public Map<String, List<CustomMessage>> groupByType() {
        return groupMessages(CustomMessage::type);
    }
    
    public Set<String> getAllLabels() {
        return messages.stream()
            .map(CustomMessage::label)
            .filter(label -> label != null && !label.isEmpty())
            .collect(Collectors.toSet());
    }
    
    public Set<String> getAllTypes() {
        return messages.stream()
            .map(CustomMessage::type)
            .filter(type -> type != null && !type.isEmpty())
            .collect(Collectors.toSet());
    }

    public void printSummary() {
        System.out.println("=== Messages Summary ===");
        System.out.println("Total messages: " + messages.size());
        
        if (messages.isEmpty()) {
            return;
        }
        
        Map<String, Long> labelCounts = messages.stream()
            .collect(Collectors.groupingBy(
                CustomMessage::label,
                Collectors.counting()
            ));
        
        Map<String, Long> typeCounts = messages.stream()
            .collect(Collectors.groupingBy(
                CustomMessage::type,
                Collectors.counting()
            ));
        
        System.out.println("\nBy Label:");
        labelCounts.forEach((label, count) -> 
            System.out.printf("  %s: %d%n", 
                label.isEmpty() ? "(empty)" : label, 
                count));
        
        System.out.println("\nBy Type:");
        typeCounts.forEach((type, count) -> 
            System.out.printf("  %s: %d%n", 
                type.isEmpty() ? "(empty)" : type, 
                count));
    }
    
    public String getSummaryAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Messages Summary ===\n");
        sb.append("Total messages: ").append(messages.size()).append("\n");
        
        // Add label counts
        Map<String, Long> labelCounts = messages.stream()
            .collect(Collectors.groupingBy(CustomMessage::label, Collectors.counting()));
        
        if (!labelCounts.isEmpty()) {
            sb.append("\nBy Label:\n");
            labelCounts.forEach((label, count) -> 
                sb.append(String.format("  %s: %d%n",
                    label.isEmpty() ? "(empty)" : label,
                    count)));
        }
        
        return sb.toString();
    }
}