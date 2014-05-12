package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Listener;

public interface RawListener<B extends ArcheBot> extends Listener<B> {

    public void onServerError(B bot, String cause, String message);

    public void onUnknownLine(B bot, String line);
}
