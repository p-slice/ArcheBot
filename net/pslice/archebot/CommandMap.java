package net.pslice.archebot;

import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class CommandMap {

    private final TreeMap<String, Command> commands = new TreeMap<>();

    public Command getCommand(String id) {
        if (!isRegistered(id))
            throw new RuntimeException("[CommandMap:getCommand] Attempted to get unknown command: " + id);
        return commands.get(id.toLowerCase());
    }

    public TreeSet<Command> getCommands() {
        return new TreeSet<>(commands.values());
    }

    public TreeSet<String> getRegisteredIds() {
        return new TreeSet<>(commands.keySet());
    }

    public boolean isRegistered(Command command) {
        return commands.containsValue(command);
    }

    public boolean isRegistered(String id) {
        return commands.containsKey(id.toLowerCase());
    }

    public void register(String id, Command command) {
        commands.put(id.toLowerCase(), command);
    }

    public void register(Command command, Command... commands) {
        register(command.name, command);
        for (String id : command.ids)
            register(id, command);
        for (Command cmd : commands) {
            register(cmd.name, cmd);
            for (String id : cmd.ids)
                register(id, cmd);
        }
    }

    public int size() {
        return commands.size();
    }

    public int totalCommands() {
        return new HashSet<>(commands.values()).size();
    }

    public void unregister(Command command) {
        unregister(command.name);
        for (String ID : command.ids)
            if (getCommand(ID) == command)
                unregister(ID);
    }

    public void unregister(String id) {
        if (commands.containsKey(id.toLowerCase()))
            commands.remove(id.toLowerCase());
    }
}
