package com.quest.etna.repositories;

import com.quest.etna.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    // Méthode personnalisée pour récupérer les OrderItems associés à un Order spécifique
    List<OrderItem> findByOrderId(Integer orderId);
}
