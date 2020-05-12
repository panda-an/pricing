package com.lenovo.vro.pricing.entity.ext;

import com.lenovo.vro.pricing.entity.CostTapeOrder;

public class CostTapeOrderForm extends CostTapeOrder {

    private int pageNum;

    private int pageSize;

    private String userId;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
