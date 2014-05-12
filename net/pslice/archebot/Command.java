package net.pslice.archebot;

public abstract class Command<B extends ArcheBot> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // Information about the command
    protected final String ID,
                           parameters,
                           description;

    // The permission required to run the command
    protected final User.Permission permission;

    // Whether or not the command is enabled
    protected boolean enabled = true;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public Command(String ID)
    {
        this(ID, User.Permission.DEFAULT);
    }

    public Command(String ID, User.Permission permission)
    {
        this(ID, permission, "[No parameters specified]", "[No description specified]");
    }

    public Command(String ID, String parameters, String description)
    {
        this(ID, User.Permission.DEFAULT, parameters, description);
    }

    public Command(String ID, User.Permission permission, String parameters, String description)
    {
        this.ID = ID.toLowerCase();
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

    public String getID()
    {
        return ID;
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
        return String.format("%s {Permission: %s} {Parameters: %s} {Description: %s} {Enabled: %b}",
                ID,
                permission,
                parameters,
                description,
                enabled);
    }
}
