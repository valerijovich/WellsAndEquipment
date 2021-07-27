package com.valerijovich;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("dbinfo")
public class DbInfo {

    @XStreamImplicit
    private List<Well> wellList = new ArrayList<>();

    public List<Well> getWellList() {
        return wellList;
    }

    public void setWellList(List<Well> wellList) {
        this.wellList = wellList;
    }
}
