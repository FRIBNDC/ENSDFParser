package ensdfparser.check;

import java.util.regex.Pattern;

public class JPIPattern {
    private final Pattern pattern;
    
    public JPIPattern(String regex) {
        this.pattern = Pattern.compile(regex);
    }
    
    public JPIMatcher matcher(String input) {
        return new JPIMatcher(pattern, input);
    }
    
    public Pattern getRawPattern() {
        return pattern;
    }
    
    public String patternString() {
        return pattern.pattern();
    }
    
    @Override
    public String toString() {
        return pattern.toString();
    }
}
