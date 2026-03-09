import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrdersService } from './orders.service';
import { FormsModule } from '@angular/forms';
import { InstrumentService, Instrument } from '../../core/instrument.service';
import { WebSocketService } from '../../core/websocket.service';

interface Order {
  id?: number;
  instrumentSymbol: string;
  type: 'BUY' | 'SELL';
  price: number;
  quantity: number;
  status: 'OPEN' | 'PARTIALLY_FILLED' | 'FILLED' | 'CANCELLED';
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
  isInstrumentHalted = false;

  selectedOrder?: Order;
  selectedInstrument?: Instrument;

  view: 'OPEN' | 'HISTORY' = 'OPEN';

  ticket = {
    type: 'BUY' as 'BUY' | 'SELL',
    price: 0,
    quantity: 0
  };

  constructor(
    private ordersService: OrdersService,
    private cd: ChangeDetectorRef,
    private instrumentService: InstrumentService,
    private websocket: WebSocketService,
    private zone: NgZone
  ) {}

  ngOnInit(): void {

    this.loadOrders();

    // WebSocket order events
    this.websocket.orderEvents$.subscribe((event: Order) => {

      console.log("ORDER EVENT RECEIVED", event);

      this.zone.run(() => {

        const exists = this.orders.some(o => o.id === event.id);

        if (exists) {

          // replace existing order
          this.orders = this.orders.map(o =>
            o.id === event.id ? event : o
          );

        } else {

          // prepend new order
          this.orders = [event, ...this.orders];

        }

        this.rebuildLists();

        this.cd.detectChanges();

      });

    });

    // Instrument change listener
    this.instrumentService.selectedInstrument$
      .subscribe((inst: Instrument | null) => {

        if (!inst) return;

        // avoid reload loop
        if (this.selectedInstrument?.symbol === inst.symbol) return;

        this.selectedInstrument = inst;

        this.isInstrumentHalted = inst.halted;

        this.cd.detectChanges();

        setTimeout(() => {
          window.dispatchEvent(new Event('resize'));
        });

      });

  }

  rebuildLists() {

    this.openOrders = this.orders.filter(
      o => o.status === 'OPEN' || o.status === 'PARTIALLY_FILLED'
    );

    this.history = this.orders.filter(
      o => o.status === 'FILLED' || o.status === 'CANCELLED'
    );

  }

  loadOrders() {

    this.loading = true;

    this.ordersService.getOrders().subscribe({

      next: (res: Order[]) => {

        this.orders = res;

        this.rebuildLists();

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

    if (this.isPlacingOrder || this.isInstrumentHalted) return;

    this.isPlacingOrder = true;

    this.ordersService.createOrder(this.ticket).subscribe({

      next: () => {

        this.selectedOrder = undefined;
        this.isNewOrderMode = false;

        this.resetTicket();

        this.cd.detectChanges();

      },

      error: () => {

        this.isPlacingOrder = false;

      },

      complete: () => {

        this.isPlacingOrder = false;
        this.cd.detectChanges();

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

    this.selectedOrder = undefined;

    this.ordersService.cancelOrder(orderId).subscribe({

      next: () => {

        this.isNewOrderMode = false;

      },

      error: () => {

        order.processing = false;

      }

    });

  }

  modifyOrder(orderId: number, event?: MouseEvent) {

    if (event) event.stopPropagation();

    if (this.isInstrumentHalted) return;

    if (!this.selectedOrder || this.selectedOrder.id !== orderId) return;

    const order = this.selectedOrder;

    if (order.processing) return;

    order.processing = true;

    this.ordersService.modifyOrder(orderId, this.ticket).subscribe({

      next: () => {

        this.selectedOrder = undefined;
        this.isNewOrderMode = false;

        this.cd.detectChanges();

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

    this.ticket.type = order.type === 'BUY' ? 'SELL' : 'BUY';
    this.ticket.price = order.price;
    this.ticket.quantity = order.quantity;

  }

  trackByOrderId(index: number, order: Order) {
    return order.id;
  }

}
