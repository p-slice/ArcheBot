package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;

public interface PingListener<B extends ArcheBot> extends ArcheBot.Listener<B> {

    public void onPing(B bot);
}
