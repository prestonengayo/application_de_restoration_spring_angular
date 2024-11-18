package com.quest.etna.controller;

import com.quest.etna.model.Order;
import com.quest.etna.model.User;
import com.quest.etna.model.UserRole;
import com.quest.etna.repositories.OrderRepository;
import com.quest.etna.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    // GET: Récupérer toutes les commandes (Admin) ou les commandes de l'utilisateur
    @GetMapping("")
    public ResponseEntity<List<Order>> getAllOrders() {
        User authenticatedUser = getAuthenticatedUser();
        List<Order> orders;
        if (authenticatedUser.getRole() == UserRole.ROLE_ADMIN) {
            orders = orderRepository.findAll(); // Admin peut voir toutes les commandes
        } else {
            orders = orderRepository.findByUser(authenticatedUser); // Utilisateur voit ses commandes
        }
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    // GET: Récupérer une commande par son ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable("id") Integer id) {
        Optional<Order> order = orderRepository.findById(id);
        User authenticatedUser = getAuthenticatedUser();
        
        if (order.isPresent()) {
            if (isAuthorized(authenticatedUser, order.get())) {
                return ResponseEntity.status(HttpStatus.OK).body(order.get());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to access this order");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable("id") Integer id, @RequestBody Order updatedOrder) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            Order existingOrder = order.get();
            existingOrder.setStatus(updatedOrder.getStatus()); // On modifie uniquement le statut
            orderRepository.save(existingOrder);
            return ResponseEntity.status(HttpStatus.OK).body(existingOrder);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
    }

    
    // POST: Créer une nouvelle commande
    @PostMapping("")
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        User authenticatedUser = getAuthenticatedUser();
        order.setUser(authenticatedUser);
        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    
    // DELETE: Supprimer une commande (Admin uniquement)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable("id") Integer id) {
        Optional<Order> order = orderRepository.findById(id);
        User authenticatedUser = getAuthenticatedUser();

        Map<String, String> response = new HashMap<>();

        if (order.isPresent()) {
            if (authenticatedUser.getRole() == UserRole.ROLE_ADMIN || authenticatedUser.getId().equals(order.get().getUser().getId())) {
                orderRepository.delete(order.get());
                response.put("message", "Order deleted successfully");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                response.put("message", "You do not have permission to delete this order");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        } else {
            response.put("message", "Order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    // Utilitaire pour obtenir l'utilisateur authentifié
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }

    // Vérifie si l'utilisateur est autorisé à accéder à la commande
    private boolean isAuthorized(User user, Order order) {
        return user.getRole() == UserRole.ROLE_ADMIN || user.getId().equals(order.getUser().getId());
    }
}
