package net.pslice.archebot;

import net.pslice.archebot.ArcheBot.Handler;
import net.pslice.archebot.events.*;
import net.pslice.archebot.handlers.*;
import net.pslice.archebot.output.ErrorMessage;
import net.pslice.utilities.Queue;
import net.pslice.utilities.StringUtils;

import java.io.*;
import java.net.Socket;

@SuppressWarnings("unchecked")
final class Connection {

    private final ArcheBot bot;
    private final Socket socket;
    private final OutputThread output;
    private boolean active = false;

    Connection(ArcheBot bot) throws ConnectionException, IOException {
        this.bot = bot;

        String nick = bot.getProperty(Property.nick),
               login = bot.getProperty(Property.login),
               realname = bot.getProperty(Property.realname).replace("{VERSION}", ArcheBot.VERSION).replace("{USER_VERSION}", ArcheBot.USER_VERSION),
               server = bot.getProperty(Property.server),
               serverPass = bot.getProperty(Property.password);
        int port = bot.toInteger(Property.port),
            visibility = bot.toBoolean(Property.visible) ? 0 : 8;

        bot.nick = nick;
        bot.log("Attempting to connect to %s on port %d...", server, port);

        socket = new Socket(server, port);

        bot.log("Connection successful!");

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        InputThread input = new InputThread(reader);
        output = new OutputThread(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

        if (!serverPass.isEmpty())
            output.sendLine("PASS " + serverPass);
        output.sendLine("NICK " + nick);
        output.sendLine("USER " + login + " " + visibility + " * :" + realname);

        String message;
        while ((message = reader.readLine()) != null) {
            handleServerLine(message);

            String[] messageSplit = message.split(" ");

            if (messageSplit[1].equals("001"))
                break;
            else if (messageSplit[1].matches("43[367]")) {
                if (bot.toBoolean(Property.rename)) {
                    bot.logError("Nick rejected (Trying another one...)");
                    bot.nick = (nick += "_");
                    output.sendLine("NICK " + bot);
                } else
                    throw new ConnectionException("Nick unavailable");
            } else if (messageSplit[1].matches("[45]\\d\\d"))
                throw new ConnectionException(StringUtils.compact(messageSplit, 3));
        }
        active = true;

        socket.setSoTimeout(bot.toInteger(Property.timeoutDelay));

        input.start();
        output.start();
    }

    void close() {
        active = false;
        try {
            socket.close();
        } catch (Exception e) {
            bot.logError("An internal exception has occurred (%s)", e);
        }
    }

    void handleServerLine(String line) {
        String[] parts = line.split(" :", 2);
        String[] args = parts[0].split(" ");
        String tail = parts.length > 1 ? parts[1] : "";

        boolean hasSource = args[0].startsWith(":");
        if (hasSource && !args[0].contains("!"))
            args[0] += "!";
        User source = bot.getUser(hasSource ? args[0].substring(1, args[0].indexOf('!')) : bot.server == null ? "" : bot.server.name);
        switch (hasSource ? args[1] : args[0]) {
            case "PING":
                checkLog(Property.logPings, line);
                onPING(tail);
                return;

            case "ERROR":
                checkLog(Property.logGeneric, line);
                active = false;
                bot.disconnect(line.substring(7));
                return;

            case "PRIVMSG":
                checkLog(Property.logMessages, line);
                onPRIVMSG(source, args, tail);
                return;

            case "JOIN":
                checkLog(Property.logJoins, line);
                onJOIN(source, args);
                return;

            case "PART":
                checkLog(Property.logParts, line);
                onPART(source, args, tail);
                return;

            case "NICK":
                checkLog(Property.logNicks, line);
                onNICK(source, tail);
                return;

            case "QUIT":
                checkLog(Property.logQuits, line);
                onQUIT(source, tail);
                return;

            case "MODE":
                checkLog(Property.logModes, line);
                onMODE(source, args, tail);
                return;

            case "NOTICE":
                checkLog(Property.logNotices, line);
                onNOTICE(source, args, tail);
                return;

            case "KICK":
                checkLog(Property.logKicks, line);
                onKICK(source, args, tail);
                return;

            case "TOPIC":
                checkLog(Property.logTopics, line);
                onTOPIC(source, args, tail);
                return;

            case "INVITE":
                checkLog(Property.logInvites, line);
                onINVITE(source, tail);
                return;

            case "PONG":
                onPONG(args, tail);
                break;

            case "004":
                bot.server = bot.getServer(args[3]);
                bot.addServer(bot.server);
                for (char mode : args[5].toCharArray())
                    bot.addServerMode(new Mode(mode, Mode.Type.USER));
                break;

            case "005":
                on005(args);
                break;

            case "105":
                Server s = bot.getServer(source.nick);
                if (!bot.isServer(s.name))
                    bot.addServer(s);
                for (String arg : args) {
                    String[] info = arg.split("=", 2);
                    s.info.put(info[0], info.length == 2 ? info[1] : "");
                }
                break;

            case "311":
                User user = bot.getUser(args[3]);
                user.login = args[4];
                user.hostmask = args[5];
                user.realname = tail;
                break;

            case "312":
                on312(args, tail);
                break;

            case "322":
                if (bot.getChannel(args[3]).totalUsers() != Integer.parseInt(args[4]))
                    bot.send("WHO " + args[3]);
                break;

            case "324":
                Channel channel = bot.getChannel(args[3]);
                for (char ID : args[4].substring(1).toCharArray())
                    channel.addMode(bot.getMode(ID), "");
                break;

            case "332":
                bot.getChannel(args[3]).setTopic(tail);
                break;

            case "333":
                bot.getChannel(args[3]).setTopicSetter(args[4]);
                break;

            case "352":
                on352(args, tail);
                break;

            case "372":
                checkLog(Property.logMOTD, line);
                bot.getServer(source.nick).motd.add(tail);
                return;

            case "375":
                checkLog(Property.logMOTD, line);
                Server server = bot.getServer(source.nick);
                if (!bot.isServer(server.name))
                    bot.addServer(server);
                server.motd.clear();
                return;

            case "376":
                checkLog(Property.logMOTD, line);
                for (Handler handler : bot.getHandlers()) {
                    if (handler instanceof MOTDHandler)
                        ((MOTDHandler) handler).onMOTD(bot, bot.getServer(source.nick));
                    if (handler instanceof MOTDEvent.Handler)
                        ((MOTDEvent.Handler) handler).onMOTD(new MOTDEvent(bot, bot.getServer(source.nick)));
                }
                return;

            default:
                checkLog(Property.logGeneric, line);
                if (args[1].matches("\\d+")) {
                    int ID = Integer.parseInt(args[1]);
                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof RawHandler)
                            ((RawHandler) handler).onLine(bot, ID, line.substring(line.indexOf(ID) + 4));
                        if (handler instanceof RawEvent.Handler)
                            ((RawEvent.Handler) handler).onLine(new RawEvent(bot, ID, line.substring(line.indexOf(ID) + 4)));
                    }
                } else
                    bot.logError("An unknown command was sent by the server (%s).", args[1]);
                return;
        }

