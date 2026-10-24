package com.vkcreation.platform.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/packages")
public class PackageController {
    private final StudioPackageRepository packages;
    public PackageController(StudioPackageRepository packages){this.packages=packages;}
    public record PackageRequest(@NotBlank @Size(max=120) String name,@NotBlank @Size(max=80) String category,@NotNull @DecimalMin("0.00") BigDecimal price,@Size(max=2000) String description,@Size(max=4000) String featuresText,Boolean active,Integer sortOrder){}
    @GetMapping public List<StudioPackage> list(){return packages.findByActiveTrueOrderBySortOrderAscNameAsc();}
    @GetMapping("/admin") @PreAuthorize("hasRole('ADMIN')") public List<StudioPackage> adminList(){return packages.findAllByOrderBySortOrderAscNameAsc();}
    @PostMapping @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.CREATED) public StudioPackage create(@Valid @RequestBody PackageRequest r){return packages.save(new StudioPackage(UUID.randomUUID().toString(),r.name().trim(),r.category().trim(),r.price(),r.description(),r.featuresText(),r.active()==null||r.active(),r.sortOrder()==null?0:r.sortOrder()));}
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public StudioPackage update(@PathVariable String id,@Valid @RequestBody PackageRequest r){StudioPackage p=packages.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Package not found."));p.update(r.name().trim(),r.category().trim(),r.price(),r.description(),r.featuresText(),r.active()==null?p.isActive():r.active(),r.sortOrder()==null?p.getSortOrder():r.sortOrder());return packages.save(p);}
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String id){StudioPackage p=packages.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Package not found."));p.update(p.getName(),p.getCategory(),p.getPrice(),p.getDescription(),p.getFeaturesText(),false,p.getSortOrder());packages.save(p);}
}