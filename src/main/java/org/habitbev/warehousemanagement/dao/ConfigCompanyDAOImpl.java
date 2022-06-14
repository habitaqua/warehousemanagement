package org.habitbev.warehousemanagement.dao;

import com.google.common.collect.ImmutableMap;
import org.habitbev.warehousemanagement.entities.company.Company;
import org.habitbev.warehousemanagement.entities.company.CompanyDTO;

import java.util.Map;
import java.util.Optional;

public class ConfigCompanyDAOImpl implements CompanyDAO {

    Map<String, Company> companyConfig = ImmutableMap.of("VIVALA-BEVERAGES", Company.builder().id("VIVALA-BEVERAGES")
            .name("VIVALA-BEVERAGES").description("VIVALA-BEVERAGES").build());

    @Override
    public Optional<CompanyDTO> getCompany(String companyId) {
        Company company = companyConfig.get(companyId);
        if(company!=null) {
            return Optional.of(CompanyDTO.fromCompany(company));
        }
        return Optional.empty();
    }
}
