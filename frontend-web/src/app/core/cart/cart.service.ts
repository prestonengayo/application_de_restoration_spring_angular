import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { MenuItem } from '../../models/menu-item.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private itemsInCart: { item: MenuItem, quantity: number }[] = []; // Stocker avec les quantités
  private cartItemCountSubject = new BehaviorSubject<number>(0); // Subject pour le nombre d'items
  cartItemCount$ = this.cartItemCountSubject.asObservable(); // Observable pour écouter les changements

  constructor() {
    this.loadCart(); // Charger les articles du panier depuis localStorage au démarrage
  }

  // Ajouter un élément au panier (ou mettre à jour la quantité s'il existe déjà)
  addToCart(item: MenuItem): void {
    const existingItem = this.itemsInCart.find(cartItem => cartItem.item.id === item.id);
    if (existingItem) {
      existingItem.quantity += 1; // Incrémenter la quantité si l'article est déjà dans le panier
    } else {
      this.itemsInCart.push({ item, quantity: 1 });
    }

    this.saveCart();
    this.updateCartItemCount(); // Sauvegarder dans localStorage après chaque ajout
  }

  // Récupérer les éléments du panier
  getCartItems(): { item: MenuItem, quantity: number }[] {
    return this.itemsInCart;
  }

  // Vider le panier
  clearCart(): void {
    this.itemsInCart = [];
    this.saveCart(); // Sauvegarder dans localStorage après vidage
  }

  // Mettre à jour la quantité d'un article spécifique
  updateItemQuantity(itemId: number, quantity: number): void {
    const item = this.itemsInCart.find(cartItem => cartItem.item.id === itemId);
    if (item) {
      item.quantity = quantity;  // Mettre à jour la quantité
      if (item.quantity <= 0) {
        this.removeItem(itemId);  // Supprimer l'élément si la quantité est 0 ou moins
      }
      this.saveCart();  // Sauvegarder les changements
    }
  }

  // Supprimer un élément du panier
  removeItem(itemId: number): void {
    this.itemsInCart = this.itemsInCart.filter(cartItem => cartItem.item.id !== itemId);
    this.saveCart();  // Sauvegarder après suppression
  }

  // Sauvegarder les articles du panier dans localStorage ou sessionStorage
  private saveCart(): void {
    sessionStorage.setItem('cart', JSON.stringify(this.itemsInCart));
  }

  // Charger les articles du panier depuis localStorage ou sessionStorage
  private loadCart(): void {
    const storedCart = sessionStorage.getItem('cart');
    if (storedCart) {
      this.itemsInCart = JSON.parse(storedCart);
    }
  }
  updateCartItemCount(): void {
    const totalItems = this.itemsInCart.reduce((acc, cartItem) => acc + cartItem.quantity, 0);
    this.cartItemCountSubject.next(totalItems); // Mettre à jour le Subject
  }
}
