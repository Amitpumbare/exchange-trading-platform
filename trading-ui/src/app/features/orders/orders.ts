import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrdersService } from './orders.service';
import {ChangeDetection} from '@angular/cli/lib/config/workspace-schema';

interface Order {
  type: string;
  price: number;
  quantity: number;
  status: string;
}

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './orders.html',
  styleUrls: ['./orders.css']
})
export class OrdersComponent implements OnInit {

  orders: Order[] = [];
  openOrders: Order[] = [];
  loading = true;

  constructor(private ordersService: OrdersService, private cd : ChangeDetectorRef
  ) {
    console.log('OrdersComponent constructor');
  }

  ngOnInit(): void {
    console.log('OrdersComponent ngOnInit');
    this.loading = true;

    this.ordersService.getOrders().subscribe({
      next: (res: Order[]) => {
        console.log('Orders API response:', res);

        this.orders = res;
        this.openOrders = this.orders.filter(
          o => o.status === 'OPEN' || o.status === 'PARTIALLY_FILLED'
        );

        this.loading = false; // 🔥 IMPORTANT
        console.log('Loading set to false');
        this.cd.markForCheck();
      },
      error: (err) => {
        console.error('Orders API error:', err);
        this.loading = false;
      }
    });
  }
}
