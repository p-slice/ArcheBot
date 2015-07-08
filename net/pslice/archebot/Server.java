package net.pslice.archebot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class Server implements Comparable<Server> {

    public final String name;
    protected final ArrayList<String> motd = new ArrayList<>();
    protected final HashMap<String, String> info = new HashMap<>();
    protected final HashMap<Character, ModeType> modes = new HashMap<>();
    protected final TreeSet<Character> userModes = new TreeSet<>();
    protected final HashMap<Character, Character> prefixes = new HashMap<>();
    protected String description = "";

    protected Server(String name) {
        this.name = name;
    }

    public String details() {
        return String.format("%s {DESCRIPTION:%s} {MOTD:%d lines}", name, description, motd.size());
    }

    public String getDescription() {
        return description;
    }

    public String getInfo(String type) {
        return info.containsKey(type) ? info.get(type) : null;
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

    public HashSet<Character> getPrefixes() {
        return new HashSet<>(prefixes.keySet());
    }

    public TreeSet<Character> getUserModes() {
        return new TreeSet<>(userModes);
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
