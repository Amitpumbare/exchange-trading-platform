import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './signup.html',
  styleUrls: ['./signup.css']
})
export class SignupComponent {

  constructor(
    private auth: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  // 👁️ toggle states
  showPassword = false;
  showConfirmPassword = false;

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  signupForm = new FormGroup({
    fullName: new FormControl('', [
      Validators.required,
      Validators.minLength(3)
    ]),
    email: new FormControl('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$')
    ]),
    confirmPassword: new FormControl('', [
      Validators.required
    ])
  }, { validators: this.passwordMatchValidator });

  get fullName() { return this.signupForm.get('fullName'); }
  get email() { return this.signupForm.get('email'); }
  get password() { return this.signupForm.get('password'); }
  get confirmPassword() { return this.signupForm.get('confirmPassword'); }

  onSubmit() {

    if (!this.signupForm.valid) {
      this.signupForm.markAllAsTouched();
      return;
    }

    this.auth.signup(this.signupForm.value).subscribe({
      next: () => {

        this.toastr.success('Account created successfully');

        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1500);
      },
      error: (err) => {

        if (err.status === 409) {
          this.toastr.error('Email already exists');
        } else {
          this.toastr.error('Signup failed — try again');
        }
      }
    });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {

    const password = control.get('password')?.value;
    const confirm  = control.get('confirmPassword')?.value;

    if (!password || !confirm) return null;

    return password === confirm
      ? null
      : { passwordMismatch: true };
  }
}
