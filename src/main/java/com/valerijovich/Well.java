package com.valerijovich;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

// Класс, соответствующий таблице с оборудованием Well
@XStreamAlias("well")
public class Well {

    // Имя скважины
    @XStreamAsAttribute
    @XStreamAlias("name")
    private String wellName;

    // Порядковый номер скважины в таблице
    @XStreamAsAttribute
    @XStreamAlias("id")
    private int wellId;

    // Список оборудования для отображения в XML файле
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
