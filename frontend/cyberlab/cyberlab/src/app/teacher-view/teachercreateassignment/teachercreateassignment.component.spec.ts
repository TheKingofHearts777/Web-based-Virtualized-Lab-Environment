import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeachercreateassignmentComponent } from './teachercreateassignment.component';

describe('TeachercreateassignmentComponent', () => {
  let component: TeachercreateassignmentComponent;
  let fixture: ComponentFixture<TeachercreateassignmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeachercreateassignmentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeachercreateassignmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
