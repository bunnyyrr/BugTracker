import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { ProjectList } from './features/projects/project-list/project-list';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'projects', component: ProjectList },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
];
