package net.pslice.archebot;

public enum Property {

    // Command-related properties
    allowSeparatePrefix("allowSeparatePrefix", "commands", false, false),
    autoSavePerms("autoSavePerms", "commands", false, true),
    enableCommands("enableCommands", "commands", false, true),
    enableIgnore("enableIgnore", "commands", false, true),
    enableQuoteSplit("enableQuoteSplit", "commands", false, false),
    prefix("prefix", "commands", false, "+"),

    // General properties
    channels("channels", "general", true,  "#PotatoBot"),
    login("login", "general", false, "ArcheBot"),
    nick("nick", "general", false, "ArcheBot"),
    password("password", "general", true, ""),
    port("port", "general", false, 6667),
    realname("realname", "general", false, "ArcheBot (Version {VERSION}) by p_slice"),
    reconnect("reconnect", "general", false, false),
    reconnectDelay("reconnectDelay", "general", false, 60000),
    server("server", "general", false, "irc.esper.net"),
    visible("visible", "general", false, false),

    // IO-related properties
    enableColorShortcut("enableColorShortcut", "io", false, true),
    enableCPURestraint("enableCPURestraint", "io", false, true), // Requires reconnecting the bot to apply changes
    messageDelay("messageDelay", "io", false, 1000),
    timeoutDelay("timeoutDelay", "io", false, 240000), // Requires reconnecting the bot to apply changes

    // Logging-related properties
    enableLogging("enableLogging", "logging", false, true),
    logErrorTrace("logErrorTrace", "logging", false, true),
    logGeneric("logGeneric", "logging", false, true),
    logInvites("logInvites", "logging", false, true),
    logJoins("logJoins", "logging", false, true),
    logKicks("logKicks", "logging", false, true),
    logMessages("logMessages", "logging", false, true),
    logModes("logModes", "logging", false, true),
    logMOTD("logMOTD", "logging", false, true),
    logNicks("logNicks", "logging", false, true),
    logNotices("logNotices", "logging", false, true),
    logOutput("logOutput", "logging", false, true),
    logParts("logParts", "logging", false, true),
    logPings("logPings", "logging", false, true),
    logQuits("logQuits", "logging", false, true),
    logTopics("logTopics", "logging", false, true),

    // Nick-related properties
    checkNick("checkNick", "nick", false, true),
    nickservID("nickservID", "nick", true, ""),
    nickservPass("nickservPass", "nick", true, ""),
    rename("rename", "nick", false, false),
    updateNick("updateNick", "nick", false, true);

    private final String name, category;
    private final boolean allowEmpty;
    private final Object defaultValue;

    Property(String name, String category, boolean allowEmpty, Object defaultValue) {
        this.name = name;
        this.category = category;
        this.allowEmpty = allowEmpty;
        this.defaultValue = defaultValue;
    }

    public boolean allowsEmpty() {
        return allowEmpty;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return category + "/" + name;
    }

    public static Property get(String name) {
        for (Property property : Property.values())
            if (property.name.equalsIgnoreCase(name))
                return property;
        return null;
    }

    public static boolean isProperty(String name) {
        for (Property property : Property.values())
            if (property.name.equalsIgnoreCase(name))
                return true;
        return false;
    }
}
