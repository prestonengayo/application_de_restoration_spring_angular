import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { HttpClientModule, HttpResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule,RouterLink, HttpClientModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  public username: string = '';
  public password: string = '';
  public showPassword = false;
  public isFormValid = false;
  loginForm: FormGroup;
  loginError: string = "";
  public showLoginError = false;  


  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.required, Validators.minLength(4)]]
    });
  }


  /**
   * toggleShowPassword
   */
  public toggleShowPassword() {
    this.showPassword = !this.showPassword;
  }

    /**
   * submited login form 
   */
    onSubmit() {
      console.log('Soumission du formulaire de connexion');
      if (this.loginForm.valid) {
        console.log('Formulaire valide');
        const { username, password } = this.loginForm.value;
        this.authService.login(username, password).subscribe({
          next: (response) => {
            console.log('Réponse de l\'API:', response);
            if (response.body && response.body.accessToken && response.body.refreshToken) {
              
              this.authService.setToken(response.body.accessToken);
              this.authService.setRefreshToken(response.body.refreshToken);

              console.log('Access Token stocké:', this.authService.getToken());
              console.log('Refresh Token stocké:', this.authService.getRefreshToken());
    
              // Appeler l'endpoint /me pour obtenir les détails de l'utilisateur
              this.authService.getUserDetails().subscribe({
                next: (userDetails) => {
                  console.log('Détails de l\'utilisateur:', userDetails);
                  // Stocker les détails de l'utilisateur en localStorage
                  this.authService.setUserDetails(userDetails);
    
                  // Rediriger vers la page d'accueil après avoir obtenu les détails
                  this.router.navigate(['/home']);
                },
                error: (error) => {
                  console.error('Erreur lors de la récupération des détails de l\'utilisateur:', error);
                  // Gérer l'erreur ici, si nécessaire
                }
              });
            }
          },
          error: (error) => {
            this.loginError = 'Nom d\'utilisateur ou mot de passe incorrect';
            this.showLoginError = true;
          }
        });
      }
    }
    goToMenu(): void {
      this.router.navigate([`/home`]);
    }

}
