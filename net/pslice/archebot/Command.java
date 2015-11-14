package net.pslice.archebot;

import net.pslice.archebot.utilities.StringUtils;

public abstract class Command<B extends ArcheBot> implements Comparable<Command<B>> {

    protected final String name;
    protected final String[] ids;
    protected Permission permission = Permission.DEFAULT;
    protected String parameters = "[No parameters specified]", description = "[No description specified]";
    protected boolean enabled = true, requireId = false;

    public Command(String name, String... ids) {
        this.name = name;
        this.ids = ids;
    }

    public String details() {
        return String.format("%s {PERMISSION:%s} {PARAMETERS:%s} {DESCRIPTION:%s} {ENABLED:%b} {REQUIREID:%s}%s",
                name, permission, parameters, description, enabled, requireId,
                ids.length > 0 ? " {IDS:" + StringUtils.compact(ids, ",") + "}" : "");
    }

    public abstract void execute(B bot, Channel channel, User sender, String[] args);

    public String getDescription() {
        return description;
    }

    public String[] getIds() {
        return ids;
    }

    public String getName() {
        return name;
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

    public boolean requiresIdentification() {
        return requireId;
    }

    public void requiresIdentification(boolean requireId) {
        this.requireId = requireId;
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
