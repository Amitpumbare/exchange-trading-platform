import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrdersService } from './orders.service';
import { FormsModule } from '@angular/forms';

interface Order {
  id?: number;
  type: string;
  price: number;
  quantity: number;
  status: string;
  message: string;
  processing?: boolean;
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
  openOrders: Order[] = [];
  history: Order[] = [];
  loading = true;
  isPlacingOrder = false;
  isNewOrderMode = false;



  selectedOrder?: Order;
  view: 'OPEN' | 'HISTORY' = 'OPEN';

  ticket = {
    type: 'BUY',
    price: 0,
    quantity: 0
  };

  constructor(
    private ordersService: OrdersService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders() {
    this.loading = true;

    // 🔴 force-clear stale state
    this.orders = [];
    this.openOrders = [];
    this.history = [];

    this.ordersService.getOrders().subscribe({
      next: (res: Order[]) => {
        this.orders = res;

        this.openOrders = res.filter(
          o => o.status === 'OPEN' || o.status === 'PARTIALLY_FILLED'
        );

        this.history = res.filter(
          o => o.status === 'FILLED' || o.status === 'CANCELLED'
        );

        this.loading = false;
        this.cd.detectChanges(); // 👈 stronger than markForCheck
      },
      error: () => {
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }


  placeOrder() {
    if (this.isPlacingOrder) return;

    this.isPlacingOrder = true;

    this.ordersService.createOrder(this.ticket).subscribe({
      next: () => {
        this.selectedOrder = undefined;
        this.isNewOrderMode=false;
        this.resetTicket();
        this.loadOrders();
      },
      error: () => {
        this.isPlacingOrder = false;
        this.loadOrders();
      },
      complete: () => {
        this.isPlacingOrder = false;
      }
    });
  }

  resetTicket() {
    this.ticket = {
      type: 'BUY',
      price: 0,
      quantity: 0
    };
  }

  openNewOrderTicket() {
    this.selectedOrder = undefined;
    this.isNewOrderMode = true;
    this.resetTicket();
  }





  cancelOrder(orderId: number, event?: MouseEvent) {
    if (event) event.stopPropagation();

    const order = this.openOrders.find(o => o.id === orderId);
    if (!order || order.processing) return;

    order.processing = true;
    this.selectedOrder=undefined;

    this.ordersService.cancelOrder(orderId).subscribe({
      next: () => {
        this.isNewOrderMode=false;
        this.loadOrders();
      },
      error: () => {
        order.processing = false;
        this.loadOrders();
      }
    });
  }


  modifyOrder(orderId: number, event?: MouseEvent) {
    if (event) event.stopPropagation();

    if (!this.selectedOrder || this.selectedOrder.id !== orderId) return;

    const order = this.selectedOrder;
    if (order.processing) return;

    order.processing = true;

    this.ordersService.modifyOrder(orderId, this.ticket).subscribe({
      next: () => {
        this.selectedOrder = undefined;
        this.isNewOrderMode=false;
        this.loadOrders();
      },
      error: () => order.processing = false
    });
  }


  selectOrder(order: Order) {
    this.isNewOrderMode = false;

    if (this.selectedOrder?.id === order.id) {
      this.selectedOrder = undefined;
      this.isNewOrderMode = false;
      return;
    }

    this.selectedOrder = order;
    this.ticket.type = order.type;
    this.ticket.price = order.price;
    this.ticket.quantity = order.quantity;
  }


}
