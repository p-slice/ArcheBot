package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public final class WhoisMessage extends ArcheBot.Output {

    public WhoisMessage(User user) {
        this(user.getNick());
    }

    public WhoisMessage(String user) {
        super("WHOIS " + user);
    }
}
