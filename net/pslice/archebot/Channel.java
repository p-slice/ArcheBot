package net.pslice.archebot;

import net.pslice.utilities.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Channel {

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
    protected final HashMap<User, Set<Mode.TempMode>> users = new HashMap<>();

    // All normal modes
    protected final HashMap<Mode.ValueMode, String> modes = new HashMap<>();

    // All permanent modes
    protected final HashMap<Mode.PermaMode, Set<String>> permaModes = new HashMap<>();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    Channel(String name)
    {
        this.name = name;
        permaModes.put(Mode.ban, new HashSet<String>());
        permaModes.put(Mode.exempt, new HashSet<String>());
        permaModes.put(Mode.invited, new HashSet<String>());
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
        return permaModes.get(mode);
    }

    public boolean isArg(Mode.PermaMode mode, String args)
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
        int i;
        return  name +
                (modes.size() > 0 ? " {MODES:" + StringUtils.compact(modes.keySet(), "") +"}" : "") +
                ((i = totalUsers(Mode.owner)) > 0 ? " {OWNERS:" + i + "}" : "") +
                ((i = totalUsers(Mode.superOp)) > 0 ? " {SUPEROPS:" + i + "}" : "") +
                ((i = totalUsers(Mode.op)) > 0 ? " {OPS:" + i + "}" : "") +
                ((i = totalUsers(Mode.halfOp)) > 0 ? " {HALFOPS:" + i + "}" : "") +
                ((i = totalUsers(Mode.voice)) > 0 ? " {VOICED:" + i + "}" : "") +
                " {TOTAL USERS:" + totalUsers() + "}" +
                " {TOPIC:" + topic + "}";
    }

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    void addUser(User user)
    {
        users.put(user, new HashSet<Mode.TempMode>());
    }

    void removeUser(User user)
    {
        users.remove(user);
    }

    void addMode(User user, Mode.TempMode mode)
    {
        users.get(user).add(mode);
    }

    void removeMode(User user, Mode.TempMode mode)
    {
        users.get(user).remove(mode);
    }

    void addMode(Mode mode, String value)
    {
        if (mode instanceof Mode.ValueMode)
            modes.put((Mode.ValueMode) mode, value);
        else if (mode instanceof Mode.PermaMode)
            permaModes.get(mode).add(value);
    }

    void removeMode(Mode mode, String args)
    {
        if (mode instanceof Mode.ValueMode)
            modes.remove(mode);
        else if (mode instanceof Mode.PermaMode)
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
