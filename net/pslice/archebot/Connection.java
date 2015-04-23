package net.pslice.archebot;

import net.pslice.archebot.output.ErrorMessage;
import net.pslice.archebot.events.*;
import net.pslice.archebot.handlers.*;
import net.pslice.archebot.ArcheBot.Handler;
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
               realname = bot.getProperty(Property.realname).replace("{VERSION}", ArcheBot.VERSION).replace("{UVERSION}", ArcheBot.USER_VERSION),
               server = bot.getProperty(Property.server),
               serverPass = bot.getProperty(Property.serverPass);
        int port = bot.toInteger(Property.port),
            visibility = bot.toBoolean(Property.visible) ? 0 : 8;

        bot.nick = nick;
        bot.log("Attempting to connect to %s on port %d...", server, port);

        socket = new Socket(server, port);

        bot.log("Connection successful!");

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        InputThread input = new InputThread(reader);
        output = new OutputThread(writer);

        if (!serverPass.isEmpty())
            output.sendLine("PASS " + serverPass);
        output.sendLine("NICK " + nick);
        output.sendLine("USER " + login + " " + visibility + " * :" + realname);

        String message;
        while ((message = reader.readLine()) != null) {
            handleServerLine(message);

            String[] messageSplit = message.split(" ");

            if (messageSplit[1].equals("004"))
                break;

            else if (messageSplit[1].matches("43[367]")) {
                if (bot.toBoolean(Property.rename)) {
                    bot.logError("Nick rejected (Trying another one!)");
                    bot.nick = (nick += "_");
                    output.sendLine("NICK " + bot);
                } else
                    throw new ConnectionException("Nick unavailable!");
            } else if (messageSplit[1].matches("[45]\\d\\d"))
                throw new ConnectionException(StringUtils.compact(messageSplit, 3));
        }
        active = true;
        if (!bot.isUser(bot.nick))
            bot.addUser(bot);

        socket.setSoTimeout(bot.toInteger(Property.timeoutDelay));

        input.start();
        output.start();
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

    void handleServerLine(String line) {
        if (line.startsWith("PING")) {
            checkLog(Property.logPings, line);
            output.sendLine("PONG " + line.substring(5));
            if (bot.toBoolean(Property.checkNick) && !bot.nick.equals(bot.getProperty(Property.nick)))
                send("NICK " + bot.getProperty(Property.nick), false);
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof PingHandler)
                    ((PingHandler) handler).onPing(bot);
                if (handler instanceof PingEvent.Handler)
                    ((PingEvent.Handler) handler).onPing(new PingEvent(bot));
            }
        } else if (line.startsWith("ERROR")) {
            checkLog(Property.logGeneric, line);
            active = false;
            bot.disconnect(line.substring(7));
        } else {
            String[] lineSplit = line.split(" ");
            User source = bot.getUser(lineSplit[0].substring(1).split("!")[0]);
            switch (lineSplit[1]) {
                case "PRIVMSG":
                    checkLog(Property.logMessages, line);
                    this.onPRIVMSG(source, lineSplit);
                    return;

                case "JOIN":
                    checkLog(Property.logJoins, line);
                    this.onJOIN(source, lineSplit);
                    return;

                case "PART":
                    checkLog(Property.logParts, line);
                    this.onPART(source, lineSplit);
                    return;

                case "NICK":
                    checkLog(Property.logNicks, line);
                    this.onNICK(source, lineSplit);
                    return;

                case "QUIT":
                    checkLog(Property.logQuits, line);
                    this.onQUIT(source, lineSplit);
                    return;

                case "MODE":
                    checkLog(Property.logModes, line);
                    this.onMODE(source, lineSplit);
                    return;

                case "NOTICE":
                    checkLog(Property.logNotices, line);
                    this.onNOTICE(source, lineSplit);
                    return;

                case "KICK":
                    checkLog(Property.logKicks, line);
                    this.onKICK(source, lineSplit);
                    return;

                case "TOPIC":
                    checkLog(Property.logTopics, line);
                    this.onTOPIC(source, lineSplit);
                    return;

                case "INVITE":
                    checkLog(Property.logInvites, line);
                    this.onINVITE(source, lineSplit);
                    return;

                case "PONG":
                    this.onPONG(lineSplit);
                    break;

                case "004":
                    this.on004(lineSplit);
                    break;

                case "005":
                    this.on005(lineSplit);
                    break;

                case "311":
                    User user = bot.getUser(lineSplit[3]);
                    user.login = lineSplit[4];
                    user.hostmask = lineSplit[5];
                    user.realname = StringUtils.compact(lineSplit, 7).substring(1);
                    break;

                case "312":
                    bot.getUser(lineSplit[3]).server = lineSplit[4];
                    break;

                case "322":
                    if (bot.getChannel(lineSplit[3]).totalUsers() != Integer.parseInt(lineSplit[4]))
                        bot.send("WHO " + lineSplit[3]);
                    break;

                case "324":
                    Channel channel = bot.getChannel(lineSplit[3]);
                    for (char ID : lineSplit[4].substring(1).toCharArray())
                        channel.addMode(Mode.getMode(ID), "");
                    break;

                case "332":
                    bot.getChannel(lineSplit[3]).setTopic(StringUtils.compact(lineSplit, 4).substring(1));
                    break;

                case "333":
                    bot.getChannel(lineSplit[3]).setTopicSetter(lineSplit[4]);
                    break;

                case "352":
                    this.on352(lineSplit);
                    break;

                default:
                    checkLog(Property.logGeneric, line);
                    if (lineSplit[1].matches("\\d+")) {
                        int ID = Integer.parseInt(lineSplit[1]);
                        for (Handler handler : bot.getHandlers()) {
                            if (handler instanceof RawHandler)
                                ((RawHandler) handler).onLine(bot, ID, line.substring(line.indexOf(ID) + 4));
                            if (handler instanceof RawEvent.Handler)
                                ((RawEvent.Handler) handler).onLine(new RawEvent(bot, ID, line.substring(line.indexOf(ID) + 4)));
                        }
                    } else
                        bot.print("An unknown command was sent by the server (%s).", lineSplit[1]);
                    return;
            }

            checkLog(Property.logGeneric, line);
        }
    }

    void close() {
        active = false;

        try {
            socket.close();
        } catch (Exception e) {
            bot.logError("An internal exception has occurred (%s)", e);
        }
    }

    private void checkLog(Property property, String line) {
        if (bot.toBoolean(property))
            bot.print("<-", line);
    }

    private void onPRIVMSG(User source, String[] lineSplit) {
        String message = StringUtils.compact(lineSplit, 3).substring(1);

        if (message.startsWith("\u0001") && message.endsWith("\u0001")) {
            if (lineSplit[3].substring(2).equals("ACTION")) {
                message = message.substring(8, message.length() - 1);
                if (lineSplit[2].equals(bot.nick))
                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof ActionHandler)
                            ((ActionHandler) handler).onPrivateAction(bot, source, message);
                        if (handler instanceof PrivateActionEvent.Handler)
                            ((PrivateActionEvent.Handler) handler).onPrivateAction(new PrivateActionEvent(bot, source, message));
                    }
                else
                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof ActionHandler)
                            ((ActionHandler) handler).onAction(bot, bot.getChannel(lineSplit[2]), source, message);
                        if (handler instanceof ActionEvent.Handler)
                            ((ActionEvent.Handler) handler).onAction(new ActionEvent(bot, bot.getChannel(lineSplit[2]), source, message));
                    }
            } else {
                String command = lineSplit[3].substring(2);
                for (Handler handler : bot.getHandlers()) {
                    if (handler instanceof CTCPHandler)
                        ((CTCPHandler) handler).onCTCPCommand(bot, bot.getChannel(lineSplit[2]), source, command, message.substring(lineSplit[3].length(), message.length() - 1));
                    if (handler instanceof CTCPEvent.Handler)
                        ((CTCPEvent.Handler) handler).onCTCPCommand(new CTCPEvent(bot, bot.getChannel(lineSplit[2]), source, command, message.substring(lineSplit[3].length(), message.length() - 1)));
                }
            }
            return;
        }

        if (message.startsWith(bot.getProperty(Property.prefix)) && bot.toBoolean(Property.enableCommands)) {
            String ID = lineSplit[3].substring(bot.getProperty(Property.prefix).length() + 1).toLowerCase();

            if (source.hasPermission(Permission.IGNORE) && !source.hasPermission(Permission.OPERATOR) && bot.toBoolean(Property.enableIgnore))
                return;

            if (!ID.isEmpty() || (bot.toBoolean(Property.allowSeparatePrefix) && lineSplit.length > 0)) {
                String[] args;
                if (ID.isEmpty()) {
                    ID = lineSplit[4];

                    args = new String[lineSplit.length - 5];
                    System.arraycopy(lineSplit, 5, args, 0, args.length);
                } else {
                    args = new String[lineSplit.length - 4];
                    System.arraycopy(lineSplit, 4, args, 0, args.length);
                }

                if (bot.isRegistered(ID)) {
                    Channel channel = bot.getChannel(lineSplit[2].equals(bot.nick) ? source.nick : lineSplit[2]);
                    boolean hasCommandListener = false;

                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof CommandHandler) {
                            ((CommandHandler) handler).onCommand(bot, channel, source, bot.getCommand(ID), args);
                            hasCommandListener = true;
                        }
                        if (handler instanceof CommandEvent.Handler) {
                            ((CommandEvent.Handler) handler).onCommand(new CommandEvent(bot, channel, source, bot.getCommand(ID), args));
                            hasCommandListener = true;
                        }
                    }

                    if (!hasCommandListener) {
                        Command command = bot.getCommand(ID);
                        if (!command.isEnabled() && !source.hasPermission(Permission.OPERATOR))
                            bot.send(new ErrorMessage(source, "That command is not currently enabled."));
                        else if (source.hasPermission(command.getPermission()) || source.hasPermission(Permission.OPERATOR))
                            command.execute(bot, channel, source, args);
                        else
                            bot.send(new ErrorMessage(source, "You do not have permission to do that. (Required permission: %s)", command.getPermission()));
                    }
                } else
                    bot.send(new ErrorMessage(source, "The command ID '%s' is not registered.", ID));
                return;
            }
        }

        if (lineSplit[2].equals(bot.nick))
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof MessageHandler)
                    ((MessageHandler) handler).onPrivateMessage(bot, source, message);
                if (handler instanceof PrivateMessageEvent.Handler)
                    ((PrivateMessageEvent.Handler) handler).onPrivateMessage(new PrivateMessageEvent(bot, source, message));
            }
        else
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof MessageHandler)
                    ((MessageHandler) handler).onMessage(bot, bot.getChannel(lineSplit[2]), source, message);
                if (handler instanceof MessageEvent.Handler)
                    ((MessageEvent.Handler) handler).onMessage(new MessageEvent(bot, bot.getChannel(lineSplit[2]), source, message));
            }
    }

    private void onNOTICE(User source, String[] lineSplit) {
        if (lineSplit[2].equals(bot.nick)) {
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof NoticeHandler)
                    ((NoticeHandler) handler).onPrivateNotice(bot, source, StringUtils.compact(lineSplit, 3).substring(1));
                if (handler instanceof PrivateNoticeEvent.Handler)
                    ((PrivateNoticeEvent.Handler) handler).onPrivateNotice(new PrivateNoticeEvent(bot, source, StringUtils.compact(lineSplit, 3).substring(1)));
            }
        } else {
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof NoticeHandler)
                    ((NoticeHandler) handler).onNotice(bot, bot.getChannel(lineSplit[2]), source, StringUtils.compact(lineSplit, 3).substring(1));
                if (handler instanceof NoticeEvent.Handler)
                    ((NoticeEvent.Handler) handler).onNotice(new NoticeEvent(bot, bot.getChannel(lineSplit[2]), source, StringUtils.compact(lineSplit, 3).substring(1)));
            }
        }
    }

    private void onJOIN(User source, String[] lineSplit) {
        Channel channel = bot.getChannel(lineSplit[2]);
        channel.addUser(source);

        if (source == bot) {
            bot.addChannel(channel);
            bot.send("WHO " + lineSplit[2]);
            bot.send("MODE " + lineSplit[2]);
        } else
            bot.send("WHOIS " + source);

        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof JoinHandler)
                ((JoinHandler) handler).onJoin(bot, channel, source);
            if (handler instanceof JoinEvent.Handler)
                ((JoinEvent.Handler) handler).onJoin(new JoinEvent(bot, channel, source));
        }
    }

    private void onPART(User source, String[] lineSplit) {
        Channel channel = bot.getChannel(lineSplit[2]);
        if (source == bot)
            bot.removeChannel(channel.name);
        else
            channel.removeUser(source);

        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof PartHandler)
                ((PartHandler) handler).onPart(bot, channel, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : "");
            if (handler instanceof PartEvent.Handler)
                ((PartEvent.Handler) handler).onPart(new PartEvent(bot, channel, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : ""));
        }
    }

    private void onQUIT(User source, String[] lineSplit) {
        for (Channel channel : bot.getChannels())
            if (channel.contains(source))
                channel.removeUser(source);

        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof QuitHandler)
                ((QuitHandler) handler).onQuit(bot, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : "");
            if (handler instanceof QuitEvent.Handler)
                ((QuitEvent.Handler) handler).onQuit(new QuitEvent(bot, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : ""));
        }

        source.login = "";
        source.hostmask = "";
        source.realname = "";
        source.server = "";
    }

    private void onNICK(User source, String[] lineSplit) {
        String oldNick = source.nick;

        source.nick = lineSplit[2].substring(1);
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

    private void onMODE(User source, String[] lineSplit) {
        Channel channel = bot.getChannel(lineSplit[2]);
        boolean added = lineSplit[3].startsWith("+");
        char[] modes = lineSplit[3].substring(1).toCharArray();

        if (lineSplit[3].startsWith(":")) {
            added = lineSplit[3].startsWith(":+");
            for (char ID : modes) {
                if (!User.Mode.isMode(ID))
                    continue;
                User.Mode mode = User.Mode.getMode(ID);

                if (added) {
                    source.addMode(mode);
                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof UserModeHandler)
                            ((UserModeHandler) handler).onUserModeSet(bot, source, mode);
                        if (handler instanceof UserModeEvent.Handler)
                            ((UserModeEvent.Handler) handler).onUserModeSet(new UserModeEvent(bot, source, mode, true));
                    }
                } else {
                    source.removeMode(mode);
                    for (Handler handler : bot.getHandlers()) {
                        if (handler instanceof UserModeHandler)
                            ((UserModeHandler) handler).onUserModeRemoved(bot, source, mode);
                        if (handler instanceof UserModeEvent.Handler)
                            ((UserModeEvent.Handler) handler).onUserModeSet(new UserModeEvent(bot, source, mode, false));
                    }
                }
            }
        } else {
            int i = 4;
            for (char ID : modes) {
                if (!Mode.isMode(ID))
                    continue;
                Mode mode = Mode.getMode(ID);
                String value = lineSplit.length >= i + 1 ? lineSplit[i++] : "";

                if (mode instanceof Mode.TempMode) {
                    User user = bot.getUser(value);
                    Mode.TempMode tempMode = (Mode.TempMode) mode;
                    if (added) {
                        channel.addMode(user, tempMode);
                        for (Handler handler : bot.getHandlers()) {
                            if (handler instanceof ModeHandler)
                                ((ModeHandler) handler).onModeSet(bot, channel, source, user, tempMode);
                            if (handler instanceof TempModeEvent.Handler)
                                ((TempModeEvent.Handler) handler).onTempModeSet(new TempModeEvent(bot, channel, source, user, tempMode, true));
                        }
                    } else {
                        channel.removeMode(user, tempMode);
                        for (Handler handler : bot.getHandlers()) {
                            if (handler instanceof ModeHandler)
                                ((ModeHandler) handler).onModeRemoved(bot, channel, source, user, tempMode);
                            if (handler instanceof TempModeEvent.Handler)
                                ((TempModeEvent.Handler) handler).onTempModeSet(new TempModeEvent(bot, channel, source, user, tempMode, false));
                        }
                    }
                } else {
                    if (added) {
                        channel.addMode(mode, value);
                        for (Handler handler : bot.getHandlers()) {
                            if (handler instanceof ModeHandler)
                                ((ModeHandler) handler).onModeSet(bot, channel, source, mode, value);
                            if (handler instanceof ModeEvent.Handler)
                                ((ModeEvent.Handler) handler).onModeSet(new ModeEvent(bot, channel, source, mode, value, true));
                        }
                    } else {
                        channel.removeMode(mode, value);
                        for (Handler handler : bot.getHandlers()) {
                            if (handler instanceof ModeHandler)
                                ((ModeHandler) handler).onModeRemoved(bot, channel, source, mode, value);
                            if (handler instanceof ModeEvent.Handler)
                                ((ModeEvent.Handler) handler).onModeSet(new ModeEvent(bot, channel, source, mode, value, false));
                        }
                    }
                }
            }
        }
    }

    private void onKICK(User source, String[] lineSplit) {
        Channel channel = bot.getChannel(lineSplit[2]);
        User user = bot.getUser(lineSplit[3]);

        channel.removeUser(user);
        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof KickHandler)
                ((KickHandler) handler).onKick(bot, channel, source, user, StringUtils.compact(lineSplit, 3).substring(1));
            if (handler instanceof KickEvent.Handler)
                ((KickEvent.Handler) handler).onKick(new KickEvent(bot, channel, source, user, StringUtils.compact(lineSplit, 3).substring(1)));
        }
    }

    private void onTOPIC(User source, String[] lineSplit) {
        Channel channel = bot.getChannel(lineSplit[2]);
        String topic = StringUtils.compact(lineSplit, 3).substring(1);
        channel.setTopic(topic);
        channel.setTopicSetter(source.details());

        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof TopicHandler)
                ((TopicHandler) handler).onTopicSet(bot, channel, source, topic);
            if (handler instanceof TopicEvent.Handler)
                ((TopicEvent.Handler) handler).onTopicChange(new TopicEvent(bot, channel, source, topic));
        }
    }

    private void onINVITE(User source, String[] lineSplit) {
        for (String chan : StringUtils.compact(lineSplit, 3).substring(1).split(" "))
            for (Handler handler : bot.getHandlers()) {
                if (handler instanceof InviteHandler)
                    ((InviteHandler) handler).onInvite(bot, bot.getChannel(chan), source);
                if (handler instanceof InviteEvent.Handler)
                    ((InviteEvent.Handler) handler).onInvite(new InviteEvent(bot, bot.getChannel(chan), source));
            }
    }

    private void onPONG(String[] lineSplit) {
        String message = StringUtils.compact(lineSplit, 1).substring(1);
        for (Handler handler : bot.getHandlers()) {
            if (handler instanceof PongHandler)
                ((PongHandler) handler).onPong(bot, lineSplit[0], message);
            if (handler instanceof PongEvent.Handler)
                ((PongEvent.Handler) handler).onPong(new PongEvent(bot, lineSplit[0], message));
        }
    }

    private void on352(String[] lineSplit) {
        User user = bot.getUser(lineSplit[7]);

        user.login = lineSplit[4];
        user.hostmask = lineSplit[5];
        user.realname = StringUtils.compact(lineSplit, 10);
        user.server = lineSplit[6];

        Channel channel = bot.getChannel(lineSplit[3]);
        if (!channel.contains(user))
            channel.addUser(user);

        for (char c : lineSplit[8].toCharArray())
            for (Mode mode : Mode.getModes())
                if (mode instanceof Mode.TempMode && ((Mode.TempMode) mode).getPrefix() == c)
                    channel.addMode(user, (Mode.TempMode) mode);
    }

    private void on004(String[] lineSplit) {
        char[] userModes = lineSplit[5].toCharArray();
        for (char mode : userModes)
            new User.Mode(mode);
    }

    private void on005(String[] lineSplit) {
        lineSplit = StringUtils.compact(lineSplit).replaceAll(" :.*$", "").split(" ");
        for (String arg : lineSplit) {
            if (arg.startsWith("CHANMODES=")) {
                String[] modes = arg.split("=")[1].split(",", 2);
                for (char mode : modes[0].toCharArray())
                    new Mode.PermaMode(mode);
                for (char mode : modes[1].toCharArray())
                    new Mode.ValueMode(mode);
            } else if (arg.startsWith("PREFIX=")) {
                String[] prefixSplit = arg.split("=")[1].split("\\)");
                char[] modes = prefixSplit[0].substring(1).toCharArray(),
                        prefixes = prefixSplit[1].toCharArray();
                if (modes.length != prefixes.length)
                    return;
                for (int i = 0; i < modes.length; i++)
                    new Mode.TempMode(modes[i], prefixes[i]);
            } else {
                String[] info = arg.split("=");
                String value = info.length > 1 ? info[1] : "";
                bot.setServerInfo(info[0], value);
            }
        }
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
                        bot.log("An exception (probably) caused by your code has occurred (%s)", e);
                        bot.logError("The bot should continue functioning without problems; however, you may want to try fix the issue.");
                        if (bot.toBoolean(Property.printErrorTrace))
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

        private final PrintWriter writer;
        private final Queue<String> queue = new Queue<>();

        OutputThread(PrintWriter writer) {
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
                    writer.println(line);
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
