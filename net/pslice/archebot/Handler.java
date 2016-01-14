package net.pslice.archebot;

import net.pslice.archebot.output.ErrorMessage;

public class Handler<B extends ArcheBot> {

    /**
     * Runs when an action to a channel is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that the action was sent to
     * @param user The user who sent the action
     * @param action The action that was sent
     */
    public void onAction(B bot, Channel channel, User user, String action) {}

    /**
     * Runs when an action to the bot is received
     *
     * @param bot The bot that received the message
     * @param user The user who sent the action
     * @param action The action that was sent
     */
    public void onAction(B bot, User user, String action) {
        onAction(bot, bot.channelMap.getChannel(user.nick), user, action);
    }

    /**
     * Runs when a 3-digit numeric code is received
     *
     * @param bot The bot that received the message
     * @param code The 3-digit code
     * @param args The additional arguments that were received
     * @param tail The last argument
     */
    public void onCode(B bot, int code, String[] args, String tail) {}

    /**
     * Runs when a command is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that the command was sent to
     * @param user The user who sent the command
     * @param command The command that was sent
     * @param args The additional arguments that were received
     */
    public void onCommand(B bot, Channel channel, User user, Command<B> command, String[] args) {
        if (!command.enabled && !user.hasPermission(Permission.OPERATOR))
            bot.send(new ErrorMessage(user, "That command is not currently enabled."));
        else if (command.requireId && !user.isIdentified())
            bot.send(new ErrorMessage(user, "You must be identified with NickServ to run that command."));
        else if (user.hasPermission(command.getPermission()) || user.hasPermission(Permission.OPERATOR))
            if (args[0].equals(bot.nick))
                command.execute(bot, user, args);
            else
                command.execute(bot, bot.channelMap.getChannel(args[0]), user, args);
        else
            bot.send(new ErrorMessage(user, "You do not have permission to do that. (Required permission: %s)", command.getPermission()));
    }

    /**
     * Runs when a command is received
     *
     * @param bot The bot that received the message
     * @param user The user who sent the command
     * @param command The command that was sent
     * @param args The additional arguments that were received
     */
    public void onCommand(B bot, User user, Command<B> command, String[] args) {
        onCommand(bot, bot.channelMap.getChannel(user.nick), user, command, args);
    }

    /**
     * Runs when a connection is successfully made
     *
     * @param bot The bot that connected
     */
    public void onConnect(B bot) {}

    /**
     * Runs when a CTCP command to a channel is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that the CTCP command was sent to
     * @param user The user who sent the CTCP command
     * @param command The CTCP command that was sent
     * @param args The additional arguments that were received
     */
    public void onCTCPCommand(B bot, Channel channel, User user, String command, String args) {}

    /**
     * Runs when a CTCP command to the bot is received
     *
     * @param bot The bot that received the message
     * @param user The user who sent the CTCP command
     * @param command The CTCP command that was sent
     * @param args The additional arguments that were received
     */
    public void onCTCPCommand(B bot, User user, String command, String args) {
        onCTCPCommand(bot, bot.channelMap.getChannel(user.nick), user, command, args);
    }

    /**
     * Runs when the bot becomes disconnected from a server
     *
     * @param bot The bot that disconnected
     * @param reason The reason for disconnected
     */
    public void onDisconnect(B bot, String reason) {}

    /**
     * Runs when a channel invite is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that the invite is for
     * @param user The user who sent the invite
     */
    public void onInvite(B bot, Channel channel, User user) {}

    /**
     * Runs when a join message is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that the user joined
     * @param user The user who joined the channel
     */
    public void onJoin(B bot, Channel channel, User user) {}

    /**
     * Runs when a kick message is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that the user was kicked from
     * @param kicker The user who kicked the receiver
     * @param receiver The user who was kicked by the kicker
     * @param reason The reason why the user was kicked
     */
    public void onKick(B bot, Channel channel, User kicker, User receiver, String reason) {}

