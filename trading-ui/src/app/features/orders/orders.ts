import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrdersService } from './orders.service';
import {ChangeDetection} from '@angular/cli/lib/config/workspace-schema';
import {FormsModule} from '@angular/forms';

interface Order {
  type: string;
  price: number;
  quantity: number;
  status: string;
  message: string;
}

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.html',
  styleUrls: ['./orders.css']
})
export class OrdersComponent implements OnInit {

  orders: Order[] = [];
  history: Order[] = [];
  openOrders: Order[] = [];
  loading = true;
  selectedOrder?: Order;
  view: 'OPEN' | 'HISTORY' = 'OPEN';


  constructor(private ordersService: OrdersService, private cd : ChangeDetectorRef
  ) {
    console.log('OrdersComponent constructor');
  }

  ngOnInit(): void {
    console.log('OrdersComponent ngOnInit');
    this.loadOrders();

  }

  placeOrder(){
    this.ordersService.createOrder(this.ticket).subscribe({
      next: (order: Order) => {
        alert(`Status: ${order.status}\n${order.message}`);
        this.loadOrders();
      }
    })
  }

  loadOrders(){
    this.loading = true;

    this.ordersService.getOrders().subscribe({
      next: (res: Order[]) => {
        console.log('Orders API response:', res);

        this.orders = res;
        this.openOrders = this.orders.filter(
          o => o.status === 'OPEN' || o.status === 'PARTIALLY_FILLED'
        );

        this.history = this.orders.filter(
          o => o.status === 'FILLED' || o.status === 'CANCELLED'
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

  ticket = {
    type: 'BUY',
    price: 0,
    quantity: 0
  }

  selectOrder(order: Order){
    this.selectedOrder = order;
    this.ticket.type = order.type === 'BUY'?'SELL':'BUY';
    this.ticket.price=order.price;
    this.ticket.quantity=0;
  }

}
