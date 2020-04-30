package com.lenovo.vro.pricing.service.db;

import java.io.FileNotFoundException;

public interface DbService {

    String insertWarranty() throws FileNotFoundException;

    void insertCostTapeFamilyMapping();
}
