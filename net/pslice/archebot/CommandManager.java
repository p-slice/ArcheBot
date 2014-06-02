package net.pslice.archebot;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public final class CommandManager {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // All registered commands
    private final HashMap<String, Command> commands = new HashMap<>();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    CommandManager() {}

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public void registerCommand(Command command)
    {
        commands.put(command.getName(), command);
        for (String ID : command.getIDs())
            commands.put(ID, command);
    }

    public void registerCommands(Command... commands)
    {
        for (Command command : commands)
            this.registerCommand(command);
    }

    public Command getCommand(String ID)
    {
        return commands.containsKey(ID) ? commands.get(ID) : null;
    }

    public boolean isRegistered(Command command)
    {
        return commands.containsValue(command);
    }

    public boolean isRegistered(String ID)
    {
        return commands.containsKey(ID);
    }

    public Set<String> getRegisteredIDs()
    {
        return new TreeSet<>(commands.keySet());
    }

    public Set<Command> getCommands()
    {
        return new TreeSet<>(commands.values());
    }
}
