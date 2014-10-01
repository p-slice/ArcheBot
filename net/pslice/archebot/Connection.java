package net.pslice.archebot;

import net.pslice.archebot.actions.ErrorAction;
import net.pslice.archebot.actions.RawAction;
import net.pslice.archebot.listeners.*;
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

    // The wait time (in milliseconds) between messages sent to the server
    private final int messageDelay;

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
        int    port       = Integer.parseInt(bot.getProperty(Property.port));
        messageDelay      = Integer.parseInt(bot.getProperty(Property.messageDelay));

        bot.setNick(nick);
        bot.log(2, "Attempting to connect to " + server + " on port " + port + "...");

        socket = new Socket(server, port);

        bot.log(2, "Connection successful!");

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

            else if (messageSplit[1].equals("433"))
            {
                if (StringUtils.toBoolean(bot.getProperty(Property.rename)))
                {
                    bot.log(3, "Nick already in use (Trying another one!)");
                    bot.setNick(nick += "_");
                    output.sendLine("NICK " + bot.getNick());
                }

                else
                    throw new ConnectionException("Nick already in use!");
            }

            else if (messageSplit[1].startsWith("4") || messageSplit[1].startsWith("5"))
                throw new ConnectionException(StringUtils.compact(messageSplit, 3).substring(1));
        }
        active = true;
        if (!bot.isUser(bot.getNick()))
            bot.addUser(bot);

        if (bot.getProperty(Property.timeoutDelay).matches("\\d+"))
            bot.setProperty(Property.timeoutDelay, 240000);
        socket.setSoTimeout(Integer.parseInt(bot.getProperty(Property.timeoutDelay)));

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
        bot.log(0, line);
        if (line.startsWith("PING"))
        {
            output.sendLine("PONG " + line.substring(5));
            for (Listener listener : bot.getListeners())
                if (listener instanceof PingListener)
                    ((PingListener) listener).onPing(bot);
        }

        else if (line.startsWith("ERROR"))
        {
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
                    this.PRIVMSG(source, lineSplit);
                    break;

                case "JOIN":
                    this.JOIN(source, lineSplit);
                    break;

                case "PART":
                    this.PART(source, lineSplit);
                    break;

                case "NICK":
                    this.NICK(source, lineSplit);
                    break;

                case "QUIT":
                    this.QUIT(source, lineSplit);
                    break;

                case "MODE":
                    this.MODE(source, lineSplit);
                    break;

                case "NOTICE":
                    this.NOTICE(source, lineSplit);
                    break;

                case "KICK":
                    this.KICK(source, lineSplit);
                    break;

                case "TOPIC":
                    this.TOPIC(source, lineSplit);
                    break;

                case "INVITE":
                    this.INVITE(source, lineSplit);
                    break;

                case "311":
                    this.on311(lineSplit);
                    break;

                case "312":
                    bot.getUser(lineSplit[3]).setServer(lineSplit[4]);
                    break;

                case "322":
                    this.on322(lineSplit);
                    break;

                case "324":
                    this.on324(lineSplit);
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
                    int ID = Integer.parseInt(lineSplit[1]);
                    for (Listener listener : bot.getListeners())
                        if (listener instanceof RawListener)
                            ((RawListener) listener).onLine(bot, ID, line.substring(line.indexOf("" + ID) + 4));
                    break;
            }
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
            bot.log(3, String.format(ArcheBot.exception_message, e.toString()));
        }
    }

    /*
     * =======================================
     * Private methods:
     * =======================================
     */

    private void PRIVMSG(User source, String[] lineSplit)
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
                    }
                else
                    for (Listener listener : bot.getListeners())
                    {
                        if (listener instanceof ActionListener)
                            ((ActionListener) listener).onAction(bot, bot.getChannel(lineSplit[2]), source, message);
                    }
            }

            else
            {
                String command = lineSplit[3].substring(2);
                for (Listener listener : bot.getListeners())
                    if (listener instanceof CTCPListener)
                        ((CTCPListener) listener).onCTCPCommand(bot, bot.getChannel(lineSplit[2]), source, command, message.substring(2 + command.length(), message.length() - 1));
            }
        }

        else if (message.startsWith(bot.getProperty(ArcheBot.Property.prefix)))
        {
            String ID = lineSplit[3].substring(bot.getProperty(ArcheBot.Property.prefix).length() + 1).toLowerCase();
            if (bot.isRegistered(ID))
            {
                String[] args = new String[lineSplit.length - 4];
                System.arraycopy(lineSplit, 4, args, 0, args.length);

                Channel channel = bot.getChannel(lineSplit[2].equals(bot.getNick()) ? source.getNick() : lineSplit[2]);

                for (Listener listener : bot.getListeners())
                    if (listener instanceof CommandListener)
                        ((CommandListener) listener).onCommand(bot, channel, source, bot.getCommand(ID), args);
            }

            else
                bot.send(new ErrorAction(source, String.format("The command ID '%s' is not registered!", ID)));
        }

        else
        {
            if (lineSplit[2].equals(bot.getNick()))
                for (Listener listener : bot.getListeners())
                {
                    if (listener instanceof MessageListener)
                        ((MessageListener) listener).onPrivateMessage(bot, source, message);
                }
            else
                for (Listener listener : bot.getListeners())
                {
                    if (listener instanceof MessageListener)
                        ((MessageListener) listener).onMessage(bot, bot.getChannel(lineSplit[2]), source, message);
                }
        }
    }

    private void NOTICE(User source, String[] lineSplit)
    {
        if (lineSplit[2].equals(bot.getNick()))
        {
            for (Listener listener : bot.getListeners())
                if (listener instanceof NoticeListener)
                    ((NoticeListener) listener).onPrivateNotice(bot, source, StringUtils.compact(lineSplit, 3).substring(1));
        }

        else
        {
            for (Listener listener : bot.getListeners())
                if (listener instanceof NoticeListener)
                    ((NoticeListener) listener).onNotice(bot, bot.getChannel(lineSplit[2]), source, StringUtils.compact(lineSplit, 3).substring(1));
        }
    }

    private void JOIN(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        channel.addUser(source);

        if (source.equals(bot))
        {
            bot.addChannel(channel);
            bot.send(new RawAction("WHO " + lineSplit[2]));
            bot.send(new RawAction("MODE " + lineSplit[2]));
        }

        else
            bot.send(new RawAction("WHOIS " + source.getNick()));

        for (Listener listener : bot.getListeners())
            if (listener instanceof JoinListener)
                ((JoinListener) listener).onJoin(bot, channel, source);
    }

    private void PART(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        if (source.equals(bot))
            bot.removeChannel(channel.name);
        else
            channel.removeUser(source);

        for (Listener listener : bot.getListeners())
            if (listener instanceof PartListener)
                ((PartListener) listener).onPart(bot, channel, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : "");
    }

    private void QUIT(User source, String[] lineSplit)
    {
        for (Channel channel : bot.getChannels())
            if (channel.contains(source))
                channel.removeUser(source);
        bot.removeUser(source.getNick());

        for (Listener listener : bot.getListeners())
            if (listener instanceof QuitListener)
                ((QuitListener) listener).onQuit(bot, source, lineSplit.length > 3 ? StringUtils.compact(lineSplit, 3).substring(1) : "");
    }

    private void NICK(User source, String[] lineSplit)
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
            if (listener instanceof NickChangeListener)
                ((NickChangeListener) listener).onNickChange(bot, source, oldNick);
    }

    private void MODE(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        boolean added = lineSplit[3].startsWith("+");
        char[] modes = lineSplit[3].substring(1).toCharArray();

        if (lineSplit[3].startsWith(":"))
        {
            added = lineSplit[3].startsWith(":+");
            for (char ID : modes)
            {
                if (ID == '+' || ID == '-')
                    continue;
                User.Mode mode = User.Mode.getMode(ID);

                if (added)
                {
                    source.addMode(mode);
                    for (Listener listener : bot.getListeners())
                        if (listener instanceof UserModeListener)
                            ((UserModeListener) listener).onUserModeSet(bot, source, mode);
                }
                else
                {
                    source.removeMode(mode);
                    for (Listener listener : bot.getListeners())
                        if (listener instanceof UserModeListener)
                            ((UserModeListener) listener).onUserModeRemoved(bot, source, mode);
                }
            }
        }

        else
        {
            int i = 4;
            for (char ID : modes)
            {
                Mode mode = Mode.getMode(ID);
                String value = lineSplit.length == i + 1 ? lineSplit[i++] : "";

                if (mode instanceof Mode.TempMode)
                {
                    User user = bot.getUser(value);
                    if (added)
                    {
                        channel.addMode(user, (Mode.TempMode) mode);
                        for (Listener listener : bot.getListeners())
                            if (listener instanceof ModeListener)
                                ((ModeListener) listener).onModeSet(bot, channel, source, user, (Mode.TempMode) mode);
                    }
                    else
                    {
                        channel.removeMode(user, (Mode.TempMode) mode);
                        for (Listener listener : bot.getListeners())
                            if (listener instanceof ModeListener)
                                ((ModeListener) listener).onModeRemoved(bot, channel, source, user, (Mode.TempMode) mode);
                    }
                }

                else
                {
                    if (added)
                    {
                        channel.addMode(mode, value);
                        for (Listener listener : bot.getListeners())
                            if (listener instanceof ModeListener)
                                ((ModeListener) listener).onModeSet(bot, channel, source, mode, value);
                    }

                    else
                    {
                        channel.removeMode(mode, value);
                        for (Listener listener : bot.getListeners())
                            if (listener instanceof ModeListener)
                                ((ModeListener) listener).onModeRemoved(bot, channel, source, mode, value);
                    }
                }
            }
        }
    }

    private void KICK(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        User user = bot.getUser(lineSplit[3]);

        channel.removeUser(user);
        for (Listener listener : bot.getListeners())
            if (listener instanceof KickListener)
                ((KickListener) listener).onKick(bot, channel, source, user, StringUtils.compact(lineSplit, 3).substring(1));
    }

    private void TOPIC(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        String topic = StringUtils.compact(lineSplit, 3).substring(1);
        channel.setTopic(topic);
        channel.setTopicSetter("" + source);

        for (Listener listener : bot.getListeners())
            if (listener instanceof TopicListener)
                ((TopicListener) listener).onTopicSet(bot, channel, source, topic);
    }

    private void INVITE(User source, String[] lineSplit)
    {
        for (String chan : StringUtils.compact(lineSplit, 3).substring(1).split(" "))
            for (Listener listener : bot.getListeners())
                if (listener instanceof InviteListener)
                    ((InviteListener) listener).onInvite(bot, bot.getChannel(chan), source);
    }

    private void on322(String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[3]);
        if (channel.totalUsers() != Integer.parseInt(lineSplit[4]))
            bot.send(new RawAction("WHO " + channel.name));
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
            switch (c)
            {
                case '~':
                    if (!channel.hasMode(user, Mode.owner))
                        channel.addMode(user, Mode.owner);
                    break;

                case '&':
                    if (!channel.hasMode(user, Mode.superOp))
                        channel.addMode(user, Mode.superOp);
                    break;

                case '@':
                    if (!channel.hasMode(user, Mode.op))
                        channel.addMode(user, Mode.op);
                    break;

                case '%':
                    if (!channel.hasMode(user, Mode.halfOp))
                        channel.addMode(user, Mode.halfOp);
                    break;

                case '+':
                    if (!channel.hasMode(user, Mode.voice))
                        channel.addMode(user, Mode.voice);
                    break;

                default:
                    break;
            }
    }

    private void on324(String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[3]);
        for (char ID : lineSplit[4].substring(1).toCharArray())
            channel.addMode(Mode.getMode(ID), "");
    }

    private void on311(String[] lineSplit)
    {
        User user = bot.getUser(lineSplit[3]);
        user.setLogin(lineSplit[4]);
        user.setHostmask(lineSplit[5]);
        user.setRealname(StringUtils.compact(lineSplit, 7).substring(1));
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
                        bot.log(3, "An exception caused by your code has occurred (" + e.toString() + ")");
                        bot.log(2, "The bot should continue functioning without problems; however, you may want to try fix the issue.");
                        e.printStackTrace();
                    }
                }
                if (active)
                    bot.disconnect("Connection closed", StringUtils.toBoolean(bot.getProperty(ArcheBot.Property.reconnect)));
            }

            catch (IOException e)
            {
                bot.log(3, String.format(ArcheBot.exception_message, e));
                bot.disconnect("A fatal exception occurred!", StringUtils.toBoolean(bot.getProperty(ArcheBot.Property.reconnect)));
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
            if (line.length() > 510)
                line = line.substring(0, 510);

            synchronized (writer)
            {
                try
                {
                    writer.println(line);
                    bot.log(1, line);
                }

                catch (Exception e)
                {
                    bot.log(3, String.format(ArcheBot.exception_message, e));
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
                    bot.log(2, "Too much output backlogged. Clearing all messages.");
                    queue.clear();
                }

                if (queue.hasNext())
                {
                    sendLine(queue.getNext());

                    try
                    {
                        Thread.sleep(messageDelay);
                    }

                    catch (Exception e)
                    {
                        bot.log(3, String.format(ArcheBot.exception_message, e));
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
