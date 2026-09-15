import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './core/auth.guard';
import { Login } from './features/login/login';
import { Portal } from './features/portal/portal';
import { AppDetail } from './features/app-detail/app-detail';
import { Admin } from './features/admin/admin';
import { Shell } from './shared/shell';

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [guestGuard] },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', component: Portal },
      { path: 'apps/:id', component: AppDetail },
      { path: 'admin', component: Admin, canActivate: [adminGuard] }
    ]
  },
  { path: '**', redirectTo: '' }
];
