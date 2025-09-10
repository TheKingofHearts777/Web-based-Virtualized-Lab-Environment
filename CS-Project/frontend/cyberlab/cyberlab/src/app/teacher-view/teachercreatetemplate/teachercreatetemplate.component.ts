import {Component, inject, OnInit} from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import {FormArray, FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import { HeaderComponent } from "../../shared/header/header.component";
import {MatMenuModule} from '@angular/material/menu';
import {MatButtonModule} from '@angular/material/button';
import { IntegerType, ObjectId } from 'mongodb';
import { cacheService } from '../../CacheService';
import {delay} from 'rxjs';

interface LabQuestion{
  questionNumber: number;
  questionType: string;
  question: string;
  answer: string;
}

interface LabTemplate{
  questions: LabQuestion[];
}

type FormAnswer = FormGroup<{
  text: FormControl<string>
}>;

type FormObjective = FormGroup<{
  objectiveName: FormControl<string>
  objectiveDesc: FormControl<string>
  steps: FormArray<FormStep>
  textQuestions: FormArray<FormQuestionText>
  choiceQuestions: FormArray<FormQuestionChoice>
  tfQuestions: FormArray<FormQuestionTF>
}>;

type FormStep = FormGroup<{
  stepName: FormControl<string>
  stepDesc: FormControl<string>
}>;

type FormQuestionTF = FormGroup<{
  questionName: FormControl<string>
}>;

type FormQuestionText = FormGroup<{
  questionName: FormControl<string>
}>;

type FormQuestionChoice = FormGroup<{
  questionName: FormControl<string>
  answers: FormArray<FormAnswer>
}>;

type Form = FormGroup<{
  objectives: FormArray<FormObjective>
}>;

@Component({
  selector: 'app-teachercreatetemplate',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatIcon,
    MatButton,
    MatInput,
    HeaderComponent,
    MatButtonModule,
    MatMenuModule
],
  templateUrl: './teachercreatetemplate.component.html',
  styleUrl: './teachercreatetemplate.component.css'
})
export class TeachercreatetemplateComponent {
  title = 'teacher-create-template';
  fb = inject(NonNullableFormBuilder);
  labForm: Form = this.fb.group({
    objectives: this.fb.array<FormObjective>([this.generateObjective()])
  })

  generateObjective(): FormObjective {
    return this.fb.group({
      objectiveName: '',
      objectiveDesc: '',
      steps: this.fb.array<FormStep>([]),
      textQuestions: this.fb.array<FormQuestionText>([]),
      choiceQuestions: this.fb.array<FormQuestionChoice>([]),
      tfQuestions: this.fb.array<FormQuestionTF>([])
    });
  }

  generateStep(): FormStep {
    return this.fb.group({
      stepName: '',
      stepDesc: '',
    })
  }

  generateTFQuestion(): FormQuestionTF {
    return this.fb.group({
      questionName: '',
    });
  }

  generateChoiceQuestion(): FormQuestionChoice {
    return this.fb.group({
      questionName: '',
      answers: this.fb.array<FormAnswer>([])
    });
  }

  generateTextQuestion(): FormQuestionText {
    return this.fb.group({
      questionName: '',
    });
  }

  addObjective(): void {
    this.labForm.controls.objectives.push(this.generateObjective());
  }

  removeObjective(objectiveIndex:number) {
    this.labForm.controls.objectives.removeAt(objectiveIndex);
  }

  addStep(objectiveIndex:number): void {
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.steps?.push(this.generateStep());
  }

  removeStep(objectiveIndex:number, taskIndex: number) {
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.steps?.removeAt(taskIndex);
  }

  addTFQuestion(objectiveIndex:number): void {
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.tfQuestions?.push(this.generateTFQuestion());
  }

  removeTFQuestion(objectiveIndex:number, tfQuestionIndex: number) {
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.tfQuestions?.removeAt(tfQuestionIndex);
  }

  addChoiceQuestion(objectiveIndex:number): void {
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.choiceQuestions?.push(this.generateChoiceQuestion());
  }

  removeChoiceQuestion(objectiveIndex:number, choiceQuestionIndex: number) {
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.choiceQuestions?.removeAt(choiceQuestionIndex);
  }

  addTextQuestion(objectiveIndex:number): void {
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.textQuestions?.push(this.generateTextQuestion());
  }

  removeTextQuestion(objectiveIndex:number, textQuestionIndex: number) {
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.textQuestions?.removeAt(textQuestionIndex);
  }

  addAnswer(objectiveIndex:number, choiceQuestionIndex: number): void {
    const newAnswer: FormAnswer = this.fb.group({
      text: ''
    })
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.choiceQuestions?.at(choiceQuestionIndex)
      ?.controls?.answers?.push(newAnswer);
  }

  removeAnswer(objectiveIndex:number, choiceQuestionIndex: number, answerIndex: number): void {
    this.labForm.controls.objectives.at(objectiveIndex)?.controls?.choiceQuestions?.at(choiceQuestionIndex)
      ?.controls?.answers?.removeAt(answerIndex);
  }

  async onSubmit() {
    console.log('Pushing data to cache...');
    cacheService.set("LabTemplate",this.labForm.getRawValue(),60*20);
    await new Promise(resolve => setTimeout(resolve, 1000));
    console.log('Success');
    console.log('Pulling cached data...');
    await new Promise(resolve => setTimeout(resolve, 1000));
    console.log(cacheService.get("LabTemplate"));
  }
}
