package com.vkcreation.platform.gallery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GalleryRepository extends JpaRepository<GalleryItem,String> {
 List<GalleryItem> findByPublishedTrueOrderBySortOrderAscCreatedAtDesc();
 List<GalleryItem> findAllByOrderBySortOrderAscCreatedAtDesc();
}