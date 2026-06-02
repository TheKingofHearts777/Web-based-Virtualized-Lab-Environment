import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID, Inject } from '@angular/core';
import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIcon, MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { RouterLink, RouterOutlet } from '@angular/router';
import { HeaderComponent } from "../../shared/header/header.component";
import { IntegerType, ObjectId } from 'mongodb';
import {MatDivider} from '@angular/material/divider';
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
  selector: 'app-teacherhome',
  standalone: true,
  imports: [RouterOutlet, RouterLink, MatExpansionModule, MatIcon, MatIconModule, HeaderComponent, MatDivider],
  templateUrl: './teacherhome.component.html',
  styleUrl: './teacherhome.component.css',
})

export class TeacherhomeComponent implements OnInit, AfterViewInit {
  @ViewChild('labs', { static: false }) labsContainer!: ElementRef;
  @ViewChild('assignments', { static: false }) assignmentContainer!: ElementRef;
  user: User;

  constructor(
    private renderer: Renderer2,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {   
    this.user = "" as Awaited<ReturnType<this['getUser']>>;  
  }

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      document.body.style.backgroundColor = "lightgray";
    }
    this.addLabs();
    this.addAssignments();
  }

  router: Router = new Router;
    route(destination: string, extension:string|number|Date){
      this.router.navigate([destination,{name: extension}])
    }

    async assignValue(){
      const labID = "67d1691aba78ee4a36fde8d9"  //current hard coded labID, needs to be changed based on the template ID that is used 
      this.user = await this.getUser("67d167460ba5919c1384e5f9"); //current hard coded user ID
      cacheService.set("labID", labID, 60*20); //sets the value to the local cache 
    }

  addLabs() {
    if (!this.labsContainer) {
      console.error("labsContainer is undefined.");
      return;
    }

    const location = this.labsContainer.nativeElement;
    
    while (location.firstChild) {
      location.removeChild(location.firstChild);
    }

    const length = Object.keys(labs).length;

    for (let i = 1; i <= length; i++) {
      const temp = Object.keys(labs).find(key => labs[key]["labNumber"] == i);

      if (temp) {
        console.log(`Found lab: ${temp}`);

        let key: string = temp.toString();
        const currentLab = labs[key];

        const newLab = this.renderer.createElement('button');
        this.renderer.setProperty(newLab, 'innerHTML', currentLab["labDescription"].toString());
        this.renderer.addClass(newLab, 'dynamicLabItem');
        this.renderer.setAttribute(newLab, 'mat-button', '');

        this.renderer.appendChild(location, newLab);
      }
      else {
        console.warn(`No lab found for index ${i}`);
      }
    }
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

  async addAssignments() {
    await this.assignValue();
    const labs = this.user["labInstances"];

    if(!this.assignmentContainer){
      console.error("Lab container is undefined.");
      return;
    }

    const assignmentLocation = this.assignmentContainer.nativeElement;

    const currentDate = new Date()
    

    Object.values(labs).forEach((currentLab) => { //for each lab inside of a user Id make a div element
      const newLab = this.renderer.createElement("div");
      this.renderer.setProperty(newLab, "innerHTML", currentLab.templateName);
      this.renderer.addClass(newLab, "lab-container");
      
      const dueDate = new Date(currentLab.dueDate);

      //gotta get date stuff working
      //if(currentDate < dueDate){
        this.renderer.appendChild(assignmentLocation, newLab);
      }
      /*else {        
        this.renderer.appendChild(previousLocation, newLab)
      }*/
    );
  
}}

let labs: {
  [key1: string]: {
    [key2: string]: string | number;
  };
} = {
  "lab1": {
    "labNumber": 1,
    "labDescription": "CS2430 lab 2 template"
  },
  "lab2":{
    "labNumber": 2,
    "labDescription": "CS3430 lab 5 template",
  },
  "lab3":{
    "labNumber": 3,
    "labDescription": "CS1010 lab 1 template",
  },
  "lab4": {
    "labNumber": 4,
    "labDescription": "CS2050 lab 3 template",
  }
};
