package net.pslice.archebot.handlers;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public interface NoticeHandler<B extends ArcheBot> extends ArcheBot.Handler<B> {
    void onNotice(B bot, Channel channel, User user, String notice);
    void onPrivateNotice(B bot, User user, String notice);
}
