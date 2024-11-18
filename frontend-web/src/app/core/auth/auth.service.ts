import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { lastValueFrom, Observable, throwError, BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';
import { catchError, map, tap } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8090';
  private currentUserSubject = new BehaviorSubject<any>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  // Connexion
  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/authenticate`, { username, password }, { observe: 'response' }).pipe(
      tap((response: HttpResponse<any>) => {
        // Log de la réponse en cas de succès si nécessaire
      }),
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Une erreur s\'est produite lors de la connexion.';
        if (error.status === 401) {
          errorMessage = 'Nom d\'utilisateur ou mot de passe incorrect.';
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }
  

  // Inscription
  register(firstName: string, lastName: string, username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, { firstName, lastName, username, password }, { observe: 'response' }).pipe(
      tap((response: HttpResponse<any>) => {
        // Log la réponse en cas de succès
      }),
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Une erreur s\'est produite lors de l\'inscription.';
        if (error.status === 400) {
          errorMessage = 'Données invalides. Veuillez vérifier votre saisie.';
        } else if (error.status === 409) {
          errorMessage = 'Le nom d\'utilisateur existe déjà. Veuillez en choisir un autre.';
        } else if (error.status === 500) {
          errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Récupération des détails utilisateur
  getUserDetails(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/me`);
  }

  // Stockage des tokens
  setToken(token: string): void {
    localStorage.setItem('accessToken', token);
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  clearToken(): void {
    localStorage.removeItem('accessToken');
  }

  // Stockage du refresh token
  setRefreshToken(token: string): void {
    localStorage.setItem('refreshToken', token);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  clearRefreshToken(): void {
    localStorage.removeItem('refreshToken');
  }

  // Stockage et récupération des détails utilisateur
  setUserDetails(user: any): void {
    localStorage.setItem('userDetails', JSON.stringify(user));
    this.currentUserSubject.next(user); // Notifie de la mise à jour
  }

  getUserDetailsFromStorage(): any {
    const user = localStorage.getItem('userDetails');
    return user ? JSON.parse(user) : null;
  }

  clearUserDetails(): void {
    localStorage.removeItem('userDetails');
  }

  // Vérification de connexion
  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // Vérification de rôle admin
  async isAdmin(): Promise<boolean> {
    try {
      const userDetails = await lastValueFrom(this.getUserDetails());
      return userDetails && userDetails.role === 'ROLE_ADMIN';
    } catch {
      return false;
    }
  }

  // Déconnexion
  async logout(): Promise<void> {
    if (await this.isAdmin()) {
      this.clearToken();
      this.clearRefreshToken();
      this.clearUserDetails();
      this.router.navigate(['/admin']);
    } else {
      this.clearToken();
      this.clearRefreshToken();
      this.clearUserDetails();
      this.router.navigate(['/login']);
    }
  }

  // Rafraîchissement du token
 /* refreshToken(): Observable<string> {
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken) {
      console.error('Aucun refreshToken trouvé');
      this.logout();
      return throwError(() => new Error('No refresh token available'));
    }

    const body = { refreshToken };
    
    return this.http.post<any>(`${this.apiUrl}/refresh-token`, body).pipe(
      map(response => {
        const newAccessToken = response.accessToken;
        const newRefreshToken = response.refreshToken;

        console.log('Nouveau token d\'accès:', newAccessToken);  // Log the new access token
        console.log('Nouveau refresh token:', newRefreshToken);  // Log the new refresh token

        // Stocker le nouveau access token et le refresh token
        this.setToken(newAccessToken);  
        this.setRefreshToken(newRefreshToken);

        return newAccessToken;  // Retourne le nouveau access token
      }),
      catchError(error => {
        console.error('Erreur lors du rafraîchissement du token', error);
        this.logout();
        return throwError(() => error);
      })
    );
  }
*/


  refreshToken(): Observable<string> {
    const refreshToken = this.getRefreshToken();
  
    if (!refreshToken) {
      console.error('Aucun refreshToken trouvé');
      this.logout();
      return throwError(() => new Error('No refresh token available'));
    }
  
    const body = { refreshToken };
  
    return this.http.post<any>(`${this.apiUrl}/refresh-token`, body).pipe(
      map(response => {
        const newAccessToken = response.accessToken;
        const newRefreshToken = response.refreshToken; // Récupérer le nouveau refresh token
  
        console.log('Nouveau token d\'accès:', newAccessToken);
        console.log('Nouveau refresh token:', newRefreshToken);
  
        this.setToken(newAccessToken);  // Stocker le nouveau access token
        this.setRefreshToken(newRefreshToken);  // Stocker le nouveau refresh token
  
        return newAccessToken;
      }),
      catchError(error => {
        console.error('Erreur lors du rafraîchissement du token', error);
        this.logout();
        return throwError(() => error);
      })
    );
  }
  



}
