package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;

public interface RawListener<B extends ArcheBot> extends ArcheBot.Listener<B> {

    public void onLine(B bot, int ID, String args);
}
