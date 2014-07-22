package net.pslice.archebot;

import net.pslice.utilities.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StaticChannel {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // Info about the channel
    private final String name,
                         topic,
                         topicSetter;

    // Users in the channel
    private final HashMap<StaticUser, Set<User.Mode>> users = new HashMap<>();

    // Current channel modes
    private final Set<Channel.Mode> modes;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    StaticChannel(Channel channel)
    {
        name = channel.name;
        topic = channel.getTopic();
        topicSetter = channel.getTopicSetter();
        for (User user : channel.getUsers())
            users.put(user.toStaticUser(), channel.getModes(user));
        modes = channel.getModes();
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public boolean contains(User user)
    {
        return users.containsKey(user);
    }

    public Set<Channel.Mode> getModes()
    {
        return new HashSet<>(modes);
    }

    public Set<User.Mode> getModes(StaticUser user)
    {
        return users.containsKey(user) ? new HashSet<>(users.get(user)) : null;
    }

    public String getName()
    {
        return name;
    }

    public String getTopic()
    {
        return topic;
    }

    public String getTopicSetter()
    {
        return topicSetter;
    }

    public Set<StaticUser> getUsers()
    {
        return new HashSet<>(users.keySet());
    }

    public Set<StaticUser> getUsers(User.Mode mode)
    {
        Set<StaticUser> modeUsers = new HashSet<>();

        for (StaticUser user : users.keySet())
            if (users.get(user).contains(mode))
                modeUsers.add(user);

        return modeUsers;
    }

    public boolean hasMode(Channel.Mode mode)
    {
        return modes.contains(mode);
    }

    public boolean hasMode(StaticUser user, User.Mode mode)
    {
        return users.containsKey(user) && users.get(user).contains(mode);
    }

    public int totalUsers()
    {
        return users.size();
    }

    public int totalUsers(User.Mode mode)
    {
        int size = 0;
        for (StaticUser user : users.keySet())
            if (this.hasMode(user, mode))
                size++;
        return size;
    }

    /*
     * =======================================
     * Overridden methods
     * =======================================
     */

    @Override
    public String toString()
    {
        int i;
        return  name +
                (modes.size() > 0 ? " {MODES:" + StringUtils.compact(modes, 0, "") + "}" : "") +
                ((i = totalUsers(User.Mode.owner)) > 0 ? " {OWNERS:" + i + "}" : "") +
                ((i = totalUsers(User.Mode.superOp)) > 0 ? " {SUPEROPS:" + i + "}" : "") +
                ((i = totalUsers(User.Mode.op)) > 0 ? " {OPS:" + i + "}" : "") +
                ((i = totalUsers(User.Mode.halfOp)) > 0 ? " {HALFOPS:" + i + "}" : "") +
                ((i = totalUsers(User.Mode.voice)) > 0 ? " {VOICED:" + i + "}" : "") +
                " {TOTAL USERS:" + totalUsers() + "}";
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof StaticChannel && this.toString().equals(object.toString());
    }
}
