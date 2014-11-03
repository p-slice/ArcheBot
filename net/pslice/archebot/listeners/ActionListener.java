package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public interface ActionListener<B extends ArcheBot> extends ArcheBot.Listener<B> {

    public void onAction(B bot, Channel channel, User sender, String action);

    public void onPrivateAction(B bot, User sender, String action);
}
