package com.valerijovich;

import java.util.List;

// The DAO Pattern in Java
public class Well {

    private String wellName;
    private int wellId;
    private List<Equipment> equipmentList;

    public Well() {
    }

    public Well(int wellId, String wellName) {
        this.wellId = wellId;
        this.wellName = wellName;
    }

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