    /**
     * Runs when any line is received
     *
     * @param bot The bot that received the message
     * @param source The user who send the line
     * @param command The type of message received
     * @param args The additional arguments that were received
     * @param tail The last argument
     */
    public void onLine(B bot, User source, String command, String[] args, String tail) {}

    /**
     * Runs when a message to a channel is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that the message was sent to
     * @param user The user who sent the message
     * @param message The message that was sent
     */
    public void onMessage(B bot, Channel channel, User user, String message) {}

    /**
     * Runs when a message to the bot is received
     *
     * @param bot The bot that received the message
     * @param user The user who sent the message
     * @param message The message that was sent
     */
    public void onMessage(B bot, User user, String message) {
        onMessage(bot, bot.channelMap.getChannel(user.nick), user, message);
    }

    /**
     * Runs when a channel mode message is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that had the mode change
     * @param user The user who changed the mode
     * @param mode The mode that was changed
     * @param value The new value of the mode
     * @param added Whether the mode was added or removed
     */
    public void onMode(B bot, Channel channel, User user, char mode, String value, boolean added) {}

    /**
     * Runs when a channel/user mode message is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that had the mode change
     * @param setter The user who changed the receiver's mode
     * @param receiver The user whose mode was changed by the setter
     * @param mode The mode that was changed
     * @param added Whether the mode was added or removed
     */
    public void onMode(B bot, Channel channel, User setter, User receiver, char mode, boolean added) {}

    /**
     * Runs when a user mode message is received
     *
     * @param bot The bot that received the message
     * @param user The user whose mode was changed
     * @param mode The mode that was changed
     * @param added Whether the mode was added or removed
     */
    public void onMode(B bot, User user, char mode, boolean added) {}

    /**
     * Runs when a MOTD completed message is received
     *
     * @param bot The bot that received the message
     * @param server The server that the MOTD is for
     */
    public void onMOTD(B bot, Server server) {}

    /**
     * Runs when a nick message is received
     *
     * @param bot The bot that received the message
     * @param user The user who changed their nick
     * @param oldNick The user's previous nick
     */
    public void onNick(B bot, User user, String oldNick) {}

    /**
     * Runs when a notice to a channel is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that the notice was sent to
     * @param user The user who sent the notice
     * @param notice The notice that was sent
     */
    public void onNotice(B bot, Channel channel, User user, String notice) {}

    /**
     * Runs when a notice to the bot is received
     *
     * @param bot The bot that received the message
     * @param user The user who sent the notice
     * @param notice The notice that was sent
     */
    public void onNotice(B bot, User user, String notice) {
        onNotice(bot, bot.channelMap.getChannel(user.nick), user, notice);
    }

    /**
     * Runs when a part message is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that the user is parting from
     * @param user The user who parted from the channel
     * @param reason The reason why the user parted the channel
     */
    public void onPart(B bot, Channel channel, User user, String reason) {}

    /**
     * Runs when a ping is received
     *
     * @param bot The bot that received the message
     */
    public void onPing(B bot, String message) {}

    /**
     * Runs when a pong is received
     *
     * @param bot The bot that received the message
     * @param server The server that the pong was received from
     * @param message The message sent with the pong
     */
    public void onPong(B bot, Server server, String message) {}

    /**
     * Runs when a quit message is received
     *
     * @param bot The bot that received the message
     * @param user The user who quit
     * @param reason The reason why the user quit
     */
    public void onQuit(B bot, User user, String reason) {}

    /**
     * Runs when a topic message is received
     *
     * @param bot The bot that received the message
     * @param channel The channel that had the topic change
     * @param user The user who changed the topic
     * @param topic The new channel topic
     */
    public void onTopic(B bot, Channel channel, User user, String topic) {}

    /**
     * Runs when a whois completed message is received
     *
     * @param bot The bot that received the message
     * @param user The user targeted by the whois
     */
    public void onWhois(B bot, User user) {}
}
