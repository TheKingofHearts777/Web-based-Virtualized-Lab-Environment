import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeacherfinalizationComponent } from './teacherfinalization.component';

describe('TeacherfinalizationComponent', () => {
  let component: TeacherfinalizationComponent;
  let fixture: ComponentFixture<TeacherfinalizationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeacherfinalizationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeacherfinalizationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
