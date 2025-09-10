import { Component } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIcon } from '@angular/material/icon';
import { RouterLink, RouterOutlet } from '@angular/router';
import { HeaderComponent } from "../../shared/header/header.component";

@Component({
  selector: 'app-teachercreatelab',
  standalone: true,
  imports: [RouterOutlet, RouterLink, MatExpansionModule, MatIcon, HeaderComponent],
  templateUrl: './teachercreatelab.component.html',
  styleUrl: './teachercreatelab.component.css'
})
export class TeachercreatelabComponent {
  title = 'teacher-create-lab';
}
