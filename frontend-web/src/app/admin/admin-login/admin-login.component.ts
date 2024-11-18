import { CommonModule } from '@angular/common';
import { HttpClientModule, HttpResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';


@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink, HttpClientModule],
  templateUrl: './admin-login.component.html',
  styleUrls: ['./admin-login.component.scss']
})
export class AdminLoginComponent {
  public showPassword = false;
  loginForm: FormGroup;
  loginError: string = "";
  public showLoginError = false;  

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

  public toggleShowPassword() {
    this.showPassword = !this.showPassword;
  }


/*
    onSubmit() {
      if (this.loginForm.valid) {
        const { username, password } = this.loginForm.value;
        this.authService.login(username, password).subscribe({
          next: (response: HttpResponse<any>) => {
            const accessToken = response.body.accessToken;
            const refreshToken = response.body.refreshToken;
    
            if (accessToken && refreshToken) {
              this.authService.setToken(accessToken);
              this.authService.setRefreshToken(refreshToken);
    
              this.authService.getUserDetails().subscribe({
                next: (userDetails: any) => {
                  if (userDetails.role === 'ROLE_ADMIN') {
                    this.authService.setUserDetails(userDetails);
                    this.router.navigate(['/dashboard']);
                  } else {
                    this.loginError = 'Accès refusé : seuls les administrateurs peuvent se connecter ici.';
                    this.showLoginError = true;
                    setTimeout(() => {
                      this.showLoginError = false;
                    }, 5000);
                  }
                },
                error: (error: any) => {
                  console.error('Erreur lors de la récupération des détails de l\'utilisateur:', error);
                }
              });
            }
          },
          error: () => {
            this.loginError = 'Nom d\'utilisateur ou mot de passe incorrect';
            this.showLoginError = true;
            setTimeout(() => {
              this.showLoginError = false;
            }, 5000);
          }
        });
      }
    }
    */

    onSubmit() {
      if (this.loginForm.valid) {
        const { username, password } = this.loginForm.value;
        this.authService.login(username, password).subscribe({
          next: (response: HttpResponse<any>) => {
            const accessToken = response.body.accessToken;
            const refreshToken = response.body.refreshToken;
    
            if (accessToken && refreshToken) {
              // Stocker les tokens dans localStorage
              this.authService.setToken(accessToken);
              this.authService.setRefreshToken(refreshToken);
    
              // Log pour vérifier si les tokens sont bien stockés
              console.log('Access Token stored:', localStorage.getItem('accessToken'));
              console.log('Refresh Token stored:', localStorage.getItem('refreshToken'));
    
              // Récupération des détails utilisateur
              this.authService.getUserDetails().subscribe({
                next: (userDetails: any) => {
                  if (userDetails.role === 'ROLE_ADMIN') {
                    this.authService.setUserDetails(userDetails);
                    this.router.navigate(['/dashboard']);
                  } else {
                    this.loginError = 'Accès refusé : seuls les administrateurs peuvent se connecter ici.';
                    this.showLoginError = true;
                    setTimeout(() => {
                      this.showLoginError = false;
                    }, 5000);
                  }
                },
                error: (error: any) => {
                  console.error('Erreur lors de la récupération des détails de l\'utilisateur:', error);
                }
              });
            }
          },
          error: () => {
            this.loginError = 'Nom d\'utilisateur ou mot de passe incorrect';
            this.showLoginError = true;
            setTimeout(() => {
              this.showLoginError = false;
            }, 5000);
          }
        });
      }
    }
    
    
    

}
