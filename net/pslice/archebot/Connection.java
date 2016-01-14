package net.pslice.archebot;

import net.pslice.archebot.output.ErrorMessage;
import net.pslice.archebot.utilities.Element;
import net.pslice.archebot.utilities.Queue;
import net.pslice.archebot.utilities.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

@SuppressWarnings("unchecked")
final class Connection {

    private final ArcheBot bot;
    private final Socket socket;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final Queue<String> outgoing = new Queue<>(), incoming = new Queue<>();
    private HandlerThread handler = new HandlerThread();
    private boolean active = false;

    Connection(ArcheBot bot) throws Exception {
        this.bot = bot;
        bot.nick = bot.getValue(Property.nick);

        String server = bot.getValue(Property.server),
               password = bot.getValue(Property.password);
        int port = bot.getInteger(Property.port);

        bot.log("Attempting to connect to %s on port %d...", server, port);
        socket = new Socket(server, port);
        bot.log("Connection successful!");
        active = true;

        socket.setSoTimeout(bot.getInteger(Property.timeoutDelay));

        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        if (!password.isEmpty())
            send("PASS " + password);
        send("NICK " + bot.nick);
        send(String.format("USER %s %d * :%s", bot.getValue(Property.login),
                bot.getBoolean(Property.visible) ? 0 : 8,
                bot.getValue(Property.realname).replace("$VERSION", ArcheBot.VERSION).replace("$USER_VERSION", bot.USER_VERSION)));
        bot.userMap.addUser(bot);

        handler.start();
        new OutputThread().start();
        new InputThread().start();
    }

    void breakThread() {
        handler.current = false;
        handler = new HandlerThread();
        handler.start();
    }

    void close() {
        active = false;
        try {
            socket.close();
        } catch (Exception e) {
            bot.logError("[Connection:close] An internal exception has occurred (%s)", e);
        }
    }

    synchronized void send(String line) {
        if (line != null && !line.isEmpty()) {
            int size = bot.getInteger(Property.lineLength);
            if (line.length() > size)
                line = line.substring(0, size);
            try {
                if (bot.getBoolean(Property.enableFormatting))
                    line = line.replace("\\&", "\000").replace("&r", "\017").replace("&b", "\002").replace("&", "\003").replace("\000", "&");
                writer.write(line);
                writer.newLine();
                writer.flush();
                if (bot.getBoolean(Property.logOutput))
                    bot.print("->", line.replaceAll("\\002|\\003\\d+(,\\d+)?|\\017", ""));
            } catch (Exception e) {
                bot.logError("[Connection:send] An internal exception has occurred (%s)", e);
            }
        }
    }

    synchronized void queue(String line) {
        if (line != null && !line.isEmpty())
            outgoing.add(line);
    }

