package com.valerijovich;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

// The DAO Pattern in Java
@XStreamAlias("equipment")
public class Equipment {

    @XStreamAsAttribute
    @XStreamAlias("name")
    private String equipmentName;
    @XStreamAsAttribute
    @XStreamAlias("id")
    private int equipmentId;
    @XStreamOmitField()
    private int equipmentWellId;

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
