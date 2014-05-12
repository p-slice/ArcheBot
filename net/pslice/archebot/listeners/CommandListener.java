package net.pslice.archebot.listeners;

import net.pslice.archebot.*;

public interface CommandListener<B extends ArcheBot> extends Listener<B> {

    public void onCommand(B bot, Channel channel, User sender, Command<B> command, String[] args);
}
