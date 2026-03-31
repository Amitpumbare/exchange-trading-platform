import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit {

  constructor(
    private auth: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  // 👁️ toggle state
  showPassword = false;

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  loginForm = new FormGroup({
    email: new FormControl('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8)
    ]),
    rememberMe: new FormControl(false)
  });

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }

  onSubmit() {

    if (!this.loginForm.valid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { email, password, rememberMe } = this.loginForm.value!;

    const payload = {
      email,
      password
    };

    this.auth.login(payload).subscribe({
      next: (res: any) => {

        this.auth.setToken(res.token);
        localStorage.setItem('userName', res.fullName);

        Promise.resolve().then(() => {
          this.router.navigate(['/dashboard']).then(() => {
            this.toastr.success('Login Successful');
          });
        });

      },
      error: () => {
        this.toastr.error('Invalid email or password');
      }
    });

    // Remember-me logic
    if (rememberMe && email) {
      localStorage.setItem('loginEmail', email);
      localStorage.setItem('rememberMe', 'true');
    } else {
      localStorage.removeItem('loginEmail');
      localStorage.removeItem('rememberMe');
    }
  }

  ngOnInit() {
    const remember = localStorage.getItem('rememberMe');

    if (remember === 'true') {
      this.loginForm.patchValue({
        email: localStorage.getItem('loginEmail'),
        rememberMe: true
      });
    }
  }
}
