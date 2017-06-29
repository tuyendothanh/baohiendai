package com.manuelmaly.hn.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dothanhtuyen on 2017/06/25.
 */

public class CategoryListModel implements Serializable {

    /**
     * description : Tin tuc kinh doanh
     * id : 1
     * name : Kinh Doanh
     * src : http://pic3.zhimg.com/91125c9aebcab1c84f58ce4f8779551e.jpg
     */

    private List<CategoryData> data;

    public List<CategoryData> getData() {
        return data;
    }

    public void setData(List<CategoryData> data) {
        this.data = data;
    }

    public static class CategoryData implements Serializable {
        private static final long serialVersionUID = 6789758363164898276L;
        private String description;
        private String _id;
        private String name;
        private String src;

        public CategoryData(String description, String _id, String name, String src) {
            this.description = description;
            this._id = _id;
            this.name = name;
            this.src = src;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getId() {
            return _id;
        }

        public void setId(String id) {
            this._id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getThumbnail() {
            return src;
        }

        public void setThumbnail(String thumbnail) {
            this.src = thumbnail;
        }
    }
}
