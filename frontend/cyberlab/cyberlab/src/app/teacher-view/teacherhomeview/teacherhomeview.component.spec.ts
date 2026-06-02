import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeacherhomeviewComponent } from './teacherhomeview.component';

describe('TeacherhomeviewComponent', () => {
  let component: TeacherhomeviewComponent;
  let fixture: ComponentFixture<TeacherhomeviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeacherhomeviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeacherhomeviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
