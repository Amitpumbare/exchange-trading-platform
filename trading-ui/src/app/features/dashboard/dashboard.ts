import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth/auth.service';
import {Router, RouterLink, RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent {

  user: any;

  constructor(private authService: AuthService, private router: Router) {
    this.user = this.authService.getUser();
    this.router=router;
  }

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
