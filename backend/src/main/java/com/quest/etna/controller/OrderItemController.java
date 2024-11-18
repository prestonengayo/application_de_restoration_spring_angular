package com.quest.etna.controller;

import com.quest.etna.model.OrderItem;
import com.quest.etna.repositories.OrderItemRepository;
import com.quest.etna.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/order-item")
public class OrderItemController {

    @Autowired
    private OrderItemRepository orderItemRepository;

    private final UserRepository userRepository;

    OrderItemController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET: Récupérer tous les order items
    @GetMapping("")
    public ResponseEntity<List<OrderItem>> getAllOrderItems() {
        return ResponseEntity.status(HttpStatus.OK).body(orderItemRepository.findAll());
    }

    // GET: Récupérer un order item par son ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderItemById(@PathVariable("id") Integer id) {
        Optional<OrderItem> orderItem = orderItemRepository.findById(id);
        return orderItem.<ResponseEntity<?>>map(item -> ResponseEntity.status(HttpStatus.OK).body(item))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order item not found"));
    }

    // POST: Créer un nouvel order item
    @PostMapping("")
    public ResponseEntity<?> createOrderItem(@RequestBody OrderItem orderItem) {
        orderItemRepository.save(orderItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderItem);
    }

    // PUT: Mettre à jour un order item
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderItem(@PathVariable("id") Integer id, @RequestBody OrderItem updatedOrderItem) {
        Optional<OrderItem> orderItem = orderItemRepository.findById(id);
        if (orderItem.isPresent()) {
            OrderItem item = orderItem.get();
            item.setQuantity(updatedOrderItem.getQuantity());
            item.setPrice(updatedOrderItem.getPrice());
            orderItemRepository.save(item);
            return ResponseEntity.status(HttpStatus.OK).body(item);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order item not found");
        }
    }

    // DELETE: Supprimer un order item
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrderItem(@PathVariable("id") Integer id) {
        Optional<OrderItem> orderItem = orderItemRepository.findById(id);
        if (orderItem.isPresent()) {
            orderItemRepository.delete(orderItem.get());
            return ResponseEntity.status(HttpStatus.OK).body("Order item deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order item not found");
        }
    }
}
