import { Component, Renderer2 } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatExpansionModule} from '@angular/material/expansion';
import { MatIcon } from '@angular/material/icon';
import { IntegerType, ObjectId } from 'mongodb';
import { HeaderComponent } from '../../shared/header/header.component';
import { cacheService } from '../../CacheService';

interface VmInstance{
  _id: ObjectId;
  proxmoxId: IntegerType;
  vmNode: string;
  vmName: string;
  vmCloneDate: Date;
  vmParentId: IntegerType;
}

interface LabInstance{
  templateName: string;
  templateId: IntegerType;
  courseId: ObjectId;
  dateLastAccessed: Date;
  dueDate: Date;
  vminstances: string[];
  vmNodes: VmInstance[];
  userAnswers: string[];
  completed: boolean;
}
interface User{
  _id: ObjectId;
  lastTimeVisted: Date;
  sid: string;
  username: string;
  userType: string;
  labInstances: LabInstance[];
  courses: ObjectId[];
}
@Component({
  selector: 'app-studenthome',
  standalone: true,
  imports: [RouterOutlet,RouterLink,MatExpansionModule,MatIcon, HeaderComponent],
  templateUrl: './studenthome.component.html',
  styleUrl: './studenthome.component.css'
})
export class StudenthomeComponent {
  constructor(
    private renderer: Renderer2
  ){}
  router: Router = new Router;
  title = 'student-home';
  ngOnInit(): void {
    const User = cacheService.get("User") 
    if(User == undefined){
      this.router.navigate([''])
    }
    const webpage = document.querySelector("body");
    this.renderer.listen(webpage, "click", (event: MouseEvent) => cacheService.resetTTL());
  }
}
