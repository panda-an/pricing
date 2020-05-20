package com.lenovo.vro.pricing.entity;

import java.util.Date;

public class MachineTypePhMapping {
    private Integer id;

    private String phCode;

    private String machineType;

    private Date insertTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhCode() {
        return phCode;
    }

    public void setPhCode(String phCode) {
        this.phCode = phCode == null ? null : phCode.trim();
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType == null ? null : machineType.trim();
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}