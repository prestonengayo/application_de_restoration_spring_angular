import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../core/orders/orders.service';
import { AuthService } from '../../core/auth/auth.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-history.component.html',
  styleUrls: ['./order-history.component.scss']
})
export class OrderHistoryComponent implements OnInit {
  orders: any[] = []; // Liste des commandes de l'utilisateur
  userId: number | null = null; // ID de l'utilisateur connecté
  selectedOrderId: number | null = null; // ID de la commande sélectionnée

  constructor(
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserOrders(); // Charger les commandes au démarrage
  }

  // Charger les commandes de l'utilisateur connecté
  loadUserOrders(): void {
    this.authService.getUserDetails().subscribe((user) => {
      this.userId = user.id;
      if (this.userId !== null) {
        this.orderService.getOrders().subscribe((orders) => {
          this.orders = orders;
        });
      }
    });
  }

  // Calculer le total d'une commande
  calculateOrderTotal(order: any): number {
    return order.orderItems.reduce((total: number, item: any) => total + item.price * item.quantity, 0);
  }

  // Gérer le clic pour afficher ou masquer les détails d'une commande
  toggleOrderDetails(orderId: number): void {
    if (this.selectedOrderId === orderId) {
      this.selectedOrderId = null; // Si la commande est déjà sélectionnée, la déselectionner
    } else {
      this.selectedOrderId = orderId; // Sinon, définir la commande comme sélectionnée
    }
  }

  // Vérifier si une commande est sélectionnée pour afficher les détails
  isOrderSelected(orderId: number): boolean {
    return this.selectedOrderId === orderId;
  }

  // Annuler une commande en changeant son statut à 'CANCELED'
  cancelOrder(orderId: number): void {
    this.orderService.updateOrder(orderId, ' CANCELLED').subscribe({
      next: () => {
        this.loadUserOrders(); // Recharger les commandes après annulation
      },
      error: (error) => {
        console.error('Erreur lors de l\'annulation de la commande :', error);
      }
    });
  }

  ReturnMenu(): void {
    this.router.navigate([`/home`]);
  }
}