    private void handle(String line) {
        ChannelMap channelMap = bot.channelMap;
        ServerMap serverMap = bot.serverMap;
        UserMap userMap = bot.userMap;

        String[] parts = (line.charAt(0) == ':' ? line.substring(1) : (bot.server == null ? "" : bot.server) + " " + line).split(" :", 2);
        String tail = parts.length > 1 ? parts[1] : "";
        String[] split = parts[0].split(" ");
        if (split[0].contains("!"))
            split[0] = split[0].substring(0, split[0].indexOf('!'));
        User source = userMap.getUser(split[0]);
        String command = split[1];
        String[] args = new String[split.length - 2];
        System.arraycopy(split, 2, args, 0, args.length);

        if (bot.getBoolean(Property.logInput))
            bot.print("<-", line.replaceAll("\\002|\\003\\d+(,\\d+)?|\\017", ""));

        switch (command.toUpperCase()) {
            case "ERROR": bot.shutdown(tail); return;
            case "INVITE":
                if (bot.hasHandler())
                    for (String channel : tail.split(" "))
                        bot.handler.onInvite(bot, channelMap.getChannel(channel), source);
                break;
            case "JOIN": onJoin(source, args); break;
            case "KICK": onKick(source, args, tail); break;
            case "MODE": onMode(source, args, tail); break;
            case "NICK": onNick(source, tail); break;
            case "NOTICE": onNotice(source, args, tail); break;
            case "PART": onPart(source, args, tail); break;
            case "PING": onPing(tail); break;
            case "PONG":
                if (bot.hasHandler())
                    bot.handler.onPong(bot, serverMap.getServer(args[0]), tail);
                break;
            case "PRIVMSG": onPrivmsg(source, args, tail); break;
            case "QUIT": onQuit(source, tail); break;
            case "TOPIC": onTopic(source, args, tail); break;
            case "001": on001(); break;
            case "004":
                bot.server = serverMap.getServer(args[1]);
                serverMap.addServer(bot.server);
                bot.server.version = args[2];
                for (char mode : args[3].toCharArray())
                    bot.server.userModes.add(mode);
                break;
            case "005": onX05(bot.server, args); break;
            case "105": onX05(serverMap.getServer(split[0]), args); break;
            case "311":
                User user = userMap.getUser(args[1]);
                user.login = args[2];
                user.hostmask = args[3];
                user.realname = tail;
                break;
            case "312": on312(args, tail); break;
            case "318":
                if (bot.hasHandler())
                    bot.handler.onWhois(bot, userMap.getUser(args[1]));
                break;
            case "322":
                if (channelMap.getChannel(args[1]).totalUsers() != Integer.parseInt(args[2]))
                    bot.send("WHO " + args[1]);
                break;
            case "324":
                Channel channel = channelMap.getChannel(args[1]);
                for (char ID : args[2].substring(1).toCharArray())
                    channel.addMode(ID, "");
                break;
            case "330": userMap.getUser(args[1]).nickservId = args[2]; break;
            case "332": channelMap.getChannel(args[1]).topic = tail; break;
            case "333":
                channelMap.getChannel(args[1]).topicSetter = args[2];
                channelMap.getChannel(args[1]).topicTimestamp = Long.parseLong(args[3]);
                break;
            case "351": on351(args); break;
            case "352": on352(args, tail); break;
            case "372": serverMap.getServer(split[0]).motd.add(tail); break;
            case "375":
                Server server = serverMap.getServer(split[0]);
                if (!serverMap.isServer(split[0]))
                    serverMap.addServer(server);
                server.motd.clear();
                break;
            case "376":
                if (bot.hasHandler())
                    bot.handler.onMOTD(bot, serverMap.getServer(split[0]));
                break;
            default:
                if (bot.state == State.connecting)
                    if (command.matches("43[367]")) {
                        if (bot.getBoolean(Property.rename)) {
                            bot.logError("Nick rejected (Trying another one...)");
                            userMap.removeUser(bot.nick);
                            send("NICK " + (bot.nick += "_"));
                            userMap.addUser(bot);
                        } else {
                            bot.shutdown("Nick unavailable: " + tail);
                            return;
                        }
                    } else if (command.matches("[45]\\d\\d")) {
                        bot.shutdown("An error occurred during connection: " + tail);
                        return;
                    }
                break;
        }
        if (bot.hasHandler()) {
            if (command.matches("\\d+"))
                bot.handler.onCode(bot, Integer.parseInt(command), args, tail);
            else
                bot.handler.onLine(bot, source, command, args, tail);
        }
    }

    private void onJoin(User source, String[] args) {
        Channel channel = bot.channelMap.getChannel(args[0]);
        channel.addUser(source);
        if (source == bot) {
            bot.channelMap.addChannel(channel);
            bot.send("WHO " + args[0]);
            bot.send("MODE " + args[0]);
        } else
            bot.send("WHOIS " + source.nick);
        source.known = true;
        if (bot.hasHandler())
            bot.handler.onJoin(bot, channel, source);
    }

    private void onKick(User source, String[] args, String tail) {
        Channel channel = bot.channelMap.getChannel(args[0]);
        User user = bot.userMap.getUser(args[1]);
        if (user == bot) {
            bot.channelMap.removeChannel(channel.name);
            for (User u : channel.getUsers()) {
                boolean known = false;
                for (Channel c : bot.channelMap.getChannels())
                    if (c.contains(u))
                        known = true;
                if (!known)
                    bot.userMap.removeUser(u.nick);
                u.known = known;
            }
        } else {
            channel.removeUser(user);
            boolean known = false;
            for (Channel c : bot.channelMap.getChannels())
                if (c.contains(user))
                    known = true;
            if (!known)
                bot.userMap.removeUser(user.nick);
            user.known = known;
        }
        if (bot.hasHandler())
            bot.handler.onKick(bot, channel, source, user, tail);
    }

