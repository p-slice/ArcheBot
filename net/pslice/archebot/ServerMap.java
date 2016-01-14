package net.pslice.archebot;

import java.util.TreeMap;
import java.util.TreeSet;

public class ServerMap {

    private final TreeMap<String, Server> servers = new TreeMap<>();
    boolean current = true;

    ServerMap() {}

    public Server getServer(String name) {
        return getServer(name, true);
    }

    public Server getServer(String name, boolean createNew) {
        return isServer(name) ? servers.get(name.toLowerCase()) : createNew ? new Server(name) : null;
    }

    public TreeSet<String> getServerNames() {
        return new TreeSet<>(servers.keySet());
    }

    public TreeSet<Server> getServers() {
        return new TreeSet<>(servers.values());
    }

    public boolean isCurrent() {
        return current;
    }

    public boolean isServer(String name) {
        return servers.containsKey(name.toLowerCase());
    }

    public boolean isServer(Server server) {
        return servers.containsValue(server);
    }

    public int size() {
        return servers.size();
    }

    void addServer(Server server) {
        servers.put(server.name.toLowerCase(), server);
    }
}
