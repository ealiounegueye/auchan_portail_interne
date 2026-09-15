import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  AppAccessGrant,
  ApplicationPayload,
  BusinessApp,
  Category,
  CategoryPayload,
  Department,
  DepartmentPayload,
  DocumentFormat,
  DocumentKind,
  UploadedDocument,
  User,
  UserPayload
} from './models';

@Injectable({ providedIn: 'root' })
export class PortalService {
  constructor(private readonly http: HttpClient) {}

  applications(query = '', categoryId?: number | null) {
    let params = new HttpParams();
    if (query) {
      params = params.set('q', query);
    }
    if (categoryId) {
      params = params.set('categoryId', categoryId);
    }
    return this.http.get<BusinessApp[]>('/api/applications', { params });
  }

  application(id: number) {
    return this.http.get<BusinessApp>(`/api/applications/${id}`);
  }

  allApplications() {
    return this.http.get<BusinessApp[]>('/api/admin/applications');
  }

  toggleFavorite(id: number) {
    return this.http.post<void>(`/api/applications/${id}/favorite`, {});
  }

  categories() {
    return this.http.get<Category[]>('/api/categories');
  }

  departments() {
    return this.http.get<Department[]>('/api/departments');
  }

  createCategory(payload: CategoryPayload) {
    return this.http.post<Category>('/api/admin/categories', payload);
  }

  updateCategory(id: number, payload: CategoryPayload) {
    return this.http.put<Category>(`/api/admin/categories/${id}`, payload);
  }

  reorderCategories(ids: number[]) {
    return this.http.put<Category[]>('/api/admin/categories/reorder', { ids });
  }

  deleteCategory(id: number) {
    return this.http.delete<void>(`/api/admin/categories/${id}`);
  }

  createDepartment(payload: DepartmentPayload) {
    return this.http.post<Department>('/api/admin/departments', payload);
  }

  updateDepartment(id: number, payload: DepartmentPayload) {
    return this.http.put<Department>(`/api/admin/departments/${id}`, payload);
  }

  deleteDepartment(id: number) {
    return this.http.delete<void>(`/api/admin/departments/${id}`);
  }

  downloadDocument(id: number, kind: DocumentKind, format: DocumentFormat) {
    return this.http.get(`/api/applications/${id}/documents/${kind}`, {
      params: { format },
      responseType: 'blob',
      observe: 'response'
    });
  }

  downloadUploadedDocument(id: number, kind: DocumentKind) {
    return this.http.get(`/api/applications/${id}/documents/${kind}/file`, {
      responseType: 'blob',
      observe: 'response'
    });
  }

  createApplication(payload: ApplicationPayload) {
    return this.http.post<BusinessApp>('/api/admin/applications', payload);
  }

  uploadLogo(file: File) {
    const data = new FormData();
    data.append('file', file);
    return this.http.post<{ url: string }>('/api/admin/uploads/logos', data);
  }

  uploadDocument(file: File) {
    const data = new FormData();
    data.append('file', file);
    return this.http.post<UploadedDocument>('/api/admin/uploads/documents', data);
  }

  updateApplication(id: number, payload: ApplicationPayload) {
    return this.http.put<BusinessApp>(`/api/admin/applications/${id}`, payload);
  }

  reorderApplications(ids: number[]) {
    return this.http.put<BusinessApp[]>('/api/admin/applications/reorder', { ids });
  }

  deleteApplication(id: number) {
    return this.http.delete<void>(`/api/admin/applications/${id}`);
  }

  users() {
    return this.http.get<User[]>('/api/admin/users');
  }

  createUser(payload: UserPayload) {
    return this.http.post<User>('/api/admin/users', payload);
  }

  updateUser(id: number, payload: UserPayload) {
    return this.http.put<User>(`/api/admin/users/${id}`, payload);
  }

  updateAccess(
    id: number,
    payload: {
      restrictedAccess: boolean;
      allowedApplicationIds: number[];
      applicationGrants: AppAccessGrant[];
    }
  ) {
    return this.http.put<User>(`/api/admin/users/${id}/access`, payload);
  }
}
