package net.pslice.archebot;

import net.pslice.utilities.StringUtils;

public abstract class Command<B extends ArcheBot> implements Comparable<Command<B>> {

    public final String name;
    public final String[] IDs;
    protected Permission permission = Permission.DEFAULT;
    protected String parameters = "[No parameters specified]", description = "[No description specified]";
    protected boolean enabled = true;

    public Command(String name, String... IDs) {
        this.name = name;
        this.IDs = IDs;
    }

    public String details() {
        return String.format("%s {PERMISSION:%s} {PARAMETERS:%s} {DESCRIPTION:%s} {ENABLED:%b}%s",
                name, permission, parameters, description, enabled,
                IDs.length > 0 ? " {IDs:" + StringUtils.compact(IDs, 0, ",") + "}" : "");
    }

    public abstract void execute(B bot, Channel channel, User sender, String[] args);

    public String getDescription() {
        return description;
    }

    public String getParameters() {
        return parameters;
    }

    public Permission getPermission() {
        return permission;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Command<B> command) {
        return name.compareToIgnoreCase(command.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
