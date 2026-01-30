import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login';
import { SignupComponent } from './auth/signup/signup';
import { DashboardComponent } from './features/dashboard/dashboard';
import { OrdersComponent } from './features/orders/orders';
import { TradesComponent } from './features/trades/trades';
import { AuthGuard } from './auth/auth.guard';
import { GuestGuard } from './auth/guest.guard';


export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // 🚫 Logged-in users cannot access these
  { path: 'login', component: LoginComponent, canActivate: [GuestGuard] },
  { path: 'signup', component: SignupComponent, canActivate: [GuestGuard] },

  // 🔐 Logged-out users cannot access this
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'orders', component: OrdersComponent },
      { path: 'trades', component: TradesComponent }
    ]
  }
];
