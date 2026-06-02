import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AfterViewInit, Component, ElementRef, Inject, OnInit, PLATFORM_ID, Renderer2, ViewChild } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink, RouterOutlet, Router } from '@angular/router';
import { VmwindowComponent } from './vmwindow/vmwindow.component';
import { IntegerType, ObjectId } from 'mongodb';
import { MatIconModule } from '@angular/material/icon';
import { HeaderComponent } from '../../shared/header/header.component';
import { cacheService } from '../../CacheService';
import { stringify } from 'querystring';

interface LabQuestion{
  questionNumber: number;
  questionType: string;
  question: { [key: string]: string[] };
  answer: string;
} 

interface LabTemplate{
  _id: ObjectId;
  creator: IntegerType;
  name: string;
  description: string;
  objectives: string[];
  vmTemplateIds: ObjectId[];
  questions: LabQuestion[];
}

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
  selector: 'app-studentlab',
  standalone: true,
  imports: [RouterOutlet, RouterLink, MatProgressBarModule, VmwindowComponent, MatIconModule, CommonModule ,HeaderComponent],
  templateUrl: './studentlab.component.html',
  styleUrl: './studentlab.component.css'
})

export class StudentlabComponent implements OnInit, AfterViewInit {

  @ViewChild('questions', { static: false }) questionsContainer!: ElementRef; //in the html #questions is assigned to questionsContainer, used later in location--etc assigning stuff to page
  labTemplate: LabTemplate;
  

  constructor(
    private router: Router,
    private renderer: Renderer2, 
    @Inject(PLATFORM_ID) private platformId: Object,
  ) {   //renderer2 doing God's work and preventing page loading errors 
    this.labTemplate = "" as Awaited<ReturnType<this['getLabTemplate']>>; //labTemplate initalized to the getLabTemplate promise 
  }
  
  objectives: { title: string; questions: any }[] = [];   //Initialize objectives as an empty array, use later
 
