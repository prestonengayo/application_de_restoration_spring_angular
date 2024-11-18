package com.quest.etna.dto;

public class MenuItemDTO {

    private Integer id;
    private String name;
    private Double price;
    private String description;
    private String imageUrl;
    private String categoryName; // Remplacer l'ID de la catégorie par son nom

    // Constructeur par défaut
    public MenuItemDTO() {
    }

    // Constructeur avec paramètres
    public MenuItemDTO(Integer id, String name, Double price, String description, String imageUrl, String categoryName) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.categoryName = categoryName;
    }

    // Getters et setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer integer) {
        this.id = integer;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // Méthode toString pour afficher les informations de l'objet
    @Override
    public String toString() {
        return "MenuItemDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }

    // Méthode equals pour comparer deux objets
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuItemDTO that = (MenuItemDTO) o;

        if (!id.equals(that.id)) return false;
        if (!name.equals(that.name)) return false;
        if (!price.equals(that.price)) return false;
        if (!description.equals(that.description)) return false;
        if (!imageUrl.equals(that.imageUrl)) return false;
        return categoryName.equals(that.categoryName);
    }

    // Méthode hashCode pour obtenir le hash de l'objet
    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + imageUrl.hashCode();
        result = 31 * result + categoryName.hashCode();
        return result;
    }
}
