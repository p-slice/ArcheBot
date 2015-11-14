package net.pslice.archebot.utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

public class Element implements Comparable<Element> {

    public static final String VERSION = "1.4";
    private final String tag;
    private final HashSet<Element> children = new HashSet<>();
    private String content;
    private boolean readonly = false;
    private int indent = 2, id = -1;

    public static Element read(String file) {
        String tag = file.contains(File.separator) ? file.substring(file.lastIndexOf(File.separatorChar)) : file;
        return read(file, tag.replaceAll("\\W", ""));
    }

    public static Element read(String filename, String tag) {
        Element element = new Element(tag);
        try {
            File file = new File(filename.endsWith(".pml") ? filename : filename + ".pml");
            if (!file.exists())
                return element;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            ArrayList<Element> parents = new ArrayList<>();
            parents.add(element);
            while ((line = reader.readLine()) != null) {
                int indent = 0;
                while (line.startsWith(" ")) {
                    indent++;
                    line = line.substring(1);
                }
                int i = 0, k = 1;
                while (i < indent && k < parents.size() - 1)
                    i += parents.get(k++).indent;
                int difference = indent - i;
                if (difference <= 0 || parents.size() == 1)
                    k--;
                Element parent = parents.get(k);
                while (parents.size() - 1 > k)
                    parents.remove(k + 1);
                String[] parts = line.split(" ", 2);
                String t = parts[0];
                if (!t.matches("[<\\(](#|&|[\\w.-]+)[>\\)]"))
                    continue;
                String content = parts.length > 1 ? parts[1].replaceAll("^ ", "") : "";
                if (t.matches("[<\\(]&[>\\)]"))
                    parent.content += "\n" + content;
                else {
                    Element e = new Element(t.substring(1, t.length() - 1), content);
                    e.readonly = t.matches("\\((#|[\\w.-]+)\\)");
                    parent.addChild(e);
                    if (difference > 0)
                        parent.indent = difference;
                    parents.add(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading '" + tag + "': " + e);
        }
        return element;
    }

    public Element(String tag) {
        this(tag, "");
    }

    public Element(String tag, String content) {
        if (!tag.matches("#|[\\w.-]+"))
            throw new RuntimeException("Error creating '" + tag + "': Illegal characters in tag.");
        this.tag = tag;
        this.content = content;
    }

    public void addChild(Element element) {
        if (readonly)
            throw new RuntimeException("Error modifying '" + tag + "': Element is set to readonly.");
        if (element.id != -1)
            throw new RuntimeException("Error modifying '" + tag + "': Element '" + element.tag + "' already has parent.");
        int id = 0;
        while (isChild(element.tag, id))
            id++;
        children.add(element);
        element.id = id;
    }

    public Element getChild(String tag) {
        return getChild(tag, tag.equals("#") ? -1 : 0);
    }

    public Element getChild(String tag, int id) {
        String[] split = tag.split("/", 2);
        tag = split[0];
        int localId = id;
        if (tag.matches("[\\w#.-]+:-?\\d+")) {
            String[] parts = tag.split(":");
            tag = parts[0];
            localId = Integer.parseInt(parts[1]);
        }
        if (localId > 0)
            while (!isChild(tag, localId))
                addChild(new Element(tag));
        if (split.length > 1)
            return getChild(tag, localId).getChild(split[1], id);
        for (Element child : children)
            if (tag.equalsIgnoreCase(child.tag) && localId == child.id)
                return child;
        Element child = new Element(tag);
        addChild(child);
        return child;
    }

    public TreeSet<Element> getChildren() {
        return new TreeSet<>(children);
    }

    public TreeSet<Element> getChildren(String tag) {
        TreeSet<Element> elements = new TreeSet<>();
        for (Element element : children)
            if (tag.equalsIgnoreCase(element.tag))
                elements.add(element);
        return elements;
    }

    public String getContent() {
        return content;
    }

    public int getId() {
        return id;
    }

    public int getIndent() {
        return indent;
    }

    public String getTag() {
        return tag;
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }

    public boolean isChild(String tag) {
        return isChild(tag, 0);
    }

    public boolean isChild(String tag, int id) {
        String[] split = tag.split("/", 2);
        tag = split[0];
        int localId = id;
        if (tag.matches("[\\w#.-]+:-?\\d+")) {
            String[] parts = tag.split(":");
            tag = parts[0];
            localId = Integer.parseInt(parts[1]);
        }
        if (split.length > 1) {
            return isChild(tag, localId) && getChild(tag, localId).isChild(split[1], id);
        }
        for (Element child : children)
            if (tag.equalsIgnoreCase(child.tag) && localId == child.id)
                return true;
        return false;
    }

    public boolean isChild(Element element) {
        return children.contains(element);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void removeChild(String tag) {
        removeChild(tag, 0);
    }

    public void removeChild(String tag, int id) {
        for (Element child : getChildren(tag))
            if (id == child.id) {
                child.id = -1;
                children.remove(child);
            }
        for (Element child : getChildren(tag))
            if (child.id > id)
                child.id--;
    }

    public void removeChild(Element element) {
        if (!isChild(element))
            throw new RuntimeException("Error modifying '" + tag + "': Not parent of element '" + element.tag + "'.");
        for (Element child : getChildren(tag))
            if (child.id > element.id)
                child.id--;
        element.id = -1;
        children.remove(element);
    }

    public void removeChildren() {
        for (Element child : children)
            child.id = -1;
        children.clear();
    }

    public void removeChildren(String tag) {
        for (Element child : getChildren(tag)) {
            child.id = -1;
            children.remove(child);
        }
    }

    public void setContent(String content) {
        if (readonly)
            throw new RuntimeException("Error modifying '" + tag + "': Element is set to readonly.");
        this.content = content;
    }

    public void setIndent(int indent) {
        if (readonly)
            throw new RuntimeException("Error modifying '" + tag + "': Element is set to readonly.");
        if (indent < 1)
            throw new RuntimeException("Error modifying '" + tag + "': Indent must be greater than 0." );
        this.indent = indent;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public int size() {
        return children.size();
    }

    public int size(String tag) {
        int size = 0;
        for (Element child : children)
            if (tag.equals(child.tag))
                size++;
        return size;
    }

    public void write() {
        write(tag);
    }

    public void write(String file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file.endsWith(".pml") ? file : file + ".pml"));
            for (Element child : getChildren())
                write(writer, child, 0);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Error writing '" + tag + "': " + e);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Element element) {
        if (tag.equalsIgnoreCase(element.tag))
            return Integer.compare(id, element.id);
        if (tag.matches("^-?\\d+$") && element.tag.matches("^-?\\d+$"))
            return Integer.compare(Integer.parseInt(tag), Integer.parseInt(element.tag));
        if (tag.matches("^[a-zA-Z_]+-?\\d+$") && element.tag.matches("^[a-zA-Z_]+-?\\d+$")
                && tag.replaceAll("-?\\d+", "").equalsIgnoreCase(element.tag.replaceAll("-?\\d+", "")))
            return Integer.compare(Integer.parseInt(tag.replaceAll("[a-zA-Z_]+", "")), Integer.parseInt(element.tag.replaceAll("[a-zA-Z_]+", "")));
        return tag.compareToIgnoreCase(element.tag);
    }

    @Override
    public String toString() {
        return (readonly ? "(" : "<") + tag + (readonly ? ") " : "> ") + content;
    }

    private static void write(BufferedWriter writer, Element element, int indent) throws IOException {
        String[] parts = element.content.split("\\n");
        writer.write(StringUtils.repeat(" ", indent));
        writer.write(element.readonly ? "(" : "<");
        writer.write(element.tag);
        writer.write(element.readonly ? ") " : "> ");
        writer.write(parts[0]);
        writer.newLine();
        for (int i = 1; i < parts.length; i++) {
            writer.write(StringUtils.repeat(" ", indent + element.indent));
            writer.write(element.readonly ? "(&) " : "<&> ");
            writer.write(parts[i]);
            writer.newLine();
        }
        for (Element child : element.getChildren())
            write(writer, child, indent + element.indent);
    }
}
