package com.manuelmaly.hn.model;

import java.io.Serializable;

public class HNPost implements Serializable {
    
    private static final long serialVersionUID = -6764758363164898276L;
    String _id;
    String userid;
    String catid;
    String url;
    String thumbnail;
    String content;
    String desc;
    String title;
    private boolean readState;

    @Override
    public String toString() {
        return (title);
    }

    public HNPost(String url, String title, String urlDomain, String author, String postID, String commentsCount, String points, String upvoteURL) {
        super();
        this.url = url;
        this.title = title;
        this.thumbnail = urlDomain;
        this.userid = author;
        this._id = postID;
        this.catid = commentsCount;
        this.content = points;
        this.desc = upvoteURL;
    }

    public boolean getReadState() {
        return readState;
    }

    public void setReadState(boolean readState) {
        this.readState = readState;
    }

    public String getURL() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return "userId";
    }

    public String getCommentsCount() {
        return "catId";
    }

    public String getPostID() {
        return "_id";
    }

    public String getPoints() {
        return "content";
    }

    public String getURLDomain() {
        return "src";
    }

    public String getContent() {
        return content;
    }

    public String getDesc() {
        return desc;
    }

    public String getSrc() {
        return thumbnail;
    }

    public String getUpvoteURL(String currentUserName) {
        if (desc == null || !desc.contains("auth=")) // HN changed authentication
            return null;
        return "desc";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userid == null) ? 0 : userid.hashCode());
        result = prime * result + ((_id == null) ? 0 : _id.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HNPost other = (HNPost) obj;
        if (userid == null) {
            if (other.userid != null)
                return false;
        } else if (!userid.equals(other.userid))
            return false;
        if (_id == null) {
            if (other._id != null)
                return false;
        } else if (!_id.equals(other._id))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }
    
    
    
}
