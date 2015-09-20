package net.pslice.archebot;

public enum Property {

    /** Lets commands be run with a space between the prefix and the command */
    allowSeparatePrefix ("commands", true),
    /** Lets permissions be saved automatically on shutdown */
    autoSavePerms       ("commands", true),
    /** Lets commands be turned on or off */
    enableCommands      ("commands", true),
    /** Lets commands by users with permission.ignore be ignored */
    enableIgnore        ("commands", true),
    /** Lets users use the bot's name instead of the prefix */
    enableNickPrefix    ("commands", true),
    /** Lets users separate arguments with quotation marks in addition to spaces */
    enableQuoteSplit    ("commands", true),
    /** The prefix used by the bot to recognize commands */
    prefix              ("commands", "+"),
    /** The error sent to users when the try to run an unknown command */
    unknownCommandMsg   ("commands", "The command ID '$COMMAND' is not registered."),
    /** All channels that are automatically joined when the bot connects to a server */
    channels       ("general", ""),
    /** The bot's server login identification */
    login          ("general", "ArcheBot"),
    /** The bot's default nick */
    nick           ("general", "ArcheBot"),
    /** The password for connecting to a server */
    password       ("general", ""),
    /** The server port */
    port           ("general", 6667),
    /** The bot's realname */
    realname       ("general", "ArcheBot (Version $VERSION) by p_slice"),
    /** Lets the bot reconnect automatically if disconnected because of an error */
    reconnect      ("general", false),
    /** The time in milliseconds between reconnect attempts */
    reconnectDelay ("general", 60000),
    /** The server name */
    server         ("general", "irc.esper.net"),
    /** Lets the bot be set as visible or hidden on the server */
    visible        ("general", false),
    /** Lets &[#] shortcuts be used to format messages */
    enableFormatting("io", true),
    /** Lets the bot start shutting down immediately without waiting for a server confirmation */
    immediateDisconnect ("io", false),
    /** The maximum number of characters in a single line */
    lineLength          ("io", 510),
    /** The time in milliseconds between sending each message */
    messageDelay        ("io", 1000),
    /** The maximum number of messages that can be in the output queue before in clears itself */
    queueSize           ("io", 1000),
    /** The time in milliseconds between thread loops to prevent CPU over-usage (Requires reconnecting to apply changes) */
    sleepTime           ("io", 50),
    /** The time in milliseconds of server silence before timing out (Requires reconnecting to apply changes) */
    timeoutDelay        ("io", 240000),
    /** Lets certain message types be blocked from logging */
    block         ("logging", ""),
    /** Lets logging be turned on and off */
    enableLogging ("logging", true),
    /** Lets internal errors be logged */
    logErrorTrace ("logging", true),
    /** Lets messages sent by the bot be logged */
    logOutput     ("logging", true),
    /** Lets the nick be reset to the default if it is different */
    checkNick    ("nick", false),
    /** The bot's identification for NickServ */
    nickservID   ("nick", ""),
    /** The bot's password for NickServ */
    nickservPass ("nick", ""),
    /** Lets the bot rename itself if its default nick is already used on the server */
    rename       ("nick", false),
    /** Lets the default nick be modified when the server-side nick is */
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
