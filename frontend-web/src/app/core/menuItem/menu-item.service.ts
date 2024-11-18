import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class MenuItemService {
  private apiUrl = 'http://localhost:8090/menu-item';

  constructor(private http: HttpClient) {}

  // Récupérer tous les éléments de menu
  getMenuItems(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // Créer un nouvel élément de menu avec fichier
  createMenuItem(menuItem: FormData): Observable<any> {
    return this.http.post<any>(this.apiUrl, menuItem);
  }

  // Mettre à jour un élément de menu avec fichier
  updateMenuItem(id: number, menuItem: FormData): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, menuItem);
  }

  // Supprimer un élément de menu
  deleteMenuItem(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
