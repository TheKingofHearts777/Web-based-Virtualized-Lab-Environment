import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeachercreatelabComponent } from './teachercreatelab.component';

describe('TeachercreatelabComponent', () => {
  let component: TeachercreatelabComponent;
  let fixture: ComponentFixture<TeachercreatelabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeachercreatelabComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeachercreatelabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
