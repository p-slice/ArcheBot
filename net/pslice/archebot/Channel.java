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
    private String topic       = "",
                   topicSetter = "";

    // All users in the channel, and their ranks
    private final HashMap<User, Set<Mode.UserMode>> users = new HashMap<>();

    // All basic (0 args) modes
    private final Set<Mode.BasicMode> basicModes = new HashSet<>();

    // All simple (1 arg) modes
    private final HashMap<Mode.SimpleMode, String> simpleModes = new HashMap<>();

    // All complex (1+ args) modes
    private final HashMap<Mode.ComplexMode, Set<String>> complexModes = new HashMap<>();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    Channel(String name)
    {
        this.name = name;
        complexModes.put(Mode.ban, new HashSet<String>());
        complexModes.put(Mode.exempt, new HashSet<String>());
        complexModes.put(Mode.invited, new HashSet<String>());
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

    public String getArgs(Mode.SimpleMode mode)
    {
        return simpleModes.containsKey(mode) ? simpleModes.get(mode) : "";
    }

    public Set<String> getArgs(Mode.ComplexMode mode)
    {
        return complexModes.get(mode);
    }

    public boolean isArg(Mode.ComplexMode mode, String args)
    {
        return complexModes.get(mode).contains(args);
    }

    public Set<Mode.BasicMode> getBasicModes()
    {
        return new HashSet<>(basicModes);
    }

    public Set<Mode.SimpleMode> getSimpleModes()
    {
        return new HashSet<>(simpleModes.keySet());
    }

    public Set<Mode.UserMode> getModes(User user)
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

    public Set<User> getUsers(Mode.UserMode mode)
    {
        Set<User> modeUsers = new HashSet<>();

        for (User user : users.keySet())
            if (users.get(user).contains(mode))
                modeUsers.add(user);

        return modeUsers;
    }

    public boolean hasMode(Mode mode)
    {
        if (mode instanceof Mode.BasicMode)
            return basicModes.contains(mode);
        else if (mode instanceof Mode.SimpleMode)
            return simpleModes.containsKey(mode);
        else
            throw new IllegalArgumentException("Mode must be instance of BasicMode or SimpleMode");
    }

    public boolean hasMode(User user, Mode.UserMode mode)
    {
        return users.containsKey(user) && users.get(user).contains(mode);
    }

    public int totalUsers()
    {
        return users.size();
    }

    public int totalUsers(Mode.UserMode mode)
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
                (basicModes.size() > 0 ? " {MODES:" + (basicModes.size() > 0 ? StringUtils.compact(basicModes, 0, "") : "NONE")
                        + ":" + (simpleModes.keySet().size() > 0 ? StringUtils.compact(simpleModes.keySet(), 0, "") : "NONE") + "}" : "") +
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
        users.put(user, new HashSet<Mode.UserMode>());
    }

    void removeUser(User user)
    {
        users.remove(user);
    }

    void addMode(User user, Mode.UserMode mode)
    {
        users.get(user).add(mode);
    }

    void removeMode(User user, Mode.UserMode mode)
    {
        users.get(user).remove(mode);
    }

    void addMode(Mode mode)
    {
        if (mode instanceof Mode.BasicMode)
            basicModes.add((Mode.BasicMode) mode);
    }

    void addMode(Mode mode, String args)
    {
        if (mode instanceof Mode.SimpleMode)
            simpleModes.put((Mode.SimpleMode) mode, args);
        else if (mode instanceof Mode.ComplexMode)
            complexModes.get(mode).add(args);
    }

    void removeMode(Mode mode)
    {
        if (mode instanceof Mode.BasicMode)
            basicModes.remove(mode);
        else if (mode instanceof Mode.SimpleMode)
            simpleModes.remove(mode);
    }

    void removeMode(Mode mode, String args)
    {
        if (mode instanceof Mode.ComplexMode)
            complexModes.get(mode).remove(args);
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
