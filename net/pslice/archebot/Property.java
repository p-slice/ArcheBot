package net.pslice.archebot;

public enum Property {

    allowSeparatePrefix("allowSeparatePrefix", false, false),
    channels("channels", true,  "#PotatoBot"),
    checkNick("checkNick", false, true),
    enableColorShortcut("enableColorShortcut", false, true),
    enableCommands("enableCommands", false, true),
    enableCPURestraint("enableCPURestraint", false, true),
    enableIgnore("enableIgnore", false, true),
    enableQuoteSplit("enableQuoteSplit", false, false),
    logGeneric("verbose/logGeneric", false, true),
    logInvites("verbose/logInvites", false, true),
    logJoins("verbose/logJoins", false, true),
    logKicks("verbose/logKicks", false, true),
    logMessages("verbose/logMessages", false, true),
    logModes("verbose/logModes", false, true),
    logNicks("verbose/logNicks", false, true),
    logNotices("verbose/logNotices", false, true),
    logOutput("verbose/logOutput", false, true),
    logParts("verbose/logParts", false, true),
    logPings("verbose/logPings", false, true),
    logQuits("verbose/logQuits", false, true),
    logTopics("verbose/logTopics", false, true),
    login("login", false, "ArcheBot"),
    messageDelay("messageDelay", false, 1000),
    nick("nick", false, "ArcheBot"),
    nickservID("nickservID", true,  ""),
    nickservPass("nickservPass", true,  ""),
    port("port", false, 6667),
    prefix("prefix", false, "+"),
    printErrorTrace("printErrorTrace", false, true),
    realname("realname", false, "ArcheBot (Version {VERSION}) by p_slice"),
    reconnect("reconnect", false, false),
    reconnectDelay("reconnectDelay", false, 60000),
    rename("rename", false, false),
    server("server", false, "irc.esper.net"),
    serverPass("serverPass", true,  ""),
    timeoutDelay("timeoutDelay", false, 240000),
    updateNick("updateNick", false, true),
    verbose("verbose", false, true),
    visible("visible", false, false);

    private final String name;
    private final boolean allowEmpty;
    private final Object defaultValue;

    Property(String name, boolean allowEmpty, Object defaultValue) {
        this.name = name;
        this.allowEmpty = allowEmpty;
        this.defaultValue = defaultValue;
    }

    public boolean allowsEmpty() {
        return allowEmpty;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Property get(String name) {
        name = name.replaceAll(".+/", "");
        for (Property property : Property.values())
            if (property.name.replaceAll(".+/", "").equalsIgnoreCase(name))
                return property;
        return null;
    }

    public static boolean isProperty(String name) {
        name = name.replaceAll(".+/", "");
        for (Property property : Property.values())
            if (property.name.replaceAll(".+/", "").equalsIgnoreCase(name))
                return true;
        return false;
    }
}
