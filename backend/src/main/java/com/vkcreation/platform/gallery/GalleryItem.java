package com.vkcreation.platform.gallery;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="gallery_items")
public class GalleryItem {
 @Id @Column(length=36,nullable=false) private String id;
 @Column(nullable=false,length=180) private String title;
 @Column(nullable=false,length=20) private String mediaType;
 @Column(nullable=false,length=1200) private String secureUrl;
 @Column(length=120) private String category;
 @Column(nullable=false) private boolean published=true;
 @Column(name="sort_order",nullable=false) private int sortOrder=0;
 @Column(name="created_at",insertable=false,updatable=false) private LocalDateTime createdAt;
 protected GalleryItem(){}
 public GalleryItem(String id,String title,String mediaType,String secureUrl,String category,boolean published,int sortOrder){this.id=id;this.title=title;this.mediaType=mediaType;this.secureUrl=secureUrl;this.category=category;this.published=published;this.sortOrder=sortOrder;}
 public String getId(){return id;} public String getTitle(){return title;} public String getMediaType(){return mediaType;} public String getSecureUrl(){return secureUrl;} public String getCategory(){return category;} public boolean isPublished(){return published;} public int getSortOrder(){return sortOrder;} public LocalDateTime getCreatedAt(){return createdAt;}
 public void update(String title,String mediaType,String secureUrl,String category,boolean published,int sortOrder){this.title=title;this.mediaType=mediaType;this.secureUrl=secureUrl;this.category=category;this.published=published;this.sortOrder=sortOrder;}
}