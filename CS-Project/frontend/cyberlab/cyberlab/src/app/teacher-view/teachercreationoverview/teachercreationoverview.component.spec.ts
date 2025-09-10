import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeachercreationoverviewComponent } from './teachercreationoverview.component';

describe('TeachercreationoverviewComponent', () => {
  let component: TeachercreationoverviewComponent;
  let fixture: ComponentFixture<TeachercreationoverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeachercreationoverviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeachercreationoverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