  currentObjectiveIndex = 0;
  ngOnInit(): void {
    this.addQuestions()                                   //initallizes the page to have the questions                               
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      document.body.style.backgroundColor = "lightgray";  //prevents errors
    }
  }

  //go forward a page, this will be changed later when pages are added
  nextStep() {
    if (this.currentObjectiveIndex < this.objectives.length - 1) {
      this.currentObjectiveIndex++;
      this.addQuestions();
    }
    else {
      this.router.navigate(['/student-lab-results']);
    }
  }

  //go back a page, this will be changed later when pages are added
  prevStep() {
    if (this.currentObjectiveIndex > 0) {
      this.currentObjectiveIndex--;
      this.addQuestions();
    }
    else {
      this.router.navigate(['/student-courses']);
    }
  }

  //returns the page % for the progress bar
  get progress() {
    return ((this.currentObjectiveIndex + 1) / this.objectives.length) * 100;
  }

  async assignValue(){
    //don't leave this as hard coded
    const labID = cacheService.get("labID")
    console.log("labID = " + labID)
    if(typeof labID == "string"){
      this.labTemplate = await this.getLabTemplate(labID)
    }
    
  }

  async getLabTemplate(id: string): Promise<LabTemplate>{ //get the labtemplate from the ip/lab-template/id, the id is passed in from the previous page later
    const url = `http://184.100.71.203:682/lab-template/${id}`; //url to make api calls 
    const response = await fetch(url, {                   //how we call
      method: 'GET',                                      //method
      headers: {                                          //headers give us json formatting
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
    return new Promise((resolve) => {                     //sends the labTemplate promise 
       resolve(response.json())
    });
  } 
  
  getUserInfo(): Promise<User>{
    //not correct url
    var htmlBase = "http://184.100.71.203:682/lab-template/"
    const headers = new Headers()
    headers.set('Content-Type','application/json')
    headers.set('Accept', 'application/json')

    const request: RequestInfo = new Request(htmlBase+"0", {
      method: 'GET',
      headers: headers
    })

    return fetch(request).then(res => res.json()).then(res => {return res as User})
  }

  //function that changes the button color when clicked, will add more functionality later for actual answering logic
  answerQuestion(event: Event) {
    console.log("Button Clicked!");

    const button = event.target as HTMLButtonElement;

    // Find the closest question container
    const questionContainer = button.closest(".question-container");
    if (!questionContainer) {
        console.error("No question container found for button:", button);
        return;
    }

    // Find all answer bubbles inside the same question container
    const buttonsInSameQuestion = questionContainer.querySelectorAll(".answer-bubble");

    // Reset all buttons in the same question to gray
    buttonsInSameQuestion.forEach(btn => {
        (btn as HTMLElement).style.backgroundColor = "#E8E8E8";
    });

    // Change clicked button to blue
    button.style.backgroundColor = "#0167B1";
  }

  async addQuestions() {                                      //uses async because it's expecting a db call
    await this.assignValue()                                  //gets lab template with the id given (eventually will be with the id that was used before)
    var questions = this.labTemplate["questions"];            //assigns questions to the info in labTemplate, assigned values in function above
    
    if (!this.questionsContainer) {                           //error handling, but tbh I don't think this is needed anymore
      console.error("questionsContainer is undefined.");
      return;
    }

    const location = this.questionsContainer.nativeElement;    //this allows the whole of the page to be sent to the html (see last line)

    // while (location.firstChild) {                              
    //   location.removeChild(location.firstChild);
    // }

    Object.values(questions).forEach((currentQuestion) => {   //for each question do this and the i = currentQuestion
      const newQuestionContainer = this.renderer.createElement("div");    //make a div element for a container
      this.renderer.addClass(newQuestionContainer, "question-container"); //assign a name to be used in the css

      const questionHeader = this.renderer.createElement("div");  //make a div for the header
      this.renderer.addClass(questionHeader, "question-header");  //add css class

      const newNumber = this.renderer.createElement("div");       //number circle div
      this.renderer.setProperty(newNumber, "innerHTML", currentQuestion.questionNumber.toString()); //get the question# from currentQuestion
      this.renderer.addClass(newNumber, "question-number");       //add css class

      const newQuestion = this.renderer.createElement("div");     //question text div
      this.renderer.setProperty(newQuestion, "innerHTML", Object.keys(currentQuestion.question)[0]); //assign the text to the question text (called question??) then they key is next (idk why its there)
      this.renderer.addClass(newQuestion, "question-text");       //add css class

      this.renderer.appendChild(questionHeader, newNumber);       //put the new number first in container
      this.renderer.appendChild(questionHeader, newQuestion);     //put the question text second in container
      this.renderer.appendChild(newQuestionContainer, questionHeader);  //put the header in the question container

      if (currentQuestion.questionType === "WRITTEN") {           //same thing as before, but if it is written or t/f
          const newInput = this.renderer.createElement("input");
          this.renderer.addClass(newInput, 'question-input');
          this.renderer.appendChild(newQuestionContainer, newInput);
      } 
      else if (["MULTIPLE_CHOICE", "TRUE_FALSE"].includes(currentQuestion.questionType)) {
        const answers = currentQuestion.question[Object.keys(currentQuestion.question)[0]]; // This gets the array of answers
        
        answers.forEach((answer: string) => {
            const newAnswerContainer = this.renderer.createElement("div");
            this.renderer.addClass(newAnswerContainer, "answer-container");
    
            const newButton = this.renderer.createElement("button");
            this.renderer.addClass(newButton, "answer-bubble");
            this.renderer.listen(newButton, "click", (event: MouseEvent) => this.answerQuestion(event));
    
            const newAnswerText = this.renderer.createElement("span");
            this.renderer.setProperty(newAnswerText, "innerHTML", answer);
    
            this.renderer.appendChild(newAnswerContainer, newButton);
            this.renderer.appendChild(newAnswerContainer, newAnswerText);
            this.renderer.appendChild(newQuestionContainer, newAnswerContainer);
        });
      }
    this.renderer.appendChild(location, newQuestionContainer);//add this to the page
    });
   }
  }