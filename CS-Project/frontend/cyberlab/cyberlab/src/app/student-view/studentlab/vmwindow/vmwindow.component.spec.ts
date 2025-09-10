import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VmwindowComponent } from './vmwindow.component';
import { describe, beforeEach, it } from 'node:test';

describe('VmwindowComponent', () => {
  let component: VmwindowComponent;
  let fixture: ComponentFixture<VmwindowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VmwindowComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VmwindowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
