import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, UpperCasePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../user.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, UpperCasePipe, NgIf],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'] // Fixed typo here
})
export class HeaderComponent implements OnInit {
  userRole: string = '';
  username: string = '';

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.userRole = this.userService.getUserRole();
    this.username = this.userService.getUsername();
  }

  logout() {
    this.userService.clearUserRole();
    this.userService.clearUsername();
  }
}
