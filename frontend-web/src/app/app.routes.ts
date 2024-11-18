import { Routes, RouterModule } from '@angular/router';
import { LoginComponent } from './authentification/login/login.component';
import { RegisterComponent } from './authentification/register/register.component';
import { UserListComponent } from './admin/user-list/user-list.component';
import { AuthGuard } from './user/guards/auth/auth.guard';
import { UserDetailComponent } from './user/user-detail/user-detail.component';
import { HomeComponent } from './application/home/home.component';
import { AddressComponent } from './application/address/address.component';
import { NgModule } from '@angular/core';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { ProductsComponent } from './admin/products/products.component';
import { HomeDashboardComponent } from './admin/home-dashboard/home-dashboard.component';
import { ProfileComponent } from './admin/profile/profile.component';
import { AdminLoginComponent } from './admin/admin-login/admin-login.component';
import { AdminGuard } from './user/guards/admin/admin.guard';
import { CategoryComponent } from './admin/category/category.component';
import { OrdersComponent } from './admin/orders/orders.component';
import { CartComponent }from './application/cart/cart.component';
import { OrderHistoryComponent } from './application/order-history/order-history.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'users', component: UserListComponent, /* canActivate: [AuthGuard] */ },
  { path: 'home', component: HomeComponent, /*canActivate: [AuthGuard]*/ },
  { path: 'editUser/:id', component: AddressComponent, /*canActivate: [AuthGuard]*/ },
  { path: 'user/:id', component: UserDetailComponent, /*canActivate: [AuthGuard]*/ },
  { path: 'admin', component: AdminLoginComponent },
  { path: 'cart', component: CartComponent },
  { path: 'history', component: OrderHistoryComponent },
  { 
    path: 'dashboard', 
    component: DashboardComponent, 
    canActivate: [AdminGuard],
    children: [
      { path: 'homeDashboard', component: HomeDashboardComponent },
      { path: 'products', component: ProductsComponent },
      { path: 'users', component: UserListComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'category', component: CategoryComponent },
      { path: 'orders', component: OrdersComponent },
      { path: '', redirectTo: 'homeDashboard', pathMatch: 'full' } 
    ]
  },
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
