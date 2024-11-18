import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { CartService } from '../../core/cart/cart.service'; // Assurez-vous que le chemin est correct
import { MenuItem } from '../../models/menu-item.model'; // Assurez-vous que le chemin est correct
import { AuthService } from '../../core/auth/auth.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule], // Importer CommonModule et FormsModule ici
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  isUserLoggedIn: boolean = false;
  menuItems: MenuItem[] = [];
  filteredMenuItems: MenuItem[] = [];
  errorMessage: string = '';
  user: any = null;
  cartItemCount: number = 0;
  quantities: { [itemId: number]: number } = {};
  cartMessage: string = '';

  constructor(private http: HttpClient, private cartService: CartService, private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    console.log(this.cartService.getCartItems());
    this.isUserLoggedIn = this.authService.isLoggedIn();
    if (this.isUserLoggedIn) {
      // Charger les détails de l'utilisateur connecté
      this.authService.getUserDetails().subscribe((userDetails) => {
        this.user = userDetails; // Stocker les détails de l'utilisateur dans la propriété user
      });
    }
    // Charger les éléments du panier dès que la page s'actualise
    this.cartService.updateCartItemCount();
    
    // Ecouter les changements dans le panier
    this.cartService.cartItemCount$.subscribe(count => {
      this.cartItemCount = count;
    });

    this.loadMenuItems();
  }

  // Méthode pour charger le nombre d'items dans le panier
  updateCartItemCount(): void {
    const cartItems = this.cartService.getCartItems();
    this.cartItemCount = cartItems.length;
    console.log('Nombre d\'items dans le panier:', this.cartItemCount);
  }

  loadMenuItems(): void {
    this.http.get<MenuItem[]>('http://localhost:8090/menu-item').subscribe({
      next: (response) => {
        this.menuItems = response;
        this.filteredMenuItems = response; // Par défaut, afficher tous les items
        this.menuItems.forEach(item => {
          this.quantities[item.id] = 1; // Définir la quantité par défaut à 1
        });
      },
      
      error: (error) => {
        this.errorMessage = 'Erreur lors du chargement des items du menu.';
        console.error(error);
      }
    });
  }

  addToCart(item: MenuItem): void {
    if (this.authService.isLoggedIn()) {
      const quantity = this.quantities[item.id];
      for (let i = 0; i < quantity; i++) {
        this.cartService.addToCart(item);
      }
      console.log(`Ajout de ${quantity} ${item.name}(s) au panier`);

      // Afficher un message temporaire lorsque l'article est ajouté au panier
      this.cartMessage = `${quantity} ${item.name}(s) ajouté(s) au panier`;
      setTimeout(() => {
        this.cartMessage = ''; // Masquer le message après 3 secondes
      }, 3000);

    } else {
      alert('Vous devez être connecté pour ajouter des articles au panier.');
      this.router.navigate(['/login']);
    }
  }

  filterMenu(category: string): void {
    if (category === 'all') {
      this.filteredMenuItems = this.menuItems; // Afficher tous les items
    } else {
      this.filteredMenuItems = this.menuItems.filter(item => item.categoryName === category);
    }
  }

  logout(): void {
    this.authService.clearToken();
    this.authService.clearRefreshToken();
    this.authService.clearUserDetails();
    this.cartService.clearCart();
    console.log('token: ', this.authService.getToken());
    this.router.navigate(['/login']);
  }

  goToProfile(): void {
    const userId = this.authService.getUserDetailsFromStorage().id;
    this.router.navigate([`/user/${userId}`]);
  }

  goToHistory(): void {
    this.router.navigate([`/history`]);
  }
}

