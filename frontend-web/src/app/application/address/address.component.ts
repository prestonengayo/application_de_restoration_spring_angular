import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AddressService } from '../../core/address/address.service';
import { UserService } from '../../core/user/user.service';
import { Address } from './address.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-address',
  templateUrl: './address.component.html',
  styleUrls: ['./address.component.scss']
})
export class AddressComponent implements OnInit {
  addressForm: FormGroup;
  addressEditForm: FormGroup;
  usernameForm: FormGroup; // Formulaire pour changer le nom d'utilisateur
  errorMessage: string = '';
  currentUserId: number | null = null;
  address: Address[] = [];
  selectedAddressId: number | null = null;
  isEditing: boolean = false;

  constructor(
    private fb: FormBuilder,
    private addressService: AddressService,
    private userService: UserService,
    private router: Router
  ) {
    this.addressForm = this.fb.group({
      street: ['', Validators.required],
      postalCode: ['', Validators.required],
      city: ['', Validators.required],
      country: ['', Validators.required]
    });

    this.addressEditForm = this.fb.group({
      street: ['', Validators.required],
      postalCode: ['', Validators.required],
      city: ['', Validators.required],
      country: ['', Validators.required]
    });

    // Formulaire pour changer le nom d'utilisateur
    this.usernameForm = this.fb.group({
      username: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadAddress();
    this.loadCurrentUserDetails(); // Chargez les détails de l'utilisateur courant
  }

  onSubmit(): void {
    if (this.addressForm.valid) {
      this.addressService.addAddress(this.addressForm.value).subscribe({
        next: (response) => {
          console.log('Adresse ajoutée avec succès', response);
          this.loadAddress(); // Recharger les adresses après ajout
          this.addressForm.reset();
        },
        error: (error) => {
          console.error('Erreur lors de l\'ajout de l\'adresse', error);
          this.errorMessage = 'Une erreur est survenue lors de l\'ajout de l\'adresse.';
        }
      });
    }
  }

  // Nouvelle méthode pour changer le nom d'utilisateur
  changeUsername(): void {
    if (this.usernameForm.valid) {
      const newUsername = this.usernameForm.value.username;
      this.userService.updateUsername(this.currentUserId, newUsername).subscribe({
        next: () => {
          console.log('Nom d\'utilisateur mis à jour avec succès');
          this.usernameForm.reset(); // Réinitialiser le formulaire après la mise à jour
        },
        error: (error) => {
          console.error('Erreur lors de la mise à jour du nom d\'utilisateur', error);
          this.errorMessage = 'Une erreur est survenue lors de la mise à jour du nom d\'utilisateur.';
        }
      });
    }
  }

  editAddress(address: Address): void {
    if (this.selectedAddressId === address.id) {
      this.isEditing = !this.isEditing;
      this.selectedAddressId = this.isEditing ? address.id : null;
    } else {
      this.isEditing = true;
      this.selectedAddressId = address.id ?? null;
    }

    if (this.isEditing) {
      this.addressEditForm.patchValue({
        street: address.street,
        postalCode: address.postalCode,
        city: address.city,
        country: address.country
      });
    }
  }

  updateAddress(): void {
    if (this.addressEditForm.valid && this.selectedAddressId) {
      this.addressService.updateAddress(this.selectedAddressId, this.addressEditForm.value).subscribe({
        next: () => {
          console.log('Adresse mise à jour avec succès');
          this.loadAddress();
          this.isEditing = false;
          this.selectedAddressId = null;
        },
        error: (error) => {
          console.error('Erreur lors de la mise à jour de l\'adresse', error);
          this.errorMessage = 'Une erreur est survenue lors de la mise à jour de l\'adresse.';
        }
      });
    }
  }

  deleteAddress(addressId: number): void {
    this.addressService.deleteAddress(addressId).subscribe({
      next: () => {
        console.log('Adresse supprimée avec succès');
        this.loadAddress(); // Recharger la liste des adresses après suppression
      },
      error: (error) => {
        console.error('Erreur lors de la suppression de l\'adresse', error);
        this.errorMessage = 'Une erreur est survenue lors de la suppression de l\'adresse.';
      }
    });
  }

  loadAddress(): void {
    const currentUserDetails = localStorage.getItem('userDetails');
    if (currentUserDetails) {
      const currentUser = JSON.parse(currentUserDetails);
      this.currentUserId = currentUser.id;
    }
    if (this.currentUserId) {
      this.addressService.getAddresses().subscribe({
        next: (data) => {
          this.address = data;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des adresses', error);
          this.errorMessage = 'Impossible de charger les adresses.';
        }
      });
    }
  }

  loadCurrentUserDetails(): void {
    const currentUserDetails = localStorage.getItem('userDetails');
    if (currentUserDetails) {
      const currentUser = JSON.parse(currentUserDetails);
      this.usernameForm.patchValue({ username: currentUser.username });
    }
  }

  goToHome(): void {
    this.router.navigate(['/home']);
  }
}
