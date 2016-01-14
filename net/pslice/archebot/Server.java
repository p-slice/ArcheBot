package net.pslice.archebot;

import java.util.*;

public class Server implements Comparable<Server> {

    protected final String name;
    protected final ArrayList<String> motd = new ArrayList<>();
    protected final HashMap<String, String> data = new HashMap<>();
    protected final TreeMap<Character, ModeType> modes = new TreeMap<>();
    protected final TreeSet<Character> userModes = new TreeSet<>();
    protected final HashMap<Character, Character> prefixes = new HashMap<>();
    protected String description = "", version = "";

    protected Server(String name) {
        this.name = name;
    }

    public String getData(String type) {
        return data.containsKey(type) ? data.get(type) : null;
    }

    public HashSet<String> getDataTypes() {
        return new HashSet<>(data.keySet());
    }

    public String getDescription() {
        return description;
    }

    public char getMode(char prefix) {
        if (!supportsPrefix(prefix))
            throw new RuntimeException(prefix + " is not a supported prefix!");
        return prefixes.get(prefix);
    }

    public TreeSet<Character> getModes() {
        return new TreeSet<>(modes.keySet());
    }

    public ModeType getModeType(char mode) {
        if (!supportsMode(mode))
            throw new RuntimeException(mode + " is not a supported mode!");
        return modes.get(mode);
    }

    public ArrayList<String> getMotd() {
        return new ArrayList<>(motd);
    }

    public String getName() {
        return name;
    }

    public HashSet<Character> getPrefixes() {
        return new HashSet<>(prefixes.keySet());
    }

    public TreeSet<Character> getUserModes() {
        return new TreeSet<>(userModes);
    }

    public String getVersion() {
        return version;
    }

    public boolean supportsPrefix(char prefix) {
        return prefixes.containsKey(prefix);
    }

    public boolean supportsMode(char mode) {
        return modes.containsKey(mode);
    }

    public boolean supportsUserMode(char mode) {
        return userModes.contains(mode);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Server server) {
        return name.compareToIgnoreCase(server.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
