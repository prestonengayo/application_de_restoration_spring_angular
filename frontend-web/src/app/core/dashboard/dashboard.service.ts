import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private ordersUrl = 'http://localhost:8090/order';
  private categoriesUrl = 'http://localhost:8090/category';

  constructor(private http: HttpClient) {}

  // Récupérer toutes les commandes
  getOrders(): Observable<any[]> {
    return this.http.get<any[]>(this.ordersUrl);
  }

  // Récupérer toutes les catégories
  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(this.categoriesUrl);
  }
}
