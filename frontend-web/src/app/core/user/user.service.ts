import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Address } from '../../application/address/address.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8090/user/';  // Modifier avec votre URL d'API

  constructor(private http: HttpClient) {}

  getUsers(): Observable<any[]> {
    const reponse=this.http.get<any[]>(this.apiUrl);
    return reponse;
  }

  getUserById(userId: number): Observable<any> {
    const reponse=this.http.get<any>(`${this.apiUrl}${userId}`);
    return reponse;
  }

  updateUser(userId: number, userData: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${userId}`, userData);
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}${userId}`);
  }

  getUserAddresses(userId: number): Observable<Address[]> {
    const URL = `http://localhost:8090/address/`;
    return this.http.get<Address[]>(URL); // Adapté selon votre API
  }

  updateUsername(userId: number | null, newUsername: string): Observable<any> {
  return this.http.put<any>(`${this.apiUrl}${userId}`, { username: newUsername });
  }

  

}
