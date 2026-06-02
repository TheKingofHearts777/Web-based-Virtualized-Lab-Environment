import { Component } from '@angular/core';
import {RouterLink, RouterOutlet} from '@angular/router';
import {
  MatAccordion,
  MatExpansionPanel,
  MatExpansionPanelDescription,
  MatExpansionPanelHeader, MatExpansionPanelTitle
} from '@angular/material/expansion';
import {MatIcon} from '@angular/material/icon';
import { IntegerType, ObjectId } from 'mongodb';
import { HeaderComponent } from "../../shared/header/header.component";

interface VmInstance{
  _id: ObjectId;
  proxmoxId: IntegerType;
  vmNode: string;
  vmName: string;
  vmCloneDate: Date;
  vmParentId: IntegerType;
}

enum UserType {
  Instructor,
  Student,
  Admin
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
  userType: UserType;
  labInstances: LabInstance[];
  courses: ObjectId[];
}

@Component({
  selector: 'app-teacherhomeview',
  standalone: true,
  imports: [RouterLink, RouterOutlet, MatAccordion, MatExpansionPanel, MatExpansionPanelDescription, MatExpansionPanelHeader, MatExpansionPanelTitle, MatIcon, HeaderComponent],
  templateUrl: './teacherhomeview.component.html',
  styleUrl: './teacherhomeview.component.css'
})
export class TeacherhomeviewComponent {

}
