package net.pslice.archebot;

import net.pslice.archebot.actions.JoinAction;
import net.pslice.archebot.actions.NickservAction;
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
    public static final String VERSION = "1.6.1";

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

                this.log(3, String.format(exception_message, e));
                this.log(2, "Disconnected (A fatal exception occurred while connecting | Reconnect: " + reconnect + ")");
                if (reconnect)
                {
                    if (!this.getProperty(Property.reconnectDelay).matches("\\d+"))
                        this.setProperty(Property.reconnectDelay, 60000);
                    try
                    {
                        Thread.sleep(Integer.parseInt(this.getProperty(Property.reconnectDelay)));
                    }

                    catch (InterruptedException ie)
                    {
                        this.log(3, String.format(exception_message, ie));
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

            connection.close();
            connection = null;

            data.getSubtitle("permissions").clearSubtitles();
            for (User user : this.getUsers())
            {
                int i = 1;
                for (User.Permission permission : user.getPermissions())
                    if (!permission.equals(User.Permission.DEFAULT))
                        data.getSubtitle("permissions/" + user.getNick() + "/" + i++).setText("" + permission);
            }
            data.save();

            users.clear();
            channels.clear();

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

    public String getProperty(Property property)
    {
        return data.getSubtitle("properties/" + property).getText();
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
        data = new PMLFile("bot", fileManager.getDirectory());
        PMLTitle properties = data.getSubtitle("properties");

        if (!properties.isSubtitle("" + Property.nick))
            this.setProperty(Property.nick, "ArcheBot");
        if (!properties.isSubtitle("" + Property.login))
            this.setProperty(Property.login, "ArcheBot");
        if (!properties.isSubtitle("" + Property.realname))
            this.setProperty(Property.realname, "ArcheBot (Version {VERSION}) by p_slice");
        if (!properties.isSubtitle("" + Property.server))
            this.setProperty(Property.server, "irc.esper.net");
        if (!properties.isSubtitle("" + Property.serverPass))
            this.setProperty(Property.serverPass, "");
        if (!properties.isSubtitle("" + Property.port))
            this.setProperty(Property.port, 6667);
        if (!properties.isSubtitle("" + Property.messageDelay))
            this.setProperty(Property.messageDelay, 1000);
        if (!properties.isSubtitle("" + Property.nickservID))
            this.setProperty(Property.nickservID, "");
        if (!properties.isSubtitle("" + Property.nickservPass))
            this.setProperty(Property.nickservPass, "");
        if (!properties.isSubtitle("" + Property.prefix))
            this.setProperty(Property.prefix, "+");
        if (!properties.isSubtitle("" + Property.channels))
            this.setProperty(Property.channels, "#PotatoBot");
        if (!properties.isSubtitle("" + Property.verbose))
            this.setProperty(Property.verbose, true);
        if (!properties.isSubtitle("" + Property.rename))
            this.setProperty(Property.rename, false);
        if (!properties.isSubtitle("" + Property.reconnect))
            this.setProperty(Property.reconnect, false);
        if (!properties.isSubtitle("" + Property.reconnectDelay))
            this.setProperty(Property.reconnectDelay, 60000);

        for (User user : this.getUsers())
            user.resetPermissions();

        for (PMLTitle title : data.getSubtitle("permissions").getSubtitles())
            for (PMLTitle subtitle : title.getSubtitles())
                this.getUser(title.getTitle()).givePermission(User.Permission.generate(subtitle.getText()));

        data.save();
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
        data.getSubtitle("properties/" + property).setText(value);
    }

    public void setProperty(Property property, int value)
    {
        this.setProperty(property, "" + value);
    }

    public void setProperty(Property property, boolean value)
    {
        this.setProperty(property, "" + value);
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

    synchronized void log(int msgType, String line)
    {
        if (StringUtils.toBoolean(this.getProperty(Property.verbose)))
            stream.println(dateFormat.format(new Date()) + msgTypes[msgType] + line);
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
        reconnect("reconnect"),
        reconnectDelay("reconnectDelay");

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
    }
}
