package com.valerijovich;

// The DAO Pattern in Java
public class Equipment {

    private String equipmentName;
    private int equipmentId;
    private int equipmentWellId;

    public Equipment() {
    }

    public Equipment(String equipmentName, int equipmentId, int equipmentWellId) {
        this.equipmentName = equipmentName;
        this.equipmentId = equipmentId;
        this.equipmentWellId = equipmentWellId;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public int getEquipmentWellId() {
        return equipmentWellId;
    }

    public void setEquipmentWellId(int equipmentWellId) {
        this.equipmentWellId = equipmentWellId;
    }
}
