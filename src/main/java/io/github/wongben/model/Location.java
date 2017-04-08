package io.github.wongben.model;

/**
 * Created by WongBen on 2017-04-08.
 */
public class Location {
    private String id;
    private String name;
    private String parentId;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
