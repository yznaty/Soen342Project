package com.taskmanager;

public class Tag {

    private String name;

    public Tag(String name) {
        this.name = name.toLowerCase().trim();
    }

    public String getName() { return name; }

    public String toString() { return name; }

    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            Tag other = (Tag) obj;
            return this.name.equals(other.name);
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
