import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ProfileComponent } from '../profile/profile.component';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [ProfileComponent, RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  dropdownOpen = false;
  currentUser: any;  // Variable pour stocker les détails de l'utilisateur

  constructor(private router: Router, private authService: AuthService) {}
  
  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });
  }
  

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }

  logout() {
    // Déclenchement de la déconnexion via le service d'authentification
    this.authService.logout();
  }
}
