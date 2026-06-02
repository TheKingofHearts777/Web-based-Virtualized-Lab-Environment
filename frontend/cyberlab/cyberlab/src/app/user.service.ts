import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor() {}

  private isLocalStorageAvailable(): boolean {
    try {
      const test = 'test';
      localStorage.setItem(test, test);
      localStorage.removeItem(test);
      return true;
    } catch (e) {
      return false;
    }
  }

  getUserRole(): string {
    if (this.isLocalStorageAvailable()) {
      return localStorage.getItem('userRole') || '';
    }
    return '';
  }

  setUserRole(role: string) {
    if (this.isLocalStorageAvailable()) {
      localStorage.setItem('userRole', role);
    }
  }

  clearUserRole() {
    if (this.isLocalStorageAvailable()) {
      localStorage.removeItem('userRole');
    }
  }

  setUsername(username: string) {
    if (this.isLocalStorageAvailable()) {
      localStorage.setItem('username', username);
    }
  }

  getUsername(): string {
    if (this.isLocalStorageAvailable()) {
      return localStorage.getItem('username') || '';
    }
    return '';
  }

  clearUsername() {
    if (this.isLocalStorageAvailable()) {
      localStorage.removeItem('username');
    }
  }
}
