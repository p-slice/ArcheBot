package net.pslice.archebot;

import net.pslice.archebot.actions.JoinAction;
import net.pslice.archebot.actions.NickservAction;
import net.pslice.archebot.listeners.ConnectionListener;
import net.pslice.utilities.managers.FileManager;
import net.pslice.utilities.managers.StringManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class ArcheBot {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // The current version of ArcheBot
    public static final String VERSION = "1.0";

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
    static final SimpleDateFormat dateFormat = new SimpleDateFormat("'['hh:mm:ss'] '");

    // The FileManager used by the bot for loading and saving properties
    private final FileManager fileManager;

    // The CommandManager used by the bot to keep track of commands
    private final CommandManager commandManager = new CommandManager();

    // The Set of registered event listeners
    private final Set<Listener> listeners = new HashSet<>();

    // All currently known channels
    private final HashMap<String, Channel> channels = new HashMap<>();

    // All currently known users
    private final HashMap<String, User> users = new HashMap<>();

    // All loaded user permissions
    private final HashMap<String, Set<String>> userPermissions;

    // The properties used by the bot on connection
    private Properties properties;

    // The current connection the bot is using (null if none are active)
    private Connection connection;

    // The name the bot is known by on the server
    private String nick = "";

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ArcheBot()
    {
        this("ArcheBot Files");
    }

    public ArcheBot(String directory)
    {
        this.fileManager = new FileManager(directory);
        this.reload();

        if (!fileManager.fileExists("permissions"))
            fileManager.saveObject("permissions", new HashMap<>());
        userPermissions = fileManager.loadObject("permissions");

        for (String user : userPermissions.keySet())
            if (userPermissions.get(user).contains(User.Permission.OPERATOR.toString()))
                userPermissions.remove(User.Permission.OPERATOR.toString());

        for (String user : StringManager.breakList(this.getProperty(Property.operators)))
        {
            if (!userPermissions.containsKey(user))
                userPermissions.put(user, new HashSet<String>());
            if (!userPermissions.get(user).contains(User.Permission.OPERATOR.toString()))
                userPermissions.get(user).add(User.Permission.OPERATOR.toString());
        }

        this.log(2, "ArcheBot (Version " + VERSION + ") loaded and ready for use!");
        this.log(2, "Files loaded from '" + fileManager.getDirectory() + "' directory.");
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    @SuppressWarnings("unchecked")
    public final void connect()
    {
        if (this.isConnected())
            this.log(3, "Couldn't connect (A connection is already active)");

        else
        {
            reload();
            try
            {
                if (this.getProperty(Property.nick).equals(""))
                    this.log(3, "Couldn't connect (No nick given)");
                else if (this.getProperty(Property.server).equals(""))
                    this.log(3, "Couldn't connect (No server given)");
                else if (!this.getProperty(Property.port).matches("\\d+"))
                    this.log(3, "Couldn't connect (Port can only contain digits)");

                else
                {
                    if (this.getProperty(Property.login).equals(""))
                        this.setProperty(Property.login, this.getProperty(Property.nick));
                    if (this.getProperty(Property.realname).equals(""))
                        this.setProperty(Property.realname, "ArcheBot (Version {VERSION}) by p_slice");

                    connection = new Connection(this);

                    if (!this.getProperty(Property.nickservPass).equals(""))
                    {
                        if (this.getProperty(Property.nickservID).equals(""))
                            this.send(NickservAction.build(this.getProperty(Property.nickservPass)));
                        else
                            this.send(NickservAction.build(this.getProperty(Property.nickservID), this.getProperty(Property.nickservPass)));
                    }

                    for (String channel : StringManager.breakList(this.getProperty(Property.channels)))
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
                boolean reconnect = StringManager.toBoolean(this.getProperty(Property.reconnect));

                this.log(3, String.format(exception_message, e.toString()));
                this.log(2, "Disconnected (A fatal exception occurred while connecting | Reconnect: " + reconnect + ")");
                if (reconnect)
                    this.connect();
            }
        }
    }

    public final void disconnect(String message)
    {
        this.disconnect(message, false);
    }

    @SuppressWarnings("unchecked")
    public final void disconnect(String message, boolean reconnect)
    {
        if (connection == null)
            this.log(2, "Couldn't disconnect (No connection currently exists)");
        else
        {
            if (connection.isActive())
                connection.send("QUIT :" + message, true);

            HashMap<String, Set<String>> userPermissions = new HashMap<>();
            for (User user : this.getUsers())
            {
                userPermissions.put(user.getNick(), new HashSet<String>());
                for (User.Permission permission : user.getPermissions())
                    userPermissions.get(user.getNick()).add(permission.toString());
            }
            fileManager.saveObject("permissions", userPermissions);

            users.clear();
            channels.clear();

            connection.close();
            connection = null;

            this.log(2, "Disconnected (" + message + " | Reconnect: " + reconnect + ")");

            for (Listener listener : listeners)
                if (listener instanceof ConnectionListener)
                    ((ConnectionListener) listener).onDisconnect(this, message, reconnect);
            if (reconnect)
                this.connect();
        }
    }

    public final Channel getChannel(String name)
    {
        return channels.containsKey(name) ? channels.get(name) : null;
    }

    public final Set<Channel> getChannels()
    {
        return new HashSet<>(channels.values());
    }

    public final CommandManager getCommandManager()
    {
        return commandManager;
    }

    public final FileManager getFileManager()
    {
        return fileManager;
    }

    public final Set<Listener> getListeners()
    {
        return new HashSet<>(listeners);
    }

    public final String getNick()
    {
        return nick;
    }

    public final String getProperty(Property property)
    {
        return properties.getProperty(property.toString());
    }

    public final User getUser(String nick)
    {
        User user;
        if (users.containsKey(nick))
            user = users.get(nick);
        else
        {
            if (!userPermissions.containsKey(nick))
                userPermissions.put(nick, new HashSet<String>());
            user = new User(nick);
            users.put(nick, user);
            for (String p : userPermissions.get(nick))
                user.givePermission(User.Permission.generate(p));
            if (!user.hasPermission(User.Permission.DEFAULT))
                user.givePermission(User.Permission.DEFAULT);
        }
        return user;
    }

    public final Set<User> getUsers()
    {
        return new HashSet<>(users.values());
    }

    public final boolean isConnected()
    {
        return connection != null;
    }

    public final boolean isInChannel(String name)
    {
        return channels.containsKey(name);
    }

    public final boolean isUser(String nick)
    {
        return users.containsKey(nick);
    }

    public final void registerListener(Listener listener)
    {
        listeners.add(listener);
    }

    public void reload()
    {
        if (new File(fileManager.getDirectory()).mkdir())
            this.log(2, "New files directory created.");

        if (!fileManager.propertiesExists(Property.file_name))
            fileManager.saveProperties(Property.file_name, Property.getDefaultValues());
        properties = fileManager.loadProperties(Property.file_name);
    }

    public final void send(IrcAction action)
    {
        if (this.isConnected())
            connection.send(action.toString(), false);
        else
            this.log(3, "Couldn't send output (No active connection exists!)");
    }

    public final void setProperty(Property property, String value)
    {
        properties.setProperty(property.toString(), value);
        fileManager.saveProperties(Property.file_name, properties);
    }

    public final void setProperty(Property property, int value)
    {
        this.setProperty(property, "" + value);
    }

    public final void setProperty(Property property, boolean value)
    {
        this.setProperty(property, "" + value);
    }

    public final User toUser()
    {
        return isConnected() ? this.getUser(nick) : null;
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof ArcheBot && obj.toString().equals(this.toString());
    }

    @Override
    public String toString()
    {
        return String.format("%s {LOGIN:%s} {REALNAME:%s} {VERSION:%s} {CONNECTED:%b}",
                nick,
                this.getProperty(Property.login),
                this.getProperty(Property.realname),
                VERSION,
                this.isConnected());
    }

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    synchronized void log(int msgType, String line)
    {
        if (!this.isConnected() || StringManager.toBoolean(this.getProperty(Property.verbose)))
            System.out.println(dateFormat.format(new Date()) + msgTypes[msgType] + line);
    }

    void setNick(String nick)
    {
        this.nick = nick;
    }

    void addUser(User user)
    {
        users.put(user.getNick(), user);
    }

    void removeUser(String name)
    {
        if (users.containsKey(name))
            users.remove(name);
    }

    void addChannel(Channel channel)
    {
        channels.put(channel.getName(), channel);
    }

    void removeChannel(String name)
    {
        channels.remove(name);
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static enum Property {

        /*
         * =======================================
         * Enum values:
         * =======================================
         */

        nick("nick"),
        login("login"),
        realname("real name"),
        server("server"),
        serverPass("server password"),
        port("port"),
        nickservID("nickserv ID"),
        nickservPass("nickserv password"),
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

        public static final String file_name = "bot";

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
            return string;
        }

        /*
         * =======================================
         * Static methods:
         * =======================================
         */

        public static Property getProperty(String string)
        {
            for (Property property : Property.values())
                if (property.string.equals(string))
                    return property;
            return null;
        }

        public static Properties getDefaultValues()
        {
            Properties properties = new Properties()
            {
                @Override
                public synchronized Enumeration<Object> keys()
                {
                    return Collections.enumeration(new TreeSet<>(super.keySet()));
                }
            };

            properties.setProperty(nick.toString(), "ArcheBot");
            properties.setProperty(login.toString(), "ArcheBot");
            properties.setProperty(realname.toString(), "ArcheBot (Version {VERSION}) by p_slice");
            properties.setProperty(server.toString(), "irc.esper.net");
            properties.setProperty(serverPass.toString(), "");
            properties.setProperty(port.toString(), "6667");
            properties.setProperty(nickservID.toString(), "");
            properties.setProperty(nickservPass.toString(), "");
            properties.setProperty(prefix.toString(), "+");
            properties.setProperty(operators.toString(), "p_slice");
            properties.setProperty(channels.toString(), "#PotatoBot");
            properties.setProperty(verbose.toString(), "true");
            properties.setProperty(rename.toString(), "false");
            properties.setProperty(reconnect.toString(), "false");

            return properties;
        }
    }
}
