package net.pslice.archebot;

import net.pslice.archebot.output.ErrorMessage;
import net.pslice.archebot.output.RawMessage;
import net.pslice.archebot.events.*;
import net.pslice.archebot.listeners.*;
import net.pslice.archebot.ArcheBot.Listener;
import net.pslice.archebot.ArcheBot.Property;
import net.pslice.utilities.Queue;
import net.pslice.utilities.StringUtils;

import java.io.*;
import java.net.Socket;

@SuppressWarnings("unchecked")
final class Connection {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // Instance of the bot
    private final ArcheBot bot;

    // The socket being used to connect to the server
    private final Socket socket;

    // The thread handling all output
    private final OutputThread output;

    // The status of the connection
    private boolean active = false;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    Connection(ArcheBot bot) throws ConnectionException, IOException
    {
        this.bot = bot;

        String nick       = bot.getProperty(Property.nick),
               login      = bot.getProperty(Property.login),
               realname   = bot.getProperty(Property.realname).replace("{VERSION}", ArcheBot.VERSION).replace("{UVERSION}", ArcheBot.USER_VERSION),
               server     = bot.getProperty(Property.server),
               serverPass = bot.getProperty(Property.serverPass);
        int    port       = bot.toInteger(Property.port);

        bot.setNick(nick);
        bot.log("Attempting to connect to %s on port %d...", server, port);

        socket = new Socket(server, port);

        bot.log("Connection successful!");

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        InputThread input = new InputThread(reader);
        output = new OutputThread(writer);

        if (!serverPass.equals(""))
            output.sendLine("PASS " + serverPass);
        output.sendLine("NICK " + nick);
        output.sendLine("USER " + login + " 8 * :" + realname);

        String message;
        while ((message = reader.readLine()) != null)
        {
            handleServerLine(message);

            String[] messageSplit = message.split(" ");

            if (messageSplit[1].equals("004"))
                break;

            else if (messageSplit[1].matches("43[367]"))
            {
                if (bot.toBoolean(Property.rename))
                {
                    bot.logError("Nick rejected (Trying another one!)");
                    bot.setNick(nick += "_");
                    output.sendLine("NICK " + bot.getNick());
                }

                else
                    throw new ConnectionException("Nick unavailable!");
            }

            else if (messageSplit[1].matches("[45]\\d\\d"))
                throw new ConnectionException(StringUtils.compact(messageSplit, 3));
        }
        active = true;
        if (!bot.isUser(bot.getNick()))
            bot.addUser(bot);

        socket.setSoTimeout(bot.toInteger(Property.timeoutDelay));

        input.start();
        output.start();
    }

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    boolean isActive()
    {
        return active;
    }

    void send(String line, boolean direct)
    {
        if (direct)
            output.sendLine(line);
        else
            output.addMessageToQueue(line);
    }

