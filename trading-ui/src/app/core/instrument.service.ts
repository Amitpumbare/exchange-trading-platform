import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Instrument {
  instrumentId: string;
  symbol: string;
  halted: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class InstrumentService {

  private baseUrl = environment.apiBaseUrl;

  private selectedInstrumentSubject =
    new BehaviorSubject<Instrument | null>(null);

  selectedInstrument$ =
    this.selectedInstrumentSubject.asObservable();

  constructor(private http: HttpClient) {}

  loadInstruments() {
    return this.http.get<any[]>(
      `${this.baseUrl}/instruments/get-instruments`
    ).pipe(
      map(list =>
        list.map(inst => ({
          instrumentId: inst.instrumentId,
          symbol: inst.symbol,
          halted: inst.instrumentStatus === 'HALTED'
        }))
      )
    );
  }

  setInstrument(inst: Instrument) {

    // emit NEW object reference to guarantee change detection
    this.selectedInstrumentSubject.next({
      instrumentId: inst.instrumentId,
      symbol: inst.symbol,
      halted: inst.halted
    });

  }

  getInstrument(): Instrument | null {
    return this.selectedInstrumentSubject.value;
  }
}
