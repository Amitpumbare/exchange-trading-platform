import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InstrumentService, Instrument } from '../../core/instrument.service';
import { DepthService, DepthLevel } from './market-depth.service';

@Component({
  selector: 'app-market-depth',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './market-depth.html',
  styleUrls: ['./market-depth.css']
})
export class MarketDepthComponent implements OnInit {

  bids: DepthLevel[] = [];
  asks: DepthLevel[] = [];

  levels = Array.from({ length: 10 });

  loading = true;

  selectedInstrument?: Instrument;

  constructor(
    private depthService: DepthService,
    private instrumentService: InstrumentService,
    private cd: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {

    // same pattern used in OrdersComponent
    this.instrumentService.selectedInstrument$
      .subscribe((inst: Instrument | null) => {

        if (!inst) return;

        // prevent reload loop when router switches tabs
        if (this.selectedInstrument?.symbol === inst.symbol) return;

        this.selectedInstrument = inst;

        this.loadDepth(inst.instrumentId);

      });

  }

  loadDepth(instrumentId: string) {

    this.loading = true;

    this.depthService.getDepth(instrumentId)
      .subscribe({

        next: (res) => {

          this.zone.run(() => {

            this.bids = res.bids || [];
            this.asks = res.asks || [];

            this.loading = false;

            this.cd.detectChanges();

          });

        },

        error: () => {

          this.loading = false;

          this.cd.detectChanges();

        }

      });

  }

}
