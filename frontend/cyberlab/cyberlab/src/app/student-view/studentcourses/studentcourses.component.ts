import { Component, ElementRef, Inject, OnInit, PLATFORM_ID, Renderer2, ViewChild  } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import {MatExpansionModule} from '@angular/material/expansion';
import moment from 'moment';
import { Console } from 'console';
import { Router } from '@angular/router';
import { IntegerType, ObjectId } from 'mongodb';
import { HeaderComponent } from '../../shared/header/header.component';
import { resolve } from 'path';
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

const date = new Date();

@Component({
  selector: 'app-studentcourses',
  standalone: true,
  imports: [RouterOutlet, RouterLink, MatExpansionModule, HeaderComponent],
  templateUrl: './studentcourses.component.html',
  styleUrl: './studentcourses.component.css',
  
})

export class StudentcoursesComponent  implements OnInit {
  @ViewChild('upcomingLabs', { static: false }) upcomingLabContainer!: ElementRef;
  @ViewChild('previousLabs', { static: false }) previousLabContainer!: ElementRef;
  user: User;

  constructor(  
    private renderer: Renderer2, 
      @Inject(PLATFORM_ID) private platformId: Object,
    ) {   //renderer2 doing God's work and preventing page loading errors 
      this.user = "" as Awaited<ReturnType<this['getUser']>>; //labTemplate initalized to the getLabTemplate promise 
    }

  title?:string;

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    //Called after ngAfterContentInit when the component's view has been initialized. Applies to components only.
    //Add 'implements AfterViewInit' to the class.
    this.addLabs()
  }

  router: Router = new Router;
  route(destination: string, extension:string|number|Date){
    this.router.navigate([destination,{name: extension}])
  }

  async assignValue(){
    const labID = "67d1691aba78ee4a36fde8d9"  //current hard coded labID, needs to be changed based on the template ID that is used 
    this.user = await this.getUser("67d166b133ba4bac9cd3b99c"); //current hard coded user ID
    cacheService.set("labID", labID, 60*20); //sets the value to the local cache 
  }

  async getUser(userId: string): Promise<User> {
    const url = `http://184.100.71.203:682/users/${userId}`;
    
    try {
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      });
  
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
  
      return await response.json();
    } catch (error) {
      console.error("Error fetching user:", error);
      return {} as User; // Return an empty object in case of failure
    }
  }
  

  async addLabs() {
    await this.assignValue();
    const labs = this.user["labInstances"];

    if(!this.upcomingLabContainer || !this.previousLabContainer){
      console.error("Lab container is undefined.");
      return;
    }

    const upcomingLocation = this.upcomingLabContainer.nativeElement;
    const previousLocation = this.previousLabContainer.nativeElement;

    const currentDate = new Date()
    

    Object.values(labs).forEach((currentLab) => { //for each lab inside of a user Id make a div element
      const newLab = this.renderer.createElement("div");
      this.renderer.setProperty(newLab, "innerHTML", currentLab.templateName);
      this.renderer.addClass(newLab, "lab-container");
      
      const dueDate = new Date(currentLab.dueDate);

      if(currentDate < dueDate){
        this.renderer.appendChild(upcomingLocation, newLab);
      }
      else {        
        this.renderer.appendChild(previousLocation, newLab)
      }
    });
  }
}; 
