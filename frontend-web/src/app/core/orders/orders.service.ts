import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private baseUrl = 'http://localhost:8090/order'; // L'URL de l'API pour les commandes

  constructor(private http: HttpClient) {}

  // Récupérer toutes les commandes
  getOrders(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  // Mettre à jour une commande (par exemple pour changer son statut)
  updateOrder(orderId: number, updatedStatus: string): Observable<any> {
    return this.http.patch(`${this.baseUrl}/${orderId}`, { status: updatedStatus });
  }
  
  // Supprimer une commande
  deleteOrder(orderId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${orderId}`);
  }

  // Créer une nouvelle commande
  createOrder(orderData: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}`, orderData);
  }
  
}
