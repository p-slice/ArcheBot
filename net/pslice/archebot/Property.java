package net.pslice.archebot;

public enum Property {

    // Command-related properties
    allowSeparatePrefix ("commands", true),
    autoSavePerms       ("commands", true),
    enableCommands      ("commands", true),
    enableIgnore        ("commands", true),
    enableNickPrefix    ("commands", true),
    enableQuoteSplit    ("commands", true),
    prefix              ("commands", "+"),
    unknownCommandMsg   ("commands", "The command ID '$COMMAND' is not registered."),

    // General properties
    channels       ("general", ""),
    login          ("general", "ArcheBot"),
    nick           ("general", "ArcheBot"),
    password       ("general", ""),
    port           ("general", 6667),
    realname       ("general", "ArcheBot (Version $VERSION) by p_slice"),
    reconnect      ("general", false),
    reconnectDelay ("general", 60000),
    server         ("general", "irc.esper.net"),
    visible        ("general", false),

    // IO-related properties
    enableColorShortcut ("io", true),
    lineLength          ("io", 510),
    messageDelay        ("io", 1000),
    queueSize           ("io", 1000),
    sleepTime           ("io", 50), // Requires reconnecting the bot to apply changes
    timeoutDelay        ("io", 240000), // Requires reconnecting the bot to apply changes

    // Logging-related properties
    enableLogging ("logging", true),
    logErrors     ("logging", true),
    logErrorTrace ("logging", true),
    logGeneric    ("logging", true),
    logInvites    ("logging", true),
    logJoins      ("logging", true),
    logKicks      ("logging", true),
    logMessages   ("logging", true),
    logModes      ("logging", true),
    logMOTD       ("logging", true),
    logNicks      ("logging", true),
    logNotices    ("logging", true),
    logOutput     ("logging", true),
    logParts      ("logging", true),
    logPings      ("logging", true),
    logQuits      ("logging", true),
    logTopics     ("logging", true),

    // Nick-related properties
    checkNick    ("nick", false),
    nickservID   ("nick", ""),
    nickservPass ("nick", ""),
    rename       ("nick", false),
    updateNick   ("nick", false);

    public final String category;
    public final Object defaultValue;

    Property(String category, Object defaultValue) {
        this.category = category;
        this.defaultValue = defaultValue;
    }

    public boolean allowsEmpty() {
        return defaultValue.equals("");
    }

    public String fullName() {
        return category + "/" + name();
    }

    public static boolean isValue(String name) {
        for (Property property : Property.values())
            if (property.name().equalsIgnoreCase(name))
                return true;
        return false;
    }
}
