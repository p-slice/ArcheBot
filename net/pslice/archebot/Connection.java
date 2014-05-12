package net.pslice.archebot;

import net.pslice.archebot.actions.ErrorAction;
import net.pslice.archebot.actions.RawAction;
import net.pslice.archebot.listeners.*;
import net.pslice.utilities.managers.StringManager;

import java.io.*;
import java.net.Socket;
import java.util.*;

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
    private final OutputThread outputThread;

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

        String nick       = bot.getProperty(ArcheBot.Property.nick),
               login      = bot.getProperty(ArcheBot.Property.login),
               realname   = bot.getProperty(ArcheBot.Property.realname).replace("{VERSION}", ArcheBot.VERSION).replace("{UVERSION}", ArcheBot.USER_VERSION),
               server     = bot.getProperty(ArcheBot.Property.server),
               serverPass = bot.getProperty(ArcheBot.Property.serverPass);
        int port          = Integer.parseInt(bot.getProperty(ArcheBot.Property.port));

        bot.setNick(nick);
        bot.log(2, "Attempting to connect to " + server + " on port " + port + "...");

        socket = new Socket(server, port);

        bot.log(2, "Connection successful!");

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        InputThread inputThread = new InputThread(this, reader);
        outputThread = new OutputThread(this, writer);

        if (!serverPass.equals(""))
            outputThread.sendLine("PASS " + serverPass);
        outputThread.sendLine("NICK " + nick);
        outputThread.sendLine("USER " + login + " 8 * :" + realname);

        String message;
        int tries = 1;
        while ((message = reader.readLine()) != null)
        {
            handleServerLine(message);

            String[] messageSplit = message.split(" ");

            if (messageSplit[1].equals("004"))
                break;

            else if (messageSplit[1].equals("433"))
            {
                if (StringManager.toBoolean(bot.getProperty(ArcheBot.Property.rename)))
                {
                    bot.log(3, "Nick already in use (Trying another one!)");
                    bot.setNick(nick + tries);
                    outputThread.sendLine("NICK " + bot.getNick());
                    tries++;
                }

                else
                    throw new ConnectionException("Nick already in use!");
            }

            else if (messageSplit[1].startsWith("4") || messageSplit[1].startsWith("5"))
                throw new ConnectionException(StringManager.compressArray(messageSplit, 3, true));
        }
        active = true;

        socket.setSoTimeout(4 * 60000);

        inputThread.start();
        outputThread.start();
    }

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    ArcheBot getBot()
    {
        return bot;
    }

    boolean isActive()
    {
        return active;
    }

    void send(String line, boolean direct)
    {
        if (direct)
            outputThread.sendLine(line);
        else
            outputThread.addMessageToQueue(line);
    }

    void handleServerLine(String line)
    {
        bot.log(0, line);
        if (line.startsWith("PING"))
            outputThread.sendLine("PONG " + line.substring(5));
        else
        {
            String[] lineSplit = line.split(" ");

            User source = bot.getUser(lineSplit[0].substring(1).split("!")[0]);
            if (lineSplit[0].contains("!") && !bot.isUser(source.getNick()))
                bot.addUser(source);

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

                case "332":
                    bot.getChannel(lineSplit[3]).setTopic(StringManager.compressArray(lineSplit, 4, true));
                    break;

                case "333":
                    bot.getChannel(lineSplit[3]).setTopicSetter(lineSplit[4]);
                    break;

                case "352":
                    this.E352(lineSplit);
                    break;

                case "324":
                    this.E324(lineSplit);
                    break;

                default:
                    if (lineSplit[1].matches("[45]\\d\\d"))
                    {
                        for (Listener listener : bot.getListeners())
                            if (listener instanceof RawListener)
                                ((RawListener) listener).onServerError(bot, lineSplit[2], StringManager.compressArray(lineSplit, 3, true));
                    }

                    else
                    {
                        for (Listener listener : bot.getListeners())
                            if (listener instanceof RawListener)
                                ((RawListener) listener).onUnknownLine(bot, line);
                    }
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
        if (lineSplit[2].equals(bot.getNick()))
        {
            for (Listener listener : bot.getListeners())
                if (listener instanceof MessageListener)
                    ((MessageListener) listener).onPrivateMessage(bot, source, StringManager.compressArray(lineSplit, 3, true));
        }

        else
        {
            Channel channel = bot.getChannel(lineSplit[2]);
            if (lineSplit[3].substring(1).startsWith(bot.getProperty(ArcheBot.Property.prefix)))
            {
                String command = lineSplit[3].substring(bot.getProperty(ArcheBot.Property.prefix).length() + 1).toLowerCase();
                if (bot.getCommandManager().isRegistered(command))
                {
                    String[] args = new String[lineSplit.length - 4];
                    System.arraycopy(lineSplit, 4, args, 0, args.length);
                    for (Listener listener : bot.getListeners())
                        if (listener instanceof CommandListener)
                            ((CommandListener) listener).onCommand(bot, channel, source, bot.getCommandManager().getCommand(command), args);
                }

                else
                    bot.send(ErrorAction.build(source, String.format("The command '%s' is not registered!", command)));
            }

            else
            {
                for (Listener listener : bot.getListeners())
                    if (listener instanceof MessageListener)
                        ((MessageListener) listener).onMessage(bot, bot.getChannel(lineSplit[2]), source, StringManager.compressArray(lineSplit, 3, true));
            }
        }
    }

    private void NOTICE(User source, String[] lineSplit)
    {
        if (lineSplit[2].equals(bot.getNick()))
        {
            for (Listener listener : bot.getListeners())
                if (listener instanceof NoticeListener)
                    ((NoticeListener) listener).onPrivateNotice(bot, source, StringManager.compressArray(lineSplit, 3, true));
        }

        else
        {
            for (Listener listener : bot.getListeners())
                if (listener instanceof NoticeListener)
                    ((NoticeListener) listener).onNotice(bot, bot.getChannel(lineSplit[2]), source, StringManager.compressArray(lineSplit, 3, true));
        }
    }

    private void JOIN(User source, String[] lineSplit)
    {
        if (source.equals(bot.toUser()))
        {
            bot.addChannel(new Channel(lineSplit[2]));
            bot.send(RawAction.build("WHO " + lineSplit[2]));
            bot.send(RawAction.build("MODE " + lineSplit[2]));
        }

        else
            bot.send(RawAction.build("WHOIS " + source.getNick()));

        Channel channel = bot.getChannel(lineSplit[2]);
        channel.addUser(source);

        for (Listener listener : bot.getListeners())
            if (listener instanceof JoinListener)
                ((JoinListener) listener).onJoin(bot, channel, source);
    }

    private void PART(User source, String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[2]);
        if (source.equals(bot.toUser()))
            bot.removeChannel(channel.getName());
        else
            channel.removeUser(source);

        for (Listener listener : bot.getListeners())
            if (listener instanceof PartListener)
                ((PartListener) listener).onPart(bot, channel, source, lineSplit.length > 3 ? StringManager.compressArray(lineSplit, 3, true) : "");
    }

    private void QUIT(User source, String[] lineSplit)
    {
        for (Channel channel : bot.getChannels())
            if (channel.contains(source))
                channel.removeUser(source);
        bot.removeUser(source.getNick());

        for (Listener listener : bot.getListeners())
            if (listener instanceof QuitListener)
                ((QuitListener) listener).onQuit(bot, source, lineSplit.length > 3 ? StringManager.compressArray(lineSplit, 3, true) : "");
    }

    private void NICK(User source, String[] lineSplit)
    {
        String oldNick = source.getNick();
        String newNick = lineSplit[2];

        source.setNick(newNick);
        bot.removeUser(oldNick);
        bot.addUser(source);

        if (oldNick.equals(bot.getNick()))
            bot.setNick(newNick);

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
            return;

        if (lineSplit.length == 4)
        {
            for (char ID : modes)
            {
                Channel.Mode mode = Channel.Mode.getMode(ID);
                if (added)
                {
                    channel.addMode(mode);
                    for (Listener listener : bot.getListeners())
                        if (listener instanceof ChannelModeListener)
                            ((ChannelModeListener) listener).onChannelModeSet(bot, channel, source, mode);
                }
                else
                {
                    channel.removeMode(mode);
                    for (Listener listener : bot.getListeners())
                        if (listener instanceof ChannelModeListener)
                            ((ChannelModeListener) listener).onChannelModeRemoved(bot, channel, source, mode);
                }
            }
        }

        else if (lineSplit.length == 5)
        {
            User user = bot.getUser(lineSplit[4]);
            for (char ID : modes)
            {
                User.Mode mode = User.Mode.getModeFromID(ID);
                if (added)
                {
                    channel.addMode(user, mode);
                    for (Listener listener : bot.getListeners())
                        if (listener instanceof UserModeListener)
                            ((UserModeListener) listener).onUserModeSet(bot, channel, source, user, mode);
                }
                else
                {
                    channel.removeMode(user, mode);
                    for (Listener listener : bot.getListeners())
                        if (listener instanceof UserModeListener)
                            ((UserModeListener) listener).onUserModeRemoved(bot, channel, source, user, mode);
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
                ((KickListener) listener).onKick(bot, channel, source, user, StringManager.compressArray(lineSplit, 4, true));
    }

    private void E352(String[] lineSplit)
    {
        User user = bot.getUser(lineSplit[7]);
        if (!bot.isUser(lineSplit[7]))
            bot.addUser(user);

        user.setLogin(lineSplit[4]);
        user.setHostmask(lineSplit[5]);
        user.setRealname(StringManager.compressArray(lineSplit, 10));
        user.setServer(lineSplit[6]);

        Channel channel = bot.getChannel(lineSplit[3]);
        if (!channel.contains(user))
            channel.addUser(user);

        for (char c : lineSplit[8].toCharArray())
            switch (c)
            {
                case '~':
                    if (!channel.hasMode(user, User.Mode.owner))
                        channel.addMode(user, User.Mode.owner);
                    break;

                case '&':
                    if (!channel.hasMode(user, User.Mode.superOp))
                        channel.addMode(user, User.Mode.superOp);
                    break;

                case '@':
                    if (!channel.hasMode(user, User.Mode.op))
                        channel.addMode(user, User.Mode.op);
                    break;

                case '%':
                    if (!channel.hasMode(user, User.Mode.halfOp))
                        channel.addMode(user, User.Mode.halfOp);
                    break;

                case '+':
                    if (!channel.hasMode(user, User.Mode.voice))
                        channel.addMode(user, User.Mode.voice);
                    break;

                default:
                    break;
            }
    }

    private void E324(String[] lineSplit)
    {
        Channel channel = bot.getChannel(lineSplit[3]);

        for (char ID : lineSplit[4].substring(1).toCharArray())
            channel.addMode(Channel.Mode.getMode(ID));
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    private static final class InputThread extends Thread {
        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        // Instance of the connection
        private final Connection connection;

        // The reader for socket input
        private final BufferedReader reader;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        InputThread(Connection connection, BufferedReader reader)
        {
            this.connection = connection;
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
                while (connection.isActive() && (line = reader.readLine()) != null)
                {
                    try
                    {
                        connection.handleServerLine(line);
                    }

                    catch (Exception e)
                    {
                        connection.getBot().log(3, "An exception caused by your code has occurred (" + e.toString() + ")");
                        connection.getBot().log(2, "The bot should continue functioning without problems; however, you may want to try fix the issue.");
                        e.printStackTrace();
                    }
                }
                if (connection.isActive())
                    connection.getBot().disconnect("Connection closed", StringManager.toBoolean(connection.getBot().getProperty(ArcheBot.Property.reconnect)));
            }

            catch (IOException e)
            {
                connection.getBot().log(3, String.format(ArcheBot.exception_message, e));
                connection.getBot().disconnect("A fatal exception occurred!", StringManager.toBoolean(connection.getBot().getProperty(ArcheBot.Property.reconnect)));
            }
        }
    }

    private static final class OutputThread extends Thread {
        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        // Instance of the connection
        private final Connection connection;

        // The writer for socket output
        private final BufferedWriter writer;

        // The queue of output lines
        private final List<String> queue = new LinkedList<>();

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        OutputThread(Connection connection, BufferedWriter writer)
        {
            this.connection = connection;
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
                    writer.write(line + "\n\r");
                    writer.flush();
                    connection.getBot().log(1, line);
                }

                catch (Exception e)
                {
                    connection.getBot().log(3, String.format(ArcheBot.exception_message, e));
                }
            }
        }

        synchronized void addMessageToQueue(String message)
        {
            queue.add(message);
        }

        /*
         * =======================================
         * Overridden methods:
         * =======================================
         */

        @Override
        public void run()
        {
            while (connection.isActive())
            {
                if (queue.size() > 100)
                {
                    connection.getBot().log(2, "Too much output backlogged. Clearing all messages.");
                    queue.clear();
                }

                try
                {
                    Thread.sleep(1000);

                    if (queue.size() > 0)
                    {
                        sendLine(queue.get(0));
                        queue.remove(0);
                    }
                }

                catch (Exception e)
                {
                    connection.getBot().log(3, String.format(ArcheBot.exception_message, e));
                }
            }
        }
    }

    static final class ConnectionException extends Exception {

        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        private final String line;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        public ConnectionException(String line)
        {
            this.line = line;
        }

        /*
         * =======================================
         * Local methods:
         * =======================================
         */

        String getLine()
        {
            return line;
        }
    }
}
