package net.pslice.archebot;

import net.pslice.archebot.events.DisconnectEvent;
import net.pslice.archebot.handlers.ConnectionHandler;
import net.pslice.utilities.PMLElement;
import net.pslice.utilities.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class ArcheBot extends User {

    public static final String VERSION = "1.16";
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss:SSS] ");
    public String USER_VERSION = "[No user version specified]";
    final HashSet<Handler> handlers = new HashSet<>();
    State state = State.idle;
    private final TreeMap<String, Channel> channels = new TreeMap<>();
    private final TreeMap<String, User> users = new TreeMap<>();
    private final TreeMap<String, Command> commands = new TreeMap<>();
    private final TreeMap<String, Server> servers = new TreeMap<>();
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

    public void breakThread() {
        if (isConnected())
            connection.breakThread();
        else
            logError("Unable to break handler thread (No active connection exists)");
    }

    public void connect() {
        if (isConnected())
            logError("Unable to connect (A connection is already active)");
        else {
            reload();
            try {
                state = State.connecting;
                connection = new Connection(this);
                connectTime = System.currentTimeMillis();
                addUser(this);
            } catch (Exception e) {
                boolean reconnect = toBoolean(Property.reconnect);
                logError("[ArcheBot:connect] An internal exception has occurred (%s)", e);
                log("Disconnected (A fatal exception occurred while connecting | Reconnect: %b)", reconnect);
                if (reconnect) {
                    try {
                        Thread.sleep(toInteger(Property.reconnectDelay));
                    } catch (InterruptedException ie) {
                        logError("[ArcheBot:connect] An internal exception has occurred (%s)", ie);
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

    public void disconnect(String message, boolean reconnect) {
        if (!isConnected())
            logError("Unable to disconnect (No active connection exists)");
        else {
            connection.send("QUIT :" + message, !toBoolean(Property.immediateDisconnect));
            if (reconnect) {
                try {
                    breakThread();
                    int sleepTime = toInteger(Property.sleepTime);
                    while (state != State.idle) {
                        Thread.sleep(sleepTime);
                    }
                    connect();
                } catch (Exception e) {
                    logError("[ArcheBot:disconnect] An internal exception has occurred (%s)", e);
                }
            }
        }
    }

    public String get(Property property) {
        if (data.getChild("properties/" + property.fullName()).getContent().isEmpty() && !property.allowsEmpty())
            reset(property);
        return data.getChild("properties/" + property.fullName()).getContent();
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

    public TreeSet<String> getRegisteredCommandIDs() {
        return new TreeSet<>(commands.keySet());
    }

    public long getRuntime() {
        return System.currentTimeMillis() - startTime;
    }

    public Server getServer(String name) {
        return servers.containsKey(name.toLowerCase()) ? servers.get(name.toLowerCase()) : new Server(name);
    }

    public TreeSet<Server> getServers() {
        return new TreeSet<>(servers.values());
    }

    public State getState() {
        return state;
    }

    public long getUptime() {
        return isConnected() ? System.currentTimeMillis() - connectTime : -1;
    }

    public User getUser(String nick) {
        if (nick.isEmpty())
            return new User();
        if (!users.containsKey(nick.toLowerCase())) {
            User user = new User(nick);
            addUser(user);
            updatePermissions(user);
        }
        return users.get(nick.toLowerCase());
    }

    public TreeSet<User> getUsers() {
        return new TreeSet<>(users.values());
    }

    public boolean isConnected() {
        return connection != null && state == State.connected;
    }

    public boolean isRegistered(Command command) {
        return commands.containsValue(command);
    }

    public boolean isRegistered(String ID) {
        return commands.containsKey(ID.toLowerCase());
    }

    public boolean isRegistered(Handler handler) {
        return handlers.contains(handler);
    }

    public boolean isServer(String name) {
        return servers.containsKey(name.toLowerCase());
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
        if (directory == null && data != null)
            return;

        if (directory != null) {
            if (new File(directory).mkdir())
                log("New directory created.");
            data = PMLElement.read((directory.isEmpty() ? "" : directory + File.separator) + "bot", "bot");
        } else
            data = new PMLElement("bot");
        PMLElement properties = data.getChild("properties");

        for (Property property : Property.values())
            if (!properties.isChild(property.fullName()))
                reset(property);
        for (User user : users.values())
            updatePermissions(user);

        saveData();
    }

    public Object reset(Property property) {
        return set(property, property.defaultValue);
    }

    public void savePermissions(User user) {
        PMLElement permData = data.getChild("permissions/" + user);
        if (permData.size() > 0)
            permData.removeChildren();
        for (Permission permission : user.permissions)
            if (permission != Permission.DEFAULT)
                permData.getChild("#").setContent(permission.toString());
        if (permData.size() == 0)
            permData.setParent(null);
    }

    public void saveData() {
        if (directory != null)
            data.write((directory.isEmpty() ? "" : directory + File.separator) + "bot");
    }

    public void send(String output, Object... objects) {
        if (isConnected())
            connection.send(String.format(output, objects), true);
        else
            logError("Unable to send output (No active connection exists)");
    }

    public void send(Output output) {
        send(output.line);
    }

    public Object set(Property property, Object value) {
        data.getChild("properties/" + property.fullName()).setContent(value.toString());
        return value;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setLogStream(PrintStream stream) {
        this.stream = stream;
    }

    public boolean toBoolean(Property property) {
        return StringUtils.toBoolean(get(property));
    }

    public int toInteger(Property property) {
        String value = get(property);
        if (value.matches("\\d+"))
            return Integer.parseInt(value);
        else if (property.defaultValue instanceof Integer)
            return (int) reset(property);
        throw new RuntimeException("Cannot convert the value of " + property.name() + " to an integer.");
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

    void addChannel(Channel channel) {
        channels.put(channel.name.toLowerCase(), channel);
    }

    void addServer(Server server) {
        servers.put(server.name.toLowerCase(), server);
    }

    void addUser(User user) {
        users.put(user.nick.toLowerCase(), user);
    }

    synchronized void print(String prefix, String line) {
        if (data == null || toBoolean(Property.enableLogging))
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

    @SuppressWarnings("unchecked")
    void shutdown(String reason) {
        connection.close();
        connection = null;
        connectTime = 0;

        for (Handler handler : handlers) {
            if (handler instanceof ConnectionHandler)
                ((ConnectionHandler) handler).onDisconnect(this, reason);
            if (handler instanceof DisconnectEvent.Handler)
                ((DisconnectEvent.Handler) handler).onDisconnect(new DisconnectEvent(this, reason));
        }
        if (toBoolean(Property.autoSavePerms))
            for (User user : getUsers())
                savePermissions(user);
        saveData();
        log("Disconnected (%s)", reason);

        users.clear();
        channels.clear();
        servers.clear();
        modes.clear();
        super.modes.clear();

        state = State.idle;
    }

    void updatePermissions(User user) {
        user.resetPermissions();
        if (data.getChild("permissions").isChild(user.nick))
            for (PMLElement permission : data.getChild("permissions/" + user.nick).getChildren())
                user.give(Permission.get(permission.getTag().matches("^#\\d+$") ? permission.getContent() : permission.getTag()));
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
