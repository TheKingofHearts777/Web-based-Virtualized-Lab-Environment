import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle } from '@angular/material/expansion';
import { MatIcon } from '@angular/material/icon';
import { MatCard, MatCardTitle } from '@angular/material/card';
import { MatDatepicker, MatDatepickerInput, MatDatepickerToggle, MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormField, MatFormFieldModule, MatHint } from '@angular/material/form-field';
import { MatInput, MatInputModule } from '@angular/material/input';
import { HeaderComponent } from "../../shared/header/header.component";
import { provideNativeDateAdapter, MAT_DATE_LOCALE } from '@angular/material/core';

@Component({
  selector: 'app-teacherfinalization',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatIcon,
    MatCardTitle,
    MatCard,
    MatFormFieldModule,
    MatDatepickerModule,
    MatDatepickerToggle,
    MatDatepickerInput,
    MatDatepicker,
    MatInputModule,
    HeaderComponent
  ],
  providers: [
    provideNativeDateAdapter(),
    { provide: MAT_DATE_LOCALE, useValue: 'en-US' }
  ],
  templateUrl: './teacherfinalization.component.html',
  styleUrls: ['./teacherfinalization.component.css']
})
export class TeacherfinalizationComponent {
  title = 'teacher-finalization';
}
