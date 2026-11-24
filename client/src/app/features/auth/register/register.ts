import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthService } from '../../../core/auth.service';
import { ApiError } from '../../../core/models/auth';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-register',
  styleUrl: '../auth.scss',
  templateUrl: './register.html',
})
export class Register {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.pattern(/\S/)]],
    email: ['', [Validators.required, Validators.pattern(/\S/)]],
    password: ['', Validators.required],
  });

  error = signal<string | null>(null);
  fieldError = signal<string | null>(null);
  loading = signal(false);
  submitted = signal(false);

  isInvalid(name: 'username' | 'email' | 'password'): boolean {
    const empty = this.submitted() && this.form.controls[name].invalid;
    return empty || this.fieldError() === name;
  }

  clearErrors(): void {
    this.error.set(null);
    this.fieldError.set(null);
    this.submitted.set(false);
  }

  submit(): void {
    this.submitted.set(true);
    this.fieldError.set(null);
    if (this.form.invalid) {
      this.error.set('Заполните все обязательные поля');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const raw = this.form.getRawValue();
    const data = {
      username: raw.username.trim(),
      email: raw.email.trim(),
      password: raw.password,
    };

    this.auth
      .register(data)
      .pipe(switchMap(() => this.auth.login({ login: data.username, password: data.password })))
      .subscribe({
        next: () => this.router.navigateByUrl('/projects'),
        error: (e: HttpErrorResponse) => {
          this.loading.set(false);
          const api = e.error as ApiError | null;
          this.error.set(api?.message ?? 'Не удалось связаться с сервером');
          this.fieldError.set(api?.field ?? null);
        },
      });
  }
}
