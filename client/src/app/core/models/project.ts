export type Role = 'TESTER' | 'DEVELOPER' | 'TEAM_LEAD';

export interface ProjectSummary {
  id: number;
  name: string;
  description?: string | null;
  role: Role;
  openBugCount: number;
}

export interface Project {
  id: number;
  name: string;
  description?: string | null;
  myRole: Role;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
}
