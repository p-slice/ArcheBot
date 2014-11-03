package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;

public interface ConnectionListener<B extends ArcheBot> extends ArcheBot.Listener<B> {

    public void onConnect(B bot);

    public void onDisconnect(B bot, String message, boolean reconnect);
}
