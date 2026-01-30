import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TradesService } from './trades.service';

interface Trade {
  buyOrderId: number;
  sellOrderId: number;
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
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadTrades();
  }

  loadTrades() {
    this.loading = true;

    this.tradesService.getTrades().subscribe({
      next: (res) => {
        console.log('Trades API:', res);

        this.trades = res;
        this.loading = false;
        this.cd.markForCheck();
      },
      error: (err) => {
        console.error('Trades API error', err);
        this.loading = false;
        this.cd.markForCheck();
      }
    });
  }
}
