package net.pslice.archebot.events;

import net.pslice.archebot.*;

public class CommandEvent<B extends ArcheBot> extends Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final Channel channel;
    private final User user;
    private final Command<B> command;
    private final String[] args;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public CommandEvent(B bot, Channel channel, User user, Command<B> command, String[] args)
    {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.command = command;
        this.args = args;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public Channel getChannel()
    {
        return channel;
    }

    public User getUser()
    {
        return user;
    }

    public Command<B> getCommand()
    {
        return command;
    }

    public String[] getArgs()
    {
        return args;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends net.pslice.archebot.Listener<B>
    {
        public void onCommand(CommandEvent<B> event);
    }
}
