package net.pslice.archebot;

import java.util.TreeMap;
import java.util.TreeSet;

public class ServerMap {

    private final TreeMap<String, Server> servers = new TreeMap<>();

    ServerMap() {}

    public Server getServer(String name) {
        return isServer(name) ? servers.get(name.toLowerCase()) : new Server(name);
    }

    public TreeSet<String> getServerNames() {
        return new TreeSet<>(servers.keySet());
    }

    public TreeSet<Server> getServers() {
        return new TreeSet<>(servers.values());
    }

    public boolean isServer(String name) {
        return servers.containsKey(name.toLowerCase());
    }

    void addServer(Server server) {
        servers.put(server.name.toLowerCase(), server);
    }
}
