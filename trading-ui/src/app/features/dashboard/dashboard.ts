import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth/auth.service';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { InstrumentService, Instrument } from '../../core/instrument.service';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent {

  userName = '';

  instruments: Instrument[] = [];

  selectedInstrument: Instrument | null = null;

  dropdownOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private instrumentService: InstrumentService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {

    this.userName = localStorage.getItem('userName') || '';

    this.instrumentService.loadInstruments()
      .subscribe(inst => {

        this.instruments = inst;

        if (inst.length > 0) {

          this.selectedInstrument = inst[0];

          this.instrumentService.setInstrument(inst[0]);

        }

        this.cd.detectChanges();

      });
  }

  toggleDropdown() {

    this.dropdownOpen = !this.dropdownOpen;

  }

  selectInstrument(inst: Instrument) {

    this.selectedInstrument = inst;

    this.instrumentService.setInstrument(inst);

    this.dropdownOpen = false;

  }

  onLogout() {

    this.authService.logout();

    localStorage.removeItem('userName');

    this.router.navigate(['/login']);

  }

}
