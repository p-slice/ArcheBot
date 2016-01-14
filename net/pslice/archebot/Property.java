package net.pslice.archebot;

public enum Property {

    /** Lets commands be run with a space between the prefix and the command */
    allowSeparatePrefix(true),
    /** Lets permissions be saved automatically on shutdown */
    autoSavePerms(true),
    /** Lets the nick be reset to the default if it is different */
    checkNick(false),
    /** Lets commands be turned on or off */
    enableCommands(true),
    /** Lets &[#] shortcuts be used to format messages */
    enableFormatting(true),
    /** Lets commands by users with permission.ignore be treated as regular messages */
    enableIgnore(true),
    /** Lets logging be turned on and off */
    enableLogging(true),
    /** Lets users use the bot's name instead of the prefix */
    enableNickPrefix(true),
    /** Lets users separate arguments with quotation marks in addition to spaces */
    enableQuoteSplit(true),
    /** Lets the bot start shutting down immediately without waiting for a server confirmation */
    immediateDisconnect(false),
    /** The maximum number of characters in a single line */
    lineLength(510),
    /** The bot's server login identification */
    login("ArcheBot"),
    /** Lets internal errors be logged */
    logErrorTrace(true),
    /** Lets messages sent to the bot be logged */
    logInput(true),
    /** Lets messages sent by the bot be logged */
    logOutput(true),
    /** The time in milliseconds between sending each message (Requires reconnecting to apply changes) */
    messageDelay(1000),
    /** The bot's default nick */
    nick("ArcheBot"),
    /** The bot's identification for NickServ */
    nickservId(""),
    /** The bot's password for NickServ */
    nickservPass(""),
    /** The password for connecting to a server */
    password(""),
    /** The server port */
    port(6667),
    /** The prefix used by the bot to recognize commands */
    prefix("+"),
    /** The maximum number of messages that can be in the output queue at once (Requires reconnecting to apply changes) */
    queueSize(1000),
    /** The bot's realname */
    realname("ArcheBot (Version $VERSION) by p_slice"),
    /** Lets the bot reconnect automatically if disconnected because of an error */
    reconnect(false),
    /** The time in milliseconds between reconnect attempts */
    reconnectDelay(60000),
    /** Removes extra spaces after command arguments */
    removeTrailingSpaces(false),
    /** Lets the bot rename itself if its default nick is already used on the server */
    rename(false),
    /** The server name */
    server("irc.esper.net"),
    /** The time in milliseconds between thread loops to prevent CPU over-usage (Requires reconnecting to apply changes) */
    sleepTime(50),
    /** The time in milliseconds of server silence before timing out (Requires reconnecting to apply changes) */
    timeoutDelay(240000),
    /** The error sent to users when the try to run an unknown command */
    unknownCommandMsg("The command ID '$COMMAND' is not registered."),
    /** Lets the default nick be modified when the server-side nick is */
    updateNick(false),
    /** Lets the bot be set as visible or hidden on the server */
    visible(false);

    final Object defaultValue;

    Property(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public static boolean isValue(String name) {
        for (Property property : Property.values())
            if (property.name().equalsIgnoreCase(name))
                return true;
        return false;
    }
}
