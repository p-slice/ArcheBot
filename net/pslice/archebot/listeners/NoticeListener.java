package net.pslice.archebot.listeners;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public interface NoticeListener<B extends ArcheBot> extends ArcheBot.Listener<B> {

    public void onNotice(B bot, Channel channel, User user, String notice);

    public void onPrivateNotice(B bot, User user, String notice);
}
