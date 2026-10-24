package com.vkcreation.platform.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="studio_packages")
public class StudioPackage {
    @Id @Column(length=36,nullable=false) private String id;
    @Column(nullable=false,length=120) private String name;
    @Column(nullable=false,length=80) private String category;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal price;
    @Column(length=2000) private String description;
    @Column(name="features_text",length=4000) private String featuresText;
    @Column(nullable=false) private boolean active=true;
    @Column(name="sort_order",nullable=false) private int sortOrder=0;
    protected StudioPackage(){}
    public StudioPackage(String id,String name,String category,BigDecimal price,String description,String featuresText,boolean active,int sortOrder){this.id=id;this.name=name;this.category=category;this.price=price;this.description=description;this.featuresText=featuresText;this.active=active;this.sortOrder=sortOrder;}
    public String getId(){return id;} public String getName(){return name;} public String getCategory(){return category;} public BigDecimal getPrice(){return price;} public String getDescription(){return description;} public String getFeaturesText(){return featuresText;} public boolean isActive(){return active;} public int getSortOrder(){return sortOrder;}
    public void update(String name,String category,BigDecimal price,String description,String featuresText,boolean active,int sortOrder){this.name=name;this.category=category;this.price=price;this.description=description;this.featuresText=featuresText;this.active=active;this.sortOrder=sortOrder;}
}