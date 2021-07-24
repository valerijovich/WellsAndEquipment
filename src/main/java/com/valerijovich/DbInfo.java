package com.valerijovich;

import java.util.ArrayList;
import java.util.List;

public class DbInfo {

    private List<Well> wellList = new ArrayList<>();

    public DbInfo() {
    }

    public DbInfo(List<Well> wellList) {
        this.wellList = wellList;
    }

    public List<Well> getWellList() {
        return wellList;
    }

    public void setWellList(List<Well> wellList) {
        this.wellList = wellList;
    }
}
