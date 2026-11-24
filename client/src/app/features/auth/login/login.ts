import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../../../core/models/auth';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-login',
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    login: ['', Validators.required],
    password: ['', Validators.required],
  });

  error = signal<string | null>(null);
  loading = signal(false);
  submitted = signal(false);

  isInvalid(name: 'login' | 'password'): boolean {
    return this.submitted() && this.form.controls[name].invalid;
  }

  submit(): void {
    this.submitted.set(true);
    if (this.form.invalid) {
      this.error.set('Заполните все обязательные поля');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/projects'),
      error: (e: HttpErrorResponse) => {
        this.loading.set(false);
        const api = e.error as ApiError | null;
        this.error.set(api?.message ?? 'Не удалось связаться с сервером');
      },
    });
  }
}
