import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrdersService } from './orders.service';
import { FormsModule } from '@angular/forms';
import { InstrumentService, Instrument } from '../../core/instrument.service';

interface Order {
  id?: number;
  instrumentSymbol: string;
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

  // THIS drives all disable logic
  isInstrumentHalted = false;

  selectedOrder?: Order;

  view: 'OPEN' | 'HISTORY' = 'OPEN';

  ticket = {
    type: 'BUY',
    price: 0,
    quantity: 0
  };

  constructor(
    private ordersService: OrdersService,
    private cd: ChangeDetectorRef,
    private instrumentService: InstrumentService
  ) {}

  ngOnInit(): void {

    // subscribe once to instrument context
    this.instrumentService.selectedInstrument$
      .subscribe((inst: Instrument | null) => {

        if (!inst) return;

        // update HALT state
        this.isInstrumentHalted = inst.halted;

        // immediately refresh UI bindings
        this.cd.detectChanges();

        // reload orders for new instrument context
        this.loadOrders();

        // ✅ FIX: force layout recalculation so scroll works immediately
        setTimeout(() => {
          window.dispatchEvent(new Event('resize'));
        });

      });

  }


  loadOrders() {

    this.loading = true;

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

        this.cd.detectChanges();

      },

      error: () => {

        this.loading = false;

        this.cd.detectChanges();

      }

    });

  }


  placeOrder() {

    // exchange rule: cannot place during HALT
    if (this.isPlacingOrder || this.isInstrumentHalted) return;

    this.isPlacingOrder = true;

    this.ordersService.createOrder(this.ticket).subscribe({

      next: () => {

        this.selectedOrder = undefined;

        this.isNewOrderMode = false;

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

    // cancel ALWAYS allowed even when halted
    order.processing = true;

    this.selectedOrder = undefined;

    this.ordersService.cancelOrder(orderId).subscribe({

      next: () => {

        this.isNewOrderMode = false;

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

    // exchange rule: cannot modify during HALT
    if (this.isInstrumentHalted) return;

    if (!this.selectedOrder || this.selectedOrder.id !== orderId) return;

    const order = this.selectedOrder;

    if (order.processing) return;

    order.processing = true;

    this.ordersService.modifyOrder(orderId, this.ticket).subscribe({

      next: () => {

        this.selectedOrder = undefined;

        this.isNewOrderMode = false;

        this.loadOrders();

      },

      error: () => order.processing = false

    });

  }


  selectOrder(order: Order) {

    this.isNewOrderMode = false;

    if (this.selectedOrder?.id === order.id) {

      this.selectedOrder = undefined;

      return;

    }

    this.selectedOrder = order;

    // opposite side logic
    this.ticket.type = order.type === 'BUY' ? 'SELL' : 'BUY';

    this.ticket.price = order.price;

    this.ticket.quantity = order.quantity;

  }

}
