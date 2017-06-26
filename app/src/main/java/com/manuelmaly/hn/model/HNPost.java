package com.manuelmaly.hn.model;

import java.io.Serializable;

public class HNPost implements Serializable {
    
    private static final long serialVersionUID = -6764758363164898276L;
    String _id;
    String userId;
    String catId;
    String url;
    String src;
    String content;
    String desc;
    String title;
    //String __v;
    //    private String mURL;
//    private String mTitle;
//    private String mAuthor;
//    private int mCommentsCount;
//    private int mPoints;
//    private String mURLDomain;
//    private String mPostID; // as found in link to comments
//    private String mUpvoteURL;

    @Override
    public String toString() {
        return (title);
    }

    public HNPost(String url, String title, String urlDomain, String author, String postID, String commentsCount, String points, String upvoteURL) {
        super();
        this.url = url;
        this.title = title;
        this.src = urlDomain;
        this.userId = author;
        this._id = postID;
        this.catId = commentsCount;
        this.content = points;
        this.desc = upvoteURL;
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

    public String getUpvoteURL(String currentUserName) {
        if (desc == null || !desc.contains("auth=")) // HN changed authentication
            return null;
        return "desc";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
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
