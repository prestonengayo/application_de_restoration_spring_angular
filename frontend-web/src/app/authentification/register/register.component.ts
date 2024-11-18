import { Component } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'app-register',
  imports: [RouterLink,CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  public username: string = '';
  public password: string = '';
  public showPassword = false;
  public isFormValid = false;
  registerForm: FormGroup;
  public showConfirmPassword = false;
  public showRegisterError = false;
  public registerError = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.registerForm = this.fb.group({
      firstname: ['', [Validators.required]],
      lastname: ['', [Validators.required]],
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      confirmPassword: ['', [Validators.required]],
    }, { validator: this.passwordMatchValidator });
  }

  passwordValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;

    const errors: ValidationErrors = {};
    if (!/[A-Z]/.test(value)) errors['uppercase'] = true;
    if (!/[a-z]/.test(value)) errors['lowercase'] = true;
    if (!/[0-9]/.test(value)) errors['numeric'] = true;
    if (!/[\W_]/.test(value)) errors['special'] = true;
    if (value.length < 8) errors['minlength'] = true;

    return Object.keys(errors).length ? errors : null;
  }

  passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordsNotMatching: true };
  }

  register() {
    const { firstName, lastname, username, password } = this.registerForm.value;

    this.authService.register(firstName, lastname, username, password).subscribe(() => {
      this.router.navigate(['/login']);
    });
  }

  public toggleShowPassword() {
    this.showPassword = !this.showPassword;
  }

  toggleShowConfirmPassword() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onSubmit() {
    if (this.registerForm.valid) {
      const { firstname, lastname, username, password } = this.registerForm.value;

      this.authService.register(firstname, lastname, username, password).subscribe({
        next: (response: any) => {
          this.showRegisterError = false;
          this.router.navigate(['/login']);
        },
        error: (error: any) => {
          this.showRegisterError = true;
          this.registerError = error.message || 'Une erreur s\'est produite lors de l\'inscription.';
        }
      });
    }
  }

  getPasswordErrorMessage() {
    const control = this.registerForm.get('password');
    if (control?.touched && control?.errors) {
      if (control.errors['required']) {
        return 'Ce champ est requis.';
      }
      const errors = control.errors;
      const messages = [];
      if (errors['uppercase']) messages.push('au moins une lettre majuscule');
      if (errors['lowercase']) messages.push('au moins une lettre minuscule');
      if (errors['numeric']) messages.push('au moins un chiffre');
      if (errors['special']) messages.push('au moins un caractère spécial');
      if (errors['minlength']) messages.push('au moins 8 caractères');
      return `Le mot de passe doit contenir ${messages.join(', ')}.`;
    }
    return null;
  }
}
