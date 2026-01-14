import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {AuthService} from '../auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit{

  constructor( private auth: AuthService, private router: Router) {}

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
    console.log("Submit clicked");

    if (!this.loginForm.valid) {
      console.log('Form invalid');
      return;
    }

    const { email, password, rememberMe } = this.loginForm.value!;
    console.log("Sending payload:", { email, password });

    const payload = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password
    };

    this.auth.login(payload).subscribe({
      next: (res) => {
        console.log("SERVER RESPONSE:", res);
        this.auth.setUser(res);
        this.router.navigate(['/dashboard']);
        alert("Login Successfully");
      },
      error: (err) => {
        console.error("LOGIN ERROR:", err);
      }
    });


    // remember-me logic
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
