package net.pslice.archebot;

import net.pslice.archebot.output.Output;
import net.pslice.archebot.utilities.Element;
import net.pslice.archebot.utilities.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class ArcheBot extends User {

    public static final String VERSION = "1.19";
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss:SSS] ");
    public String USER_VERSION = "[No user version specified]";
    final HashSet<Handler> handlers = new HashSet<>();
    State state = State.idle;
    ChannelMap channelMap = new ChannelMap();
    ServerMap serverMap = new ServerMap();
    UserMap userMap = new UserMap(this);
    private final TreeMap<String, Command> commands = new TreeMap<>();
    private final long startTime = System.currentTimeMillis();
    private String directory;
    private Element data;
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

    public void clearServerData() {
        if (getBoolean(Property.autoSavePerms))
            for (User user : userMap.getUsers())
                savePermissions(user);
        if (directory != null)
            saveData();
        channelMap = new ChannelMap();
        serverMap = new ServerMap();
        userMap = new UserMap(this);
        modes.clear();
        if (isConnected())
            userMap.addUser(this);
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
            } catch (Exception e) {
                boolean reconnect = getBoolean(Property.reconnect);
                logError("[ArcheBot:connect] An internal exception has occurred (%s)", e);
                log("Disconnected (A fatal exception occurred while connecting | Reconnect: %b)", reconnect);
                if (reconnect) {
                    try {
                        Thread.sleep(getInteger(Property.reconnectDelay));
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
            connection.send("QUIT :" + message, !getBoolean(Property.immediateDisconnect));
            if (reconnect)
                try {
                    breakThread();
                    int sleepTime = getInteger(Property.sleepTime);
                    while (state != State.idle)
                        Thread.sleep(sleepTime);
                    connect();
                } catch (Exception e) {
                    logError("[ArcheBot:disconnect] An internal exception has occurred (%s)", e);
                }
        }
    }

    public boolean getBoolean(Property property) {
        return StringUtils.toBoolean(getProperty(property));
    }

    public ChannelMap getChannelMap() {
        return channelMap;
    }

    public Command getCommand(String id) {
        return isRegistered(id) ? commands.get(id.toLowerCase()) : null;
    }

    public TreeSet<Command> getCommands() {
        return new TreeSet<>(commands.values());
    }

    public String getDirectory() {
        return directory;
    }

    public Element getData(String name) {
        return data.getChild(name);
    }

    public HashSet<Handler> getHandlers() {
        return new HashSet<>(handlers);
    }

    public int getInteger(Property property) {
        String value = getProperty(property);
        if (value.matches("\\d+"))
            return Integer.parseInt(value);
        else if (property.defaultValue instanceof Integer)
            return (int) reset(property);
        throw new RuntimeException("Cannot convert the value of " + property.name() + " to an integer.");
    }

    public String getProperty(Property property) {
        Element e = data.getChild("properties/" + property.fullName());
        if (!e.hasContent() && !property.allowsEmpty())
            reset(property);
        return e.getContent();
    }

    public TreeSet<String> getRegisteredCommandIds() {
        return new TreeSet<>(commands.keySet());
    }

    public long getRuntime() {
        return System.currentTimeMillis() - startTime;
    }

    public ServerMap getServerMap() {
        return serverMap;
    }

    public State getState() {
        return state;
    }

    public long getUptime() {
        return isConnected() ? System.currentTimeMillis() - connectTime : -1;
    }

    public UserMap getUserMap() {
        return userMap;
    }

    public boolean isConnected() {
        return connection != null && state == State.connected;
    }

    public boolean isRegistered(Command command) {
        return commands.containsValue(command);
    }

    public boolean isRegistered(String id) {
        return commands.containsKey(id.toLowerCase());
    }

    public boolean isRegistered(Handler handler) {
        return handlers.contains(handler);
    }

    public void log(String line, Object... objects) {
        print("<>", objects.length > 0 ? String.format(line, objects) : line);
    }

    public void logError(String error, Object... objects) {
        print("== Error:", objects.length > 0 ? String.format(error, objects) : error);
    }

    public void register(Command command, Command... commands) {
        this.commands.put(command.name.toLowerCase(), command);
        for (String id : command.ids)
            this.commands.put(id.toLowerCase(), command);
        for (Command cmd : commands) {
            this.commands.put(cmd.name.toLowerCase(), cmd);
            for (String id : cmd.ids)
                this.commands.put(id.toLowerCase(), cmd);
        }
    }

    public void register(Handler handler) {
        handlers.add(handler);
    }

    public void reload() {
        if (directory == null && data != null)
            return;

        try {
            if (directory != null) {
                if (new File(directory).mkdir())
                    log("New directory created.");
                data = Element.read((directory.isEmpty() ? "" : directory + File.separator) + "bot");
            } else
                data = new Element("bot");
            Element properties = data.getChild("properties");

            for (Property property : Property.values())
                if (!properties.isChild(property.fullName()))
                    reset(property);
            for (User user : userMap.getUsers())
                updatePermissions(user);

            if (directory != null)
                saveData();
        } catch (Exception e) {
            logError("[ArcheBot:reload] Unable to reload: " + e);
        }
    }

    public Object reset(Property property) {
        return setProperty(property, property.defaultValue);
    }

    public void savePermissions(User user) {
        Element permData = data.getChild("permissions/" + user);
        if (permData.size() > 0)
            permData.removeChildren();
        for (Permission permission : user.permissions.keySet())
            if (user.isSavable(permission))
                permData.getChild("#").setContent(permission.toString());
        if (permData.size() == 0)
            data.getChild("permissions").removeChild(permData);
    }

    public void saveData() {
        if (directory != null)
            data.write((directory.isEmpty() ? "" : directory + File.separator) + "bot");
        else
            logError("Unable to save data file (Directory must be specified)");
    }

    public void send(String output, Object... objects) {
        if (objects.length > 0)
            output = String.format(output, objects);
        if (isConnected())
            connection.send(output, true);
        else
            logError("Unable to send output [%s] (No active connection exists)", output);
    }

    public void send(Output output) {
        send(output.getLine());
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setLogStream(PrintStream stream) {
        this.stream = stream;
    }

    public Object setProperty(Property property, Object value) {
        data.getChild("properties/" + property.fullName()).setContent(value.toString());
        return value;
    }

    public void unregister(Command command) {
        unregister(command.name);
        for (String ID : command.ids)
            if (getCommand(ID) == command)
                unregister(ID);
    }

    public void unregister(String id) {
        if (commands.containsKey(id.toLowerCase()))
            commands.remove(id.toLowerCase());
    }

    public void unregister(Handler handler) {
        if (handlers.contains(handler))
            handlers.remove(handler);
    }

    synchronized void print(String prefix, String line) {
        if (data == null || getBoolean(Property.enableLogging))
            stream.println(dateFormat.format(new Date()) + prefix + " " + line);
    }

    @SuppressWarnings("unchecked")
    void shutdown(String reason) {
        state = State.disconnecting;
        connection.close();
        connection = null;
        for (Handler handler : handlers)
            handler.onDisconnect(this, reason);
        connectTime = 0;
        clearServerData();
        log("Disconnected (%s)", reason);
        state = State.idle;
    }

    void updatePermissions(User user) {
        user.resetPermissions();
        if (data.getChild("permissions").isChild(user.nick))
            for (Element permission : data.getChild("permissions/" + user.nick).getChildren())
                user.givePermission(Permission.get(permission.getTag().equals("#") ? permission.getContent() : permission.getTag()));
    }
}
