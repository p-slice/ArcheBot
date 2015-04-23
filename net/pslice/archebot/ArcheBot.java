package net.pslice.archebot;

import net.pslice.archebot.events.ConnectEvent;
import net.pslice.archebot.events.DisconnectEvent;
import net.pslice.archebot.output.JoinMessage;
import net.pslice.archebot.output.NickservMessage;
import net.pslice.archebot.handlers.ConnectionHandler;
import net.pslice.pml.PMLElement;
import net.pslice.utilities.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ArcheBot extends User {

    public static final String VERSION = "1.12";
    public static String USER_VERSION = "";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss:SSS] ");
    private final Set<Handler> handlers = new HashSet<>();
    private final HashMap<String, Channel> channels = new HashMap<>();
    private final HashMap<String, User> users = new HashMap<>();
    private final HashMap<String, Command> commands = new HashMap<>();
    private final HashMap<String, String> serverInfo = new HashMap<>();
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
        this.reload();

        this.log("ArcheBot (Version %s) loaded and ready for use!", VERSION);
    }

    @SuppressWarnings("unchecked")
    public void connect() {
        if (this.isConnected())
            this.logError("Unable to connect (A connection is already active)");
        else {
            this.reload();
            try {
                connection = new Connection(this);
                connectTime = System.currentTimeMillis();

                if (!this.getProperty(Property.nickservPass).isEmpty())
                    if (this.getProperty(Property.nickservID).isEmpty())
                        this.send(new NickservMessage(this.getProperty(Property.nickservPass)));
                    else
                        this.send(new NickservMessage(this.getProperty(Property.nickservID), this.getProperty(Property.nickservPass)));

                if (!this.getProperty(Property.channels).isEmpty())
                    for (String channel : StringUtils.breakList(this.getProperty(Property.channels)))
                        this.send(new JoinMessage(channel));

                for (Handler handler : handlers) {
                    if (handler instanceof ConnectionHandler)
                        ((ConnectionHandler) handler).onConnect(this);
                    if (handler instanceof ConnectEvent.Handler)
                        ((ConnectEvent.Handler) handler).onConnect(new ConnectEvent(this));
                }
            } catch (Connection.ConnectionException e) {
                this.logError("Connection refused (%s)", e.getMessage());
            } catch (Exception e) {
                boolean reconnect = this.toBoolean(Property.reconnect);

                this.logError("An internal exception has occurred (%s)", e);
                this.log("Disconnected (A fatal exception occurred while connecting | Reconnect: %b)", reconnect);
                if (reconnect) {
                    try {
                        Thread.sleep(this.toInteger(Property.reconnectDelay));
                    } catch (InterruptedException ie) {
                        this.logError("An internal exception has occurred (%s)", ie);
                    }
                    this.connect();
                }
            }
        }
    }

    public void disconnect() {
        this.disconnect("Shutting down");
    }

    public void disconnect(String message) {
        this.disconnect(message, false);
    }

    @SuppressWarnings("unchecked")
    public void disconnect(String message, boolean reconnect) {
        if (!this.isConnected())
            this.logError("Unable to disconnect (No active connection exists)");
        else {
            if (connection.isActive())
                connection.send("QUIT :" + message, true);
            connectTime = 0;

            connection.close();
            connection = null;

            data.getChild("permissions").removeChildren();
            for (User user : this.getUsers())
                for (Permission permission : user.getPermissions())
                    if (!permission.equals(Permission.DEFAULT))
                        data.getChild("permissions/" + user + "/" + permission);
            saveProperties();

            users.clear();
            channels.clear();

            this.log("Disconnected (%s | Reconnect: %b)", message, reconnect);

            for (Handler handler : handlers) {
                if (handler instanceof ConnectionHandler)
                    ((ConnectionHandler) handler).onDisconnect(this, message, reconnect);
                if (handler instanceof DisconnectEvent.Handler)
                    ((DisconnectEvent.Handler) handler).onDisconnect(new DisconnectEvent(this, message, reconnect));
            }
            if (reconnect)
                this.connect();
        }
    }

    public Channel getChannel(String name) {
        return channels.containsKey(name) ? channels.get(name) : new Channel(name);
    }

    public Set<Channel> getChannels() {
        return new HashSet<>(channels.values());
    }

    public Command getCommand(String ID) {
        return commands.containsKey(ID.toLowerCase()) ? commands.get(ID.toLowerCase()) : null;
    }

    public Set<Command> getCommands() {
        return new TreeSet<>(commands.values());
    }

    public String getDirectory() {
        return directory;
    }

    public PMLElement getData(String name) {
        return data.getChild(name);
    }

    public Set<Handler> getHandlers() {
        return new HashSet<>(handlers);
    }

    public String getProperty(Property property) {
        if (data.getChild("properties/" + property).getContent().isEmpty() && !property.allowsEmpty())
            this.setProperty(property, property.getDefaultValue());
        return data.getChild("properties/" + property).getContent();
    }

    public Set<String> getRegisteredCommandIDs() {
        return new TreeSet<>(commands.keySet());
    }

    public long getRuntime() {
        return System.currentTimeMillis() - startTime;
    }

    public String getServerInfo(String info) {
        return serverInfo.containsKey(info) ? serverInfo.get(info) : null;
    }

    public long getUptime() {
        return this.isConnected() ? System.currentTimeMillis() - connectTime : -1;
    }

    public User getUser(String nick) {
        if (!users.containsKey(nick))
            users.put(nick, new User(nick));
        return users.get(nick);
    }

    public Set<User> getUsers() {
        return new HashSet<>(users.values());
    }

    public boolean isConnected() {
        return connection != null;
    }

    public boolean isInChannel(Channel channel) {
        return this.isInChannel(channel.name);
    }

    public boolean isInChannel(String name) {
        return channels.containsKey(name);
    }

    public boolean isRegistered(Command command) {
        return commands.containsValue(command);
    }

    public boolean isRegistered(String ID) {
        return commands.containsKey(ID.toLowerCase());
    }

    public boolean isUser(String nick) {
        return users.containsKey(nick);
    }

    public void log(String line, Object... objects) {
        this.print("<>", String.format(line, objects));
    }

    public void logError(String error, Object... objects) {
        this.print("== Error:", String.format(error, objects));
    }

    public void register(Command... commands) {
        for (Command command : commands) {
            this.commands.put(command.getName().toLowerCase(), command);
            for (String ID : command.getIDs())
                this.commands.put(ID.toLowerCase(), command);
        }
    }

    public void register(Handler handler) {
        handlers.add(handler);
    }

    public void reload() {
        if (directory != null) {
            if (new File(directory).mkdir())
                this.log("New directory created.");
            data = PMLElement.read((directory.isEmpty() ? "" : directory + File.separator) + "bot", "bot");
        } else
            data = new PMLElement("bot");
        PMLElement properties = data.getChild("properties");

        for (Property property : Property.values())
            if (!properties.isChild(property.toString()))
                this.setProperty(property, property.getDefaultValue());

        for (User user : this.getUsers())
            user.resetPermissions();
        for (PMLElement user : data.getChild("permissions").getChildren())
            this.updatePermissions(this.getUser(user.getTag()));

        saveProperties();
    }

    public void saveProperties() {
        if (directory != null)
            data.write((directory.isEmpty() ? "" : directory + File.separator) + "bot");
    }

    public void send(String output) {
        if (this.isConnected())
            connection.send(output, false);
        else
            this.logError("Unable to send output (No active connection exists)");
    }

    public void send(Output output) {
        this.send(output.toString());
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
        return StringUtils.toBoolean(this.getProperty(property));
    }

    public int toInteger(Property property) {
        return this.getProperty(property).matches("\\d+") ?
                Integer.parseInt(this.getProperty(property)) :
                (int) this.setProperty(property, property.getDefaultValue());
    }

    public void unregister(Command command) {
        this.unregister(command.getName());
        for (String ID : command.getIDs())
            if (this.getCommand(ID) == command)
                this.unregister(ID);
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
                nick, login, hostmask, realname, server, VERSION, this.isConnected(), this.getUptime());
    }

    void addChannel(Channel channel) {
        channels.put(channel.name, channel);
    }

    void addUser(User user) {
        users.put(user.nick, user);
    }

    synchronized void print(String prefix, String line) {
        if (data == null || this.toBoolean(Property.verbose))
            stream.println(dateFormat.format(new Date()) + prefix + " " + line);
    }

    void removeChannel(String name) {
        if (channels.containsKey(name))
            channels.remove(name);
    }

    void removeUser(String nick) {
        if (users.containsKey(nick))
            users.remove(nick);
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
