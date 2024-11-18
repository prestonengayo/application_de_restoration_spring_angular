import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MenuItemService } from '../../core/menuItem/menu-item.service';
import { ReactiveFormsModule } from '@angular/forms';  
import { CommonModule } from '@angular/common';
import { CategoryService } from '../../core/category/category.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit {
  menuItems: any[] = []; // Liste des éléments de menu
  selectedItem: any = null; // Élément de menu sélectionné pour l'édition
  menuForm: FormGroup; // Formulaire pour l'ajout/mise à jour
  isModalOpen = false; // Pour gérer l'ouverture/fermeture du modal
  categories: any[] = []; // Liste des catégories
  selectedFile: File | null = null; // Fichier sélectionné pour l'image

  constructor(private fb: FormBuilder, private menuItemService: MenuItemService, private categoryService: CategoryService) {
    this.menuForm = this.fb.group({
      name: ['', Validators.required],
      price: ['', Validators.required],
      description: ['', Validators.required],
      categoryName: ['', Validators.required] // Champ pour le nom de la catégorie
    });
  }

  ngOnInit(): void {
    this.loadMenuItems(); // Charger les éléments de menu au démarrage
    this.loadCategories(); // Charger les catégories
  }

  // Gérer le fichier sélectionné
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0]; // Stocker le fichier sélectionné
    }
  }

  // Charger tous les éléments de menu
  loadMenuItems(): void {
    this.menuItemService.getMenuItems().subscribe((items) => {
      this.menuItems = items;
    });
  }

  // Charger toutes les catégories
  loadCategories(): void {
    this.categoryService.getCategories().subscribe((categories) => {
      this.categories = categories;
    });
  }

  // Ouvrir le modal pour ajouter ou mettre à jour un élément de menu
  openMenuModal(item: any = null): void {
    this.isModalOpen = true;
    this.selectedItem = item;
  
    if (item) {
      // Préremplir le formulaire avec les valeurs de l'élément sélectionné
      this.menuForm.patchValue(item);
    } else {
      // Réinitialiser le formulaire pour un nouvel élément
      this.menuForm.reset();
    }
  }

  // Fermer le modal
  closeMenuModal(): void {
    this.isModalOpen = false;
    this.selectedItem = null;
    this.menuForm.reset();
  }

  /*
  // Soumettre le formulaire pour ajouter ou mettre à jour un élément de menu
  onSubmitMenu(): void {
    if (this.menuForm.valid) {

      const menuItem = {
        name : this.menuForm.value.name,
        price : this.menuForm.value.price,
        description : this.menuForm.value.description,
        imageUrl : this.menuForm.value.imageUrl,
        category : this.menuForm.value.categoryName
      };

      if (this.selectedItem) {
        // Si un élément est sélectionné, c'est une mise à jour
        this.menuItemService.updateMenuItem(this.selectedItem.id, menuItem).subscribe(() => {
          this.loadMenuItems();
          this.closeMenuModal();
        });
      } else {
        // Sinon, c'est un ajout
        this.menuItemService.createMenuItem(menuItem).subscribe(() => {
          this.loadMenuItems();
          this.closeMenuModal();
        });
      }
    }
  }
*/

// Soumettre le formulaire pour ajouter ou mettre à jour un élément de menu
onSubmitMenu(): void {
  if (this.menuForm.valid) {
    const formData = new FormData(); // Utiliser FormData pour l'envoi multipart

    // Ajouter les données du formulaire
    formData.append('name', this.menuForm.value.name);
    formData.append('price', this.menuForm.value.price);
    formData.append('description', this.menuForm.value.description);
    formData.append('category', this.menuForm.value.categoryName);

    // Ajouter le fichier sélectionné si présent
    if (this.selectedFile) {
      formData.append('image', this.selectedFile);
    }

    if (this.selectedItem) {
      // Mise à jour
      this.menuItemService.updateMenuItem(this.selectedItem.id, formData).subscribe(() => {
        this.loadMenuItems();
        this.closeMenuModal();
      });
    } else {
      // Ajout
      this.menuItemService.createMenuItem(formData).subscribe(() => {
        this.loadMenuItems();
        this.closeMenuModal();
      });
    }
  }
}
  

  // Supprimer un élément de menu
  deleteMenuItem(itemId: number): void {
    this.menuItemService.deleteMenuItem(itemId).subscribe(() => {
      this.menuItems = this.menuItems.filter(item => item.id !== itemId);
      this.loadMenuItems(); // Recharger les éléments de menu après la suppression
    });
  }
}
