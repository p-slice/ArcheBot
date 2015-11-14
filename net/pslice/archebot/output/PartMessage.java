package net.pslice.archebot.output;

import net.pslice.archebot.Channel;

public final class PartMessage extends Output {

    public PartMessage(Channel channel) {
        this(channel.getName());
    }

    public PartMessage(String channel) {
        this(channel, "");
    }

    public PartMessage(Channel channel, String reason, Object... objects) {
        this(channel.getName(), reason, objects);
    }

    public PartMessage(String channel, String reason, Object... objects) {
        super(String.format("PART %s :%s", channel, objects.length > 0 ? String.format(reason, objects) : reason));
    }
}
