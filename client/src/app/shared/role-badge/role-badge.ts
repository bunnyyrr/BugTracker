import { Component, computed, input } from '@angular/core';
import { Role, ROLE_LABELS } from '../../core/models/project';

@Component({
  selector: 'app-role-badge',
  templateUrl: './role-badge.html',
  styleUrl: './role-badge.scss',
})
export class RoleBadge {
  role = input.required<Role>();
  label = computed(() => ROLE_LABELS[this.role()]);
}
