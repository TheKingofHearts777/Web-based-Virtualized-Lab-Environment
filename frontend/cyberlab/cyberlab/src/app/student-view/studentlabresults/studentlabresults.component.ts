import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

import {
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle
} from "@angular/material/expansion";
import {MatIcon} from "@angular/material/icon";
import { HeaderComponent } from "../../shared/header/header.component";

@Component({
  selector: 'app-studentlabresults',
  standalone: true,
    imports: [RouterOutlet, RouterLink, MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle, MatIcon, HeaderComponent],
  templateUrl: './studentlabresults.component.html',
  styleUrl: './studentlabresults.component.css'
})
export class StudentlabresultsComponent {
  title = 'student-lab-results';
}