    private void onMode(User source, String[] args, String tail) {
        if (!tail.isEmpty()) {
            boolean added = tail.charAt(0) == '+';
            for (char mode : tail.substring(1).toCharArray()) {
                if (added)
                    source.addMode(mode);
                else
                    source.removeMode(mode);
                if (bot.hasHandler())
                    bot.handler.onMode(bot, source, mode, added);
            }
        } else {
            Channel channel = bot.channelMap.getChannel(args[0]);
            boolean added = args[1].charAt(0) == '+';
            int i = 2;
            for (char mode : args[1].substring(1).toCharArray()) {
                String value = args.length >= i + 1 ? args[i++] : "";
                ModeType type = bot.server.getModeType(mode);
                if (type == ModeType.status) {
                    User user = bot.userMap.getUser(value);
                    if (added)
                        channel.addMode(user, mode);
                    else
                        channel.removeMode(user, mode);
                    if (bot.hasHandler())
                        bot.handler.onMode(bot, channel, source, user, mode, added);
                } else {
                    if (added) {
                        if (type == ModeType.list)
                            channel.addListMode(mode, value);
                        else
                            channel.addMode(mode, value);
                    } else {
                        if (type == ModeType.list)
                            channel.removeListMode(mode, value);
                        else
                            channel.removeMode(mode);
                    }
                    if (bot.hasHandler())
                        bot.handler.onMode(bot, channel, source, mode, value, added);
                }
            }
        }
    }

    private void onNick(User source, String tail) {
        String oldNick = source.nick;
        source.nick = tail;
        bot.userMap.removeUser(oldNick);
        bot.userMap.addUser(source);
        bot.updatePermissions(source);
        if (source == bot && bot.getBoolean(Property.updateNick))
            bot.setValue(Property.nick, source.nick);
        if (bot.hasHandler())
            bot.handler.onNick(bot, source, oldNick);
    }

    private void onNotice(User source, String[] args, String tail) {
        if (bot.hasHandler()) {
            if (args[0].equals(bot.nick))
                bot.handler.onNotice(bot, source, tail);
            else
                bot.handler.onNotice(bot, bot.channelMap.getChannel(args[0]), source, tail);
        }
    }

    private void onPart(User source, String[] args, String tail) {
        ChannelMap map = bot.channelMap;
        Channel channel = map.getChannel(args[0]);
        if (source == bot) {
            map.removeChannel(channel.name);
            for (User user : channel.getUsers()) {
                boolean known = false;
                for (Channel c : map.getChannels())
                    if (c.contains(user))
                        known = true;
                if (!known)
                    bot.userMap.removeUser(user.nick);
                user.known = known;
            }
        } else {
            channel.removeUser(source);
            boolean known = false;
            for (Channel c : map.getChannels())
                if (c.contains(source))
                    known = true;
            if (!known)
                bot.userMap.removeUser(source.nick);
            source.known = known;
        }
        if (bot.hasHandler())
            bot.handler.onPart(bot, channel, source, tail);
    }

    private void onPing(String tail) {
        send("PONG :" + tail);
        if (bot.getBoolean(Property.checkNick) && !bot.nick.equals(bot.getValue(Property.nick)))
            queue("NICK " + bot.getValue(Property.nick));
        if (bot.hasHandler())
            bot.handler.onPing(bot, tail);
    }

