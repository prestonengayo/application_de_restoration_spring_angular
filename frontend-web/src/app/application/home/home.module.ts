import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HomeComponent } from './home.component';

@NgModule({
  declarations: [HomeComponent],
  imports: [
    CommonModule,  // Assurez-vous d'importer le CommonModule
    RouterModule    // Import du RouterModule si vous utilisez la navigation
  ],
  exports: [HomeComponent]
})
export class HomeModule { }
