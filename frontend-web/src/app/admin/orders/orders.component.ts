import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { OrderService } from '../../core/orders/orders.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit {
  orders: any[] = []; // Liste des commandes avec OrderItems
  selectedOrder: any = null; // Commande sélectionnée pour la modification
  orderForm: FormGroup; // Formulaire pour la mise à jour du statut de la commande
  isModalOpen = false; // Contrôle de l'ouverture du modal

  constructor(
    private orderService: OrderService,
    private fb: FormBuilder
  ) {
    this.orderForm = this.fb.group({
      status: ['', Validators.required] // Permet la modification du statut de la commande
    });
  }

  ngOnInit(): void {
    this.loadOrders(); // Charger les commandes au démarrage
  }

  // Charger toutes les commandes
  loadOrders(): void {
    this.orderService.getOrders().subscribe((orders) => {
      this.orders = orders;
    });
  }

  // Calculer le total d'une commande
  calculateOrderTotal(order: any): number {
    return order.orderItems.reduce((total: number, item: any) => total + item.price * item.quantity, 0);
  }

  // Ouvrir le modal pour modifier le statut d'une commande
  openOrderModal(order: any): void {
    this.isModalOpen = true;
    this.selectedOrder = order;
    this.orderForm.patchValue({
      status: order.status // Pré-remplir le statut
    });
  }

  // Fermer le modal
  closeOrderModal(): void {
    this.isModalOpen = false;
    this.selectedOrder = null;
    this.orderForm.reset();
  }

  // Soumettre le formulaire pour mettre à jour le statut de la commande
  onSubmitOrder(): void {
    if (this.orderForm.valid && this.selectedOrder) {
      const updatedStatus = this.orderForm.value.status;
      this.orderService.updateOrder(this.selectedOrder.id, updatedStatus).subscribe(() => {
        this.loadOrders();
        this.closeOrderModal();
      });
    }
  }
  
  

  // Supprimer une commande
  deleteOrder(orderId: number): void {
    this.orderService.deleteOrder(orderId).subscribe(() => {
      this.orders = this.orders.filter(order => order.id !== orderId);
    });
  }
}
