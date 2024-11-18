package com.quest.etna.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Name cannot be empty")
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull(message = "Price cannot be empty")
    @Column(nullable = false)
    private Double price;

    @Column(name = "description", length = 1000)
    private String description;

    @Pattern(regexp = "^(http|https)://.*$", message = "Image URL should be a valid URL")
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    private Category category;  // Each menu item belongs to a category

    // Default constructor
    public MenuItem() {
    }

    // Constructor with all fields
    public MenuItem(String name, Double price, String description, String imageUrl, Category category) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // equals, hashCode, and toString methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return id.equals(menuItem.id) &&
               name.equals(menuItem.name) &&
               price.equals(menuItem.price) &&
               Objects.equals(description, menuItem.description) &&
               Objects.equals(imageUrl, menuItem.imageUrl) &&
               category.equals(menuItem.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, description, imageUrl, category);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", price=" + price +
               ", description='" + description + '\'' +
               ", imageUrl='" + imageUrl + '\'' +
               ", category=" + category +
               '}';
    }
}
