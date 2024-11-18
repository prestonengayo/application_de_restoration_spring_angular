import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';  // Import du ReactiveFormsModule
import { CategoryService } from '../../core/category/category.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-category',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],  // Ajout de ReactiveFormsModule dans les imports
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss']
})
export class CategoryComponent implements OnInit {
  categories: any[] = []; // Liste des catégories
  categoryForm: FormGroup; // Formulaire pour l'ajout/mise à jour des catégories
  selectedCategory: any = null; // Catégorie sélectionnée pour la mise à jour
  isCategoryModalOpen = false; // Pour ouvrir/fermer la modale

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService
  ) {
    this.categoryForm = this.fb.group({
      name: ['', Validators.required]  // Champ obligatoire pour le nom de la catégorie
    });
  }

  ngOnInit(): void {
    this.loadCategories();  // Charger les catégories au démarrage
  }

  // Charger la liste des catégories
  loadCategories(): void {
    this.categoryService.getCategories().subscribe((categories) => {
      this.categories = categories;
    });
  }

  // Ouvrir la modale pour l'ajout d'une nouvelle catégorie
  openCategoryModal(category: any = null): void {
    this.isCategoryModalOpen = true;
    this.selectedCategory = category;

    if (category) {
      // Si une catégorie est sélectionnée pour la modification, pré-remplir le formulaire
      this.categoryForm.patchValue({ name: category.name });
    } else {
      this.categoryForm.reset(); // Sinon, réinitialiser le formulaire pour l'ajout
    }
  }

  // Fermer la modale
  closeCategoryModal(): void {
    this.isCategoryModalOpen = false;
    this.categoryForm.reset();
  }

  // Ajouter ou mettre à jour une catégorie
  saveCategory(): void {
    if (this.selectedCategory) {
      // Mise à jour de la catégorie
      const updatedCategory = { ...this.selectedCategory, ...this.categoryForm.value };
      this.categoryService.updateCategory(updatedCategory.id, updatedCategory).subscribe({
        next: () => {
          this.loadCategories();  // Recharger les catégories après la mise à jour
          this.closeCategoryModal();  // Fermer la modale
        },
        error: (error) => console.error('Erreur lors de la mise à jour de la catégorie:', error)
      });
    } else {
      // Ajout d'une nouvelle catégorie
      this.categoryService.createCategory(this.categoryForm.value).subscribe({
        next: () => {
          this.loadCategories();  // Recharger les catégories après l'ajout
          this.closeCategoryModal();  // Fermer la modale
        },
        error: (error) => console.error('Erreur lors de la création de la catégorie:', error)
      });
    }
  }

  // Supprimer une catégorie
  deleteCategory(categoryId: number): void {
    this.categoryService.deleteCategory(categoryId).subscribe({
      next: () => {
        this.loadCategories();  // Recharger les catégories après la suppression
      },
      error: (error) => console.error('Erreur lors de la suppression de la catégorie:', error)
    });
  }
}