    private void onPrivmsg(User source, String[] args, String tail) {
        if (bot.hasHandler()) {
            ChannelMap map = bot.channelMap;
            if (tail.matches("^\\001.+\\001$")) {
                String[] parts = tail.substring(1, tail.length() - 1).split(" ", 2);
                String s = parts.length > 1 ? parts[1] : "";
                if (parts[0].equals("ACTION")) {
                    if (args[0].equals(bot.nick))
                        bot.handler.onAction(bot, source, s);
                    else
                        bot.handler.onAction(bot, map.getChannel(args[0]), source, s);
                } else {
                    if (args[0].equals(bot.nick))
                        bot.handler.onCTCPCommand(bot, source, parts[0], s);
                    else
                        bot.handler.onCTCPCommand(bot, map.getChannel(args[0]), source, parts[0], s);
                }
                return;
            }
            if ((tail.startsWith(bot.getValue(Property.prefix)) || (tail.matches("^" + bot.nick + "[,:]? .+")
                    && bot.getBoolean(Property.enableNickPrefix))) && bot.getBoolean(Property.enableCommands))
                if (onPrivmsgCommand(source, args, tail))
                    return;
            if (args[0].equals(bot.nick))
                bot.handler.onMessage(bot, source, tail);
            else
                bot.handler.onMessage(bot, map.getChannel(args[0]), source, tail);
        }
    }

    private boolean onPrivmsgCommand(User source, String[] args, String tail) {
        String prefix = bot.getValue(Property.prefix);
        if (source.hasPermission(Permission.IGNORE) && !source.hasPermission(Permission.OPERATOR) && bot.getBoolean(Property.enableIgnore))
            return false;
        tail = tail.replaceAll("^" + bot.nick + "[,:]? ", prefix);
        if (bot.getBoolean(Property.removeTrailingSpaces))
            tail = tail.replaceAll(" +$", "");
        else if (tail.endsWith(" ") || tail.endsWith("\"\""))
            tail += " ";
        boolean separate = tail.startsWith(prefix + " ");
        if (separate && !bot.getBoolean(Property.allowSeparatePrefix))
            return false;
        String[] parts = tail.substring(prefix.length() + (separate ? 1 : 0)).split(" ", 2);
        CommandMap map = bot.getCommandMap();
        if (map.isRegistered(parts[0])) {
            String[] cmdArgs = parts.length > 1 ? bot.getBoolean(Property.enableQuoteSplit) ? StringUtils.splitArgs(parts[1]) : parts[1].split(" ") : new String[0];
            if (args[0].equals(bot.nick))
                bot.handler.onCommand(bot, source, map.getCommand(parts[0]), cmdArgs);
            else
                bot.handler.onCommand(bot, bot.channelMap.getChannel(args[0]), source, map.getCommand(parts[0]), cmdArgs);
        } else
            bot.send(new ErrorMessage(source, bot.getValue(Property.unknownCommandMsg)
                    .replace("$COMMAND", parts[0])
                    .replace("$PREFIX", prefix)));
        return true;
    }

    private void onQuit(User source, String tail) {
        for (Channel channel : bot.channelMap.getChannels())
            if (channel.contains(source))
                channel.removeUser(source);
        bot.userMap.removeUser(source.nick);
        source.known = false;
        if (bot.hasHandler())
            bot.handler.onQuit(bot, source, tail);
    }

    private void onTopic(User source, String[] args, String tail) {
        Channel channel = bot.channelMap.getChannel(args[0]);
        channel.topic = tail;
        channel.topicSetter = source.getIdentity();
        if (bot.hasHandler())
            bot.handler.onTopic(bot, channel, source, tail);
    }

    private void on001() {
        bot.state = State.connected;
        String id = bot.getValue(Property.nickservId),
                pass = bot.getValue(Property.nickservPass);
        if (!pass.isEmpty())
            if (id.isEmpty())
                queue("NICKSERV IDENTIFY " + pass);
            else
                queue("NICKSERV IDENTIFY " + id + " " + pass);
        if (bot.getConfiguration() != null)
            for (Element element : bot.getConfiguration().getChildren("channel"))
                bot.send("JOIN " + element.getContent());
        if (bot.hasHandler())
            bot.handler.onConnect(bot);
    }

