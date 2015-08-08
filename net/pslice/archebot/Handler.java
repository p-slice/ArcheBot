package net.pslice.archebot;

public class Handler<B extends ArcheBot> {

    // Override this to handle channel actions.
    public void onAction(B bot, Channel channel, User user, String action) {}

    // Override this to handle private actions.
    public void onAction(B bot, User user, String action) {}

    // Override this to handle server code messages.
    public void onCode(B bot, int code, String[] args, String tail) {}

    // Override this to handle commands.
    public void onCommand(B bot, Channel channel, User user, Command<B> command, String[] args) {}

    // Override this to handle connects.
    public void onConnect(B bot) {}

    // Override this to handle channel CTCP commands.
    public void onCTCPCommand(B bot, Channel channel, User user, String command, String args) {}

    // Override this to handle private CTCP commands.
    public void onCTCPCommand(B bot, User user, String command, String args) {}

    /// Override this to handle disconnects
    public void onDisconnect(B bot, String reason) {}

    // Override this to handle invites
    public void onInvite(B bot, Channel channel, User user) {}

    // Override this to handle joins
    public void onJoin(B bot, Channel channel, User user) {}

    // Override this to handle kicks
    public void onKick(B bot, Channel channel, User kicker, User receiver, String reason) {}

    // Override this to handle all server lines
    public void onLine(B bot, String command, String[] args, String tail) {}

    // Override this to handle channel messages
    public void onMessage(B bot, Channel channel, User user, String message) {}

    // Override this to handle private messages
    public void onMessage(B bot, User user, String message) {}

    // Override this to handle generic channel modes
    public void onMode(B bot, Channel channel, User user, char mode, String value, boolean added) {}

    // Override this to handle channel-user status modes
    public void onMode(B bot, Channel channel, User setter, User receiver, char mode, boolean added) {}

    // Override this to handle user moves
    public void onMode(B bot, User user, char mode, boolean added) {}

    // Override this to handle MOTD messages
    public void onMOTD(B bot, Server server) {}

    // Override this to handle nick changes
    public void onNick(B bot, User user, String oldNick) {}

    // Override this to handle channel notices
    public void onNotice(B bot, Channel channel, User user, String notice) {}

    // Override this to handle private notices
    public void onNotice(B bot, User user, String notice) {}

    // Override this to handle parts
    public void onPart(B bot, Channel channel, User user, String reason) {}

    // Override this to handle pings
    public void onPing(B bot) {}

    // Override this to handle pongs
    public void onPong(B bot, Server server, String message) {}

    // Override this to handle quits
    public void onQuit(B bot, User user, String reason) {}

    // Override this to handle topic changes
    public void onTopic(B bot, Channel channel, User user, String topic) {}
}
