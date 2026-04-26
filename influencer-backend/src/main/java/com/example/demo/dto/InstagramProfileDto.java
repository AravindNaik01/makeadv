package com.example.demo.dto;

public class InstagramProfileDto {
    private int followers;
    private int following;
    private int posts;
    private int avgLikes;
    private int avgComments;
    private boolean verified;

    public InstagramProfileDto() {}

    public InstagramProfileDto(int followers, int following, int posts, int avgLikes, int avgComments, boolean verified) {
        this.followers = followers;
        this.following = following;
        this.posts = posts;
        this.avgLikes = avgLikes;
        this.avgComments = avgComments;
        this.verified = verified;
    }

    public int getFollowers() { return followers; }
    public void setFollowers(int followers) { this.followers = followers; }

    public int getFollowing() { return following; }
    public void setFollowing(int following) { this.following = following; }

    public int getPosts() { return posts; }
    public void setPosts(int posts) { this.posts = posts; }

    public int getAvgLikes() { return avgLikes; }
    public void setAvgLikes(int avgLikes) { this.avgLikes = avgLikes; }

    public int getAvgComments() { return avgComments; }
    public void setAvgComments(int avgComments) { this.avgComments = avgComments; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
