package de.jojii.matrixclientserver.Bot;

public class Member {
    private final String id, display_name, avatar_url;

    public Member(String id, String display_name, String avatar_url) {
        this.id = id;
        this.display_name = display_name;
        this.avatar_url = avatar_url;
    }

    public String getId() {
        return id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id='" + id + '\'' +
                ", display_name='" + display_name + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                '}';
    }
}