    void handleServerLine(String line)
    {
        if (line.startsWith("PING"))
        {
            if (bot.toBoolean(Property.logPings))
                bot.log(0, line);
            output.sendLine("PONG " + line.substring(5));
            for (Listener listener : bot.getListeners())
            {
                if (listener instanceof PingListener)
                    ((PingListener) listener).onPing(bot);
                if (listener instanceof PingEvent.Listener)
                    ((PingEvent.Listener) listener).onPing(new PingEvent(bot));
            }
        }

        else if (line.startsWith("ERROR"))
        {
            if (bot.toBoolean(Property.logGeneric))
                bot.log(0, line);
            active = false;
            bot.disconnect(line.substring(7));
        }

        else
        {
            String[] lineSplit = line.split(" ");

            User source = bot.getUser(lineSplit[0].substring(1).split("!")[0]);

            switch (lineSplit[1])
            {
                case "PRIVMSG":
                    if (bot.toBoolean(Property.logMessages))
                        bot.log(0, line);
                    this.onPRIVMSG(source, lineSplit);
                    return;

                case "JOIN":
                    if (bot.toBoolean(Property.logJoins))
                        bot.log(0, line);
                    this.onJOIN(source, lineSplit);
                    return;

                case "PART":
                    if (bot.toBoolean(Property.logParts))
                        bot.log(0, line);
                    this.onPART(source, lineSplit);
                    return;

                case "NICK":
                    if (bot.toBoolean(Property.logNicks))
                        bot.log(0, line);
                    this.onNICK(source, lineSplit);
                    return;

                case "QUIT":
                    if (bot.toBoolean(Property.logQuits))
                        bot.log(0, line);
                    this.onQUIT(source, lineSplit);
                    break;

                case "MODE":
                    if (bot.toBoolean(Property.logModes))
                        bot.log(0, line);
                    this.onMODE(source, lineSplit);
                    return;

                case "NOTICE":
                    if (bot.toBoolean(Property.logNotices))
                        bot.log(0, line);
                    this.onNOTICE(source, lineSplit);
                    return;

                case "KICK":
                    if (bot.toBoolean(Property.logKicks))
                        bot.log(0, line);
                    this.onKICK(source, lineSplit);
                    return;

                case "TOPIC":
                    if (bot.toBoolean(Property.logTopics))
                        bot.log(0, line);
                    this.onTOPIC(source, lineSplit);
                    return;

                case "INVITE":
                    if (bot.toBoolean(Property.logInvites))
                        bot.log(0, line);
                    this.onINVITE(source, lineSplit);
                    return;

                case "004":
                    this.on004(lineSplit);
                    break;

                case "005":
                    this.on005(lineSplit);
                    break;

                case "311":
                    User user = bot.getUser(lineSplit[3]);
                    user.setLogin(lineSplit[4]);
                    user.setHostmask(lineSplit[5]);
                    user.setRealname(StringUtils.compact(lineSplit, 7).substring(1));
                    break;

                case "312":
                    bot.getUser(lineSplit[3]).setServer(lineSplit[4]);
                    break;

                case "322":
                    if (bot.getChannel(lineSplit[3]).totalUsers() != Integer.parseInt(lineSplit[4]))
                        bot.send(new RawMessage("WHO " + lineSplit[3]));
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
                    if (bot.toBoolean(Property.logGeneric))
                        bot.log(0, line);
                    if (lineSplit[1].matches("\\d+"))
                    {
                        int ID = Integer.parseInt(lineSplit[1]);
                        for (Listener listener : bot.getListeners())
                        {
                            if (listener instanceof RawListener)
                                ((RawListener) listener).onLine(bot, ID, line.substring(line.indexOf("" + ID) + 4));
                            if (listener instanceof RawEvent.Listener)
                                ((RawEvent.Listener) listener).onLine(new RawEvent(bot, ID, line.substring(line.indexOf("" + ID) + 4)));
                        }
                    }
                    else
                        bot.log("An unknown command was sent by the server (%s).", lineSplit[1]);
                    return;
            }

            if (bot.toBoolean(Property.logGeneric))
                bot.log(0, line);
        }
    }

    void close()
    {
        active = false;

        try
        {
            socket.close();
        }

        catch (Exception e)
        {
            bot.logError("An internal exception has occurred (%s)", e);
        }
    }

    /*
     * =======================================
     * Private methods:
     * =======================================
     */

