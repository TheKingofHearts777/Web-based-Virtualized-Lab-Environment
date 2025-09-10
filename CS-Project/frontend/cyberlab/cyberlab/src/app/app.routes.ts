import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';                   //these link the routes to the page componenets
import { StudentcoursesComponent } from './student-view/studentcourses/studentcourses.component';
import { StudentlabComponent } from './student-view/studentlab/studentlab.component';
import { StudentlabresultsComponent } from './student-view/studentlabresults/studentlabresults.component';
import { TeacherhomeComponent } from './teacher-view/teacherhome/teacherhome.component';
import { TeachercreatelabComponent } from './teacher-view/teachercreatelab/teachercreatelab.component';
import { TeachercreatetemplateComponent } from './teacher-view/teachercreatetemplate/teachercreatetemplate.component';
import { TeachercreationoverviewComponent } from './teacher-view/teachercreationoverview/teachercreationoverview.component';
import { TeachercreateassignmentComponent } from './teacher-view/teachercreateassignment/teachercreateassignment.component';
import { TeacherassignpointsComponent } from './teacher-view/teacherassignpoints/teacherassignpoints.component';
import { TeacherfinalizationComponent } from './teacher-view/teacherfinalization/teacherfinalization.component';
import { StudenthomeComponent } from './student-view/studenthome/studenthome.component';
import {TeacherhomeviewComponent} from './teacher-view/teacherhomeview/teacherhomeview.component';
import { VmwindowComponent as StudentvmwindowComponent } from './student-view/studentlab/vmwindow/vmwindow.component';
export const routes: Routes = [
    { path: '', component: LoginComponent},                                 //defualt page when loading website

    { path: 'student-home', component: StudenthomeComponent},               //Student view pages
    { path: 'student-courses', component: StudentcoursesComponent},
    { path: 'student-lab', component: StudentlabComponent},
    { path: 'student-lab/vmwindow', component: StudentvmwindowComponent},
    { path: 'student-lab/:name', component: StudentlabComponent},
    { path: 'student-lab/:name/vmwindow', component: StudentvmwindowComponent},
    { path: 'student-lab-results', component: StudentlabresultsComponent},
    { path: 'teacher-home', component: TeacherhomeComponent},               //teacher view pages
    { path: 'teacher-create-lab', component: TeachercreatelabComponent},
    { path: 'teacher-create-template', component: TeachercreatetemplateComponent},
    { path: 'teacher-creation-overview', component: TeachercreationoverviewComponent},
    { path: 'teacher-create-assignment', component: TeachercreateassignmentComponent},
    { path: 'teacher-assign-points', component: TeacherassignpointsComponent},
    { path: 'teacher-finalization', component: TeacherfinalizationComponent},
    { path: 'teacher-home-view', component: TeacherhomeviewComponent}
];
