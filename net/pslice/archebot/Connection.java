package net.pslice.archebot;

import net.pslice.archebot.ArcheBot.Handler;
import net.pslice.archebot.events.*;
import net.pslice.archebot.handlers.*;
import net.pslice.archebot.output.ErrorMessage;
import net.pslice.utilities.Queue;
import net.pslice.utilities.StringUtils;

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
    private final Queue<String> outgoing = new Queue<>(),
            incoming = new Queue<>();
    private HandlerThread handler = new HandlerThread();

    Connection(ArcheBot bot) throws Exception{
        this.bot = bot;

        String nick = bot.get(Property.nick),
                server = bot.get(Property.server),
                password = bot.get(Property.password);
        int port = bot.toInteger(Property.port);

        bot.nick = nick;
        bot.log("Attempting to connect to %s on port %d...", server, port);
        socket = new Socket(server, port);
        bot.log("Connection successful!");

        socket.setSoTimeout(bot.toInteger(Property.timeoutDelay));

        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        if (!password.isEmpty())
            send("PASS " + password, false);
        send("NICK " + nick, false);
        send(String.format("USER %s %d * :%s", bot.get(Property.login),
                bot.toBoolean(Property.visible) ? 0 : 8,
                bot.get(Property.realname).replace("$VERSION", ArcheBot.VERSION).replace("$USER_VERSION", bot.USER_VERSION)),
                false);

        handler.start();
        new OutputThread().start();
        new InputThread().start();
    }

    void breakThread() {
        handler.active = false;
        handler = new HandlerThread();
        handler.start();
    }

    void close() {
        bot.state = State.disconnecting;
        try {
            socket.close();
        } catch (Exception e) {
            bot.logError("An internal exception has occurred (%s)", e);
        }
    }

    synchronized void send(String line, boolean queue) {
        if (line == null || line.isEmpty())
            return;

        if (queue)
            outgoing.add(line);
        else {
            int size = bot.toInteger(Property.lineLength);
            if (line.length() > size)
                line = line.substring(0, size);
            try {
                if (bot.toBoolean(Property.enableColorShortcut))
                    line = line.replace("\\&", "\000").replace("&r", "\017").replace("&b", "\002").replace("&", "\003").replace("\000", "&");
                writer.write(line);
                writer.newLine();
                writer.flush();
                if (bot.toBoolean(Property.logOutput))
                    bot.print("->", line.replaceAll("\\002|\\003\\d+(,\\d+)?|\\017", ""));
            } catch (Exception e) {
                bot.logError("[Connection:send] An internal exception has occurred (%s)", e);
            }
        }
    }

    private void checkLog(Property property, String line) {
        if (bot.toBoolean(property))
            bot.print("<-", line.replaceAll("\\002|\\003\\d+(,\\d+)?|\\017", ""));
    }

    private void handle(String line) {
        String[] parts = (line.startsWith(":") ? line.substring(1) : (bot.server == null ? "" : bot.server) + " " + line).split(" :", 2);
        String tail = parts.length > 1 ? parts[1] : "";
        String[] split = parts[0].split(" ");
        if (split[0].contains("!"))
            split[0] = split[0].substring(0, split[0].indexOf('!'));
        User source = bot.getUser(split[0]);
        String command = split[1];
        String[] args = new String[split.length - 2];
        System.arraycopy(split, 2, args, 0, args.length);

        switch (command.toUpperCase()) {
            case "ERROR": {
                checkLog(Property.logErrors, line);
                if (bot.state != State.disconnecting) {
                    bot.state = State.disconnecting;
                    bot.shutdown(tail);
                }
                break; }
            case "INVITE": onInvite(source, tail, line); break;
            case "JOIN": onJoin(source, args, line); break;
            case "KICK": onKick(source, args, tail, line); break;
            case "MODE": onMode(source, args, tail, line); break;
            case "NICK": onNick(source, tail, line); break;
            case "NOTICE": onNotice(source, args, tail, line); break;
            case "PART": onPart(source, args, tail, line); break;
            case "PING": onPing(tail, line); break;
            case "PONG": onPong(args, tail, line); break;
            case "PRIVMSG": onPrivmsg(source, args, tail, line); break;
            case "QUIT": onQuit(source, tail, line); break;
            case "TOPIC": onTopic(source, args, tail, line); break;
            case "001": on001(line); break;
            case "004": {
                checkLog(Property.logGeneric, line);
                bot.server = bot.getServer(args[1]);
                bot.addServer(bot.server);
                for (char mode : args[3].toCharArray())
                    bot.server.userModes.add(mode);
                break; }
            case "005": on005(args, line); break;
            case "105": on105(source, args, line); break;
            case "311": {
                checkLog(Property.logGeneric, line);
                User user = bot.getUser(args[1]);
                user.login = args[2];
                user.hostmask = args[3];
                user.realname = tail;
                break; }
            case "312": on312(args, tail, line); break;
            case "322": {
                checkLog(Property.logGeneric, line);
                if (bot.getChannel(args[1]).totalUsers() != Integer.parseInt(args[2]))
                    bot.send("WHO " + args[1]);
                break; }
            case "324": {
                checkLog(Property.logGeneric, line);
                Channel channel = bot.getChannel(args[1]);
                for (char ID : args[2].substring(1).toCharArray())
                    channel.addMode(ID, "");
                break; }
            case "330": {
                checkLog(Property.logGeneric, line);
                source.nickservID = args[2];
                break; }
            case "332":
                checkLog(Property.logGeneric, line);
                bot.getChannel(args[1]).setTopic(tail);
                break;
            case "333":
                checkLog(Property.logGeneric, line);
                bot.getChannel(args[1]).setTopicSetter(args[2]);
                break;
            case "352": on352(args, tail, line); break;
            case "372": {
                checkLog(Property.logMOTD, line);
                bot.getServer(source.nick).motd.add(tail);
                break; }
            case "375": {
                checkLog(Property.logMOTD, line);
                Server server = bot.getServer(source.nick);
                if (!bot.isServer(server.name))
                    bot.addServer(server);
                server.motd.clear();
                break; }
            case "376": {
                checkLog(Property.logMOTD, line);
                Server server = bot.getServer(source.nick);
                for (Handler handler : bot.handlers) {
                    if (handler instanceof MOTDHandler)
                        ((MOTDHandler) handler).onMOTD(bot, server);
                    if (handler instanceof MOTDEvent.Handler)
                        ((MOTDEvent.Handler) handler).onMOTD(new MOTDEvent(bot, server));
                }
                break; }
            default:
                if (command.matches("[45]\\d\\d")) {
                    checkLog(Property.logErrors, line);
                    if (bot.state == State.connecting) {
                        if (command.matches("43[367]")) {
                            if (bot.toBoolean(Property.rename)) {
                                bot.logError("Nick rejected (Trying another one...)");
                                bot.nick += "_";
                                send("NICK " + bot, false);
                            } else
                                bot.shutdown("Nick unavailable");
                        } else
                            bot.shutdown(tail);
                    }
                } else
                    checkLog(Property.logGeneric, line);
                if (command.matches("\\d+")) {
                    int ID = Integer.parseInt(command);
                    for (Handler handler : bot.handlers) {
                        if (handler instanceof CodeHandler)
                            ((net.pslice.archebot.handlers.CodeHandler) handler).onCode(bot, ID, args, tail);
                        if (handler instanceof CodeEvent.Handler)
                            ((CodeEvent.Handler) handler).onLine(new CodeEvent(bot, ID, args, tail));
                    }
                } else
                    bot.logError("An unknown command was sent by the server (%s).", command);
                break;
        }

        for (Handler handler : bot.handlers) {
            if (handler instanceof LineHandler)
                ((LineHandler) handler).onLine(bot, source, command, args, tail);
            if (handler instanceof LineEvent.Handler)
                ((LineEvent.Handler) handler).onLine(new LineEvent(bot, source, command, args, tail));
        }
    }

    private void onInvite(User source, String tail, String line) {
        checkLog(Property.logInvites, line);

        for (String channel : tail.split(" "))
            for (Handler handler : bot.handlers) {
                if (handler instanceof InviteHandler)
                    ((InviteHandler) handler).onInvite(bot, bot.getChannel(channel), source);
                if (handler instanceof InviteEvent.Handler)
                    ((InviteEvent.Handler) handler).onInvite(new InviteEvent(bot, bot.getChannel(channel), source));
            }
    }

    private void onJoin(User source, String[] args, String line) {
        checkLog(Property.logJoins, line);
        Channel channel = bot.getChannel(args[0]);
        channel.addUser(source);
        if (source == bot) {
            bot.addChannel(channel);
            bot.send("WHO " + args[0]);
            bot.send("MODE " + args[0]);
        } else
            bot.send("WHOIS " + source);

        for (Handler handler : bot.handlers) {
            if (handler instanceof JoinHandler)
                ((JoinHandler) handler).onJoin(bot, channel, source);
            if (handler instanceof JoinEvent.Handler)
                ((JoinEvent.Handler) handler).onJoin(new JoinEvent(bot, channel, source));
        }
    }

    private void onKick(User source, String[] args, String tail, String line) {
        checkLog(Property.logKicks, line);
        Channel channel = bot.getChannel(args[0]);
        User user = bot.getUser(args[1]);
        channel.removeUser(user);
        for (Handler handler : bot.handlers) {
            if (handler instanceof KickHandler)
                ((KickHandler) handler).onKick(bot, channel, source, user, tail);
            if (handler instanceof KickEvent.Handler)
                ((KickEvent.Handler) handler).onKick(new KickEvent(bot, channel, source, user, tail));
        }
    }

    private void onMode(User source, String[] args, String tail, String line) {
        checkLog(Property.logModes, line);
        Channel channel = bot.getChannel(args[0]);
        if (!tail.isEmpty()) {
            boolean added = tail.startsWith("+");
            for (char mode : tail.substring(1).toCharArray()) {
                if (added)
                    source.addMode(mode);
                else
                    source.removeMode(mode);
                for (Handler handler : bot.handlers) {
                    if (handler instanceof UserModeHandler)
                        ((UserModeHandler) handler).onUserMode(bot, source, mode, added);
                    if (handler instanceof UserModeEvent.Handler)
                        ((UserModeEvent.Handler) handler).onUserMode(new UserModeEvent(bot, source, mode, added));
                }
            }
        } else {
            boolean added = args[1].startsWith("+");
            int i = 2;
            for (char mode : args[1].substring(1).toCharArray()) {
                String value = args.length >= i + 1 ? args[i++] : "";
                ModeType type = bot.server.getModeType(mode);
                if (type == ModeType.status) {
                    User user = bot.getUser(value);
                    if (added)
                        channel.addMode(user, mode);
                    else
                        channel.removeMode(user, mode);
                    for (Handler handler : bot.handlers) {
                        if (handler instanceof ModeHandler)
                            ((ModeHandler) handler).onMode(bot, channel, source, user, mode, added);
                        if (handler instanceof StatusModeEvent.Handler)
                            ((StatusModeEvent.Handler) handler).onStatusMode(new StatusModeEvent(bot, channel, source, user, mode, added));
                    }
                } else if (type == ModeType.value) {
                    if (added)
                        channel.addMode(mode, value);
                    else
                        channel.removeMode(mode);
                    for (Handler handler : bot.handlers) {
                        if (handler instanceof ModeHandler)
                            ((ModeHandler) handler).onMode(bot, channel, source, mode, value, added);
                        if (handler instanceof StatusModeEvent.Handler)
                            ((ModeEvent.Handler) handler).onMode(new ModeEvent(bot, channel, source, mode, value, added));
                    }
                }
            }
        }
    }

    private void onNick(User source, String tail, String line) {
        checkLog(Property.logNicks, line);
        String oldNick = source.nick;
        source.nick = tail;
        bot.removeUser(oldNick);
        bot.addUser(source);
        bot.updatePermissions(source);
        if (source == bot && bot.toBoolean(Property.updateNick))
            bot.set(Property.nick, source.nick);
        for (Handler handler : bot.handlers) {
            if (handler instanceof NickHandler)
                ((NickHandler) handler).onNickChange(bot, source, oldNick);
            if (handler instanceof NickEvent.Handler)
                ((NickEvent.Handler) handler).onNickChange(new NickEvent(bot, source, oldNick));
        }
    }

    private void onNotice(User source, String[] args, String tail, String line) {
        checkLog(Property.logNotices, line);
        if (args[0].equals(bot.nick)) {
            for (Handler handler : bot.handlers) {
                if (handler instanceof NoticeHandler)
                    ((NoticeHandler) handler).onPrivateNotice(bot, source, tail);
                if (handler instanceof PrivateNoticeEvent.Handler)
                    ((PrivateNoticeEvent.Handler) handler).onPrivateNotice(new PrivateNoticeEvent(bot, source, tail));
            }
        } else {
            for (Handler handler : bot.handlers) {
                if (handler instanceof NoticeHandler)
                    ((NoticeHandler) handler).onNotice(bot, bot.getChannel(args[0]), source, tail);
                if (handler instanceof NoticeEvent.Handler)
                    ((NoticeEvent.Handler) handler).onNotice(new NoticeEvent(bot, bot.getChannel(args[0]), source, tail));
            }
        }
    }

    private void onPart(User source, String[] args, String tail, String line) {
        checkLog(Property.logParts, line);
        Channel channel = bot.getChannel(args[0]);
        if (source == bot)
            bot.removeChannel(channel.name);
        else
            channel.removeUser(source);
        for (Handler handler : bot.handlers) {
            if (handler instanceof PartHandler)
                ((PartHandler) handler).onPart(bot, channel, source, tail);
            if (handler instanceof PartEvent.Handler)
                ((PartEvent.Handler) handler).onPart(new PartEvent(bot, channel, source, tail));
        }
    }

    private void onPing(String tail, String line) {
        checkLog(Property.logPings, line);
        send("PONG :" + tail, false);
        if (bot.toBoolean(Property.checkNick) && !bot.nick.equals(bot.get(Property.nick)))
            send("NICK " + bot.get(Property.nick), false);
        for (Handler handler : bot.handlers) {
            if (handler instanceof PingHandler)
                ((PingHandler) handler).onPing(bot);
            if (handler instanceof PingEvent.Handler)
                ((PingEvent.Handler) handler).onPing(new PingEvent(bot));
        }
    }

    private void onPong(String[] args, String tail, String line) {
        checkLog(Property.logGeneric, line);
        for (Handler handler : bot.handlers) {
            if (handler instanceof PongHandler)
                ((PongHandler) handler).onPong(bot, args[0], tail);
            if (handler instanceof PongEvent.Handler)
                ((PongEvent.Handler) handler).onPong(new PongEvent(bot, args[0], tail));
        }
    }

    private void onPrivmsg(User source, String[] args, String tail, String line) {
        checkLog(Property.logMessages, line);
        if (tail.startsWith("\001") && tail.endsWith("\001")) {
            tail = tail.substring(1, tail.length() - 1);
            String[] parts = tail.split(" ", 2);
            String s = parts.length > 1 ? parts[1] : "";
            if (parts[0].equals("ACTION")) {
                if (args[0].equals(bot.nick))
                    for (Handler handler : bot.handlers) {
                        if (handler instanceof ActionHandler)
                            ((ActionHandler) handler).onPrivateAction(bot, source, s);
                        if (handler instanceof PrivateActionEvent.Handler)
                            ((PrivateActionEvent.Handler) handler).onPrivateAction(new PrivateActionEvent(bot, source, s));
                    }
                else
                    for (Handler handler : bot.handlers) {
                        if (handler instanceof ActionHandler)
                            ((ActionHandler) handler).onAction(bot, bot.getChannel(args[0]), source, s);
                        if (handler instanceof ActionEvent.Handler)
                            ((ActionEvent.Handler) handler).onAction(new ActionEvent(bot, bot.getChannel(args[0]), source, s));
                    }
            } else {
                for (Handler handler : bot.handlers) {
                    if (handler instanceof CTCPHandler)
                        ((CTCPHandler) handler).onCTCPCommand(bot, bot.getChannel(args[0]), source, parts[0], s);
                    if (handler instanceof CTCPEvent.Handler)
                        ((CTCPEvent.Handler) handler).onCTCPCommand(new CTCPEvent(bot, bot.getChannel(args[0]), source, parts[0], s));
                }
            }
            return;
        }

        if ((tail.startsWith(bot.get(Property.prefix)) || (tail.startsWith(bot.nick + ", ") && bot.toBoolean(Property.enableNickPrefix))) && bot.toBoolean(Property.enableCommands)) {
            if (source.has(Permission.IGNORE) && !source.has(Permission.OPERATOR) && bot.toBoolean(Property.enableIgnore))
                return;
            tail = tail.replaceAll("^" + bot.nick + ", ", bot.get(Property.prefix));
            boolean separate = tail.startsWith(bot.get(Property.prefix) + " ");
            if (separate && !bot.toBoolean(Property.allowSeparatePrefix))
                return;
            String[] parts = tail.substring(bot.get(Property.prefix).length() + (separate ? 1 : 0)).split(" ", 2);
            if (bot.isRegistered(parts[0])) {
                Channel channel = bot.getChannel(args[0].equals(bot.nick) ? source.nick : args[0]);
                boolean hasCommandHandler = false;
                String[] cmdArgs = parts.length > 1 ? bot.toBoolean(Property.enableQuoteSplit) ? StringUtils.splitArgs(parts[1]) : parts[1].split(" ") : new String[0];
                for (Handler handler : bot.handlers) {
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
                    if (!command.isEnabled() && !source.has(Permission.OPERATOR))
                        bot.send(new ErrorMessage(source, "That command is not currently enabled."));
                    else if (source.has(command.getPermission()) || source.has(Permission.OPERATOR))
                        command.execute(bot, channel, source, cmdArgs);
                    else
                        bot.send(new ErrorMessage(source, "You do not have permission to do that. (Required permission: %s)", command.getPermission()));
                }
            } else
                bot.send(new ErrorMessage(source, bot.get(Property.unknownCommandMsg).replace("$COMMAND", parts[0]).replace("$PREFIX", bot.get(Property.prefix))));
            return;
        }

        if (args[0].equals(bot.nick))
            for (Handler handler : bot.handlers) {
                if (handler instanceof MessageHandler)
                    ((MessageHandler) handler).onPrivateMessage(bot, source, tail);
                if (handler instanceof PrivateMessageEvent.Handler)
                    ((PrivateMessageEvent.Handler) handler).onPrivateMessage(new PrivateMessageEvent(bot, source, tail));
            }
        else
            for (Handler handler : bot.handlers) {
                if (handler instanceof MessageHandler)
                    ((MessageHandler) handler).onMessage(bot, bot.getChannel(args[0]), source, tail);
                if (handler instanceof MessageEvent.Handler)
                    ((MessageEvent.Handler) handler).onMessage(new MessageEvent(bot, bot.getChannel(args[0]), source, tail));
            }
    }

    private void onQuit(User source, String tail, String line) {
        checkLog(Property.logQuits, line);
        if (source == bot) {
            bot.state = State.disconnecting;
            bot.shutdown(tail);
            return;
        }
        for (Channel channel : bot.getChannels())
            if (channel.contains(source))
                channel.removeUser(source);
        for (Handler handler : bot.handlers) {
            if (handler instanceof QuitHandler)
                ((QuitHandler) handler).onQuit(bot, source, tail);
            if (handler instanceof QuitEvent.Handler)
                ((QuitEvent.Handler) handler).onQuit(new QuitEvent(bot, source, tail));
        }
        bot.savePermissions(source);
        bot.removeUser(source.nick);
    }

    private void onTopic(User source, String[] args, String tail, String line) {
        checkLog(Property.logTopics, line);
        Channel channel = bot.getChannel(args[0]);
        channel.setTopic(tail);
        channel.setTopicSetter(source.details());
        for (Handler handler : bot.handlers) {
            if (handler instanceof TopicHandler)
                ((TopicHandler) handler).onTopicSet(bot, channel, source, tail);
            if (handler instanceof TopicEvent.Handler)
                ((TopicEvent.Handler) handler).onTopicChange(new TopicEvent(bot, channel, source, tail));
        }
    }

    private void on001(String line) {
        checkLog(Property.logGeneric, line);
        bot.state = State.connected;
        if (!bot.get(Property.nickservPass).isEmpty())
            if (bot.get(Property.nickservID).isEmpty())
                send("NICKSERV IDENTIFY " + bot.get(Property.nickservPass), true);
            else
                send("NICKSERV IDENTIFY " + bot.get(Property.nickservID) + " " + bot.get(Property.nickservPass), true);
        if (!bot.get(Property.channels).isEmpty())
            for (String channel : StringUtils.breakList(bot.get(Property.channels)))
                send("JOIN " + channel, true);
        for (Handler handler : bot.handlers) {
            if (handler instanceof ConnectionHandler)
                ((ConnectionHandler) handler).onConnect(bot);
            if (handler instanceof ConnectEvent.Handler)
                ((ConnectEvent.Handler) handler).onConnect(new ConnectEvent(bot));
        }
    }

    private void on005(String[] args, String line) {
        checkLog(Property.logGeneric, line);
        Server server = bot.server;
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
                    server.prefixes.put(modes[i], prefixes[i]);
                }
            }
            String[] info = arg.split("=", 2);
            bot.server.info.put(info[0], info.length == 2 ? info[1] : "");
        }
    }

    private void on105(User source, String[] args, String line) {
        checkLog(Property.logGeneric, line);
        Server server = bot.getServer(source.nick);
        if (!bot.isServer(server.name))
            bot.addServer(server);
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
                    server.prefixes.put(modes[i], prefixes[i]);
                }
            }
            String[] info = arg.split("=", 2);
            bot.server.info.put(info[0], info.length == 2 ? info[1] : "");
        }
    }

    private void on312(String[] args, String tail, String line) {
        checkLog(Property.logGeneric, line);
        Server server = bot.getServer(args[2]);
        if (!bot.isServer(args[2]))
            bot.addServer(server);
        bot.getUser(args[1]).server = server;
        server.description = tail;
    }

    private void on352(String[] args, String tail, String line) {
        checkLog(Property.logGeneric, line);
        User user = bot.getUser(args[5]);
        user.login = args[2];
        user.hostmask = args[3];
        user.realname = tail.substring(2);
        user.server = bot.getServer(args[4]);
        if (!bot.isServer(args[4]))
            bot.addServer(user.server);
        Channel channel = bot.getChannel(args[1]);
        if (!channel.contains(user))
            channel.addUser(user);
        for (char prefix : args[6].toCharArray())
            if (bot.server.supportsPrefix(prefix))
                channel.addMode(user, bot.server.getMode(prefix));
    }

    private final class HandlerThread extends Thread {

        private boolean active = false;

        @Override
        public void run() {
            active = true;
            int sleepTime = bot.toInteger(Property.sleepTime);
            while ((bot.state == net.pslice.archebot.State.connected ||
                    bot.state == net.pslice.archebot.State.connecting) && active) {
                try {
                    if (incoming.size() > 0)
                        handle(incoming.getNext());
                    else
                        Thread.sleep(sleepTime);
                } catch (Exception e) {
                    bot.logError("An exception (probably) caused by your code has occurred (%s)", e);
                    bot.log("The bot should continue functioning without problems; however, you may want to try fix the issue.");
                    if (bot.toBoolean(Property.logErrorTrace)) {
                        bot.log("Error stack trace:");
                        for (StackTraceElement element : e.getStackTrace())
                            bot.log(element.toString());
                    }
                }
            }
        }
    }

    private final class InputThread extends Thread {

        @Override
        public void run() {
            int sleepTime = bot.toInteger(Property.sleepTime);
            String line;
            try {
                while ((bot.state == net.pslice.archebot.State.connected ||
                        bot.state == net.pslice.archebot.State.connecting) &&
                        (line = reader.readLine()) != null) {
                    incoming.add(line);
                    Thread.sleep(sleepTime);
                }
            } catch (Exception e) {
                bot.logError("[Connection:InputThread:run] An internal exception has occurred (%s)", e);
                bot.state = net.pslice.archebot.State.disconnecting;
                bot.shutdown("A fatal exception occurred");
                if (bot.toBoolean(Property.reconnect))
                    bot.connect();
            }
        }
    }

    private final class OutputThread extends Thread {

        @Override
        public void run() {
            int sleepTime = bot.toInteger(Property.sleepTime);
            while (bot.state == net.pslice.archebot.State.connected ||
                    bot.state == net.pslice.archebot.State.connecting) {
                try {
                    if (outgoing.size() > bot.toInteger(Property.queueSize)) {
                        bot.log("Too much output backlogged (Clearing all messages).");
                        outgoing.clear();
                    }
                    if (outgoing.hasNext()) {
                        send(outgoing.getNext(), false);
                        Thread.sleep(bot.toInteger(Property.messageDelay));
                    } else
                        Thread.sleep(sleepTime);
                } catch (Exception e) {
                    bot.logError("[Connection:OutputThread:run] An internal exception has occurred (%s)", e);
                }
            }
        }
    }
}