        checkLog(Property.logGeneric, line);
    }

    boolean isActive() {
        return active;
    }

    void send(String line, boolean direct) {
        if (direct)
            output.sendLine(line);
        else
            output.addMessageToQueue(line);
    }

    private void checkLog(Property property, String line) {
        if (bot.toBoolean(property))
            bot.print("<-", line);
    }

    private void onINVITE(User source, String tail) {
        for (String channel : tail.split(" "))
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof InviteHandler)
                    ((InviteHandler) handler).onInvite(bot, bot.getChannel(channel), source);
                if (handler instanceof InviteEvent.Handler)
                    ((InviteEvent.Handler) handler).onInvite(new InviteEvent(bot, bot.getChannel(channel), source));
            }
    }

    private void onJOIN(User source, String[] args) {
        Channel channel = bot.getChannel(args[2]);
        channel.addUser(source);

        if (source == bot) {
            bot.addChannel(channel);
            bot.send("WHO " + args[2]);
            bot.send("MODE " + args[2]);
        } else
            bot.send("WHOIS " + source);

        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof JoinHandler)
                ((JoinHandler) handler).onJoin(bot, channel, source);
            if (handler instanceof JoinEvent.Handler)
                ((JoinEvent.Handler) handler).onJoin(new JoinEvent(bot, channel, source));
        }
    }

    private void onKICK(User source, String[] args, String tail) {
        Channel channel = bot.getChannel(args[2]);
        User user = bot.getUser(args[3]);

        channel.removeUser(user);
        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof KickHandler)
                ((KickHandler) handler).onKick(bot, channel, source, user, tail);
            if (handler instanceof KickEvent.Handler)
                ((KickEvent.Handler) handler).onKick(new KickEvent(bot, channel, source, user, tail));
        }
    }

    private void onMODE(User source, String[] args, String tail) {
        Channel channel = bot.getChannel(args[2]);

        if (!tail.isEmpty()) {
            boolean added = tail.startsWith("+");
            for (char c : tail.substring(1).toCharArray()) {
                Mode mode = bot.getUserMode(c);
                if (!mode.isUser())
                    continue;
                if (added)
                    source.addMode(mode);
                else
                    source.removeMode(mode);
                for (Handler handler : bot.getHandlers()) {
                    if (handler instanceof UserModeHandler)
                        ((UserModeHandler) handler).onUserMode(bot, source, mode, added);
                    if (handler instanceof UserModeEvent.Handler)
                        ((UserModeEvent.Handler) handler).onUserMode(new UserModeEvent(bot, source, mode, added));
                }
            }
        } else {
            boolean added = args[3].startsWith("+");
            int i = 4;
            for (char c : args[3].substring(1).toCharArray()) {
                Mode mode = bot.getMode(c);
                String value = args.length >= i + 1 ? args[i++] : "";
                if (mode.isStatus()) {
                    User user = bot.getUser(value);
                    if (added)
                        channel.addMode(user, mode);
                    else
                        channel.removeMode(user, mode);
                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof ModeHandler)
                            ((ModeHandler) handler).onMode(bot, channel, source, user, mode, added);
                        if (handler instanceof StatusModeEvent.Handler)
                            ((StatusModeEvent.Handler) handler).onStatusMode(new StatusModeEvent(bot, channel, source, user, mode, added));
                    }
                } else if (mode.isValue()) {
                    if (added)
                        channel.addMode(mode, value);
                    else
                        channel.removeMode(mode, value);
                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof ModeHandler)
                            ((ModeHandler) handler).onMode(bot, channel, source, mode, value, added);
                        if (handler instanceof StatusModeEvent.Handler)
                            ((ModeEvent.Handler) handler).onMode(new ModeEvent(bot, channel, source, mode, value, added));
                    }
                }
            }
        }
    }

    private void onNICK(User source, String tail) {
        String oldNick = source.nick;

        source.nick = tail;
        bot.removeUser(oldNick);
        bot.addUser(source);
        bot.updatePermissions(source);

        if (source == bot && bot.toBoolean(Property.updateNick))
            bot.setProperty(Property.nick, source.nick);

        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof NickHandler)
                ((NickHandler) handler).onNickChange(bot, source, oldNick);
            if (handler instanceof NickEvent.Handler)
                ((NickEvent.Handler) handler).onNickChange(new NickEvent(bot, source, oldNick));
        }
    }

    private void onNOTICE(User source, String[] args, String tail) {
        if (args[2].equals(bot.nick)) {
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof NoticeHandler)
                    ((NoticeHandler) handler).onPrivateNotice(bot, source, tail);
                if (handler instanceof PrivateNoticeEvent.Handler)
                    ((PrivateNoticeEvent.Handler) handler).onPrivateNotice(new PrivateNoticeEvent(bot, source, tail));
            }
        } else {
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof NoticeHandler)
                    ((NoticeHandler) handler).onNotice(bot, bot.getChannel(args[2]), source, tail);
                if (handler instanceof NoticeEvent.Handler)
                    ((NoticeEvent.Handler) handler).onNotice(new NoticeEvent(bot, bot.getChannel(args[2]), source, tail));
            }
        }
    }

    private void onPART(User source, String[] args, String tail) {
        Channel channel = bot.getChannel(args[2]);
        if (source == bot)
            bot.removeChannel(channel.name);
        else
            channel.removeUser(source);

        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof PartHandler)
                ((PartHandler) handler).onPart(bot, channel, source, tail);
            if (handler instanceof PartEvent.Handler)
                ((PartEvent.Handler) handler).onPart(new PartEvent(bot, channel, source, tail));
        }
    }

    private void onPING(String tail) {
        output.sendLine("PONG :" + tail);
        if (bot.toBoolean(Property.checkNick) && !bot.nick.equals(bot.getProperty(Property.nick)))
            output.addMessageToQueue("NICK " + bot.getProperty(Property.nick));
        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof PingHandler)
                ((PingHandler) handler).onPing(bot);
            if (handler instanceof PingEvent.Handler)
                ((PingEvent.Handler) handler).onPing(new PingEvent(bot));
        }
    }

    private void onPONG(String[] args, String tail) {
        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof PongHandler)
                ((PongHandler) handler).onPong(bot, args[0], tail);
            if (handler instanceof PongEvent.Handler)
                ((PongEvent.Handler) handler).onPong(new PongEvent(bot, args[0],tail));
        }
    }

    private void onPRIVMSG(User source, String[] args, String tail) {
        if (tail.startsWith("\u0001") && tail.endsWith("\u0001")) {
            tail = tail.substring(1, tail.length() - 1);
            String[] parts = tail.split(" ", 2);
            String s = parts.length > 1 ? parts[1] : "";
            if (parts[0].equals("ACTION")) {
                if (args[2].equals(bot.nick))
                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof ActionHandler)
                            ((ActionHandler) handler).onPrivateAction(bot, source, s);
                        if (handler instanceof PrivateActionEvent.Handler)
                            ((PrivateActionEvent.Handler) handler).onPrivateAction(new PrivateActionEvent(bot, source, s));
                    }
                else
                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof ActionHandler)
                            ((ActionHandler) handler).onAction(bot, bot.getChannel(args[2]), source, s);
                        if (handler instanceof ActionEvent.Handler)
                            ((ActionEvent.Handler) handler).onAction(new ActionEvent(bot, bot.getChannel(args[2]), source, s));
                    }
            } else {
                for (Handler handler : bot.getHandlers()) {
                    if (handler instanceof CTCPHandler)
                        ((CTCPHandler) handler).onCTCPCommand(bot, bot.getChannel(args[2]), source, parts[0], s);
                    if (handler instanceof CTCPEvent.Handler)
                        ((CTCPEvent.Handler) handler).onCTCPCommand(new CTCPEvent(bot, bot.getChannel(args[2]), source, parts[0], s));
                }
            }
            return;
        }

        if (tail.startsWith(bot.getProperty(Property.prefix)) && bot.toBoolean(Property.enableCommands)) {
            if (source.hasPermission(Permission.IGNORE) && !source.hasPermission(Permission.OPERATOR) && bot.toBoolean(Property.enableIgnore))
                return;
            boolean separate = tail.startsWith(bot.getProperty(Property.prefix) + " ");
            if (separate && !bot.toBoolean(Property.allowSeparatePrefix))
                return;
            String[] parts = tail.substring(bot.getProperty(Property.prefix).length() + (separate ? 1 : 0)).split(" ", 2);
            if (bot.isRegistered(parts[0])) {
                Channel channel = bot.getChannel(args[2].equals(bot.nick) ? source.nick : args[2]);
                boolean hasCommandHandler = false;
                String[] cmdArgs = parts.length > 1 ? bot.toBoolean(Property.enableQuoteSplit) ? StringUtils.splitArgs(parts[1]) : parts[1].split(" ") : new String[0];
                for (Handler handler : bot.getHandlers()) {
                    if (handler instanceof CommandHandler) {
                        ((CommandHandler) handler).onCommand(bot, channel, source, bot.getCommand(parts[0]), cmdArgs);
                        hasCommandHandler = true;
                    }
                    if (handler instanceof CommandEvent.Handler) {
                        ((CommandEvent.Handler) handler).onCommand(new CommandEvent(bot, channel, source, bot.getCommand(parts[0]), cmdArgs));
                        hasCommandHandler = true;
                    }
                }
                if (!hasCommandHandler) {
                    Command command = bot.getCommand(parts[0]);
                    if (!command.isEnabled() && !source.hasPermission(Permission.OPERATOR))
                        bot.send(new ErrorMessage(source, "That command is not currently enabled."));
                    else if (source.hasPermission(command.getPermission()) || source.hasPermission(Permission.OPERATOR))
                        command.execute(bot, channel, source, cmdArgs);
                    else
                        bot.send(new ErrorMessage(source, "You do not have permission to do that. (Required permission: %s)", command.getPermission()));
                }
            } else
                bot.send(new ErrorMessage(source, "The command ID '%s' is not registered.", parts[0]));
            return;
        }

        if (args[2].equals(bot.nick))
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof MessageHandler)
                    ((MessageHandler) handler).onPrivateMessage(bot, source, tail);
                if (handler instanceof PrivateMessageEvent.Handler)
                    ((PrivateMessageEvent.Handler) handler).onPrivateMessage(new PrivateMessageEvent(bot, source, tail));
            }
        else
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof MessageHandler)
                    ((MessageHandler) handler).onMessage(bot, bot.getChannel(args[2]), source, tail);
                if (handler instanceof MessageEvent.Handler)
                    ((MessageEvent.Handler) handler).onMessage(new MessageEvent(bot, bot.getChannel(args[2]), source, tail));
            }
    }

    private void onQUIT(User source, String tail) {
        for (Channel channel : bot.getChannels())
            if (channel.contains(source))
                channel.removeUser(source);

        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof QuitHandler)
                ((QuitHandler) handler).onQuit(bot, source, tail);
            if (handler instanceof QuitEvent.Handler)
                ((QuitEvent.Handler) handler).onQuit(new QuitEvent(bot, source, tail));
        }

        bot.savePermissions(source);
        bot.removeUser(source.nick);
    }

    private void onTOPIC(User source, String[] args, String tail) {
        Channel channel = bot.getChannel(args[2]);
        channel.setTopic(tail);
        channel.setTopicSetter(source.details());

        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof TopicHandler)
                ((TopicHandler) handler).onTopicSet(bot, channel, source, tail);
            if (handler instanceof TopicEvent.Handler)
                ((TopicEvent.Handler) handler).onTopicChange(new TopicEvent(bot, channel, source, tail));
        }
    }

    private void on005(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("CHANMODES=")) {
                String[] modes = arg.split("=")[1].split(",", 2);
                for (char mode : modes[0].toCharArray())
                    bot.addServerMode(new Mode(mode, Mode.Type.LIST));
                for (char mode : modes[1].replace(",", "").toCharArray())
                    bot.addServerMode(new Mode(mode, Mode.Type.VALUE));
            } else if (arg.startsWith("PREFIX=")) {
                String[] prefixSplit = arg.split("=")[1].split("\\)");
                char[] modes = prefixSplit[0].substring(1).toCharArray(),
                       prefixes = prefixSplit[1].toCharArray();
                if (modes.length != prefixes.length)
                    return;
                for (int i = 0; i < modes.length; i++)
                    bot.addServerMode(new Mode(modes[i], Mode.Type.STATUS, prefixes[i]));
            } else {
                String[] info = arg.split("=", 2);
                bot.server.info.put(info[0], info.length == 2 ? info[1] : "");
            }
        }
    }

    private void on312(String[] args, String tail) {
        Server server = bot.getServer(args[4]);
        if (!bot.isServer(args[4]))
            bot.addServer(server);
        bot.getUser(args[3]).server = server;
        server.description = tail;
    }

    private void on352(String[] args, String tail) {
        User user = bot.getUser(args[7]);

        user.login = args[4];
        user.hostmask = args[5];
        user.realname = tail.substring(2);
        user.server = bot.getServer(args[6]);
        if (!bot.isServer(args[6]))
            bot.addServer(user.server);

        Channel channel = bot.getChannel(args[3]);
        if (!channel.contains(user))
            channel.addUser(user);

        for (char c : args[8].toCharArray())
            for (Mode mode : bot.getAllModes(Mode.Type.STATUS))
                if (mode.getPrefix() == c)
                    channel.addMode(user, mode);
    }

    private final class InputThread extends Thread {

        private final BufferedReader reader;

        InputThread(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            boolean sleep = bot.toBoolean(Property.enableCPURestraint);
            try {
                String line;
                while (active && (line = reader.readLine()) != null) {
                    try {
                        handleServerLine(line);
                        if (sleep)
                            Thread.sleep(50);
                    } catch (Exception e) {
                        bot.logError("An exception (probably) caused by your code has occurred (%s)", e);
                        bot.log("The bot should continue functioning without problems; however, you may want to try fix the issue.");
                        if (bot.toBoolean(Property.logErrorTrace))
                            e.printStackTrace();
                    }
                }
                if (active)
                    bot.disconnect("Connection closed", bot.toBoolean(Property.reconnect));
            } catch (IOException e) {
                bot.logError("An internal exception has occurred (%s)", e);
                bot.disconnect("A fatal exception occurred", bot.toBoolean(Property.reconnect));
            }
        }
    }

    private final class OutputThread extends Thread {

        private final BufferedWriter writer;
        private final Queue<String> queue = new Queue<>();

        OutputThread(BufferedWriter writer) {
            this.writer = writer;
        }

        void sendLine(String line) {
            if (line == null || line.isEmpty())
                return;
            if (line.length() > 510)
                line = line.substring(0, 510);

            synchronized (writer) {
                try {
                    if (bot.toBoolean(Property.enableColorShortcut))
                        line = line.replace("\\&", "\000").replace("&r", "\017").replace("&b", "\002").replace("&", "\003").replace("\000", "&");
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                    if (bot.toBoolean(Property.logOutput))
                        bot.print("->", line);
                } catch (Exception e) {
                    bot.logError("An internal exception has occurred (%s)", e);
                }
            }
        }

        synchronized void addMessageToQueue(String message) {
            queue.addItem(message);
        }

        @Override
        public void run() {
            boolean sleep = bot.toBoolean(Property.enableCPURestraint);
            while (active) {
                if (queue.size() > 1000) {
                    bot.log("Too much output backlogged (Clearing all messages).");
                    queue.clear();
                }

                if (queue.hasNext()) {
                    sendLine(queue.getNext());

                    try {
                        Thread.sleep(bot.toInteger(Property.messageDelay));
                    } catch (Exception e) {
                        bot.logError("An internal exception has occurred (%s)", e);
                    }
                } else if (sleep)
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                        bot.logError("An internal exception has occurred (%s)", e);
                    }
            }
        }
    }

    static final class ConnectionException extends Exception {
        private ConnectionException(String cause) {
            super(cause);
        }
    }
}
