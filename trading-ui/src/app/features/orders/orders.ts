import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrdersService } from './orders.service';
import { FormsModule } from '@angular/forms';
import { InstrumentService, Instrument } from '../../core/instrument.service';
import { WebSocketService } from '../../core/websocket.service';
import { ToastrService } from 'ngx-toastr';

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
  isInstrumentHalted = false;

  selectedOrder?: Order;
  selectedInstrument?: Instrument;

  view: 'OPEN' | 'HISTORY' = 'OPEN';

  isTicketOpen = false;
  mode: 'CREATE' | 'EDIT' = 'CREATE';

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
    private zone: NgZone,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {

    this.loadOrders();

    this.websocket.orderEvents$.subscribe((event: Order) => {

      this.zone.run(() => {

        const existing = this.orders.find(o => o.id === event.id);

        if (!existing || existing.status !== event.status) {
          this.handleOrderToast(event);
        }

        const index = this.orders.findIndex(o => o.id === event.id);

        if (index !== -1) {
          this.orders[index] = event;
        } else {
          this.orders.unshift(event);
        }

        this.rebuildLists();
        this.cd.detectChanges();

      });

    });

    this.instrumentService.selectedInstrument$
      .subscribe((inst: Instrument | null) => {

        if (!inst) return;
        if (this.selectedInstrument?.symbol === inst.symbol) return;

        this.selectedInstrument = inst;
        this.isInstrumentHalted = inst.halted;

        this.cd.detectChanges();

        setTimeout(() => {
          window.dispatchEvent(new Event('resize'));
        });

      });

  }

  handleOrderToast(event: Order) {

    switch (event.status) {

      case 'FILLED':
        this.toastr.success(
          `Executed ${event.quantity} @ ₹${event.price}`,
          'Trade Executed ⚡'
        );
        break;

      case 'PARTIALLY_FILLED':
        this.toastr.info(
          `Partial fill ${event.quantity} @ ₹${event.price}`,
          'Order Update 📊'
        );
        break;

      case 'CANCELLED':
        this.toastr.warning(
          'Order cancelled',
          'Cancelled ❌'
        );
        break;

      case 'OPEN':
        this.toastr.success(
          'Order placed successfully',
          'Order Placed 📈'
        );
        break;
    }

  }

  rebuildLists() {

    this.openOrders = this.orders.filter(
      o =>
        o.status?.toUpperCase().trim() === 'OPEN' ||
        o.status?.toUpperCase().trim() === 'PARTIALLY_FILLED'
    );

    this.history = this.orders.filter(
      o =>
        o.status?.toUpperCase().trim() === 'FILLED' ||
        o.status?.toUpperCase().trim() === 'CANCELLED'
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

  resetTicket() {

    this.ticket = {
      type: 'BUY',
      price: 0,
      quantity: 0
    };

  }

  openNewOrderTicket() {

    this.selectedOrder = undefined;
    this.mode = 'CREATE';
    this.isTicketOpen = true;

    this.resetTicket();

  }

  cancelOrder(orderId: number, event?: MouseEvent) {

    if (event) event.stopPropagation();

    const order = this.openOrders.find(o => o.id === orderId);
    if (!order || order.processing) return;

    order.processing = true;

    this.ordersService.cancelOrder(orderId).subscribe({

      next: () => {
        this.closeTicket();
      },

      error: () => {
        order.processing = false;
      }

    });

  }

  modifyOrder(orderId: number, event?: MouseEvent) {

    if (event) {
      event.stopPropagation();
      event.preventDefault();
    }

    if (this.isInstrumentHalted) return;

    const order = this.openOrders.find(o => o.id === orderId);
    if (!order) return;

    this.selectedOrder = order;
    this.mode = 'EDIT';
    this.isTicketOpen = true;

    this.ticket.type = order.type === 'BUY' ? 'SELL' : 'BUY';
    this.ticket.price = order.price;
    this.ticket.quantity = order.quantity;

  }

  selectOrder(order: Order) {

    if (this.selectedOrder?.id === order.id && this.isTicketOpen) {
      this.closeTicket();
      return;
    }

    this.modifyOrder(order.id!);
  }

  trackByOrderId(index: number, order: Order) {
    return order.id;
  }

  submitOrder() {

    if (this.isInstrumentHalted) return;

    if (this.mode === 'EDIT' && this.selectedOrder) {

      const orderId = this.selectedOrder.id!;
      const order = this.selectedOrder;

      if (order.processing) return;

      order.processing = true;

      this.ordersService.modifyOrder(orderId, this.ticket).subscribe({
        next: () => this.closeTicket(),
        error: () => order.processing = false
      });

    } else {

      if (this.isPlacingOrder) return;

      this.isPlacingOrder = true;

      this.ordersService.createOrder(this.ticket).subscribe({

        next: () => this.closeTicket(),

        error: () => this.isPlacingOrder = false,

        complete: () => {
          this.isPlacingOrder = false;
          this.cd.detectChanges();
        }

      });

    }

  }

  closeTicket() {

    this.isTicketOpen = false;
    this.selectedOrder = undefined;

    setTimeout(() => {

      this.selectedOrder = undefined;
      this.resetTicket();

      this.cd.detectChanges();

    }, 300);

  }

}
