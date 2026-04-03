import { Component, OnInit, OnDestroy, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrdersService } from './orders.service';
import { FormsModule } from '@angular/forms';
import { InstrumentService, Instrument } from '../../core/instrument.service';
import { WebSocketService } from '../../core/websocket.service';
import { ToastrService } from 'ngx-toastr';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

interface Order {
  id?: number;
  instrumentSymbol: string;
  type: 'BUY' | 'SELL';
  price: number;
  quantity: number;
  executedQuantity: number;
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
export class OrdersComponent implements OnInit, OnDestroy {

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

  private lastCancelTime = 0;
  private cancelTimeout: any = null;
  private destroy$ = new Subject<void>();

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

    this.websocket.orderEvents$
      .pipe(takeUntil(this.destroy$))
      .subscribe((event: Order) => {

        this.zone.run(() => {

          try {
            this.processEvent(event);

            const index = this.orders.findIndex(o => o.id === event.id);

            if (index !== -1) {
              // ✅ IMMUTABLE UPDATE (fixes UI sync issues)
              this.orders = this.orders.map(o =>
                o.id === event.id ? { ...o, ...event } : o
              );
            } else {
              this.orders = [event, ...this.orders];
            }

            this.rebuildLists();
            this.cd.detectChanges();

          } catch (err) {
            console.error("🚨 Event crash prevented:", err, event);
          }

        });

      });

    this.instrumentService.selectedInstrument$
      .pipe(takeUntil(this.destroy$))
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

  processEvent(event: Order) {

    if (!event || !event.status) return; // ✅ guard

    const now = Date.now();

    if (now - this.lastCancelTime > 2000) {
      this.lastCancelTime = 0;
      if (this.cancelTimeout) {
        clearTimeout(this.cancelTimeout);
        this.cancelTimeout = null;
      }
    }

    switch (event.status) {

      case 'CANCELLED':

        if (this.cancelTimeout) {
          clearTimeout(this.cancelTimeout);
        }

        this.cancelTimeout = setTimeout(() => {
          this.toastr.warning('Order cancelled', 'Cancelled ❌');
          this.cancelTimeout = null;
        }, 300);

        this.lastCancelTime = now;
        return;

      case 'OPEN':

        if (now - this.lastCancelTime < 500) {

          if (this.cancelTimeout) {
            clearTimeout(this.cancelTimeout);
            this.cancelTimeout = null;
          }

          this.toastr.info('Order modified', 'Updated ✏️');

          this.lastCancelTime = 0;
          return;
        }

        this.toastr.success(
          `Order placed (${event.type})`,
          'Placed 📈'
        );

        this.lastCancelTime = 0;
        return;

      case 'PARTIALLY_FILLED': {

        const executed = event.executedQuantity ?? 0;
        const total = event.quantity ?? 0;
        const price = event.price ?? 0;

        this.toastr.info(
          `Filled ${executed}/${total} @ ₹${price}`,
          'Order Update 📊'
        );

        return;
      }

      case 'FILLED': {

        const executed = event.executedQuantity ?? event.quantity ?? 0;
        const price = event.price ?? 0;

        this.toastr.success(
          `Executed ${executed} @ ₹${price}`,
          'Trade Executed ⚡'
        );

        return;
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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
        this.loadOrders();
      },
      error: () => order.processing = false
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

      const order = this.selectedOrder;

      if (order.processing) return;

      order.processing = true;

      this.ordersService.modifyOrder(order.id!, this.ticket).subscribe({
        next: () => {
          this.closeTicket();
          this.loadOrders();
        },
        error: () => order.processing = false
      });

    } else {

      if (this.isPlacingOrder) return;

      this.isPlacingOrder = true;

      this.ordersService.createOrder(this.ticket).subscribe({

        next: () => {
          this.closeTicket();
          this.loadOrders();
        },

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
