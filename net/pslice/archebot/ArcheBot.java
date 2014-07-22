package net.pslice.archebot;

import net.pslice.archebot.actions.JoinAction;
import net.pslice.archebot.actions.NickservAction;
import net.pslice.archebot.listeners.ConnectionListener;
import net.pslice.utilities.FileManager;
import net.pslice.utilities.PMLFile;
import net.pslice.utilities.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ArcheBot {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // The current version of ArcheBot
    public static final String VERSION = "1.4";

    // Users may set this for usage in their own code
    public static String USER_VERSION = "";

    // The message to display if an internal error occurs
    static final String exception_message = "An internal exception has occurred (%s)";

    // Symbols to indicate the type of message being logged
    private static final String[] msgTypes = {
            "<- ",
            "-> ",
            "<> ",
            "== Error: "
    };

    // The format of the date in the bot's output log
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("'['HH:mm:ss:SSS'] '");

    // The Set of registered event listeners
    private final Set<Listener> listeners = new HashSet<>();

    // All currently known channels
    private final HashMap<String, Channel> channels = new HashMap<>();

    // All currently known users
    private final HashMap<String, User> users = new HashMap<>();

    // All registered commands
    private final HashMap<String, Command> commands = new HashMap<>();

    // The FileManager used for loading/saving files
    private final FileManager fileManager;

    // The PML file containing all bot data
    private PMLFile properties;

    // The output stream to log data to
    private PrintStream stream = System.out;

    // The current connection the bot is using
    private Connection connection;

    // The time the last connection was formed at, used for calculating uptime
    private long connectTime = 0;

    // The name the bot is known by on the server
    private String nick = "";

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
        fileManager = new FileManager(directory);
        this.reload();

        this.log(2, "ArcheBot (Version " + VERSION + ") loaded and ready for use!");
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
            this.log(3, "Unable to connect (A connection is already active)");
        else
        {
            this.reload();
            try
            {
                if (this.getProperty(Property.nick).equals(""))
                    this.log(3, "Unable to connect (No nick given)");
                else if (this.getProperty(Property.server).equals(""))
                    this.log(3, "Unable to connect (No server given)");
                else if (!this.getProperty(Property.port).matches("\\d+"))
                    this.log(3, "Unable to connect (Port can only contain digits)");
                else
                {
                    if (this.getProperty(Property.login).equals(""))
                        this.setProperty(Property.login, this.getProperty(Property.nick));
                    if (this.getProperty(Property.realname).equals(""))
                        this.setProperty(Property.realname, "ArcheBot (Version {VERSION}) by p_slice");
                    if (!this.getProperty(Property.messageDelay).matches("\\d+"))
                        this.setProperty(Property.messageDelay, 1000);

                    connection = new Connection(this);
                    connectTime = System.currentTimeMillis();

                    if (!this.getProperty(Property.nickservPass).equals(""))
                    {
                        if (this.getProperty(Property.nickservID).equals(""))
                            this.send(NickservAction.build(this.getProperty(Property.nickservPass)));
                        else
                            this.send(NickservAction.build(this.getProperty(Property.nickservID), this.getProperty(Property.nickservPass)));
                    }

                    for (String channel : StringUtils.breakList(this.getProperty(Property.channels)))
                        this.send(JoinAction.build(channel));

                    for (Listener listener : listeners)
                        if (listener instanceof ConnectionListener)
                            ((ConnectionListener) listener).onConnect(this);
                }
            }

            catch (Connection.ConnectionException e)
            {
                this.log(3, "Connection refused (" + e.getLine() + ")");
            }

            catch (Exception e)
            {
                boolean reconnect = StringUtils.toBoolean(this.getProperty(Property.reconnect));

                this.log(3, String.format(exception_message, e.toString()));
                this.log(2, "Disconnected (A fatal exception occurred while connecting | Reconnect: " + reconnect + ")");
                if (reconnect)
                {
                    try
                    {
                        Thread.sleep(60000);
                    }

                    catch (InterruptedException ie)
                    {
                        this.log(3, String.format(exception_message, ie.toString()));
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
            this.log(3, "Unable to disconnect (No connection currently exists)");
        else
        {
            if (connection.isActive())
                connection.send("QUIT :" + message, true);
            connectTime = 0;

            properties.removeAll("permissions");
            for (User user : this.getUsers())
            {
                int i = 1;
                for (User.Permission permission : user.getPermissions())
                    if (!permission.equals(User.Permission.DEFAULT))
                        properties.write("permissions." + user.getNick() + "." + i++, "" + permission);
            }
            properties.save();

            users.clear();
            channels.clear();

            connection.close();
            connection = null;
            nick = "";

            this.log(2, "Disconnected (" + message + " | Reconnect: " + reconnect + ")");

            for (Listener listener : listeners)
                if (listener instanceof ConnectionListener)
                    ((ConnectionListener) listener).onDisconnect(this, message, reconnect);
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
        return commands.containsKey(ID) ? commands.get(ID) : null;
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

    public String getNick()
    {
        return nick;
    }

    public String getProperty(Property property)
    {
        return properties.getText(property.toString());
    }

    public Set<String> getRegisteredCommandIDs()
    {
        return new TreeSet<>(commands.keySet());
    }

    public long getUptime()
    {
        return this.isConnected() ? System.currentTimeMillis() - connectTime : 0;
    }

    public User getUser(String nick)
    {
        User user;
        if (users.containsKey(nick))
            user = users.get(nick);
        else
            users.put(nick, user = new User(nick));
        return user;
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
        return commands.containsKey(ID);
    }

    public boolean isUser(String nick)
    {
        return users.containsKey(nick);
    }

    public void log(String line)
    {
        this.log(2, line);
    }

    public void registerCommand(Command command)
    {
        commands.put(command.getName(), command);
        for (String ID : command.getIDs())
            commands.put(ID, command);
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
            stream.println("[New directory created]");
        properties = fileManager.loadPMLFile("bot", 2);

        if (!properties.isTitle("" + Property.nick))
            this.setProperty(Property.nick, "ArcheBot");
        if (!properties.isTitle("" + Property.login))
            this.setProperty(Property.login, "ArcheBot");
        if (!properties.isTitle("" + Property.realname))
            this.setProperty(Property.realname, "ArcheBot (Version {VERSION}) by p_slice");
        if (!properties.isTitle("" + Property.server))
            this.setProperty(Property.server, "irc.esper.net");
        if (!properties.isTitle("" + Property.serverPass))
            this.setProperty(Property.serverPass, "");
        if (!properties.isTitle("" + Property.port))
            this.setProperty(Property.port, 6667);
        if (!properties.isTitle("" + Property.messageDelay))
            this.setProperty(Property.messageDelay, 1000);
        if (!properties.isTitle("" + Property.nickservID))
            this.setProperty(Property.nickservID, "");
        if (!properties.isTitle("" + Property.nickservPass))
            this.setProperty(Property.nickservPass, "");
        if (!properties.isTitle("" + Property.prefix))
            this.setProperty(Property.prefix, "+");
        if (!properties.isTitle("" + Property.channels))
            this.setProperty(Property.channels, "#PotatoBot");
        if (!properties.isTitle("" + Property.verbose))
            this.setProperty(Property.verbose, true);
        if (!properties.isTitle("" + Property.rename))
            this.setProperty(Property.rename, false);
        if (!properties.isTitle("" + Property.reconnect))
            this.setProperty(Property.reconnect, false);

        for (User user : this.getUsers())
            user.resetPermissions();

        if (!properties.isTitle("permissions"))
            properties.write("permissions", "");

        for (String nick : properties.getSubtitles("permissions"))
            for (String p : properties.getSubtitles(nick))
                this.getUser(properties.getOriginalTitle(nick)).givePermission(User.Permission.generate(properties.getText(p)));

        properties.save();
    }

    public void send(IrcAction action)
    {
        if (this.isConnected())
            connection.send(action.toString(), false);
        else
            this.log(3, "Send method failed (No active connection exists!)");
    }

    public void setOutputStream(PrintStream stream)
    {
        this.stream = stream;
    }

    public void setProperty(Property property, String value)
    {
        properties.write("" + property, value);
    }

    public void setProperty(Property property, int value)
    {
        this.setProperty(property, "" + value);
    }

    public void setProperty(Property property, boolean value)
    {
        this.setProperty(property, "" + value);
    }

    public User toUser()
    {
        return this.isConnected() ? this.getUser(nick) : new User(nick);
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
                this.getProperty(Property.login),
                this.getProperty(Property.realname),
                this.getProperty(Property.server),
                VERSION,
                this.isConnected(),
                this.getUptime());
    }

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    synchronized void log(int msgType, String line)
    {
        if (StringUtils.toBoolean(this.getProperty(Property.verbose)))
            stream.println(dateFormat.format(new Date()) + msgTypes[msgType] + line);
    }

    void setNick(String nick)
    {
        this.nick = nick;
    }

    void addUser(User user)
    {
        users.put(user.getNick(), user);
    }

    void removeUser(String nick)
    {
        if (users.containsKey(nick))
            users.remove(nick);
    }

    void addChannel(Channel channel)
    {
        channels.put(channel.name, channel);
    }

    void removeChannel(String name)
    {
        if (channels.containsKey(name))
            channels.remove(name);
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

        nick("nick"),
        login("login"),
        realname("realname"),
        server("server"),
        serverPass("serverPass"),
        port("port"),
        messageDelay("messageDelay"),
        nickservID("nickservID"),
        nickservPass("nickservPass"),
        prefix("prefix"),
        operators("operators"),
        channels("channels"),
        verbose("verbose"),
        rename("rename"),
        reconnect("reconnect");

        /*
         * =======================================
         * Variables and objects:
         * =======================================
         */

        private final String string;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        private Property(String string)
        {
            this.string = string;
        }

        /*
         * =======================================
         * Overridden methods:
         * =======================================
         */

        @Override
        public String toString()
        {
            return "properties." + string;
        }

        /*
         * =======================================
         * Static methods:
         * =======================================
         */

        public static Property getProperty(String string)
        {
            for (Property property : Property.values())
                if (property.string.equals(string) || property.toString().equals(string))
                    return property;
            return null;
        }
    }
}