    private void onPRIVMSG(User source, String[] lineSplit)
    {
        String message = StringUtils.compact(lineSplit, 3).substring(1);

        if (message.startsWith("\u0001") && message.endsWith("\u0001"))
        {
            if (lineSplit[3].substring(2).equals("ACTION"))
            {
                message = message.substring(8, message.length() - 1);
                if (lineSplit[2].equals(bot.getNick()))
                    for (Listener listener : bot.getListeners())
                    {
                        if (listener instanceof ActionListener)
                            ((ActionListener) listener).onPrivateAction(bot, source, message);
                        if (listener instanceof PrivateActionEvent.Listener)
                            ((PrivateActionEvent.Listener) listener).onPrivateAction(new PrivateActionEvent(bot, source, message));
                    }
                else
                    for (Listener listener : bot.getListeners())
                    {
                        if (listener instanceof ActionListener)
                            ((ActionListener) listener).onAction(bot, bot.getChannel(lineSplit[2]), source, message);
                        if (listener instanceof ActionEvent.Listener)
                            ((ActionEvent.Listener) listener).onAction(new ActionEvent(bot, bot.getChannel(lineSplit[2]), source, message));
                    }
            }

            else
            {
                String command = lineSplit[3].substring(2);
                for (Listener listener : bot.getListeners())
                {
                    if (listener instanceof CTCPListener)
                        ((CTCPListener) listener).onCTCPCommand(bot, bot.getChannel(lineSplit[2]), source, command, message.substring(lineSplit[3].length(), message.length() - 1));
                    if (listener instanceof CTCPEvent.Listener)
                        ((CTCPEvent.Listener) listener).onCTCPCommand(new CTCPEvent(bot, bot.getChannel(lineSplit[2]), source, command, message.substring(lineSplit[3].length(), message.length() - 1)));
                }
            }
            return;
        }

        if (message.startsWith(bot.getProperty(Property.prefix)) && bot.toBoolean(Property.enableCommands))
        {
            String ID = lineSplit[3].substring(bot.getProperty(Property.prefix).length() + 1).toLowerCase();

            if (!ID.equals("") || (bot.toBoolean(Property.allowSeparatePrefix) && lineSplit.length > 0))
            {
                String[] args;
                if (ID.equals(""))
                {
                    ID = lineSplit[4];
                    args = new String[lineSplit.length - 5];
                    System.arraycopy(lineSplit, 5, args, 0, args.length);
                }

                else
                {
                    args = new String[lineSplit.length - 4];
                    System.arraycopy(lineSplit, 4, args, 0, args.length);
                }

                if (bot.isRegistered(ID))
                {
                    Channel channel = bot.getChannel(lineSplit[2].equals(bot.getNick()) ? source.getNick() : lineSplit[2]);
                    boolean hasCommandListener = false;

                    for (Listener listener : bot.getListeners())
                    {
                        if (listener instanceof CommandListener)
                        {
                            ((CommandListener) listener).onCommand(bot, channel, source, bot.getCommand(ID), args);
                            hasCommandListener = true;
                        }
                        if (listener instanceof CommandEvent.Listener)
                        {
                            ((CommandEvent.Listener) listener).onCommand(new CommandEvent(bot, channel, source, bot.getCommand(ID), args));
                            hasCommandListener = true;
                        }
                    }

                    if (!hasCommandListener)
                    {
                        Command command = bot.getCommand(ID);
                        if (!command.isEnabled() && !source.hasPermission(User.Permission.OPERATOR))
                            bot.send(new ErrorMessage(source, "That command is not currently enabled."));
                        else if (source.hasPermission(command.getPermission()) || source.hasPermission(User.Permission.OPERATOR))
                            command.execute(bot, channel, source, args);
                        else
                            bot.send(new ErrorMessage(source, "You do not have permission to do that. (Required permission: %s)", command.getPermission()));
                    }
                }

                else
                    bot.send(new ErrorMessage(source, "The command ID '%s' is not registered.", ID));
                return;
            }
        }

        if (lineSplit[2].equals(bot.getNick()))
            for (Listener listener : bot.getListeners())
            {
                if (listener instanceof MessageListener)
                    ((MessageListener) listener).onPrivateMessage(bot, source, message);
                if (listener instanceof PrivateMessageEvent.Listener)
                    ((PrivateMessageEvent.Listener) listener).onPrivateMessage(new PrivateMessageEvent(bot, source, message));
            }

        else
            for (Listener listener : bot.getListeners())
            {
                if (listener instanceof MessageListener)
                    ((MessageListener) listener).onMessage(bot, bot.getChannel(lineSplit[2]), source, message);
                if (listener instanceof MessageEvent.Listener)
                    ((MessageEvent.Listener) listener).onMessage(new MessageEvent(bot, bot.getChannel(lineSplit[2]), source, message));
            }
    }

    private void onNOTICE(User source, String[] lineSplit)
    {
        if (lineSplit[2].equals(bot.getNick()))
        {
            for (Listener listener : bot.getListeners())
            {
                if (listener instanceof NoticeListener)
                    ((NoticeListener) listener).onPrivateNotice(bot, source, StringUtils.compact(lineSplit, 3).substring(1));
                if (listener instanceof PrivateNoticeEvent.Listener)
                    ((PrivateNoticeEvent.Listener) listener).onPrivateNotice(new PrivateNoticeEvent(bot, source, StringUtils.compact(lineSplit, 3).substring(1)));
            }
        }

        else
        {
            for (Listener listener : bot.getListeners())
            {
                if (listener instanceof NoticeListener)
                    ((NoticeListener) listener).onNotice(bot, bot.getChannel(lineSplit[2]), source, StringUtils.compact(lineSplit, 3).substring(1));
                if (listener instanceof NoticeEvent.Listener)
                    ((NoticeEvent.Listener) listener).onNotice(new NoticeEvent(bot, bot.getChannel(lineSplit[2]), source, StringUtils.compact(lineSplit, 3).substring(1)));
            }
        }
    }

