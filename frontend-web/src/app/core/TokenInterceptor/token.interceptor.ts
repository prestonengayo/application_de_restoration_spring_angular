import { HttpEvent, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { AuthService } from '../auth/auth.service';
import { inject } from '@angular/core';

export function TokenInterceptor(req: HttpRequest<any>, next: HttpHandlerFn): Observable<HttpEvent<any>> {
  const authService = inject(AuthService);

  // Passer les requêtes de login et de registre sans token
  if (req.url.includes('/authenticate') || req.url.includes('/register')) {
    return next(req);
  }

  const token = authService.getToken();
  let authReq = req;

  // Vérifier si l'en-tête Authorization n'existe pas déjà
  if (token && !req.headers.has('Authorization')) {
    authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  // BehaviorSubject pour suivre le statut du refresh
  const refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);
  let isRefreshing = false;
  
  // Fonction de gestion des erreurs 401
  function handle401Error(request: HttpRequest<any>): Observable<HttpEvent<any>> {
    if (!isRefreshing) {
      isRefreshing = true;
      refreshTokenSubject.next(null);  // Reset le sujet du token
  
      return authService.refreshToken().pipe(
        switchMap((response: any) => {
          isRefreshing = false;
          const newAccessToken = response.accessToken;
          const newRefreshToken = response.refreshToken;
          
          // Stocker à la fois le nouveau access token et refresh token
          authService.setToken(newAccessToken);
          authService.setRefreshToken(newRefreshToken);
          
          refreshTokenSubject.next(newAccessToken);  // Stocke le nouveau access token
  
          // Refaire la requête initiale avec le nouveau access token
          return next(request.clone({ setHeaders: { Authorization: `Bearer ${newAccessToken}` } }));
        }),
        catchError((error) => {
          isRefreshing = false;
          authService.logout();  // Déconnecte l'utilisateur si le refresh échoue
          return throwError(() => error);
        })
      );
    } else {
      // Si une requête de rafraîchissement est déjà en cours, attend que le nouveau token soit émis
      return refreshTokenSubject.pipe(
        filter(token => token !== null),  // Attends jusqu'à ce que le token soit disponible
        take(1),
        switchMap((newToken) => next(request.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } })))
      );
    }
  }

  // Traitement de la requête et gestion des erreurs
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && token) {
        // En cas d'erreur 401 (token expiré ou invalide), tente de rafraîchir le token
        return handle401Error(authReq);
      }
      // Autres erreurs
      return throwError(() => error);
    })
  );
}
