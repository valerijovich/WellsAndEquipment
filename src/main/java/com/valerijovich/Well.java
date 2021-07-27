package com.valerijovich;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

// The DAO Pattern in Java
@XStreamAlias("well")
public class Well {

    @XStreamAsAttribute
    @XStreamAlias("name")
    private String wellName;
    @XStreamAsAttribute
    @XStreamAlias("id")
    private int wellId;
    @XStreamImplicit
    private List<Equipment> equipmentList;

    public int getWellId() {
        return wellId;
    }

    public void setWellId(int wellId) {
        this.wellId = wellId;
    }

    public String getWellName() {
        return wellName;
    }

    public void setWellName(String wellName) {
        this.wellName = wellName;
    }

    public List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }
}
