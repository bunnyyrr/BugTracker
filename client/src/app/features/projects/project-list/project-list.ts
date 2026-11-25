import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ProjectService } from '../../../core/project.service';
import { ProjectSummary } from '../../../core/models/project';
import { ProjectCard } from '../project-card/project-card';

@Component({
  selector: 'app-project-list',
  imports: [ProjectCard],
  templateUrl: './project-list.html',
  styleUrl: './project-list.scss',
})
export class ProjectList implements OnInit {
  private readonly projectService = inject(ProjectService);

  projects = signal<ProjectSummary[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  countLabel = computed(() => {
    const n = this.projects().length;
    const lastDigit = n % 10;
    const lastTwoDigits = n % 100;

    if (lastDigit === 1 && lastTwoDigits !== 11) {
      return `${n} проект, в котором вы участвуете`;
    }
    if (lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 12 || lastTwoDigits > 14)) {
      return `${n} проекта, в которых вы участвуете`;
    }
    return `${n} проектов, в которых вы участвуете`;
  });

  ngOnInit(): void {
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects.set(projects);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(e.error?.message ?? 'Не удалось связаться с сервером');
        this.loading.set(false);
      },
    });
  }
}
