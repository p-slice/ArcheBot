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

    // All users in the channel, and their ranks
    private final HashMap<User, Set<Mode.TempMode>> users = new HashMap<>();

    // All normal modes
    private final HashMap<Mode.ValueMode, String> modes = new HashMap<>();

    // All permanent modes
    private final HashMap<Mode.PermaMode, Set<String>> permaModes = new HashMap<>();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    StaticChannel(Channel channel)
    {
        this.name = channel.name;
        this.topic = channel.getTopic();
        this.topicSetter = channel.getTopicSetter();
        for (Mode.ValueMode mode : channel.getModes())
            modes.put(mode, channel.getValue(mode));
        permaModes.put(Mode.ban, channel.getValues(Mode.ban));
        permaModes.put(Mode.exempt, channel.getValues(Mode.exempt));
        permaModes.put(Mode.invited, channel.getValues(Mode.invited));
        for (User user : channel.getUsers())
            users.put(user, channel.getModes(user));
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

    public String getValue(Mode.ValueMode mode)
    {
        return modes.containsKey(mode) ? modes.get(mode) : "";
    }

    public Set<String> getValues(Mode.PermaMode mode)
    {
        return new HashSet<>(permaModes.get(mode));
    }

    public boolean isValue(Mode.PermaMode mode, String args)
    {
        return permaModes.get(mode).contains(args);
    }

    public Set<Mode.ValueMode> getModes()
    {
        return new HashSet<>(modes.keySet());
    }

    public Set<Mode.TempMode> getModes(User user)
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

    public Set<User> getUsers()
    {
        return new HashSet<>(users.keySet());
    }

    public Set<User> getUsers(Mode.TempMode mode)
    {
        Set<User> modeUsers = new HashSet<>();

        for (User user : users.keySet())
            if (users.get(user).contains(mode))
                modeUsers.add(user);

        return modeUsers;
    }

    public boolean hasMode(Mode.ValueMode mode)
    {
        return modes.containsKey(mode);
    }

    public boolean hasMode(User user, Mode.TempMode mode)
    {
        return users.containsKey(user) && users.get(user).contains(mode);
    }

    public int totalUsers()
    {
        return users.size();
    }

    public int totalUsers(Mode.TempMode mode)
    {
        int size = 0;
        for (User user : users.keySet())
            if (this.hasMode(user, mode))
                size++;
        return size;
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public String toString()
    {
        int i;
        return  name +
                (modes.size() > 0 ? " {MODES:" + StringUtils.compact(modes.keySet(), "") : "") +
                ((i = totalUsers(Mode.owner)) > 0 ? " {OWNERS:" + i + "}" : "") +
                ((i = totalUsers(Mode.superOp)) > 0 ? " {SUPEROPS:" + i + "}" : "") +
                ((i = totalUsers(Mode.op)) > 0 ? " {OPS:" + i + "}" : "") +
                ((i = totalUsers(Mode.halfOp)) > 0 ? " {HALFOPS:" + i + "}" : "") +
                ((i = totalUsers(Mode.voice)) > 0 ? " {VOICED:" + i + "}" : "") +
                " {TOTAL USERS:" + totalUsers() + "}" +
                " {TOPIC:" + topic + "}";
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof StaticChannel && this.toString().equals(object.toString());
    }
}