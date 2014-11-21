package net.pslice.archebot;

import net.pslice.archebot.events.ConnectEvent;
import net.pslice.archebot.events.DisconnectEvent;
import net.pslice.archebot.output.JoinMessage;
import net.pslice.archebot.output.NickservMessage;
import net.pslice.archebot.listeners.ConnectionListener;
import net.pslice.pml.PMLFile;
import net.pslice.pml.PMLTitle;
import net.pslice.utilities.FileManager;
import net.pslice.utilities.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ArcheBot extends User {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // The current version of ArcheBot
    public static final String VERSION = "1.10";

    // Users may set this for usage in their own code
    public static String USER_VERSION = "";

    // The format of the date in the bot's output log
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss:SSS] ");

    // All registered event listeners
    private final Set<Listener> listeners = new HashSet<>();

    // All currently known channels
    private final HashMap<String, Channel> channels = new HashMap<>();

    // All currently known users
    private final HashMap<String, User> users = new HashMap<>();

    // All registered commands
    private final HashMap<String, Command> commands = new HashMap<>();

    // The file manager used for loading/saving files
    private final FileManager fileManager;

    // The time the bot was created, used for calculating runtime
    private final long startTime = System.currentTimeMillis();

    // The PML file containing all bot data
    private PMLFile data;

    // The output stream to log data to
    private PrintStream stream = System.out;

    // The current connection the bot is using
    private Connection connection;

    // The time the last connection was formed at, used for calculating uptime
    private long connectTime = 0;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ArcheBot()
    {
        this("");
    }

    public ArcheBot(String directory)
    {
        super("");
        fileManager = new FileManager(directory);
        this.reload();

        this.log("ArcheBot (Version %s) loaded and ready for use!", VERSION);
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    @SuppressWarnings("unchecked")
    public void connect()
    {
        if (this.isConnected())
            this.logError("Unable to connect (A connection is already active)");
        else
        {
            this.reload();
            try
            {
                if (this.checkEmpty(Property.nick))
                    this.logError("Unable to connect (No nick provided)");
                else if (this.checkEmpty(Property.server))
                    this.logError("Unable to connect (No server provided)");
                else if (!this.getProperty(Property.port).matches("\\d+"))
                    this.logError("Unable to connect (Port can only contain digits)");
                else
                {
                    if (this.checkEmpty(Property.login))
                        this.setProperty(Property.login, this.getProperty(Property.nick));
                    if (this.checkEmpty(Property.realname))
                        this.setProperty(Property.realname, Property.realname.defaultValue);

                    connection = new Connection(this);
                    connectTime = System.currentTimeMillis();

                    if (!this.checkEmpty(Property.nickservPass))
                    {
                        if (this.checkEmpty(Property.nickservID))
                            this.send(new NickservMessage(this.getProperty(Property.nickservPass)));
                        else
                            this.send(new NickservMessage(this.getProperty(Property.nickservID), this.getProperty(Property.nickservPass)));
                    }

                    for (String channel : StringUtils.breakList(this.getProperty(Property.channels)))
                        this.send(new JoinMessage(channel));

                    for (Listener listener : listeners)
                    {
                        if (listener instanceof ConnectionListener)
                            ((ConnectionListener) listener).onConnect(this);
                        if (listener instanceof ConnectEvent.Listener)
                            ((ConnectEvent.Listener) listener).onConnect(new ConnectEvent(this));
                    }
                }
            }

            catch (Connection.ConnectionException e)
            {
                this.logError("Connection refused (%s)", e);
            }

            catch (Exception e)
            {
                boolean reconnect = this.toBoolean(Property.reconnect);

                this.logError("An internal exception has occurred (%s)", e);
                this.log("Disconnected (A fatal exception occurred while connecting | Reconnect: %b)", reconnect);
                if (reconnect)
                {
                    try
                    {
                        Thread.sleep(this.toInteger(Property.reconnectDelay));
                    }

                    catch (InterruptedException ie)
                    {
                        this.logError("An internal exception has occurred (%s)", ie);
                    }
                    this.connect();
                }
            }
        }
    }

    public void disconnect()
    {
        this.disconnect("Shutting down");
    }

    public void disconnect(String message)
    {
        this.disconnect(message, false);
    }

    @SuppressWarnings("unchecked")
    public void disconnect(String message, boolean reconnect)
    {
        if (!this.isConnected())
            this.logError("Unable to disconnect (No connection currently exists)");
        else
        {
            if (connection.isActive())
                connection.send("QUIT :" + message, true);
            connectTime = 0;

            connection.close();
            connection = null;

            data.getSubtitle("permissions").clearSubtitles();
            for (User user : this.getUsers())
                for (User.Permission permission : user.getPermissions())
                    if (!permission.equals(User.Permission.DEFAULT))
                        data.getSubtitle("permissions/" + user.getNick() + "/" + permission);
            data.save();

            users.clear();
            channels.clear();

            this.log("Disconnected (%s | Reconnect: %b)", message, reconnect);

            for (Listener listener : listeners)
            {
                if (listener instanceof ConnectionListener)
                    ((ConnectionListener) listener).onDisconnect(this, message, reconnect);
                if (listener instanceof DisconnectEvent.Listener)
                    ((DisconnectEvent.Listener) listener).onDisconnect(new DisconnectEvent(this, message, reconnect));
            }
            if (reconnect)
                this.connect();
        }
    }

    public Channel getChannel(String name)
    {
        return channels.containsKey(name) ? channels.get(name) : new Channel(name);
    }

    public Set<Channel> getChannels()
    {
        return new HashSet<>(channels.values());
    }

    public Command getCommand(String ID)
    {
        return commands.containsKey(ID.toLowerCase()) ? commands.get(ID.toLowerCase()) : null;
    }

    public Set<Command> getCommands()
    {
        return new TreeSet<>(commands.values());
    }

    public FileManager getFileManager()
    {
        return fileManager;
    }

    public Set<Listener> getListeners()
    {
        return new HashSet<>(listeners);
    }

    public String getProperty(Property property)
    {
        return data.getSubtitle("properties/" + property).getText();
    }

    public Set<String> getRegisteredCommandIDs()
    {
        return new TreeSet<>(commands.keySet());
    }

    public long getRuntime()
    {
        return System.currentTimeMillis() - startTime;
    }

    public long getUptime()
    {
        return this.isConnected() ? System.currentTimeMillis() - connectTime : 0;
    }

    public User getUser(String nick)
    {
        if (!users.containsKey(nick))
            users.put(nick, new User(nick));
        return users.get(nick);
    }

    public Set<User> getUsers()
    {
        return new HashSet<>(users.values());
    }

    public boolean isConnected()
    {
        return connection != null;
    }

    public boolean isInChannel(Channel channel)
    {
        return this.isInChannel(channel.name);
    }

    public boolean isInChannel(String name)
    {
        return channels.containsKey(name);
    }

    public boolean isRegistered(Command command)
    {
        return commands.containsValue(command);
    }

    public boolean isRegistered(String ID)
    {
        return commands.containsKey(ID.toLowerCase());
    }

    public boolean isUser(String nick)
    {
        return users.containsKey(nick);
    }

    public void log(String line, Object... objects)
    {
        this.log(2, String.format(line, objects));
    }

    public void logError(String error, Object... objects)
    {
        this.log(3, String.format(error, objects));
    }

    public void registerCommand(Command command)
    {
        commands.put(command.getName().toLowerCase(), command);
        for (String ID : command.getIDs())
            commands.put(ID.toLowerCase(), command);
    }

    public void registerCommands(Command... commands)
    {
        for (Command command : commands)
            this.registerCommand(command);
    }

    public void registerListener(Listener listener)
    {
        listeners.add(listener);
    }

    public void reload()
    {
        if (new File(fileManager.getDirectory()).mkdir())
            this.log("New directory created.");
        data = new PMLFile("bot", fileManager.getDirectory());
        PMLTitle properties = data.getSubtitle("properties");

        for (Property property : Property.values())
            if (!properties.isSubtitle("" + property))
                this.setProperty(property, property.defaultValue);

        for (User user : this.getUsers())
            user.resetPermissions();
        for (PMLTitle user : data.getSubtitle("permissions").getSubtitles())
            for (PMLTitle permission : user.getSubtitles())
                this.getUser(user.getTitle()).givePermission(User.Permission.generate(permission.getTitle().matches("\\d+") ? permission.getText() : permission.getTitle()));

        data.save();
    }

    public void removeCommand(Command command)
    {
        this.removeCommand(command.getName());
        for (String ID : command.getIDs())
            this.removeCommand(ID);
    }

    public void removeCommand(String ID)
    {
        if (commands.containsKey(ID.toLowerCase()))
            commands.remove(ID.toLowerCase());
    }

    public void removeListener(Listener listener)
    {
        if (listeners.contains(listener))
            listeners.remove(listener);
    }

    public void saveProperties()
    {
        data.save();
    }

    public void send(Output output)
    {
        if (this.isConnected())
            connection.send("" + output, false);
        else
            this.logError("Send method failed (No active connection exists!)");
    }

    public void setOutputStream(PrintStream stream)
    {
        this.stream = stream;
    }

    public Object setProperty(Property property, Object value)
    {
        data.getSubtitle("properties/" + property).setText("" + value);
        return value;
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public String toString()
    {
        return String.format("%s {LOGIN:%s} {REALNAME:%s} {SERVER:%s} {VERSION:%s} {CONNECTED:%b} {UPTIME:%d}",
                nick,
                login,
                realname,
                server,
                VERSION,
                this.isConnected(),
                this.getUptime());
    }

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    void addChannel(Channel channel)
    {
        channels.put(channel.name, channel);
    }

    void addUser(User user)
    {
        users.put(user.getNick(), user);
    }

    synchronized void log(int type, String line)
    {
        if (this.toBoolean(Property.verbose) || data == null)
            stream.println(dateFormat.format(new Date()) + (type == 0 ? "<-" : type == 1 ? "->" : type == 2 ? "<>" : "== Error:") + " " + line);
    }

    void removeChannel(String name)
    {
        if (channels.containsKey(name))
            channels.remove(name);
    }

    void removeUser(String nick)
    {
        if (users.containsKey(nick))
            users.remove(nick);
    }

    boolean toBoolean(Property property)
    {
        return StringUtils.toBoolean(this.getProperty(property));
    }

    int toInteger(Property property)
    {
        return this.getProperty(property).matches("\\d+") ?
                Integer.parseInt(this.getProperty(property)) :
                (int) this.setProperty(property, property.defaultValue);
    }

    /*
     * =======================================
     * Private methods:
     * =======================================
     */

    private boolean checkEmpty(Property property)
    {
        return this.getProperty(property).equals("");
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static enum Property
    {
        /*
         * =======================================
         * Enum values:
         * =======================================
         */

        nick                ("nick",                "ArcheBot"),
        login               ("login",               "ArcheBot"),
        realname            ("realname",            "ArcheBot (Version {VERSION}) by p_slice"),
        server              ("server",              "irc.esper.net"),
        serverPass          ("serverPass",          ""),
        port                ("port",                6667),
        messageDelay        ("messageDelay",        1000),
        nickservID          ("nickservID",          ""),
        nickservPass        ("nickservPass",        ""),
        prefix              ("prefix",              "+"),
        allowSeparatePrefix ("allowSeparatePrefix", false),
        enableCommands      ("enableCommands",      true),
        channels            ("channels",            "#PotatoBot"),
        printErrorTrace     ("printErrorTrace",     true),
        rename              ("rename",              false),
        reconnect           ("reconnect",           false),
        reconnectDelay      ("reconnectDelay",      60000),
        timeoutDelay        ("timeoutDelay",        240000),
        verbose             ("verbose",             true),
        logPings            ("verbose/logPings",    true),
        logMessages         ("verbose/logMessages", true),
        logNotices          ("verbose/logNotices",  true),
        logNicks            ("verbose/logNicks",    true),
        logTopics           ("verbose/logTopics",   true),
        logJoins            ("verbose/logJoins",    true),
        logParts            ("verbose/logParts",    true),
        logQuits            ("verbose/logQuits",    true),
        logKicks            ("verbose/logKicks",    true),
        logModes            ("verbose/logModes",    true),
        logInvites          ("verbose/logInvites",  true),
        logGeneric          ("verbose/logGeneric",  true),
        logOutput           ("verbose/logOutput",   true);

        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        private final String name;
        private final Object defaultValue;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        private Property(String name, Object defaultValue)
        {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        /*
         * =======================================
         * public methods:
         * =======================================
         */

        public Object getDefaultValue()
        {
            return defaultValue;
        }

        /*
         * =======================================
         * Overridden methods:
         * =======================================
         */

        @Override
        public String toString()
        {
            return name;
        }

        /*
         * =======================================
         * Static methods:
         * =======================================
         */

        public static boolean isProperty(String name)
        {
            for (Property property : Property.values())
                if (property.name.toLowerCase().replace("verbose/", "").equals(name.toLowerCase()))
                    return true;
            return false;
        }

        public static Property getProperty(String name)
        {
            for (Property property : Property.values())
                if (property.name.toLowerCase().replace("verbose/", "").equals(name.toLowerCase()))
                    return property;
            return null;
        }
    }

    public static interface Listener<B extends ArcheBot> {}

    public static class Event<B extends ArcheBot>
    {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

        private final B bot;
        private final long timeStamp = System.currentTimeMillis();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

        protected Event(B bot)
        {
            this.bot = bot;
        }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

        public B getBot()
        {
            return bot;
        }

        public long getTimeStamp()
        {
            return timeStamp;
        }
    }

    public static class Output
    {
        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        // The message to be sent to the server
        private final String line;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        protected Output(String line)
        {
            this.line = line;
        }

        /*
         * =======================================
         * Overridden methods:
         * =======================================
         */

        @Override
        public final String toString()
        {
            return line;
        }

        @Override
        public final boolean equals(Object obj)
        {
            return obj instanceof Output && obj.toString().equals(line);
        }
    }
}
