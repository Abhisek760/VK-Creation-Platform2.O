package com.vkcreation.platform.gallery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/gallery")
public class GalleryController {
 private final GalleryRepository gallery;
 public GalleryController(GalleryRepository gallery){this.gallery=gallery;}
 public record MediaRequest(@NotBlank @Size(max=180) String title,@NotBlank String mediaType,@NotBlank @Size(max=1200) String secureUrl,@Size(max=120) String category,Boolean published,Integer sortOrder){}
 @GetMapping public List<GalleryItem> publicGallery(){return gallery.findByPublishedTrueOrderBySortOrderAscCreatedAtDesc();}
 @GetMapping("/admin") @PreAuthorize("hasRole('ADMIN')") public List<GalleryItem> adminList(){return gallery.findAllByOrderBySortOrderAscCreatedAtDesc();}
 @PostMapping @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.CREATED) public GalleryItem create(@Valid @RequestBody MediaRequest r){validate(r);return gallery.save(new GalleryItem(UUID.randomUUID().toString(),r.title().trim(),r.mediaType().toUpperCase(),r.secureUrl().trim(),r.category(),r.published()==null||r.published(),r.sortOrder()==null?0:r.sortOrder()));}
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public GalleryItem update(@PathVariable String id,@Valid @RequestBody MediaRequest r){validate(r);GalleryItem item=gallery.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Gallery item not found."));item.update(r.title().trim(),r.mediaType().toUpperCase(),r.secureUrl().trim(),r.category(),r.published()==null?item.isPublished():r.published(),r.sortOrder()==null?item.getSortOrder():r.sortOrder());return gallery.save(item);}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String id){if(!gallery.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Gallery item not found.");gallery.deleteById(id);}
 private void validate(MediaRequest r){if(!List.of("IMAGE","VIDEO").contains(r.mediaType().toUpperCase()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"mediaType must be IMAGE or VIDEO.");try{URI u=URI.create(r.secureUrl());if(!List.of("https").contains(u.getScheme())||u.getHost()==null)throw new IllegalArgumentException();}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Media URL must be a valid HTTPS URL.");}}
}