import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudentlabresultsComponent } from './studentlabresults.component';

describe('StudentlabresultsComponent', () => {
  let component: StudentlabresultsComponent;
  let fixture: ComponentFixture<StudentlabresultsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudentlabresultsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudentlabresultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
