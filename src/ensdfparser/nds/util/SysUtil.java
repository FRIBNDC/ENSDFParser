package ensdfparser.nds.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SysUtil {
    
    // Prevent instantiation of utility class
    private SysUtil() {}
    
    // ========== OS Detection ==========
    
    /**
     * Check if the current operating system is Windows.
     * 
     * @return true if running on Windows, false otherwise
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    /**
     * Check if the current operating system is macOS.
     * 
     * @return true if running on macOS, false otherwise
     */
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
    
    /**
     * Check if the current operating system is Linux.
     * 
     * @return true if running on Linux, false otherwise
     */
    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("aix");
    }
    
    /**
     * Get the current operating system name.
     * 
     * @return "Windows", "macOS", "Linux", or "Unknown"
     */
    public static String getOS() {
        if (isWindows()) return "Windows";
        if (isMac()) return "macOS";
        if (isLinux()) return "Linux";
        return "Unknown";
    }
    
    /**
     * Get the current user's home directory.
     * 
     * @return The user's home directory path
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }
    
    /**
     * Get the current working directory.
     * 
     * @return The current working directory path
     */
    public static String getWorkingDir() {
        return System.getProperty("user.dir");
    }
    
    /**
     * Get the file separator for the current platform.
     * 
     * @return File separator ("/" on Unix, "\\" on Windows)
     */
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }
    
    /**
     * Get the line separator for the current platform.
     * 
     * @return Line separator ("\n" on Unix, "\r\n" on Windows)
     */
    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }
    
    /**
     * Get the path separator for the current platform.
     * 
     * @return Path separator (":" on Unix, ";" on Windows)
     */
    public static String getPathSeparator() {
        return System.getProperty("path.separator");
    }
    
    // ========== File Permission Methods ==========
    
    /**
     * Generate the appropriate chmod command for the current platform.
     * Returns a command line string ready to be written to a .bat or .sh file.
     * 
     * @param filePath The absolute path to the target file or directory
     * @param permission The permission in numeric (e.g., "777", "755") or symbolic (e.g., "rwx", "rx") format
     * @param isDirectory True if the target is a directory (for recursive flag)
     * @return The command line string for the current platform
     */
    public static String getChmodCmd(String filePath, String permission, boolean isDirectory) {
        if (isWindows()) {
            // Windows needs both: remove the read-only attribute lock + set ACL permissions
            String attribCmd = "attrib -r \"" + filePath + "\"";
            if (isDirectory) {
                attribCmd += " /s /d";
            }
            
            String icaclsCmd = "icacls \"" + filePath + "\" /grant " + mapPermToWindows(permission);
            if (isDirectory) {
                icaclsCmd += " /t /c /q";
            }
            
            return attribCmd + " && " + icaclsCmd;
        } else {
            // Linux/macOS - use chmod directly
            String chmodPerm = permission;
            
            // Convert bare symbolic like "rwx" to "a+rwx"
            if (permission.matches("[rwx]+")) {
                chmodPerm = "a+" + permission;
            }
            
            String cmd = "chmod";
            if (isDirectory) {
                cmd += " -R";
            }
            return cmd + " " + chmodPerm + " \"" + filePath + "\"";
        }
    }
    
    /**
     * Overload for backward compatibility - auto-detects if path is a directory.
     * @deprecated Use getChmodCmd(String, String, boolean) for environment-agnostic behavior.
     */
    @Deprecated
    public static String getChmodCmd(String filePath, String permission) {
        boolean isDirectory = new File(filePath).isDirectory();
        return getChmodCmd(filePath, permission, isDirectory);
    }
    
    /**
     * Generate a complete script file content for the current platform.
     * 
     * @param filePath The absolute path to the target file or directory
     * @param permission The permission in numeric or symbolic format
     * @param isDirectory True if the target is a directory (for recursive flag)
     * @return Complete script content (.bat for Windows, .sh for Linux/macOS)
     */
    public static String getChmodScript(String filePath, String permission, boolean isDirectory) {
        String cmd = getChmodCmd(filePath, permission, isDirectory);
        
        if (isWindows()) {
            // Escape literal percent signs in the file path specifically for the echo command context
            String escapedEchoPath = filePath.replace("%", "%%");
            
            return "@echo off\r\n" +
                   "setlocal enabledelayedexpansion\r\n" +
                   "echo Setting permissions for: " + escapedEchoPath + "\r\n" + 
                   cmd + "\r\n" +
                   "if !errorlevel! equ 0 (\r\n" +
                   "  echo [SUCCESS] Permissions set successfully\r\n" +
                   ") else (\r\n" +
                   "  echo [ERROR] Failed to set permissions\r\n" +
                   "  exit /b !errorlevel!\r\n" +
                   ")\r\n" +
                   "endlocal";
        } else {
            // Evaluates command inline to safely comply with 'set -e' constraints
            return "#!/usr/bin/env bash\n" +
                   "set -e\n" +
                   "echo \"Setting permissions for: " + filePath + "\"\n" + 
                   "if " + cmd + "; then\n" +
                   "  echo \"[SUCCESS] Permissions set successfully\"\n" +
                   "else\n" +
                   "  echo \"[ERROR] Failed to set permissions\"\n" +
                   "  exit 1\n" +
                   "fi";
        }
    }
    
    /**
     * Overload for backward compatibility - auto-detects if path is a directory.
     * @deprecated Use getChmodScript(String, String, boolean) for environment-agnostic behavior.
     */
    @Deprecated
    public static String getChmodScript(String filePath, String permission) {
        boolean isDirectory = new File(filePath).isDirectory();
        return getChmodScript(filePath, permission, isDirectory);
    }
    
    // ========== Environment Utilities ==========
    
    /**
     * Get an environment variable value.
     * 
     * @param key The environment variable name
     * @return The value, or null if not found
     */
    public static String getEnv(String key) {
        return System.getenv(key);
    }
    
    /**
     * Get a system property value.
     * 
     * @param key The system property name
     * @return The value, or null if not found
     */
    public static String getProperty(String key) {
        return System.getProperty(key);
    }
    
    /**
     * Get a system property value with a default fallback.
     * 
     * @param key The system property name
     * @param defaultValue The default value if property is not found
     * @return The property value or default
     */
    public static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
    
    /**
     * Check if a system property is set.
     * 
     * @param key The system property name
     * @return true if the property is set, false otherwise
     */
    public static boolean hasProperty(String key) {
        return System.getProperty(key) != null;
    }
    
    // ========== Process Utilities ==========

    /**
     * Executes a system shell command string natively on the host OS.
     * Automatically adapts shell routing (cmd.exe vs /bin/sh) based on OS state.
     * 
     * @param commandLine The raw single line terminal instruction to pass
     * @return True if the process returns an exit code of 0, false otherwise
     */
    public static boolean runCommand(String commandLine) {
        try {
            List<String> systemShell = new ArrayList<>();
            if (isWindows()) {
                systemShell.add("cmd.exe");
                systemShell.add("/c");
            } else {
                systemShell.add("/bin/sh");
                systemShell.add("-c");
            }
            systemShell.add(commandLine);

            ProcessBuilder pb = new ProcessBuilder(systemShell);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Prevent system stream buffers from stalling execution paths
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) { /* no-op */ }
            }

            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== Private Helper Mapping Logic ==========
    
    private static String mapPermToWindows(String perm) {
        if (perm.matches("\\d+")) {
            switch (perm) {
                case "777": return "Everyone:F";
                case "755": return "Everyone:(RX)";
                case "754": return "Everyone:(RX)";
                case "750": return "Everyone:(RX)";
                case "744": return "Everyone:(R)";
                case "700": return "%%username%%:(R,W,D)";
                case "644": return "Everyone:(R)";
                case "640": return "Everyone:(R)";
                case "600": return "%%username%%:(R,W)";
                case "400": return "%%username%%:(R)";
                default: return "Everyone:(R,W,X)";
            }
        }
        
        if (perm.contains("=") || perm.contains("+") || perm.contains("-")) {
            return mapSymbolicToWindows(perm);
        }
        
        return mapBareToWindows(perm);
    }
    
    private static String mapBareToWindows(String perm) {
        switch (perm) {
            case "rwx": return "Everyone:F";
            case "rw":  return "Everyone:(R,W)";
            case "rx":  return "Everyone:(RX)";
            case "r":   return "Everyone:(R)";
            case "wx":  return "Everyone:(W,X)";
            case "w":   return "Everyone:(W)";
            case "x":   return "Everyone:(X)";
            default:    return "Everyone:(R,W,X)";
        }
    }
    
    private static String getWindowsUserPerm(String perm) {
        switch (perm) {
            case "rwx": return "F";
            case "rw":  return "(R,W)";
            case "rx":  return "(RX)";
            case "r":   return "(R)";
            case "wx":  return "(W,X)";
            case "w":   return "(W)";
            case "x":   return "(X)";
            default:    return "(R,W,X)";
        }
    }
    
    private static String mapSymbolicToWindows(String sym) {
        boolean hasUser = false, hasGroup = false, hasOther = false;
        String userPerm = "", groupPerm = "", otherPerm = "";
        
        String[] parts = sym.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("u=") || part.startsWith("u+") || part.startsWith("u-")) {
                hasUser = true;
                userPerm = extractPerm(part);
            } else if (part.startsWith("g=") || part.startsWith("g+") || part.startsWith("g-")) {
                hasGroup = true;
                groupPerm = extractPerm(part);
            } else if (part.startsWith("o=") || part.startsWith("o+") || part.startsWith("o-")) {
                hasOther = true;
                otherPerm = extractPerm(part);
            } else if (part.startsWith("a=") || part.startsWith("a+") || part.startsWith("a-")) {
                String p = extractPerm(part);
                return mapBareToWindows(p);
            }
        }
        
        if (hasUser && !hasGroup && !hasOther) {
            return "%%username%%:" + getWindowsUserPerm(userPerm);
        }
        
        String combined = userPerm + groupPerm + otherPerm;
        if (combined.contains("rwx")) return "Everyone:F";
        if (combined.contains("rw")) return "Everyone:(R,W)";
        if (combined.contains("rx")) return "Everyone:(RX)";
        if (combined.contains("r")) return "Everyone:(R)";
        if (combined.contains("wx")) return "Everyone:(W,X)";
        if (combined.contains("w")) return "Everyone:(W)";
        if (combined.contains("x")) return "Everyone:(X)";
        
        return "Everyone:(R,W,X)";
    }
    
    private static String extractPerm(String part) {
        if (part.contains("=")) return part.substring(part.indexOf("=") + 1);
        if (part.contains("+")) return part.substring(part.indexOf("+") + 1);
        if (part.contains("-")) return part.substring(part.indexOf("-") + 1);
        return part;
    }
}