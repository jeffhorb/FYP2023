package com.ecom.fyp2023.ModelClasses;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupName;
    private String description;
    private List<String> members; // List of user IDs who are members of the group
    List <String> admins;

    // Constructors
    public Group() {
        // Default constructor required for Firestore
    }

    public Group(String groupName, String description, List<String> members,List<String> admins) {
        this.groupName = groupName;
        this.description = description;
        this.members = members;
        this.admins = admins;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    // Getters and Setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    // Add a member to the group
    public void addMember(String memberId) {
        if (members == null) {
            members = new ArrayList<>();
        }
        members.add(memberId);
    }

    // Add a member to the group
    public void addAmins(String memberId) {
        if (admins == null) {
            admins = new ArrayList<>();
        }
        admins.add(memberId);
    }

    // Remove a member from the group
    public void removeMember(String memberId) {
        if (members != null) {
            members.remove(memberId);
        }
    }

    // Override toString() method for debugging
    @Override
    public String toString() {
        return "Group{" +
                ", groupName='" + groupName + '\'' +
                ", description='" + description + '\'' +
                ", members=" + members +
                '}';
    }
}