    private void onJOIN(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        channel.addUser(source);

        if (source.equals(bot))
        {
            bot.addChannel(channel);
            bot.send(new RawMessage("WHO " + lineSplit[2]));
            bot.send(new RawMessage("MODE " + lineSplit[2]));
        }

        else
            bot.send(new RawMessage("WHOIS " + source.getNick()));

        for (Listener listener : bot.getListeners())
        {
            if (listener instanceof JoinListener)
                ((JoinListener) listener).onJoin(bot, channel, source);
            if (listener instanceof JoinEvent.Listener)
                ((JoinEvent.Listener) listener).onJoin(new JoinEvent(bot, channel, source));
        }
    }

    private void onPART(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        if (source.equals(bot))
            bot.removeChannel(channel.name);
        else
            channel.removeUser(source);

        for (Listener listener : bot.getListeners())
        {
            if (listener instanceof PartListener)
                ((PartListener) listener).onPart(bot, channel, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : "");
            if (listener instanceof PartEvent.Listener)
                ((PartEvent.Listener) listener).onPart(new PartEvent(bot, channel, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : ""));
        }
    }

    private void onQUIT(User source, String[] lineSplit)
    {
        for (Channel channel : bot.getChannels())
            if (channel.contains(source))
                channel.removeUser(source);

        for (Listener listener : bot.getListeners())
        {
            if (listener instanceof QuitListener)
                ((QuitListener) listener).onQuit(bot, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : "");
            if (listener instanceof QuitEvent.Listener)
                ((QuitEvent.Listener) listener).onQuit(new QuitEvent(bot, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : ""));
        }

        source.setLogin("");
        source.setHostmask("");
        source.setRealname("");
        source.setServer("");
    }

    private void onNICK(User source, String[] lineSplit)
    {
        String oldNick = source.getNick();
        String newNick = lineSplit[2].substring(1);

        source.setNick(newNick);
        bot.removeUser(oldNick);
        bot.addUser(source);

        if (oldNick.equals(bot.getNick()))
            bot.setNick(newNick);
        else
            bot.reload();

        for (Listener listener : bot.getListeners())
        {
            if (listener instanceof NickChangeListener)
                ((NickChangeListener) listener).onNickChange(bot, source, oldNick);
            if (listener instanceof NickEvent.Listener)
                ((NickEvent.Listener) listener).onNickChange(new NickEvent(bot, source, oldNick));
        }
    }

