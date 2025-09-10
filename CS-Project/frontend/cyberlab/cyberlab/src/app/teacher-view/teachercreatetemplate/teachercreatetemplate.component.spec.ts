import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeachercreatetemplateComponent } from './teachercreatetemplate.component';

describe('TeachercreatetemplateComponent', () => {
  let component: TeachercreatetemplateComponent;
  let fixture: ComponentFixture<TeachercreatetemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeachercreatetemplateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeachercreatetemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
