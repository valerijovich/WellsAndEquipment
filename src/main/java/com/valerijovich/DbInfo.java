package com.valerijovich;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

// Класс для отображения в XML файле списка скважин из таблицы Well между тегами <dbinfo>...</dbinfo>
@XStreamAlias("dbinfo")
public class DbInfo {

    // Список скважин для отображения в XML файле
    @XStreamImplicit
    private List<Well> wellList = new ArrayList<>();

    public List<Well> getWellList() {
        return wellList;
    }

    public void setWellList(List<Well> wellList) {
        this.wellList = wellList;
    }
}
