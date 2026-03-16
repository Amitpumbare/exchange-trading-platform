import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TradesService } from './trades.service';
import { WebSocketService } from '../../../core/websocket.service';

interface Trade {
  instrumentSymbol: string;
  side: 'BUY' | 'SELL';
  price: number;
  quantity: number;
  executedAt: string;
}

@Component({
  selector: 'app-trades',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './trades.html',
  styleUrls: ['./trades.css']
})
export class TradesComponent implements OnInit {

  trades: Trade[] = [];
  loading = true;

  constructor(
    private tradesService: TradesService,
    private websocket: WebSocketService,
    private cd: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {

    this.loadTrades();

    this.websocket.tradeEvents$.subscribe((event: Trade) => {

      console.log("TRADE EVENT RECEIVED", event);

      this.zone.run(() => {

        // Add newest trade to top
        this.trades.unshift(event);

        // Limit list size
        if (this.trades.length > 50) {
          this.trades.pop();
        }

        this.cd.detectChanges();

      });

    });

  }

  loadTrades() {

    this.loading = true;

    this.tradesService.getTrades().subscribe({
      next: (res: Trade[]) => {

        this.trades = res;

        this.loading = false;

        this.cd.detectChanges();

      },
      error: () => {

        this.loading = false;

        this.cd.detectChanges();

      }
    });

  }

}
