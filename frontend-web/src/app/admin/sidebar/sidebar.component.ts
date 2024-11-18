import { Component } from '@angular/core';
import { HomeDashboardComponent } from '../home-dashboard/home-dashboard.component';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { UserListComponent } from '../user-list/user-list.component';
import { OrdersComponent } from '../orders/orders.component';
import { CategoryComponent } from '../category/category.component';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [HomeDashboardComponent, UserListComponent, OrdersComponent, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {

}
