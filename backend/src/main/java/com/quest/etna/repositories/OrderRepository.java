package com.quest.etna.repositories;

import com.quest.etna.model.Order;
import com.quest.etna.model.User;
import com.quest.etna.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Trouver toutes les commandes d'un utilisateur
    List<Order> findByUser(User user);
    
    List<Order> findByUserId(Integer userId);

    // Trouver toutes les commandes par statut
    List<Order> findByStatus(OrderStatus status);

    // Trouver les commandes d'un utilisateur par statut
    List<Order> findByUserAndStatus(User user, OrderStatus status);
}
