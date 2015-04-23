package net.pslice.archebot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Permission {

    private static final HashMap<String, Permission> permissions = new HashMap<>();
    public static final Permission DEFAULT = new Permission("default"),
                                   OPERATOR = new Permission("operator"),
                                   IGNORE = new Permission("ignore");
    private final HashSet<Permission> inclusions = new HashSet<>();
    private final String ID;

    private Permission(String ID) {
        if (!ID.startsWith("permission."))
            ID = "permission." + ID;
        this.ID = ID;
        permissions.put(ID, this);
    }

    public void include(Permission... permissions) {
        inclusions.addAll(Arrays.asList(permissions));
    }

    public void include(String... permissions) {
        for (String permission : permissions)
            inclusions.add(get(permission));
    }

    public boolean includes(Permission permission) {
        return inclusions.contains(permission);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Permission && obj.toString().equals(this.toString());
    }

    @Override
    public String toString() {
        return ID;
    }

    public static boolean exists(String ID) {
        if (!ID.startsWith("permission."))
            ID = "permission." + ID;
        return permissions.containsKey(ID);
    }

    public static Permission get(String ID) {
        if (!ID.startsWith("permission."))
            ID = "permission." + ID;
        return permissions.containsKey(ID) ? permissions.get(ID) : new Permission(ID);
    }

    public static Set<Permission> getAll() {
        return new HashSet<>(permissions.values());
    }
}
