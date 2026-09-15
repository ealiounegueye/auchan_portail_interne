export type Role = 'USER' | 'MANAGER' | 'ADMIN';

export interface AppAccessGrant {
  applicationId: number;
  canViewDocumentation: boolean;
  canViewTechnicalSheet: boolean;
  canViewUserGuide: boolean;
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  department: string;
  active: boolean;
  managerId: number | null;
  managerName: string | null;
  restrictedAccess: boolean;
  allowedApplicationIds: number[];
  applicationGrants?: AppAccessGrant[];
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  icon: string;
  color: string;
  sortOrder: number;
}

export interface Department {
  id: number;
  name: string;
  code: string;
  description?: string | null;
  sortOrder: number;
}

export type DocumentKind = 'documentation' | 'fiche-technique' | 'guide';
export type DocumentFormat = 'html' | 'pdf';

export interface UploadedDocument {
  storedFile: string;
  originalName: string;
  contentType: string;
}

export interface CategoryPayload {
  name: string;
  slug?: string;
  icon: string;
  color: string;
  sortOrder?: number;
}

export interface DepartmentPayload {
  name: string;
  code?: string;
  description?: string;
  sortOrder: number;
}

export interface BusinessApp {
  id: number;
  name: string;
  description: string;
  url: string;
  icon: string;
  logoUrl?: string | null;
  category: Category;
  ownerDepartment: string;
  status: string;
  featured: boolean;
  sortOrder: number;
  favorite: boolean;
  longDescription?: string | null;
  modop?: string | null;
  technicalSheetContent?: string | null;
  userGuideContent?: string | null;
  documentationUrl?: string | null;
  documentationStoredFile?: string | null;
  documentationFileName?: string | null;
  documentationContentType?: string | null;
  technicalSheetUrl?: string | null;
  technicalSheetStoredFile?: string | null;
  technicalSheetFileName?: string | null;
  technicalSheetContentType?: string | null;
  userGuideUrl?: string | null;
  userGuideStoredFile?: string | null;
  userGuideFileName?: string | null;
  userGuideContentType?: string | null;
  supportUrl?: string | null;
  supportContact?: string | null;
  version?: string | null;
  audience?: string | null;
  canViewDocumentation?: boolean;
  canViewTechnicalSheet?: boolean;
  canViewUserGuide?: boolean;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface ApplicationPayload {
  name: string;
  description: string;
  url: string;
  icon: string;
  logoUrl?: string;
  categoryId: number;
  ownerDepartment: string;
  status: string;
  featured: boolean;
  sortOrder?: number;
  longDescription?: string;
  modop?: string;
  technicalSheetContent?: string;
  userGuideContent?: string;
  documentationUrl?: string;
  documentationStoredFile?: string;
  documentationFileName?: string;
  documentationContentType?: string;
  technicalSheetUrl?: string;
  technicalSheetStoredFile?: string;
  technicalSheetFileName?: string;
  technicalSheetContentType?: string;
  userGuideUrl?: string;
  userGuideStoredFile?: string;
  userGuideFileName?: string;
  userGuideContentType?: string;
  supportUrl?: string;
  supportContact?: string;
  version?: string;
  audience?: string;
}

export interface UserPayload {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  role: Role;
  department: string;
  active: boolean;
  managerId: number | null;
  restrictedAccess: boolean;
  allowedApplicationIds: number[];
  applicationGrants: AppAccessGrant[];
}

export const ROLE_LABELS: Record<Role, string> = {
  USER: 'Collaborateur',
  MANAGER: 'Responsable',
  ADMIN: 'Administrateur'
};
