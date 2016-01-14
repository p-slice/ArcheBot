package net.pslice.archebot;

import net.pslice.archebot.output.Output;
import net.pslice.archebot.utilities.Element;
import net.pslice.archebot.utilities.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArcheBot extends User {

    public static final String VERSION = "1.20";
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss:SSS] ");
    public String USER_VERSION = "[No user version specified]";
    Handler handler;
    State state = State.idle;
    ChannelMap channelMap = new ChannelMap();
    ServerMap serverMap = new ServerMap();
    UserMap userMap = new UserMap(this);
    private final long startTime = System.currentTimeMillis();
    private long connectTime = 0;
    private CommandMap commandMap = new CommandMap();
    private Connection connection;
    private Element data;
    private PrintStream stream = System.out;
    private String configuration, directory;

    public ArcheBot() {
        this(null);
    }

    public ArcheBot(String directory) {
        this(directory, null);
    }

    public ArcheBot(String directory, String configuration) {
        this.directory = directory;
        this.configuration = configuration;
        known = true;
        reload();
        log("ArcheBot (Version %s) loaded and ready for use!", VERSION);
    }

    public void breakThread() {
        if (state != State.idle)
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
        channelMap.current = false;
        channelMap = new ChannelMap();
        serverMap.current = false;
        serverMap = new ServerMap();
        userMap.current = false;
        userMap = new UserMap(this);
        modes.clear();
        if (state != State.idle)
            userMap.addUser(this);
    }

    public void connect() {
        if (state != State.idle)
            logError("Unable to connect (Current state: %s)", state);
        else {
            reload();
            if (!hasHandler())
                log("No handler found. Set a handler to allow full interaction with the server.");
            try {
                connection = new Connection(this);
                state = State.connecting;
                connectTime = System.currentTimeMillis();
            } catch (Exception e) {
                boolean reconnect = getBoolean(Property.reconnect);
                logError("[ArcheBot:connect] An internal exception has occurred (%s)", e);
                log("Disconnected (A fatal exception occurred while connecting | Reconnect: %b)", reconnect);
                if (reconnect) {
                    try {
                        int delay = getInteger(Property.reconnectDelay);
                        log("Reconnecting in approximately %d seconds...", delay / 1000);
                        Thread.sleep(delay);
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
        if (state == State.idle)
            logError("Unable to disconnect (Current state: %s)", state);
        else {
            if (getBoolean(Property.immediateDisconnect))
                connection.send("QUIT :" + message);
            else
                connection.queue("QUIT :" + message);
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
        return StringUtils.toBoolean(getValue(property));
    }

    public ChannelMap getChannelMap() {
        return channelMap;
    }

    public CommandMap getCommandMap() {
        return commandMap;
    }

    public Element getConfiguration() {
        return configuration == null ? null : getConfiguration(configuration);
    }

    public Element getConfiguration(String configuration) {
        return data.getChild("configurations/" + configuration);
    }

    public String getDirectory() {
        return directory;
    }

    public Element getData(String name) {
        return data.getChild(name);
    }

    public Handler getHandler() {
        return handler;
    }

    public int getInteger(Property property) {
        String value = getValue(property);
        if (value.matches("\\d+"))
            return Integer.parseInt(value);
        else if (property.defaultValue instanceof Integer)
            return (int) property.defaultValue;
        throw new RuntimeException("[ArcheBot:getInteger] Cannot convert the value of " + property.name() + " to an integer.");
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
        return state != State.idle ? System.currentTimeMillis() - connectTime : -1;
    }

    public UserMap getUserMap() {
        return userMap;
    }

    public String getValue(Property property) {
        Element configuration = getConfiguration();
        if (configuration != null && configuration.isChild(property.name()) && configuration.getChild(property.name()).hasContent())
            return configuration.getChild(property.name()).getContent();
        String name = "properties/" + property.name();
        if (data.isChild(name) && data.getChild(name).hasContent())
            return data.getChild(name).getContent();
        return property.defaultValue.toString();
    }

    public boolean hasHandler() {
        return handler != null;
    }

    public void log(String line, Object... objects) {
        print("<>", objects.length > 0 ? String.format(line, objects) : line);
    }

    public void logError(String error, Object... objects) {
        print("== Error:", objects.length > 0 ? String.format(error, objects) : error);
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
            for (User user : userMap.getUsers())
                updatePermissions(user);
            if (directory != null)
                saveData();
        } catch (Exception e) {
            logError("[ArcheBot:reload] Unable to reload (%s)", e);
        }
    }

    public void removeValue(Property property) {
        removeValue(property, false);
    }

    public void removeValue(Property property, boolean global) {
        if (global || configuration == null)
            data.getChild("properties").removeChild(property.name());
        else
            getConfiguration().removeChild(property.name());
    }

    public void savePermissions(User user) {
        Element permissions = data.getChild("permissions");
        permissions.removeChildren(user.nick);
        for (Permission permission : user.permissions.keySet())
            if (user.isSavable(permission))
                permissions.addChild(new Element(user.nick, permission.getName()));
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
        if (state == State.connected)
            connection.queue(output);
        else
            logError("Unable to send output [%s] (No active connection exists)", output);
    }

    public void send(Output output) {
        send(output.getLine());
    }

    public void setCommandMap(CommandMap commandMap) {
        this.commandMap = commandMap;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public void setConfiguration(Element configuration) {
        if (data.isChild("configurations/" + configuration.getTag()))
            data.removeChild("configurations/" + configuration.getTag());
        data.getChild("configurations").addChild(configuration);
        this.configuration = configuration.getTag();
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setLogStream(PrintStream stream) {
        this.stream = stream;
    }

    public Object setValue(Property property, Object value) {
        return setValue(property, value, false);
    }

    public Object setValue(Property property, Object value, boolean global) {
        if (global || configuration == null)
            data.getChild("properties/" + property.name()).setContent(value.toString());
        else
            getConfiguration().getChild(property.name()).setContent(value.toString());
        return value;
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
        if (hasHandler())
            handler.onDisconnect(this, reason);
        connectTime = 0;
        clearServerData();
        log("Disconnected (%s)", reason);
        state = State.idle;
    }

    void updatePermissions(User user) {
        user.clearPermissions();
        for (Element element : data.getChild("permissions").getChildren(user.nick)) {
            if (element.hasContent())
                user.givePermission(element.getContent());
            for (Element permission : element.getChildren())
                user.givePermission(permission.getTag().equals("#") ? permission.getContent() : permission.getTag());
        }
    }
}
