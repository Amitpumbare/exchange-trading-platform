import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login';
import { SignupComponent } from './auth/signup/signup';
import { DashboardComponent } from './features/dashboard/dashboard';
import { OrdersComponent } from './features/orders/orders';
import { TradesComponent } from './features/trades/trades';
import { MarketDepthComponent } from './features/market-depth/market-depth';
import { AuthGuard } from './auth/auth.guard';
import { GuestGuard } from './auth/guest.guard';

export const routes: Routes = [

  // Default → login
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // AUTH PAGES
  { path: 'login', component: LoginComponent, canActivate: [GuestGuard] },

  { path: 'signup', component: SignupComponent, canActivate: [GuestGuard] },

  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./auth/forgot-password/forgot-password')
        .then(m => m.ForgotPasswordComponent),
    canActivate: [GuestGuard]
  },

  {
    path: 'reset-password',
    loadComponent: () =>
      import('./auth/reset-password/reset-password')
        .then(m => m.ResetPasswordComponent),
    canActivate: [GuestGuard]
  },

  // DASHBOARD (protected)
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],

    children: [

      // default dashboard page → orders
      { path: '', redirectTo: 'orders', pathMatch: 'full' },

      { path: 'orders', component: OrdersComponent },

      { path: 'trades', component: TradesComponent },

      { path: 'depth', component: MarketDepthComponent }

    ]
  },

  // UI safety fallback
  { path: '**', redirectTo: 'login' }

];
