package net.pslice.archebot;

import net.pslice.archebot.actions.JoinAction;
import net.pslice.archebot.actions.NickservAction;
import net.pslice.archebot.listeners.ConnectionListener;
import net.pslice.utilities.managers.FileManager;
import net.pslice.utilities.managers.StringManager;
import net.pslice.utilities.pml.PMLFile;

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
    public static final String VERSION = "1.1";

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
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("'['hh:mm:ss'] '");

    // The CommandManager used by the bot to keep track of commands
    private final CommandManager commandManager = new CommandManager();

    // The Set of registered event listeners
    private final Set<Listener> listeners = new HashSet<>();

    // All currently known channels
    private final HashMap<String, Channel> channels = new HashMap<>();

    // All currently known users
    private final HashMap<String, User> users = new HashMap<>();

    // The FileManager used for loading/saving files
    private final FileManager fileManager;

    // The PML file containing all bot data
    private PMLFile properties;

    // The current connection the bot is using
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
            this.log(3, "Couldn't connect (A connection is already active)");
        else
        {
            this.reload();
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
            this.log(2, "Couldn't disconnect (No connection currently exists)");
        else
        {
            if (connection.isActive())
                connection.send("QUIT :" + message, true);

            properties.removeAll("permissions");
            for (User user : this.getUsers())
            {
                int i = 1;
                for (User.Permission permission : user.getPermissions())
                    if (!permission.equals(User.Permission.DEFAULT))
                    {
                        properties.write("permissions." + user.getNick() + "." + i, permission.toString());
                        i++;
                    }
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

    public CommandManager getCommandManager()
    {
        return commandManager;
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

    public User getUser(String nick)
    {
        User user;
        if (users.containsKey(nick))
            user = users.get(nick);
        else
        {
            user = new User(nick);
            users.put(nick, user);
        }
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

    public boolean isInChannel(String name)
    {
        return channels.containsKey(name);
    }

    public boolean isUser(String nick)
    {
        return users.containsKey(nick);
    }

    public void registerListener(Listener listener)
    {
        listeners.add(listener);
    }

    public void reload()
    {
        if (new File(fileManager.getDirectory()).mkdir())
            System.out.println("[New directory created]");
        properties = fileManager.loadPMLFile("bot", 2);

        if (!properties.isTitle("" + Property.nick))
            properties.write("" + Property.nick, "ArcheBot");
        if (!properties.isTitle("" + Property.login))
            properties.write("" + Property.login, "ArcheBot");
        if (!properties.isTitle("" + Property.realname))
            properties.write("" + Property.realname, "ArcheBot (Version {VERSION}) by p_slice");
        if (!properties.isTitle("" + Property.server))
            properties.write("" + Property.server, "irc.esper.net");
        if (!properties.isTitle("" + Property.serverPass))
            properties.write("" + Property.serverPass, "");
        if (!properties.isTitle("" + Property.port))
            properties.write("" + Property.port, "6667");
        if (!properties.isTitle("" + Property.nickservID))
            properties.write("" + Property.nickservID, "");
        if (!properties.isTitle("" + Property.nickservPass))
            properties.write("" + Property.nickservPass, "");
        if (!properties.isTitle("" + Property.prefix))
            properties.write("" + Property.prefix, "+");
        if (!properties.isTitle("" + Property.channels))
            properties.write("" + Property.channels, "#PotatoBot");
        if (!properties.isTitle("" + Property.verbose))
            properties.write("" + Property.verbose, "true");
        if (!properties.isTitle("" + Property.rename))
            properties.write("" + Property.rename, "false");
        if (!properties.isTitle("" + Property.reconnect))
            properties.write("" + Property.reconnect, "false");

        for (User user : this.getUsers())
            for (User.Permission permission : user.getPermissions())
                user.removePermission(permission);

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
            this.log(3, "Couldn't send output (No active connection exists!)");
    }

    public void setProperty(Property property, String value)
    {
        properties.write(property.toString(), value);
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
        return String.format("%s {LOGIN:%s} {REALNAME:%s} {SERVER:%s} {VERSION:%s} {CONNECTED:%b}",
                nick,
                this.getProperty(Property.login),
                this.getProperty(Property.realname),
                this.getProperty(Property.server),
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
        if (StringManager.toBoolean(this.getProperty(Property.verbose)))
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
        realname("realname"),
        server("server"),
        serverPass("serverPass"),
        port("port"),
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
