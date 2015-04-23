package net.pslice.archebot;

public enum Property {

    nick("nick", false, "ArcheBot"),
    login("login", false, "ArcheBot"),
    realname("realname", false, "ArcheBot (Version {VERSION}) by p_slice"),
    server("server", false, "irc.esper.net"),
    serverPass("serverPass", true,  ""),
    port("port", false, 6667),
    messageDelay("messageDelay", false, 1000),
    nickservID("nickservID", true,  ""),
    nickservPass("nickservPass", true,  ""),
    prefix("prefix", false, "+"),
    allowSeparatePrefix("allowSeparatePrefix", false, false),
    enableCommands("enableCommands", false, true),
    enableCPURestraint("enableCPURestraint", false, true),
    enableColorShortcut("enableColorShortcut", false, true),
    enableIgnore("enableIgnore", false, true),
    channels("channels", true,  "#PotatoBot"),
    printErrorTrace("printErrorTrace", false, true),
    rename("rename", false, false),
    updateNick("updateNick", false, true),
    checkNick("checkNick", false, true),
    reconnect("reconnect", false, false),
    reconnectDelay("reconnectDelay", false, 60000),
    timeoutDelay("timeoutDelay", false, 240000),
    verbose("verbose", false, true),
    logPings("verbose/logPings", false, true),
    logMessages("verbose/logMessages", false, true),
    logNotices("verbose/logNotices", false, true),
    logNicks("verbose/logNicks", false, true),
    logTopics("verbose/logTopics", false, true),
    logJoins("verbose/logJoins", false, true),
    logParts("verbose/logParts", false, true),
    logQuits("verbose/logQuits", false, true),
    logKicks("verbose/logKicks", false, true),
    logModes("verbose/logModes", false, true),
    logInvites("verbose/logInvites", false, true),
    logGeneric("verbose/logGeneric", false, true),
    logOutput("verbose/logOutput", false, true),
    visible("visible", false, false);

    private final String name;
    private final boolean allowEmpty;
    private final Object defaultValue;

    Property(String name, boolean allowEmpty, Object defaultValue) {
        this.name = name;
        this.allowEmpty = allowEmpty;
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean allowsEmpty() {
        return allowEmpty;
    }

    @Override
    public String toString() {
        return name;
    }

    public static boolean isProperty(String name) {
        name = name.replace("verbose/", "");
        for (Property property : Property.values())
            if (property.name.replace("verbose/", "").equalsIgnoreCase(name))
                return true;
        return false;
    }

    public static Property getProperty(String name) {
        name = name.replace("verbose/", "");
        for (Property property : Property.values())
            if (property.name.replace("verbose/", "").equalsIgnoreCase(name))
                return property;
        return null;
    }
}
