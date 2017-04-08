package io.github.wongben.model;

import java.util.List;

/**
 * Created by WongBen on 2017/03/31 0031.
 */
public class Result {
    private List<Shop> list;
    private int recordCount;
    private  List<Location> regionNavs;

    public List<Shop> getList() {
        return list;
    }


    public void setList(List<Shop> list) {
        this.list = list;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public List<Location> getRegionNavs() {
        return regionNavs;
    }

    public void setRegionNavs(List<Location> regionNavs) {
        this.regionNavs = regionNavs;
    }
}
