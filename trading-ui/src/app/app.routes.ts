import { Routes } from '@angular/router';
import {LoginComponent} from './auth/login/login';
import {SignupComponent} from './auth/signup/signup'
import {DashboardComponent} from './features/dashboard/dashboard';
import {OrdersComponent} from './features/orders/orders';
import {TradesComponent} from './features/trades/trades';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path : 'login', component : LoginComponent},
  { path : 'signup', component : SignupComponent},
  { path : 'dashboard', component : DashboardComponent, children: [
      { path : 'orders', component : OrdersComponent},
      { path : 'trades', component : TradesComponent}
    ]}
];
