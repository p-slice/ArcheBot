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
    private final HashMap<User, Set<Mode.UserMode>> users = new HashMap<>();

    // All basic (0 args) modes
    private final Set<Mode.BasicMode> basicModes;

    // All simple (1 arg) modes
    private final HashMap<Mode.SimpleMode, String> simpleModes = new HashMap<>();

    // All complex (1+ args) modes
    private final HashMap<Mode.ComplexMode, Set<String>> complexModes = new HashMap<>();

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
        this.basicModes = channel.getBasicModes();
        for (Mode.SimpleMode mode : channel.getSimpleModes())
            simpleModes.put(mode, channel.getArgs(mode));
        complexModes.put(Mode.ban, channel.getArgs(Mode.ban));
        complexModes.put(Mode.exempt, channel.getArgs(Mode.exempt));
        complexModes.put(Mode.invited, channel.getArgs(Mode.invited));
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

    public String getArgs(Mode mode)
    {
        if (!(mode instanceof Mode.SimpleMode))
            throw new IllegalArgumentException("Mode must be instance of SimpleMode");
        return simpleModes.containsKey(mode) ? simpleModes.get(mode) : "";
    }

    public Set<String> getArgSet(Mode mode)
    {
        if (!(mode instanceof Mode.ComplexMode))
            throw new IllegalArgumentException("Mode must be instance of ComplexMode");
        return complexModes.get(mode);
    }

    public boolean isArg(Mode mode, String args)
    {
        if (!(mode instanceof Mode.ComplexMode))
            throw new IllegalArgumentException("Mode must be instance of ComplexMode");
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

    public Set<User> getUsers(Mode mode)
    {
        if (!(mode instanceof Mode.UserMode))
            throw new IllegalArgumentException("Mode must be instance of UserMode");
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

    public boolean hasMode(User user, Mode mode)
    {
        if (!(mode instanceof Mode.UserMode))
            throw new IllegalArgumentException("Mode must be instance of UserMode");
        return users.containsKey(user) && users.get(user).contains(mode);
    }

    public int totalUsers()
    {
        return users.size();
    }

    public int totalUsers(Mode mode)
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
                (basicModes.size() > 0 ? " {MODES:" + StringUtils.compact(basicModes, 0, "")
                        + ":" + StringUtils.compact(simpleModes.keySet()) + "}" : "") +
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
