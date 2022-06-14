package org.habitbev.warehousemanagement.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.dao.CompanyDAO;
import org.habitbev.warehousemanagement.dao.SKUDAO;
import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.sku.SKU;

import java.util.List;
import java.util.Optional;

public class CompanyService {



    CompanyDAO companyDAO;

    @Inject
    public CompanyService(@Named("configCompanyDAOImpl") CompanyDAO companyDAO) {
        this.companyDAO = companyDAO;
    }


    public Optional<CompanyDTO> getCompany(String companyId) {
        return companyDAO.getCompany(companyId);
    }
}
