package ensdfparser.check;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JPIMatcher - Matches trigger (to/from) and JPI (spin-parity) patterns.
 * 
 * CRITICAL: The compiled pattern MUST have branches where each branch
 * contains exactly 2 capturing groups:
 *   - Group N: the trigger ("to" or "from")
 *   - Group N+1: the JPI (spin-parity pattern)
 * 
 * Use (?: ... ) for any additional grouping within branches.
 */

public class JPIMatcher {
    private final Matcher matcher;
    private int activeOffset = -1;

    public JPIMatcher(Pattern pattern, String input) {
        // Quick validation: test pattern on a known string
        String testInput = "to 1/2+";
        Matcher testMatcher = pattern.matcher(testInput);
        
        if (testMatcher.find()) {
            int groupCount = testMatcher.groupCount();
            // Find which branch matched
            for (int i = 1; i <= groupCount; i += 2) {
                if (testMatcher.group(i) != null) {
                    // Check that next group exists
                    if (i + 1 > groupCount) {
                        throw new IllegalArgumentException(
                            "Pattern branch at offset " + i + " missing JPI capturing group"
                        );
                    }
                    break;
                }
            }
        }
        
        this.matcher = pattern.matcher(input);
    }

    public JPIMatcher(JPIPattern jpiPattern, String input) {
        this(jpiPattern.getRawPattern(),input);
    }
    
    public boolean find() {
        if (matcher.find()) {
            // Find which pair of groups actually caught the match
            // Each branch has (trigger) and (jpi) - step by 2
            for (int i = 1; i <= matcher.groupCount(); i += 2) {
                if (matcher.group(i) != null) {
                    activeOffset = i;
                    return true;
                }
            }
        }
        activeOffset = -1;
        return false;
    }

    public String trigger() {
        return activeOffset != -1 ? matcher.group(activeOffset) : "";
    }

    public String jpi() {
        return activeOffset != -1 ? matcher.group(activeOffset + 1) : "";
    }
    
    public int triggerStart() {
        return activeOffset != -1 ? matcher.start(activeOffset) : -1;
    }
    
    public int triggerEnd() {
        return activeOffset != -1 ? matcher.end(activeOffset) : -1;
    }
    
    public int jpiStart() {
        return activeOffset != -1 ? matcher.start(activeOffset + 1) : -1;
    }
    
    public int jpiEnd() {
        return activeOffset != -1 ? matcher.end(activeOffset + 1) : -1;
    }
    
    public Matcher getRawMatcher() {
        return matcher;
    }
}
