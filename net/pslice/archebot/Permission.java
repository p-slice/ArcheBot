package net.pslice.archebot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

public class Permission implements Comparable<Permission> {

    private static final HashMap<String, Permission> permissions = new HashMap<>();
    public static final Permission DEFAULT = new Permission("default"),
                                   OPERATOR = new Permission("operator"),
                                   IGNORE = new Permission("ignore");
    protected final String name;
    private final TreeSet<Permission> inclusions = new TreeSet<>();

    private Permission(String name) {
        this.name = name;
        permissions.put(name.toLowerCase(), this);
    }

    public TreeSet<Permission> getInclusions() {
        return new TreeSet<>(inclusions);
    }

    public String getName() {
        return name;
    }

    public void include(Permission permission, Permission... permissions) {
        inclusions.add(permission);
        if (permissions.length > 0)
            inclusions.addAll(Arrays.asList(permissions));
    }

    public void include(String permission, String... permissions) {
        include(get(permission));
        for (String p : permissions)
            include(get(p));
    }

    public boolean includes(String permission) {
        return includes(get(permission));
    }

    public boolean includes(Permission permission) {
        return inclusions.contains(permission);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Permission permission) {
        return name.compareToIgnoreCase(permission.name);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Permission && obj.toString().equals(toString());
    }

    @Override
    public String toString() {
        return name;
    }

    public static boolean exists(String name) {
        return permissions.containsKey(name.replaceAll("^permission\\.", "").toLowerCase());
    }

    public static Permission get(String name) {
        name = name.replaceAll("^permission\\.", "");
        return permissions.containsKey(name.toLowerCase()) ? permissions.get(name.toLowerCase()) : new Permission(name);
    }

    public static TreeSet<Permission> getAll() {
        return new TreeSet<>(permissions.values());
    }
}
