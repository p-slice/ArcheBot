package net.pslice.archebot;

import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Comparable<Server> {

    public final String name;
    protected final ArrayList<String> motd = new ArrayList<>();
    protected final HashMap<String, String> info = new HashMap<>();
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

    public ArrayList<String> getMotd() {
        return new ArrayList<>(motd);
    }

    public String getName() {
        return name;
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
