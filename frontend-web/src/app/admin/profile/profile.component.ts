import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { ProfileService } from '../../core/profile/profile.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { AddressService } from '../../core/address/address.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule,RouterLink, HttpClientModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})


export class ProfileComponent implements OnInit {
  profileForm: FormGroup;
  addressForm: FormGroup;
  addresses: any[] = [];
  selectedFile: File | null = null;
  userId?: number;
  isAddressModalOpen = false;
  addressToEdit: any = null;
  photoUrlImag!: string;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private profileService: ProfileService,
    private addressService: AddressService
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      photoUrl: ['']
    });
    
    this.addressForm = this.fb.group({
      street: ['', Validators.required],
      postalCode: ['', Validators.required],
      city: ['', Validators.required],
      country: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.authService.getUserDetails().subscribe((user) => {
      console.log(user);
      this.photoUrlImag = user.photoUrl;
      this.userId = user.id;
      this.profileForm.patchValue({
        firstName: user.firstName,
        lastName: user.lastName,
        photoUrl: user.photoUrl
      });
      this.loadUserAddresses(this.userId!);
    });
  }

  loadUserAddresses(userId: number): void {
    this.addressService.getAddresses().subscribe((addresses) => {
      this.addresses = addresses;
    });
  }

  openAddressModal(address: any = null): void {
    this.isAddressModalOpen = true;
    this.addressToEdit = address;
    if (address) {
      this.addressForm.patchValue(address);
    } else {
      this.addressForm.reset();
    }
  }

  closeAddressModal(): void {
    this.isAddressModalOpen = false;
    this.addressForm.reset();
    this.addressToEdit = null;
  }

  onSubmitAddress(): void {
    if (this.addressForm.valid) {
      if (this.addressToEdit) {
        this.addressService.updateAddress(this.addressToEdit.id, this.addressForm.value).subscribe({
          next: (updatedAddress) => {
            const index = this.addresses.findIndex(addr => addr.id === updatedAddress.id);
            this.addresses[index] = updatedAddress;
            this.closeAddressModal();
          },
          error: (error) => console.error('Erreur lors de la mise à jour de l\'adresse:', error)
        });
      } else {
        this.addressService.addAddress(this.addressForm.value).subscribe({
          next: (newAddress) => {
            this.addresses.push(newAddress);
            this.closeAddressModal();
          },
          error: (error) => console.error('Erreur lors de l\'ajout de l\'adresse:', error)
        });
      }
    }
  }

  deleteAddress(addressId: number): void {
    this.addressService.deleteAddress(addressId).subscribe({
      next: () => {
        this.addresses = this.addresses.filter(addr => addr.id !== addressId);
      },
      error: (error) => console.error('Erreur lors de la suppression de l\'adresse:', error)
    });
  }

  updateProfile(): void {
    if (this.profileForm.valid) {
      const updatedUser = this.profileForm.value;
  
      if (this.selectedFile) {
        this.profileService.uploadProfilePicture(this.userId!, this.selectedFile).subscribe({
          next: (response) => {
            updatedUser.photoUrl = response.photoUrl;
            this.submitUserProfile(updatedUser);
          },
          error: (error) => {
            console.error('Erreur lors de l\'upload de la photo de profil:', error);
          }
        });
      } else {
        this.submitUserProfile(updatedUser);
      }
    }
  }

  submitUserProfile(updatedUser: any): void {
    this.profileService.updateUserProfile(this.userId!, updatedUser).subscribe({
      next: (response: any) => {
        this.authService.setUserDetails(response);
      },
      error: (error: Error) => {
        console.error('Erreur lors de la mise à jour du profil:', error);
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
    }
  }
}
