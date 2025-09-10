import { Component } from '@angular/core'; // Angular decorator for defining a component
import { Router, RouterLink, RouterOutlet } from '@angular/router'; // Provides routing capabilities
import { MatIcon } from '@angular/material/icon'; // Provides Angular Material icons
import { MatListModule } from '@angular/material/list'; // Provides Angular Material selection list components
import { CommonModule } from '@angular/common'; // Provides directives like *ngFor and *ngIf
import { HeaderComponent } from '../../shared/header/header.component'; // Shared header component
import { cacheService } from '../../CacheService'; // Custom caching service to store selected lab template
import { ObjectId, IntegerType } from 'mongodb'; // MongoDB types for database objects

// Interface representing a question in a lab template
interface LabQuestion {
  questionNumber: number; // Order of the question in the lab
  questionType: string; // Type of question (e.g., WRITTEN, MULTIPLE_CHOICE)
  question: { [key: string]: string[] }; // Question content and possible answers
  answer: string; // Correct answer
}

// Interface representing a lab template
interface LabTemplate {
  _id: ObjectId; // Unique identifier for the lab template
  creator: IntegerType; // ID of the template's creator
  name: string; // Name of the lab template
  description: string; // Description of the lab template
  objectives: string[]; // List of objectives for the lab template
  vmTemplateIds: ObjectId[]; // IDs of associated virtual machine templates
  questions: LabQuestion[]; // Array of questions for the lab
}

@Component({
  selector: 'app-teachercreateassignment', // Selector used to reference the component in templates
  standalone: true, // Specifies the component as standalone
  imports: [RouterOutlet, RouterLink, MatIcon, MatListModule, CommonModule, HeaderComponent], // Modules to be used in the component
  templateUrl: './teachercreateassignment.component.html', // Path to the HTML template
  styleUrl: './teachercreateassignment.component.css' // Path to the CSS file for styling
})
export class TeachercreateassignmentComponent {
  title = 'teacher-create-assignment'; // Page title

  labTemplates: LabTemplate[] = []; // Array to hold lab templates fetched from the database
  selectedLabTemplate: LabTemplate | null = null; // Stores the lab template selected by the user
  errorMessage: string = ''; // Stores an error message if no selection is made

  constructor(private router: Router) {} // Injects the router for navigation

  // Lifecycle hook: Executes when the component is initialized
  async ngOnInit(): Promise<void> {
    await this.loadLabTemplates(); // Fetch lab templates from the database
  }

  // Fetches lab templates from the database
  async loadLabTemplates(): Promise<void> {
    const url = `http://184.100.71.203:682/lab-template/list?limit=100&offset=0`; // API endpoint for fetching lab templates
    try {
      const response = await fetch(url, {
        method: 'GET', // Use the GET method to retrieve data
        headers: {
          'Content-Type': 'application/json', // Specify JSON content type
          'Accept': 'application/json' // Accept JSON responses
        }
      });
      const data = await response.json(); // Parse the JSON response
      this.labTemplates = data as LabTemplate[]; // Assign the data to the labTemplates array
    } catch (error) {
      console.error('Error fetching lab templates:', error); // Log any errors
    }
  }

  // Stores the selected lab template and clears any error message
  onLabSelected(labTemplate: LabTemplate): void {
    this.selectedLabTemplate = labTemplate; // Cache the selected lab template
    this.errorMessage = ''; // Clear the error message
  }

  // Validates the selection and navigates to the next page or displays an error message
  onNext(): void {
    if (this.selectedLabTemplate) {
      cacheService.set("selectedLabTemplate", this.selectedLabTemplate, 60 * 20); // Store the selected lab template in cache for 20 minutes
      this.router.navigate(['/teacher-assign-points']); // Navigate to the next page
    } else {
      this.errorMessage = 'Please select a Lab Template to continue'; // Display an error message if no selection is made
    }
  }
}
