import { Component } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrls: ['../login/login.css']
})
export class ForgotPasswordComponent {

  constructor(
    private auth: AuthService,
    private toastr: ToastrService
  ) {}

  form = new FormGroup({

    email: new FormControl('', [
      Validators.required,
      Validators.email
    ])

  });

  get email() {
    return this.form.get('email');
  }

  onSubmit() {

    if (!this.form.valid) return;

    this.auth.forgotPassword({

      email: this.form.value.email!

    }).subscribe({

      next: () => {

        this.toastr.success(
          'Reset link generated. Check Gmail.'
        );

      },

      error: (err) => {

        this.toastr.error(
          err.error?.message || 'Failed'
        );

      }

    });

  }

}
