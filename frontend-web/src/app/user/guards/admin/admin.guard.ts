import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  async canActivate(): Promise<boolean> {
    if (await this.authService.isAdmin()) {
      return true; // Permet l'accès si l'utilisateur est admin
    } else {
      this.router.navigate(['/admin']); // Redirige vers la page de connexion admin sinon
      return false;
    }
  }
}
