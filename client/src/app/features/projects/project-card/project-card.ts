import { Component, input } from '@angular/core';
import { ProjectSummary } from '../../../core/models/project';
import { RoleBadge } from '../../../shared/role-badge/role-badge';

@Component({
  selector: 'app-project-card',
  imports: [RoleBadge],
  templateUrl: './project-card.html',
  styleUrl: './project-card.scss',
})
export class ProjectCard {
  project = input.required<ProjectSummary>();
}
