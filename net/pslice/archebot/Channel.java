package net.pslice.archebot;

import net.pslice.utilities.StringUtils;
import net.pslice.archebot.Mode.PermaMode;
import net.pslice.archebot.Mode.TempMode;
import net.pslice.archebot.Mode.ValueMode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Channel implements Comparable<Channel> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // The name of the channel
    public final String name;

    // Information about the channel topic
    protected String topic       = "",
                     topicSetter = "";

    // All users in the channel, and their ranks
    protected final HashMap<User, Set<TempMode>> users = new HashMap<>();

    // All normal modes
    protected final HashMap<ValueMode, String> modes = new HashMap<>();

    // All permanent modes
    protected final HashMap<PermaMode, Set<String>> permaModes = new HashMap<>();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    Channel(String name)
    {
        this.name = name;
        for (Mode mode : Mode.getModes())
            if (mode instanceof Mode.PermaMode)
                permaModes.put((PermaMode) mode, new HashSet<String>());
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

    public String getValue(ValueMode mode)
    {
        return modes.containsKey(mode) ? modes.get(mode) : "";
    }

    public Set<String> getValues(PermaMode mode)
    {
        return permaModes.get(mode);
    }

    public boolean isArg(PermaMode mode, String args)
    {
        return permaModes.get(mode).contains(args);
    }

    public Set<ValueMode> getModes()
    {
        return new HashSet<>(modes.keySet());
    }

    public Set<TempMode> getModes(User user)
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

    public Set<User> getUsers(TempMode mode)
    {
        Set<User> modeUsers = new HashSet<>();

        for (User user : users.keySet())
            if (users.get(user).contains(mode))
                modeUsers.add(user);

        return modeUsers;
    }

    public boolean hasMode(ValueMode mode)
    {
        return modes.containsKey(mode);
    }

    public boolean hasMode(User user, TempMode mode)
    {
        return users.containsKey(user) && users.get(user).contains(mode);
    }

    public int totalUsers()
    {
        return users.size();
    }

    public int totalUsers(TempMode mode)
    {
        int size = 0;
        for (User user : users.keySet())
            if (this.hasMode(user, mode))
                size++;
        return size;
    }

    public StaticChannel toStaticChannel()
    {
        return new StaticChannel(this);
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public String toString()
    {
        String modeUsers = "";
        for (Mode mode : Mode.getModes())
            if (mode instanceof TempMode)
                if (getUsers((TempMode) mode).size() > 0)
                    modeUsers += " {" + mode + ":" + getUsers((TempMode) mode).size() + "}";
        return  name +
                (modes.size() > 0 ? " {MODES:" + StringUtils.compact(modes.keySet(), "") + "}" : "") +
                modeUsers +
                " {TOTAL USERS:" + totalUsers() + "}" +
                " {TOPIC:" + topic + "}";
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Channel channel)
    {
        return name.toLowerCase().compareTo(channel.name.toLowerCase());
    }

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    void addUser(User user)
    {
        users.put(user, new HashSet<TempMode>());
    }

    void removeUser(User user)
    {
        users.remove(user);
    }

    void addMode(User user, TempMode mode)
    {
        if (users.containsKey(user) && !users.get(user).contains(mode))
            users.get(user).add(mode);
    }

    void removeMode(User user, TempMode mode)
    {
        if (users.containsKey(user) && users.get(user).contains(mode))
            users.get(user).remove(mode);
    }

    void addMode(Mode mode, String value)
    {
        if (mode instanceof ValueMode)
            modes.put((ValueMode) mode, value);
        else if (mode instanceof PermaMode)
            permaModes.get(mode).add(value);
    }

    void removeMode(Mode mode, String args)
    {
        if (mode instanceof ValueMode)
            modes.remove(mode);
        else if (mode instanceof PermaMode)
            permaModes.get(mode).remove(args);
    }

    void setTopic(String topic)
    {
        this.topic = topic;
    }

    void setTopicSetter(String topicSetter)
    {
        this.topicSetter = topicSetter;
    }
}