    private void onMODE(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        boolean added = lineSplit[3].startsWith("+");
        char[] modes = lineSplit[3].substring(1).toCharArray();

        if (lineSplit[3].startsWith(":"))
        {
            added = lineSplit[3].startsWith(":+");
            for (char ID : modes)
            {
                if (!User.Mode.isMode(ID))
                    continue;
                User.Mode mode = User.Mode.getMode(ID);

                if (added)
                {
                    source.addMode(mode);
                    for (Listener listener : bot.getListeners())
                    {
                        if (listener instanceof UserModeListener)
                            ((UserModeListener) listener).onUserModeSet(bot, source, mode);
                        if (listener instanceof UserModeEvent.Listener)
                            ((UserModeEvent.Listener) listener).onUserModeSet(new UserModeEvent(bot, source, mode, true));
                    }
                }
                else
                {
                    source.removeMode(mode);
                    for (Listener listener : bot.getListeners())
                    {
                        if (listener instanceof UserModeListener)
                            ((UserModeListener) listener).onUserModeRemoved(bot, source, mode);
                        if (listener instanceof UserModeEvent.Listener)
                            ((UserModeEvent.Listener) listener).onUserModeSet(new UserModeEvent(bot, source, mode, false));
                    }
                }
            }
        }

        else
        {
            int i = 4;
            for (char ID : modes)
            {
                if (!Mode.isMode(ID))
                    continue;
                Mode mode = Mode.getMode(ID);
                String value = lineSplit.length >= i + 1 ? lineSplit[i++] : "";

                if (mode instanceof Mode.TempMode)
                {
                    User user = bot.getUser(value);
                    Mode.TempMode tempMode = (Mode.TempMode) mode;
                    if (added)
                    {
                        channel.addMode(user, tempMode);
                        for (Listener listener : bot.getListeners())
                        {
                            if (listener instanceof ModeListener)
                                ((ModeListener) listener).onModeSet(bot, channel, source, user, tempMode);
                            if (listener instanceof TempModeEvent.Listener)
                                ((TempModeEvent.Listener) listener).onTempModeSet(new TempModeEvent(bot, channel, source, user, tempMode, true));
                        }
                    }
                    else
                    {
                        channel.removeMode(user, tempMode);
                        for (Listener listener : bot.getListeners())
                        {
                            if (listener instanceof ModeListener)
                                ((ModeListener) listener).onModeRemoved(bot, channel, source, user, tempMode);
                            if (listener instanceof TempModeEvent.Listener)
                                ((TempModeEvent.Listener) listener).onTempModeSet(new TempModeEvent(bot, channel, source, user, tempMode, false));
                        }
                    }
                }

                else
                {
                    if (added)
                    {
                        channel.addMode(mode, value);
                        for (Listener listener : bot.getListeners())
                        {
                            if (listener instanceof ModeListener)
                                ((ModeListener) listener).onModeSet(bot, channel, source, mode, value);
                            if (listener instanceof ModeEvent.Listener)
                                ((ModeEvent.Listener) listener).onModeSet(new ModeEvent(bot, channel, source, mode, value, true));
                        }
                    }

                    else
                    {
                        channel.removeMode(mode, value);
                        for (Listener listener : bot.getListeners())
                        {
                            if (listener instanceof ModeListener)
                                ((ModeListener) listener).onModeRemoved(bot, channel, source, mode, value);
                            if (listener instanceof ModeEvent.Listener)
                                ((ModeEvent.Listener) listener).onModeSet(new ModeEvent(bot, channel, source, mode, value, false));
                        }
                    }
                }
            }
        }
    }

    private void onKICK(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        User user = bot.getUser(lineSplit[3]);

        channel.removeUser(user);
        for (Listener listener : bot.getListeners())
        {
            if (listener instanceof KickListener)
                ((KickListener) listener).onKick(bot, channel, source, user, StringUtils.compact(lineSplit, 3).substring(1));
            if (listener instanceof KickEvent.Listener)
                ((KickEvent.Listener) listener).onKick(new KickEvent(bot, channel, source, user, StringUtils.compact(lineSplit, 3).substring(1)));
        }
    }

    private void onTOPIC(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        String topic = StringUtils.compact(lineSplit, 3).substring(1);
        channel.setTopic(topic);
        channel.setTopicSetter("" + source);

        for (Listener listener : bot.getListeners())
        {
            if (listener instanceof TopicListener)
                ((TopicListener) listener).onTopicSet(bot, channel, source, topic);
            if (listener instanceof TopicEvent.Listener)
                ((TopicEvent.Listener) listener).onTopicChange(new TopicEvent(bot, channel, source, topic));
        }
    }

    private void onINVITE(User source, String[] lineSplit)
    {
        for (String chan : StringUtils.compact(lineSplit, 3).substring(1).split(" "))
            for (Listener listener : bot.getListeners())
            {
                if (listener instanceof InviteListener)
                    ((InviteListener) listener).onInvite(bot, bot.getChannel(chan), source);
                if (listener instanceof InviteEvent.Listener)
                    ((InviteEvent.Listener) listener).onInvite(new InviteEvent(bot, bot.getChannel(chan), source));
            }
    }

    private void on352(String[] lineSplit)
    {
        User user = bot.getUser(lineSplit[7]);
        if (bot.isUser(lineSplit[7]))
            bot.addUser(user);

        user.setLogin(lineSplit[4]);
        user.setHostmask(lineSplit[5]);
        user.setRealname(StringUtils.compact(lineSplit, 10));
        user.setServer(lineSplit[6]);

        Channel channel = bot.getChannel(lineSplit[3]);
        if (!channel.contains(user))
            channel.addUser(user);

        for (char c : lineSplit[8].toCharArray())
            for (Mode mode : Mode.getModes())
                if (mode instanceof Mode.TempMode && ((Mode.TempMode) mode).getPrefix() == c)
                    channel.addMode(user, (Mode.TempMode) mode);
    }

