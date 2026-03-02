import { Component, OnInit } from '@angular/core';

import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { CommonModule } from '@angular/common';

import { ActivatedRoute, Router } from '@angular/router';

import { ToastrService } from 'ngx-toastr';

import { AuthService } from '../auth.service';


@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './reset-password.html',
  styleUrls: ['../login/login.css']
})
export class ResetPasswordComponent implements OnInit {

  token!: string;

  constructor(
    private route: ActivatedRoute,
    private auth: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  form = new FormGroup({

    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8)
    ])

  });

  get password() {
    return this.form.get('password');
  }

  ngOnInit() {

    this.token =
      this.route.snapshot.queryParamMap.get('token')!;

  }

  onSubmit() {

    if (!this.form.valid) return;

    this.auth.resetPassword({

      token: this.token,

      newPassword: this.form.value.password!

    })
    .subscribe({

      next: () => {

        this.toastr.success(
          'Password reset successful'
        );

        this.router.navigate(['/login']);

      },

      error: (err) => {

        this.toastr.error(
          err.error?.message || 'Reset failed'
        );

      }

    });

  }

}
