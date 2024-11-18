import { Component, OnInit } from '@angular/core';
import { UserService } from '../../core/user/user.service';  // Le service qui gère les requêtes utilisateur
import { HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {
  users: any[] = [];
  selectedUser: any = null;  // Utilisateur sélectionné
  addresses: any[] = [];  // Adresses de l'utilisateur sélectionné

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  // Charge la liste des utilisateurs et sélectionne le premier utilisateur par défaut
  loadUsers(): void {
    this.userService.getUsers().subscribe((users) => {
      this.users = users;
      if (this.users.length > 0) {
        this.selectUser(this.users[0]);  // Sélectionne le premier utilisateur
      }
    });
  }

  // Sélectionne un utilisateur et appelle l'API pour récupérer ses détails
  selectUser(user: any): void {
    this.userService.getUserById(user.id).subscribe({
      next: (userDetails) => {
        this.selectedUser = userDetails;  // Met à jour les détails de l'utilisateur sélectionné
        this.addresses = userDetails.addresses;  // Met à jour les adresses de l'utilisateur
      },
      error: (error) => {
        console.error('Erreur lors de la récupération des détails de l\'utilisateur:', error);
      }
    });
  }

  // Supprimer un utilisateur
  deleteUser(userId: number): void {
    this.userService.deleteUser(userId).subscribe({
      next: () => {
        this.users = this.users.filter(user => user.id !== userId);
        if (this.users.length > 0) {
          this.selectUser(this.users[0]);  // Sélectionne le premier utilisateur restant
        } else {
          this.selectedUser = null;
          this.addresses = [];
        }
      },
      error: (error) => console.error('Erreur lors de la suppression de l\'utilisateur:', error)
    });
  }
}
