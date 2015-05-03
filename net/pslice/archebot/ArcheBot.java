package net.pslice.archebot;

import net.pslice.archebot.events.ConnectEvent;
import net.pslice.archebot.events.DisconnectEvent;
import net.pslice.archebot.output.JoinMessage;
import net.pslice.archebot.output.NickservMessage;
import net.pslice.archebot.handlers.ConnectionHandler;
import net.pslice.pml.PMLElement;
import net.pslice.utilities.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class ArcheBot extends User {

    public static final String VERSION = "1.13";
    public static String USER_VERSION = "[No user version specified]";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss:SSS] ");
    private final HashSet<Handler> handlers = new HashSet<>();
    private final HashMap<String, Channel> channels = new HashMap<>();
    private final HashMap<String, User> users = new HashMap<>();
    private final HashMap<String, Command> commands = new HashMap<>();
    private final HashMap<String, String> serverInfo = new HashMap<>();
    private final HashMap<Character, Mode> modes = new HashMap<>(),
            userModes = new HashMap<>();
    private final long startTime = System.currentTimeMillis();
    private String directory;
    private PMLElement data;
    private PrintStream stream = System.out;
    private Connection connection;
    private long connectTime = 0;

    public ArcheBot() {
        this(null);
    }

    public ArcheBot(String directory) {
        this.directory = directory;
        reload();

        log("ArcheBot (Version %s) loaded and ready for use!", VERSION);
    }

    @SuppressWarnings("unchecked")
    public void connect() {
        if (isConnected())
            logError("Unable to connect (A connection is already active)");
        else {
            reload();
            try {
                connection = new Connection(this);
                connectTime = System.currentTimeMillis();
                addUser(this);

                if (!getProperty(Property.nickservPass).isEmpty())
                    if (getProperty(Property.nickservID).isEmpty())
                        send(new NickservMessage(getProperty(Property.nickservPass)));
                    else
                        send(new NickservMessage(getProperty(Property.nickservID), getProperty(Property.nickservPass)));

                if (!getProperty(Property.channels).isEmpty())
                    for (String channel : StringUtils.breakList(getProperty(Property.channels)))
                        send(new JoinMessage(channel));

                for (Handler handler : handlers) {
                    if (handler instanceof ConnectionHandler)
                        ((ConnectionHandler) handler).onConnect(this);
                    if (handler instanceof ConnectEvent.Handler)
                        ((ConnectEvent.Handler) handler).onConnect(new ConnectEvent(this));
                }
            } catch (Connection.ConnectionException e) {
                logError("Connection refused (%s)", e.getMessage());
            } catch (Exception e) {
                boolean reconnect = toBoolean(Property.reconnect);

                logError("An internal exception has occurred (%s)", e);
                log("Disconnected (A fatal exception occurred while connecting | Reconnect: %b)", reconnect);
                if (reconnect) {
                    try {
                        Thread.sleep(toInteger(Property.reconnectDelay));
                    } catch (InterruptedException ie) {
                        logError("An internal exception has occurred (%s)", ie);
                    }
                    connect();
                }
            }
        }
    }

    public void disconnect() {
        disconnect("Shutting down");
    }

    public void disconnect(String message) {
        disconnect(message, false);
    }

    @SuppressWarnings("unchecked")
    public void disconnect(String message, boolean reconnect) {
        if (!isConnected())
            logError("Unable to disconnect (No active connection exists)");
        else {
            if (connection.isActive())
                connection.send("QUIT :" + message, true);
            connectTime = 0;

            connection.close();
            connection = null;

            data.getChild("permissions").removeChildren();
            for (User user : getUsers())
                for (Permission permission : user.getPermissions())
                    if (!permission.equals(Permission.DEFAULT))
                        data.getChild("permissions/" + user + "/" + permission);
            saveProperties();

            log("Disconnected (%s | Reconnect: %b)", message, reconnect);

            for (Handler handler : handlers) {
                if (handler instanceof ConnectionHandler)
                    ((ConnectionHandler) handler).onDisconnect(this, message, reconnect);
                if (handler instanceof DisconnectEvent.Handler)
                    ((DisconnectEvent.Handler) handler).onDisconnect(new DisconnectEvent(this, message, reconnect));
            }

            users.clear();
            channels.clear();
            serverInfo.clear();
            modes.clear();

            if (reconnect)
                connect();
        }
    }

    public Channel getChannel(String name) {
        return channels.containsKey(name.toLowerCase()) ? channels.get(name.toLowerCase()) : new Channel(name);
    }

    public TreeSet<Channel> getChannels() {
        return new TreeSet<>(channels.values());
    }

    public Command getCommand(String ID) {
        return commands.containsKey(ID.toLowerCase()) ? commands.get(ID.toLowerCase()) : null;
    }

    public TreeSet<Command> getCommands() {
        return new TreeSet<>(commands.values());
    }

    public String getDirectory() {
        return directory;
    }

    public PMLElement getData(String name) {
        return data.getChild(name);
    }

    public HashSet<Handler> getHandlers() {
        return new HashSet<>(handlers);
    }

    public Mode getMode(char ID) {
        return modes.containsKey(ID) ? modes.get(ID) : null;
    }

    public TreeSet<Mode> getAllModes() {
        return new TreeSet<>(modes.values());
    }

    public TreeSet<Mode> getAllModes(Mode.Type type) {
        if (type == Mode.Type.USER)
            return new TreeSet<>(userModes.values());
        TreeSet<Mode> typeModes = new TreeSet<>();
        for (Mode mode : modes.values())
            if (mode.getType() == type)
                typeModes.add(mode);
        return typeModes;
    }

    public String getProperty(Property property) {
        if (data.getChild("properties/" + property).getContent().isEmpty() && !property.allowsEmpty())
            setProperty(property, property.getDefaultValue());
        return data.getChild("properties/" + property).getContent();
    }

    public TreeSet<String> getRegisteredCommandIDs() {
        return new TreeSet<>(commands.keySet());
    }

    public long getRuntime() {
        return System.currentTimeMillis() - startTime;
    }

    public String getServerInfo(String info) {
        return serverInfo.containsKey(info) ? serverInfo.get(info) : null;
    }

    public long getUptime() {
        return isConnected() ? System.currentTimeMillis() - connectTime : -1;
    }

    public User getUser(String nick) {
        if (!users.containsKey(nick.toLowerCase()))
            users.put(nick.toLowerCase(), new User(nick));
        return users.get(nick.toLowerCase());
    }

    public Mode getUserMode(char ID) {
        return userModes.containsKey(ID) ? userModes.get(ID) : null;
    }

    public TreeSet<User> getUsers() {
        return new TreeSet<>(users.values());
    }

    public boolean isConnected() {
        return connection != null && connection.isActive();
    }

    public boolean isMode(char ID) {
        return modes.containsKey(ID);
    }

    public boolean isRegistered(Command command) {
        return commands.containsValue(command);
    }

    public boolean isRegistered(String ID) {
        return commands.containsKey(ID.toLowerCase());
    }

    public boolean isUserMode(char ID) {
        return userModes.containsKey(ID);
    }

    public void log(String line, Object... objects) {
        print("<>", String.format(line, objects));
    }

    public void logError(String error, Object... objects) {
        print("== Error:", String.format(error, objects));
    }

    public void register(Command... commands) {
        for (Command command : commands) {
            this.commands.put(command.name.toLowerCase(), command);
            for (String ID : command.IDs)
                this.commands.put(ID.toLowerCase(), command);
        }
    }

    public void register(Handler handler) {
        handlers.add(handler);
    }

    public void reload() {
        if (directory != null) {
            if (new File(directory).mkdir())
                log("New directory created.");
            data = PMLElement.read((directory.isEmpty() ? "" : directory + File.separator) + "bot", "bot");
        } else
            data = new PMLElement("bot");
        PMLElement properties = data.getChild("properties");

        for (Property property : Property.values())
            if (!properties.isChild(property.toString()))
                setProperty(property, property.getDefaultValue());

        for (User user : getUsers())
            user.resetPermissions();
        for (PMLElement user : data.getChild("permissions").getChildren())
            updatePermissions(getUser(user.getTag()));

        saveProperties();
    }

    public void saveProperties() {
        if (directory != null)
            data.write((directory.isEmpty() ? "" : directory + File.separator) + "bot");
    }

    public void send(String output) {
        if (isConnected())
            connection.send(output, false);
        else
            logError("Unable to send output (No active connection exists)");
    }

    public void send(Output output) {
        send(output.toString());
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setLogStream(PrintStream stream) {
        this.stream = stream;
    }

    public Object setProperty(Property property, Object value) {
        data.getChild("properties/" + property).setContent(value.toString());
        return value;
    }

    public boolean toBoolean(Property property) {
        return StringUtils.toBoolean(getProperty(property));
    }

    public int toInteger(Property property) {
        return getProperty(property).matches("\\d+") ?
                Integer.parseInt(getProperty(property)) :
                (int) setProperty(property, property.getDefaultValue());
    }

    public void unregister(Command command) {
        unregister(command.name);
        for (String ID : command.IDs)
            if (getCommand(ID) == command)
                unregister(ID);
    }

    public void unregister(String ID) {
        if (commands.containsKey(ID.toLowerCase()))
            commands.remove(ID.toLowerCase());
    }

    public void unregister(Handler handler) {
        if (handlers.contains(handler))
            handlers.remove(handler);
    }

    @Override
    public String details() {
        return String.format("%s {LOGIN:%s} {HOSTMASK:%s} {REALNAME:%s} {SERVER:%s} {VERSION:%s} {CONNECTED:%b} {UPTIME:%d}",
                nick, login, hostmask, realname, server, VERSION, isConnected(), getUptime());
    }

    void addChannel(Channel channel) {
        channels.put(channel.name.toLowerCase(), channel);
    }

    void addServerMode(Mode mode) {
        if (mode.isUser())
            userModes.put(mode.getID(), mode);
        else
            modes.put(mode.getID(), mode);
    }

    void addUser(User user) {
        users.put(user.nick.toLowerCase(), user);
    }

    synchronized void print(String prefix, String line) {
        if (data == null || toBoolean(Property.verbose))
            stream.println(dateFormat.format(new Date()) + prefix + " " + line);
    }

    void removeChannel(String name) {
        if (channels.containsKey(name.toLowerCase()))
            channels.remove(name.toLowerCase());
    }

    void removeUser(String nick) {
        if (users.containsKey(nick.toLowerCase()))
            users.remove(nick.toLowerCase());
    }

    void setServerInfo(String info, String value) {
        serverInfo.put(info, value);
    }

    void updatePermissions(User user) {
        user.resetPermissions();
        if (data.getChild("permissions").isChild(user.nick))
            for (PMLElement permission : data.getChild("permissions/" + user.nick).getChildren())
                user.givePermission(Permission.get(permission.getTag().matches("^\\d+$") ? permission.getContent() : permission.getTag()));
    }

    public interface Handler<B extends ArcheBot> {}

    public static class Event<B extends ArcheBot> {

        private final B bot;
        private final long timeStamp = System.currentTimeMillis();

        protected Event(B bot) {
            this.bot = bot;
        }

        public B getBot() {
            return bot;
        }

        public long getTimeStamp() {
            return timeStamp;
        }
    }

    public static class Output {

        private final String line;

        protected Output(String line) {
            this.line = line;
        }

        @Override
        public final String toString() {
            return line;
        }

        @Override
        public final boolean equals(Object obj) {
            return obj instanceof Output && obj.toString().equals(line);
        }
    }
}
