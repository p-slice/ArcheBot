package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.Listener;
import net.pslice.archebot.User;

public interface MessageListener<B extends ArcheBot> extends Listener<B> {

    public void onMessage(B bot, Channel channel, User sender, String message);

    public void onPrivateMessage(B bot, User sender, String message);
}
