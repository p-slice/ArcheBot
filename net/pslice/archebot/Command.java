package net.pslice.archebot;

import net.pslice.utilities.managers.StringManager;

public abstract class Command<B extends ArcheBot> implements Comparable<Command<B>> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // Information about the command
    protected final String name,
                           parameters,
                           description;
    // Alternate IDs the command can be run with
    protected final String[] IDs;

    // The permission required to run the command
    protected final User.Permission permission;

    // Whether or not the command is enabled
    protected boolean enabled = true;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public Command(String name)
    {
        this(name, User.Permission.DEFAULT);
    }

    public Command(String name, User.Permission permission)
    {
        this(name, permission, "[No parameters specified]", "[No description specified]");
    }

    public Command(String name, String parameters, String description, String... IDs)
    {
        this(name, User.Permission.DEFAULT, parameters, description, IDs);
    }

    public Command(String name, User.Permission permission, String parameters, String description, String... IDs)
    {
        this.name = name;
        this.IDs = IDs;
        this.permission = permission;
        this.parameters = parameters;
        this.description = description;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public abstract void execute(B bot, Channel channel, User sender, String[] args);

    public String getName()
    {
        return name;
    }

    public String[] getIDs()
    {
        return IDs;
    }

    public String getParameters()
    {
        return parameters;
    }

    public User.Permission getPermission()
    {
        return permission;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public String toString()
    {
        return String.format("%s {Permission: %s} {Parameters: %s} {Description: %s} {Enabled: %b} {IDs: %s}",
                name,
                permission,
                parameters,
                description,
                enabled,
                StringManager.compressArray(IDs, 0, ", ", false));
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Command<B> command)
    {
        return name.compareTo(command.name);
    }
}
