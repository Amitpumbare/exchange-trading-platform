import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarketDepth } from './market-depth';

describe('MarketDepth', () => {
  let component: MarketDepth;
  let fixture: ComponentFixture<MarketDepth>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MarketDepth]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MarketDepth);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
