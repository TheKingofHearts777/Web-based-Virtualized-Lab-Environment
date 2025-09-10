import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeacherassignpointsComponent } from './teacherassignpoints.component';

describe('TeacherassignpointsComponent', () => {
  let component: TeacherassignpointsComponent;
  let fixture: ComponentFixture<TeacherassignpointsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeacherassignpointsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeacherassignpointsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
