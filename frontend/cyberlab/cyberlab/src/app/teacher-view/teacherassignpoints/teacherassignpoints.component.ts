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
  selector: 'app-teacherassignpoints',
  standalone: true,
    imports: [RouterOutlet, RouterLink, MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle, MatIcon, HeaderComponent],
  templateUrl: './teacherassignpoints.component.html',
  styleUrl: './teacherassignpoints.component.css'
})
export class TeacherassignpointsComponent {
  title = 'teacher-assign-points';
}
