package com.quest.etna.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    @NotNull(message = "MenuItem is required")
    private MenuItem menuItem;

    @NotNull(message = "Quantity is required")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Price is required")
    @Column(nullable = false)
    private Double price;

    public OrderItem() {
        // Default constructor
    }

    public OrderItem(Order order, MenuItem menuItem, Integer quantity, Double price) {
        setOrder(order);
        setMenuItem(menuItem);
        setQuantity(quantity);
        setPrice(price);
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }
        this.order = order;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        if (menuItem == null) {
            throw new IllegalArgumentException("MenuItem cannot be null.");
        }
        this.menuItem = menuItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price must be non-negative.");
        }
        this.price = price;
    }

    /**
     * Calculate the total price for this item in the order.
     * @return Total price (price * quantity)
     */
    public Double getTotalPrice() {
        return this.price * this.quantity;
    }

    // Overriding equals, hashCode, and toString methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id) &&
               Objects.equals(order, orderItem.order) &&
               Objects.equals(menuItem, orderItem.menuItem) &&
               Objects.equals(quantity, orderItem.quantity) &&
               Objects.equals(price, orderItem.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, order, menuItem, quantity, price);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
               "id=" + id +
               ", orderId=" + (order != null ? order.getId() : null) +
               ", menuItem=" + (menuItem != null ? menuItem.getName() : null) +
               ", quantity=" + quantity +
               ", price=" + price +
               '}';
    }
}
