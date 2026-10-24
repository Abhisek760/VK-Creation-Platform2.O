package com.vkcreation.platform.catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StudioPackageRepository extends JpaRepository<StudioPackage,String> {
    List<StudioPackage> findByActiveTrueOrderBySortOrderAscNameAsc();
    List<StudioPackage> findAllByOrderBySortOrderAscNameAsc();
}