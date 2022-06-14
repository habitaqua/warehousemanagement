package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.company.CompanyDTO;

import java.util.Optional;

public interface CompanyDAO {

    Optional<CompanyDTO> getCompany(String companyId);
}