    private void on004(String[] lineSplit)
    {
        char[] userModes = lineSplit[5].toCharArray();
        for (char mode : userModes)
            new User.Mode(mode);
    }

    private void on005(String[] lineSplit)
    {
        for (String arg : lineSplit)
        {
            if (arg.startsWith("CHANMODES="))
            {
                String[] modes = arg.split("=")[1].split(",");
                for (char mode : modes[0].toCharArray())
                    new Mode.PermaMode(mode);
                for (char mode : StringUtils.compact(modes, 1, "").toCharArray())
                    new Mode.ValueMode(mode);
            }

            else if (arg.startsWith("PREFIX="))
            {
                String[] prefixSplit = arg.split("=")[1].split("\\)");
                char[] modes = prefixSplit[0].substring(1).toCharArray(),
                       prefixes = prefixSplit[1].toCharArray();
                if (modes.length != prefixes.length)
                    return;
                for (int i = 0; i < modes.length; i++)
                    new Mode.TempMode(modes[i], prefixes[i]);
            }

            else
            {
                String[] info = arg.split("=");
                String value = info.length > 1 ? info[1] : "";
                bot.setServerInfo(info[0], value);
            }

        }
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    private final class InputThread extends Thread
    {

        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        // The reader for socket input
        private final BufferedReader reader;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        InputThread(BufferedReader reader)
        {
            this.reader = reader;
        }

        /*
         * =======================================
         * Overridden methods:
         * =======================================
         */

        @Override
        public void run()
        {
            try
            {
                String line;
                while (active && (line = reader.readLine()) != null)
                {
                    try
                    {
                        handleServerLine(line);
                    }

                    catch (Exception e)
                    {
                        bot.log("An exception (probably) caused by your code has occurred (%s)", e);
                        bot.logError("The bot should continue functioning without problems; however, you may want to try fix the issue.");
                        if (bot.toBoolean(Property.printErrorTrace))
                            e.printStackTrace();
                    }
                }
                if (active)
                    bot.disconnect("Connection closed", bot.toBoolean(Property.reconnect));
            }

            catch (IOException e)
            {
                bot.logError("An internal exception has occurred (%s)", e);
                bot.disconnect("A fatal exception occurred!", bot.toBoolean(Property.reconnect));
            }
        }
    }

    private final class OutputThread extends Thread
    {

        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        // The writer for socket output
        private final PrintWriter writer;

        // The queue of output lines
        private final Queue<String> queue = new Queue<>();

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        OutputThread(PrintWriter writer)
        {
            this.writer = writer;
        }

        /*
         * =======================================
         * Local methods:
         * =======================================
         */

        void sendLine(String line)
        {
            if (line == null || line.isEmpty())
                return;
            if (line.length() > 510)
                line = line.substring(0, 510);

            synchronized (writer)
            {
                try
                {
                    writer.println(line.replace("\\&", "\000").replace("&r", "\017").replace("&b", "\002").replace("&", "\003").replace("\000", "&"));
                    if (bot.toBoolean(Property.logOutput))
                        bot.log(1, line);
                }

                catch (Exception e)
                {
                    bot.logError("An internal exception has occurred (%s)", e);
                }
            }
        }

        synchronized void addMessageToQueue(String message)
        {
            queue.addItem(message);
        }

        /*
         * =======================================
         * Overridden methods:
         * =======================================
         */

        @Override
        public void run()
        {
            while (active)
            {
                if (queue.size() > 1000)
                {
                    bot.log("Too much output backlogged (Clearing all messages).");
                    queue.clear();
                }

                if (queue.hasNext())
                {
                    sendLine(queue.getNext());

                    try
                    {
                        Thread.sleep(bot.toInteger(Property.messageDelay));
                    }

                    catch (Exception e)
                    {
                        bot.logError("An internal exception has occurred (%s)", e);
                    }
                }
            }
        }
    }

    static final class ConnectionException extends Exception
    {
        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        private ConnectionException(String cause)
        {
            super(cause);
        }
    }
}
