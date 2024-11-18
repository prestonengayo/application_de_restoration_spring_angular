import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserService } from '../user/user.service';

@Injectable({
  providedIn: 'root'
})
export class AddressService {
  private apiUrl = 'http://localhost:8090/address';  // Change cela selon ton API
  currentUserId: number | null = null;

  constructor(private http: HttpClient) {}

  // Ajouter une nouvelle adresse
  addAddress(addressData: any): Observable<any> {
    const currentUserDetails = localStorage.getItem('userDetails');
    if (currentUserDetails) {
      const currentUser = JSON.parse(currentUserDetails);
      this.currentUserId = currentUser.id;
    };
    const URL = `${this.apiUrl}`;
    return this.http.post<any>(URL, addressData);
  }

  // Obtenir toutes les adresses d'un utilisateur
  getAddresses(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // Supprimer une adresse par ID
  deleteAddress(addressId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${addressId}`);
  }

  // Modifier une adresse par ID
  updateAddress(addressId: number, addressData: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${addressId}`, addressData);
  }
}
