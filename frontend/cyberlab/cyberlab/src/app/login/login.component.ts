import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import {RouterOutlet, RouterLink,Router} from '@angular/router';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {FlexLayoutModule} from '@angular/flex-layout';
import {FlexLayoutServerModule} from '@angular/flex-layout/server';
import {UserService} from '../user.service';
import { cacheService } from '../CacheService';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterOutlet,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatCardModule,
    FlexLayoutModule,
    FlexLayoutServerModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'] // Fixed the typo here
})

export class LoginComponent {
  title = 'login';
  loginForm = new FormGroup({
    username: new FormControl(''),
    password: new FormControl(''),
  });

  constructor(public router: Router, private userService: UserService) {
    // Updates the values of username and password whenever the form is changed
    this.loginForm.valueChanges.subscribe((value) =>
    {console.log(value);
    });
  }
  login() {
    const username = this.loginForm.value.username;
    let u_id: number = 0;

    // Hardcoded for simplicity - replace with actual API calls in a real-world scenario
    if (username === "Ttest" && this.loginForm.value.password === "T") {
      u_id = 1; // Teacher
      this.userService.setUserRole('teacher');
      this.userService.setUsername(username);
    } else if (username === "Stest" && this.loginForm.value.password === "S") {
      u_id = 2; // Student
      this.userService.setUserRole('student');
      this.userService.setUsername(username);
    } else {
      alert('Incorrect username or password');
      this.refresh();
      return;
    }

    if (u_id === 1) {
      this.router.navigate(['/teacher-home-view']);
      cacheService.set("User",u_id,60*20)
    } else if (u_id === 2) {
      this.router.navigate(['/student-home']);
      cacheService.set("User",u_id,60*20)
    }
  }

  // Hides the password and unhides when the button is clicked
  hide = signal(true);
  clickEventHide(event: MouseEvent) {
    this.hide.set(!this.hide());
    event.stopPropagation();
  }
  // Detects the submit button click to run the login function
  clickEventSubmit(event: MouseEvent) {
    this.login()
    event.stopPropagation();
  }

  clickEventCreate(event: MouseEvent) {
    alert("Not yet implemented");
    event.stopPropagation();
  }

  refresh(): void {
    window.location.reload();
  }
}
