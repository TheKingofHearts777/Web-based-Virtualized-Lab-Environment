import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface VMwindow {
  vncPort: number;
  vncWebSocketConnection: string;
  pveTicketCookie: string;
  ticket: string;
  port: string;
}

@Component({
  selector: 'app-vmwindow',
  standalone: true,
  imports: [],
  templateUrl: './vmwindow.component.html',
  styleUrl: './vmwindow.component.css'
})
export class VmwindowComponent implements OnInit {
  private url: string = "";

  constructor(private http: HttpClient) {}

  async ngOnInit() {
    if (typeof window !== 'undefined' && window.location.pathname.includes('vmwindow')) {
      console.log('Path contains vmwindow, hiding taskbar');
      const taskbar = document.getElementById('taskbar');
      if (taskbar) {
        taskbar.style.display = 'none';
      }
    }
    const res = await this.getVMwindow();
    console.log('VM window: ', res);
    await this.startClient(res);
  }

  getVMwindow(): Promise<VMwindow> {
    const htmlBase = encodeURI("http://127.0.0.1:8080/api/vnc/connect");
    const headers = new Headers();

    const request: RequestInfo = new Request(htmlBase, {
      method: 'GET',
      headers: headers
    });

    return fetch(request).then(res => res.json()).then(res => res.data as VMwindow);
  }

  async startClient(res: VMwindow) {
    const node = 'brodied';
    const vmid = '1001';
    const ticket = encodeURIComponent(res.ticket);
    const port = res.port;

    this.url = `wss://127.0.0.1:8006/api2/json/nodes/${node}/qemu/${vmid}/vncwebsocket?port=${port}&vncticket=${ticket}`;
    console.log('URL: ', this.url);
    const container: HTMLElement | null = document.getElementById('screen');
    if (container) {
      const { default: RFB } = await import('@novnc/novnc/lib/rfb');
      const rfb = new RFB(container, this.url, {
        wsProtocols: ['binary'],
        wsOptions:{
          rejectUnauthorized: false
        }
      });
    }
  }

  openVmInNewWindow(): void {
    if (typeof window !== 'undefined') {
      console.log("Opening VM in new window");
      window.open(window.location.pathname + '/vmwindow', '_blank');
    }
  }

  fullScreen(): void {
    if (typeof window !== 'undefined') {
      console.log("Opening VM in full screen");
      const elem = document.documentElement;
      if (elem.requestFullscreen) {
        elem.requestFullscreen();
      } else if (elem.requestFullscreen) {
        elem.requestFullscreen();
      } else if (elem.requestFullscreen) {
        elem.requestFullscreen();
      } else if (elem.requestFullscreen) {
        elem.requestFullscreen();
      }
    }
  }
}