    private void onX05(Server server, String[] args) {
        if (bot.serverMap.isServer(server.name))
            bot.serverMap.addServer(server);
        for (String arg : args) {
            if (arg.startsWith("CHANMODES=")) {
                String[] modes = arg.split("=")[1].split(",", 2);
                for (char mode : modes[0].toCharArray())
                    server.modes.put(mode, ModeType.list);
                for (char mode : modes[1].replace(",", "").toCharArray())
                    server.modes.put(mode, ModeType.value);
            } else if (arg.startsWith("PREFIX=")) {
                String[] prefixSplit = arg.split("=")[1].split("\\)");
                char[] modes = prefixSplit[0].substring(1).toCharArray(),
                        prefixes = prefixSplit[1].toCharArray();
                if (modes.length != prefixes.length)
                    return;
                for (int i = 0; i < modes.length; i++) {
                    server.modes.put(modes[i], ModeType.status);
                    server.prefixes.put(prefixes[i], modes[i]);
                }
            }
            String[] info = arg.split("=", 2);
            server.data.put(info[0], info.length == 2 ? info[1] : "");
        }
    }

    private void on312(String[] args, String tail) {
        Server server = bot.serverMap.getServer(args[2]);
        if (!bot.serverMap.isServer(args[2]))
            bot.serverMap.addServer(server);
        bot.userMap.getUser(args[1]).server = server;
        server.description = tail;
    }

    public void on351(String[] args) {
        Server server = bot.serverMap.getServer(args[2]);
        if (!bot.serverMap.isServer(args[2]))
            bot.serverMap.addServer(server);
        server.version = args[1];
    }

    private void on352(String[] args, String tail) {
        User user = bot.userMap.getUser(args[5]);
        user.login = args[2];
        user.hostmask = args[3];
        user.realname = tail.substring(2);
        user.server = bot.serverMap.getServer(args[4]);
        user.known = true;
        if (!bot.serverMap.isServer(args[4]))
            bot.serverMap.addServer(user.server);
        Channel channel = bot.channelMap.getChannel(args[1]);
        if (!channel.contains(user))
            channel.addUser(user);
        for (char prefix : args[6].toCharArray())
            if (bot.server.supportsPrefix(prefix))
                channel.addMode(user, bot.server.getMode(prefix));
    }

    private final class HandlerThread extends Thread {

        private boolean current = false;

        @Override
        public void run() {
            current = true;
            int sleepTime = bot.getInteger(Property.sleepTime);
            while (active && current) {
                try {
                    if (incoming.size() > 0)
                        handle(incoming.getNext());
                    else
                        Thread.sleep(sleepTime);
                } catch (Exception e) {
                    bot.logError("An exception (probably) caused by your code has occurred (%s)", e);
                    bot.log("The bot should continue functioning without problems; however, you may want to try fix the issue.");
                    if (bot.getBoolean(Property.logErrorTrace)) {
                        bot.log("Error stack trace:");
                        for (StackTraceElement element : e.getStackTrace())
                            bot.print("<==>", element.toString());
                    }
                }
            }
            bot.log("Handler thread [%s] terminated", getName());
        }
    }

    private final class InputThread extends Thread {

        @Override
        public void run() {
            int sleepTime = bot.getInteger(Property.sleepTime);
            String line;
            try {
                while (active && (line = reader.readLine()) != null) {
                    incoming.add(line);
                    Thread.sleep(sleepTime);
                }
            } catch (Exception e) {
                bot.logError("[Connection:InputThread:run] An internal exception has occurred (%s)", e);
                bot.shutdown("A fatal exception occurred");
                if (bot.getBoolean(Property.reconnect))
                    bot.connect();
            }
            bot.log("Input thread [%s] terminated", getName());
        }
    }

    private final class OutputThread extends Thread {

        @Override
        public void run() {
            int sleepTime = bot.getInteger(Property.sleepTime),
                    messageDelay = bot.getInteger(Property.messageDelay),
                    size = bot.getInteger(Property.queueSize);
            while (active) {
                try {
                    if (size > -1 && outgoing.size() > size) {
                        bot.log("Too much output backlogged (Clearing all messages)");
                        outgoing.clear();
                    }
                    if (outgoing.hasNext()) {
                        send(outgoing.getNext());
                        Thread.sleep(messageDelay);
                    } else
                        Thread.sleep(sleepTime);
                } catch (Exception e) {
                    bot.logError("[Connection:OutputThread:run] An internal exception has occurred (%s)", e);
                }
            }
            bot.log("Output thread [%s] terminated", getName());
        }
    }
}
