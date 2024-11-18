import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { CartService } from '../../core/cart/cart.service';
import { MenuItem } from '../../models/menu-item.model';
import { AddressService } from '../../core/address/address.service'
import { AuthService } from '../../core/auth/auth.service';
import { OrderService } from '../../core/orders/orders.service';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule, } from '@angular/forms';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule], // Importer CommonModule ici
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {
  cartItems: { item: MenuItem, quantity: number }[] = [];
  addresses: any[] = [];
  selectedAddressId: number | null = null; // Adresse sélectionnée pour la livraison
  isAddressModalOpen = false; // Pour gérer l'affichage du modal
  addressForm: FormGroup;
  
  constructor(private cartService: CartService, 
              private addressService: AddressService, 
              private fb: FormBuilder,
              private orderService: OrderService, 
              private authService: AuthService,
              private router: Router) 
    { 
    this.addressForm = this.fb.group({
      street: ['', Validators.required],
      postalCode: ['', Validators.required],
      city: ['', Validators.required],
      country: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadCartItems();  // Charger les éléments du panier au démarrage
    this.loadUserAddresses();  // Charger les adresses de l'utilisateur
  }

  loadCartItems(): void {
    this.cartItems = this.cartService.getCartItems();
    console.log(this.cartItems);  // Charger les articles depuis le service
  }

  loadUserAddresses(): void {
    this.addressService.getAddresses().subscribe((addresses) => {
      this.addresses = addresses;

      // Sélectionner la première adresse par défaut s'il y en a
      if (this.addresses.length > 0) {
        this.selectedAddressId = this.addresses[0].id;
      }
    });
  }

  updateQuantity(itemId: number, quantity: number): void {
    this.cartService.updateItemQuantity(itemId, quantity);  // Mettre à jour la quantité
    this.loadCartItems();  // Recharger les articles pour voir les modifications
  }

  getTotalPrice(): number {
    return this.cartItems.reduce((total, cartItem) => total + (cartItem.item.price * cartItem.quantity), 0);
  }

  openAddressModal(): void {
    this.isAddressModalOpen = true;
    // Ouvrir le modal pour ajouter une nouvelle adresse
  }

  closeAddressModal(): void {
    this.isAddressModalOpen = false;
    this.addressForm.reset();
  }

  onSubmitAddress(): void {
    if (this.addressForm.valid) {
      this.addressService.addAddress(this.addressForm.value).subscribe({
        next: (newAddress) => {
          this.addresses.push(newAddress);
          this.selectedAddressId = newAddress.id; // Sélectionner la nouvelle adresse
          this.closeAddressModal();
        },
        error: (error) => console.error('Erreur lors de l\'ajout de l\'adresse:', error)
      });
    }
  }

  validateCart(): void {
    if (!this.selectedAddressId) {
      console.error('Aucune adresse sélectionnée');
      return;
    }
  
    // Récupérer l'utilisateur connecté
    this.authService.getUserDetails().subscribe((user) => {
      const orderData = {
        status: 'PENDING', // Statut de la commande
        orderItems: this.cartItems.map(cartItem => ({
          menuItem: { id: cartItem.item.id },  // Seul l'ID du MenuItem est nécessaire
          quantity: cartItem.quantity,
          price: cartItem.item.price
        }))
      };
  
      console.log('Données de la commande:', orderData);
  
      // Sauvegarder la commande via le service OrderService
      this.orderService.createOrder(orderData).subscribe({
        next: (response) => {
          console.log('Commande validée avec succès:', response);
  
          // Vider le panier après validation
          this.cartService.clearCart();
  
          // Rediriger vers l'historique des commandes
          this.router.navigate(['/order-history']);
        },
        error: (error) => {
          console.error('Erreur lors de la validation de la commande:', error);
        }
      });
    });
  }
}
