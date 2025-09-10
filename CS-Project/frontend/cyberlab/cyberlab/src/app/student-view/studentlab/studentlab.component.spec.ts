import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudentlabComponent } from './studentlab.component';

describe('StudentlabComponent', () => {
  let component: StudentlabComponent;
  let fixture: ComponentFixture<StudentlabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudentlabComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudentlabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
