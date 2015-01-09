package net.pslice.archebot;

import net.pslice.archebot.User.Permission;
import net.pslice.utilities.StringUtils;

public abstract class Command<B extends ArcheBot> implements Comparable<Command<B>> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // The name of the command
    protected final String name;
    // The parameters and description of the command
    protected String parameters,
                     description;
    // Alternate IDs the command can be run with
    protected final String[] IDs;

    // The permission required to run the command
    protected final Permission permission;

    // Whether or not the command is enabled
    protected boolean enabled;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public Command(String name)
    {
        this(name, Permission.DEFAULT);
    }

    public Command(String name, boolean enabled)
    {
        this(name, Permission.DEFAULT, enabled);
    }

    public Command(String name, Permission permission)
    {
        this(name, permission, true);
    }

    public Command(String name, Permission permission, boolean enabled)
    {
        this(name, permission, "[No parameters specified]", "[No description specified]", enabled);
    }

    public Command(String name, String parameters, String description, String... IDs)
    {
        this(name, Permission.DEFAULT, parameters, description, IDs);
    }

    public Command(String name, String parameters, String description, boolean enabled, String... IDs)
    {
        this(name, Permission.DEFAULT, parameters, description, enabled, IDs);
    }

    public Command(String name, Permission permission, String parameters, String description, String... IDs)
    {
        this(name, permission, parameters, description, true, IDs);
    }

    public Command(String name, Permission permission, String parameters, String description, boolean enabled, String... IDs)
    {
        this.name = name;
        this.IDs = IDs;
        this.permission = permission;
        this.parameters = parameters;
        this.description = description;
        this.enabled = enabled;
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

    public Permission getPermission()
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

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setParameters(String parameters)
    {
        this.parameters = parameters;
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public String toString()
    {
        return String.format("%s {PERMISSION:%s} {PARAMETERS:%s} {DESCRIPTION:%s} {ENABLED:%b}%s",
                name,
                permission,
                parameters,
                description,
                enabled,
                IDs.length > 0 ? " {IDs:" + StringUtils.compact(IDs, 0, ",") + "}" : "");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Command<B> command)
    {
        return name.toLowerCase().compareTo(command.name.toLowerCase());
    }
}
