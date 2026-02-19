import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';


export interface Instrument {
  id: number;
  symbol: string;
  halted: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class InstrumentService {

  private selectedInstrumentSubject =
    new BehaviorSubject<Instrument | null>(null);

  selectedInstrument$ =
    this.selectedInstrumentSubject.asObservable();

  constructor(private http: HttpClient) {}

  loadInstruments() {

    return this.http.get<any[]>(
      'http://localhost:8081/instruments/get-instruments'
    ).pipe(

      map(list =>
        list.map(inst => ({
          id: inst.id,
          symbol: inst.symbol,
          halted: inst.instrumentStatus === 'HALTED'
        }))
      )

    );
  }



  setInstrument(inst: Instrument) {
    this.selectedInstrumentSubject.next(inst);
  }

  getInstrument(): Instrument | null {
    return this.selectedInstrumentSubject.value;
  }
}
