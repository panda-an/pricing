package com.lenovo.vro.pricing.service.db;

import java.io.FileNotFoundException;

public interface DbService {

    String insertWarranty() throws FileNotFoundException;

    String insertEo() throws FileNotFoundException;

    String insertGsc() throws FileNotFoundException;

    String insertMbgFreight() throws FileNotFoundException;

    String insertFreight() throws FileNotFoundException;

    String insertCostTapeBuMapping() throws FileNotFoundException;
}
