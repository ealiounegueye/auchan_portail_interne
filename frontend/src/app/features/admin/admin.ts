import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
  ROLE_LABELS,
  Role,
  User,
  UserPayload
} from '../../core/models';
import { AuthService } from '../../core/auth.service';
import { PortalService } from '../../core/portal.service';

@Component({
  selector: 'app-admin',
  imports: [FormsModule, RouterLink],
  templateUrl: './admin.html',
  styleUrl: './admin.scss'
})
export class Admin implements OnInit {
  readonly roleLabels = ROLE_LABELS;
  tab = signal<'apps' | 'catalog' | 'users'>('users');
  readonly apps = signal<BusinessApp[]>([]);
  readonly users = signal<User[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly departments = signal<Department[]>([]);
  readonly message = signal('');
  readonly messageError = signal(false);
  readonly draggingCategoryIndex = signal<number | null>(null);
  readonly dropTargetIndex = signal<number | null>(null);
  readonly draggingAppIndex = signal<number | null>(null);
  readonly dropAppTargetIndex = signal<number | null>(null);
  readonly logoUploading = signal(false);
  readonly logoPreview = signal('');
  readonly documentUploading = signal(false);
  readonly documentAccept =
    '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.odt,.ods,.odp,.txt,.html,.htm,.png,.jpg,.jpeg,.webp,.gif';

  appForm: ApplicationPayload = this.emptyApp();
  editingAppId: number | null = null;
  userForm: UserPayload = Admin.blankUser();
  editingUserId: number | null = null;
  categoryForm: CategoryPayload = this.emptyCategory();
  editingCategoryId: number | null = null;
  private categoryOrderSeq = 0;
  private appOrderSeq = 0;
  private pendingEditAppId: number | null = null;
  departmentForm: DepartmentPayload = this.emptyDepartment();
  editingDepartmentId: number | null = null;

  readonly managers = computed(() =>
    this.users().filter((user) => user.role === 'MANAGER' || user.role === 'ADMIN')
  );

  readonly appsByCategory = computed(() => {
    const groups = new Map<string, BusinessApp[]>();
    for (const app of this.apps()) {
      if (!app.category) {
        continue;
      }
      const name = app.category.name;
      const list = groups.get(name) ?? [];
      list.push(app);
      groups.set(name, list);
    }
    return [...groups.entries()];
  });

  constructor(
    private readonly portal: PortalService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    readonly auth: AuthService
  ) {}

  ngOnInit() {
    if (this.auth.isAdmin()) {
      this.tab.set('apps');
    }
    const editApp = Number(this.route.snapshot.queryParamMap.get('editApp'));
    if (Number.isFinite(editApp) && editApp > 0) {
      this.pendingEditAppId = editApp;
      this.tab.set('apps');
    }
    this.userForm = this.emptyUser();
    this.reload();
  }

  reload() {
    this.portal.applications().subscribe({
      next: (apps) => {
        this.apps.set(apps.filter((app) => !!app.category));
        this.applyPendingAppEdit();
      },
      error: (err) => {
        if (err?.status === 401 || err?.status === 403) {
          return;
        }
        this.flash(this.apiError(err, 'Impossible de charger les applications'), true);
      }
    });
    this.portal.categories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        if (!this.editingAppId && categories.length && !categories.some((item) => item.id === this.appForm.categoryId)) {
          this.appForm.categoryId = categories[0].id;
        }
      },
      error: () => this.flash('Impossible de charger les catégories', true)
    });
    this.portal.departments().subscribe({
      next: (departments) => {
        this.departments.set(departments);
        if (!this.editingAppId && departments.length && !this.appForm.ownerDepartment) {
          this.appForm.ownerDepartment = departments[0].name;
        }
        if (!this.editingUserId && departments.length && !this.userForm.department) {
          this.userForm.department = this.auth.user()?.department || departments[0].name;
        }
      },
      error: () => this.flash('Impossible de charger les directions', true)
    });
    this.portal.users().subscribe({
      next: (users) =>
        this.users.set(
          users.map((user) => ({
            ...user,
            allowedApplicationIds: user.allowedApplicationIds ?? [],
            applicationGrants: user.applicationGrants ?? [],
            role: user.role || 'USER'
          }))
        ),
      error: (err) =>
        this.flash(
          err?.status === 403
            ? 'Accès administration refusé. Reconnectez-vous avec admin@auchan.sn'
            : this.apiError(err, 'Impossible de charger les utilisateurs'),
          true
        )
    });
  }

  assignableRoles(): { value: Role; label: string }[] {
    return (Object.keys(ROLE_LABELS) as Role[]).map((value) => ({
      value,
      label: ROLE_LABELS[value]
    }));
  }

  editApp(app: BusinessApp) {
    this.editingAppId = app.id;
    this.appForm = {
      name: app.name,
      description: app.description,
      url: app.url,
      icon: app.icon,
      logoUrl: app.logoUrl ?? '',
      categoryId: app.category.id,
      ownerDepartment: app.ownerDepartment,
      status: app.status,
      featured: app.featured,
      sortOrder: app.sortOrder,
      longDescription: app.longDescription ?? '',
      modop: app.modop ?? '',
      technicalSheetContent: app.technicalSheetContent ?? '',
      userGuideContent: app.userGuideContent ?? '',
      documentationUrl: app.documentationUrl ?? '',
      documentationStoredFile: app.documentationStoredFile ?? '',
      documentationFileName: app.documentationFileName ?? '',
      documentationContentType: app.documentationContentType ?? '',
      technicalSheetUrl: app.technicalSheetUrl ?? '',
      technicalSheetStoredFile: app.technicalSheetStoredFile ?? '',
      technicalSheetFileName: app.technicalSheetFileName ?? '',
      technicalSheetContentType: app.technicalSheetContentType ?? '',
      userGuideUrl: app.userGuideUrl ?? '',
      userGuideStoredFile: app.userGuideStoredFile ?? '',
      userGuideFileName: app.userGuideFileName ?? '',
      userGuideContentType: app.userGuideContentType ?? '',
      supportUrl: app.supportUrl ?? '',
      supportContact: app.supportContact ?? '',
      version: app.version ?? '',
      audience: app.audience ?? ''
    };
    this.resetLogoPreview();
    queueMicrotask(() => document.getElementById('app-form')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
  }

  cancelApp() {
    this.editingAppId = null;
    this.appForm = this.emptyApp();
    this.logoUploading.set(false);
    this.documentUploading.set(false);
    this.resetLogoPreview();
  }

  logoSrc() {
    return this.logoPreview() || this.appForm.logoUrl || '';
  }

  onLogoFile(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.resetLogoPreview();
    this.logoPreview.set(URL.createObjectURL(file));
    this.logoUploading.set(true);
    this.portal.uploadLogo(file).subscribe({
      next: (response) => {
        this.appForm = { ...this.appForm, logoUrl: response.url };
        this.logoUploading.set(false);
        this.flash('Logo importé. Enregistrez l’application pour le conserver.');
      },
      error: (err) => {
        this.logoUploading.set(false);
        this.resetLogoPreview();
        input.value = '';
        this.flash(this.apiError(err, 'Import du logo impossible'), true);
      }
    });
  }

  clearLogo() {
    this.resetLogoPreview();
    this.appForm = { ...this.appForm, logoUrl: '' };
  }

  busyLabel() {
    if (this.logoUploading()) {
      return 'Import du logo…';
    }
    if (this.documentUploading()) {
      return 'Import du fichier…';
    }
    return 'Enregistrer';
  }

  onDocumentFile(event: Event, kind: DocumentKind) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.documentUploading.set(true);
    this.portal.uploadDocument(file).subscribe({
      next: (response) => {
        this.appForm = { ...this.appForm, ...this.fileFields(kind, response.storedFile, response.originalName, response.contentType) };
        this.documentUploading.set(false);
        this.flash('Fichier importé. Enregistrez l’application pour le conserver.');
      },
      error: (err) => {
        this.documentUploading.set(false);
        input.value = '';
        this.flash(this.apiError(err, 'Import du fichier impossible'), true);
      }
    });
  }

  clearDocumentFile(kind: DocumentKind) {
    this.appForm = { ...this.appForm, ...this.fileFields(kind, '', '', '') };
  }

  private fileFields(kind: DocumentKind, storedFile: string, originalName: string, contentType: string): Partial<ApplicationPayload> {
    if (kind === 'fiche-technique') {
      return {
        technicalSheetStoredFile: storedFile,
        technicalSheetFileName: originalName,
        technicalSheetContentType: contentType
      };
    }
    if (kind === 'guide') {
      return {
        userGuideStoredFile: storedFile,
        userGuideFileName: originalName,
        userGuideContentType: contentType
      };
    }
    return {
      documentationStoredFile: storedFile,
      documentationFileName: originalName,
      documentationContentType: contentType
    };
  }

  private resetLogoPreview() {
    const current = this.logoPreview();
    if (current.startsWith('blob:')) {
      URL.revokeObjectURL(current);
    }
    this.logoPreview.set('');
  }

  onAppDragStart(event: DragEvent, index: number) {
    const path = event.composedPath();
    if (path.some((node) => node instanceof HTMLElement && node.classList.contains('actions'))) {
      event.preventDefault();
      return;
    }
    this.draggingAppIndex.set(index);
    event.dataTransfer?.setData('text/plain', String(index));
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
    }
  }

  onAppDragOver(event: DragEvent, index: number) {
    if (this.draggingAppIndex() === null) {
      return;
    }
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
    this.dropAppTargetIndex.set(index);
  }

  onAppDragLeave(index: number) {
    if (this.dropAppTargetIndex() === index) {
      this.dropAppTargetIndex.set(null);
    }
  }

  onAppDrop(event: DragEvent, index: number) {
    event.preventDefault();
    const from = this.draggingAppIndex();
    this.dropAppTargetIndex.set(null);
    this.draggingAppIndex.set(null);
    if (from === null) {
      return;
    }
    this.moveAppTo(from, index);
  }

  onAppDragEnd() {
    this.draggingAppIndex.set(null);
    this.dropAppTargetIndex.set(null);
  }

  moveApp(index: number, delta: number) {
    this.moveAppTo(index, index + delta);
  }

  saveApp() {
    if (!this.appForm.name?.trim() || !this.appForm.description?.trim() || !this.appForm.url?.trim()) {
      this.flash('Nom, description et URL sont obligatoires', true);
      return;
    }
    if (!this.appForm.categoryId) {
      this.flash('Choisissez une catégorie', true);
      return;
    }
    if (!this.appForm.ownerDepartment) {
      this.flash('Choisissez une direction', true);
      return;
    }
    const { sortOrder: _sortOrder, ...form } = this.appForm;
    const payload: ApplicationPayload = {
      ...form,
      categoryId: Number(this.appForm.categoryId),
      icon: this.appForm.icon?.trim() || 'monitor',
      logoUrl: this.appForm.logoUrl?.trim() || undefined
    };
    const request = this.editingAppId
      ? this.portal.updateApplication(this.editingAppId, payload)
      : this.portal.createApplication(payload);
    request.subscribe({
      next: () => {
        this.flash('Application enregistrée');
        this.cancelApp();
        this.reload();
      },
      error: (err) => this.flash(this.apiError(err, 'Enregistrement impossible'), true)
    });
  }

  deleteApp(app: BusinessApp) {
    if (!confirm(`Supprimer ${app.name} ?`)) {
      return;
    }
    this.portal.deleteApplication(app.id).subscribe(() => this.reload());
  }

  downloadAppDocument(app: BusinessApp, kind: DocumentKind, format: DocumentFormat) {
    this.downloadDocument(app.id, kind, format);
  }

  downloadDocument(id: number, kind: DocumentKind, format: DocumentFormat) {
    this.portal.downloadDocument(id, kind, format).subscribe({
      next: (response) => this.openGeneratedFile(response, `${kind}.${format}`),
      error: (err) => this.flash(this.apiError(err, 'Génération du document impossible'), true)
    });
  }

  editCategory(category: Category) {
    this.editingCategoryId = category.id;
    this.categoryForm = {
      name: category.name,
      slug: category.slug,
      icon: category.icon,
      color: category.color
    };
  }

  cancelCategory() {
    this.editingCategoryId = null;
    this.categoryForm = this.emptyCategory();
  }

  onCategoryDragStart(event: DragEvent, index: number) {
    const path = event.composedPath();
    if (path.some((node) => node instanceof HTMLElement && node.classList.contains('actions'))) {
      event.preventDefault();
      return;
    }
    this.draggingCategoryIndex.set(index);
    event.dataTransfer?.setData('text/plain', String(index));
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
    }
  }

  onCategoryDragOver(event: DragEvent, index: number) {
    if (this.draggingCategoryIndex() === null) {
      return;
    }
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
    this.dropTargetIndex.set(index);
  }

  onCategoryDragLeave(index: number) {
    if (this.dropTargetIndex() === index) {
      this.dropTargetIndex.set(null);
    }
  }

  onCategoryDrop(event: DragEvent, index: number) {
    event.preventDefault();
    const from = this.draggingCategoryIndex();
    this.dropTargetIndex.set(null);
    this.draggingCategoryIndex.set(null);
    if (from === null) {
      return;
    }
    this.moveCategoryTo(from, index);
  }

  onCategoryDragEnd() {
    this.draggingCategoryIndex.set(null);
    this.dropTargetIndex.set(null);
  }

  moveCategory(index: number, delta: number) {
    this.moveCategoryTo(index, index + delta);
  }

  saveCategory() {
    if (!this.categoryForm.name?.trim() || !this.categoryForm.icon?.trim()) {
      this.flash('Le nom et l’icône de la catégorie sont obligatoires', true);
      return;
    }
    const payload: CategoryPayload = {
      name: this.categoryForm.name.trim(),
      slug: this.categoryForm.slug?.trim() || undefined,
      icon: this.categoryForm.icon.trim(),
      color: this.normalizeColor(this.categoryForm.color)
    };
    const request = this.editingCategoryId
      ? this.portal.updateCategory(this.editingCategoryId, payload)
      : this.portal.createCategory(payload);
    request.subscribe({
      next: () => {
        this.flash('Catégorie enregistrée');
        this.cancelCategory();
        this.reload();
      },
      error: (err) => this.flash(this.apiError(err, 'Enregistrement de la catégorie impossible'), true)
    });
  }

  deleteCategory(category: Category) {
    if (!confirm(`Supprimer la catégorie ${category.name} ?`)) {
      return;
    }
    this.portal.deleteCategory(category.id).subscribe({
      next: () => {
        this.flash('Catégorie supprimée');
        this.reload();
      },
      error: (err) => this.flash(this.apiError(err, 'Suppression impossible'), true)
    });
  }

  editDepartment(department: Department) {
    this.editingDepartmentId = department.id;
    this.departmentForm = {
      name: department.name,
      code: department.code,
      description: department.description ?? '',
      sortOrder: department.sortOrder
    };
  }

  cancelDepartment() {
    this.editingDepartmentId = null;
    this.departmentForm = this.emptyDepartment();
  }

  saveDepartment() {
    if (!this.departmentForm.name?.trim()) {
      this.flash('Le nom de la direction est obligatoire', true);
      return;
    }
    const payload: DepartmentPayload = {
      name: this.departmentForm.name.trim(),
      code: this.departmentForm.code?.trim() || undefined,
      description: this.departmentForm.description?.trim() || undefined,
      sortOrder: this.toOrder(this.departmentForm.sortOrder)
    };
    const request = this.editingDepartmentId
      ? this.portal.updateDepartment(this.editingDepartmentId, payload)
      : this.portal.createDepartment(payload);
    request.subscribe({
      next: () => {
        this.flash('Direction enregistrée');
        this.cancelDepartment();
        this.reload();
      },
      error: (err) => this.flash(this.apiError(err, 'Enregistrement de la direction impossible'), true)
    });
  }

  deleteDepartment(department: Department) {
    if (!confirm(`Supprimer la direction ${department.name} ?`)) {
      return;
    }
    this.portal.deleteDepartment(department.id).subscribe({
      next: () => {
        this.flash('Direction supprimée');
        this.reload();
      },
      error: (err) => this.flash(this.apiError(err, 'Suppression impossible'), true)
    });
  }

  editUser(user: User) {
    this.editingUserId = user.id;
    const grants = (user.applicationGrants ?? []).map((grant) => ({ ...grant }));
    this.userForm = {
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      password: '',
      role: user.role,
      department: user.department,
      active: user.active,
      managerId: user.managerId,
      restrictedAccess: user.restrictedAccess,
      allowedApplicationIds: grants.map((grant) => grant.applicationId),
      applicationGrants: grants
    };
  }

  onRoleChange(role: Role) {
    this.userForm.role = role;
    if (role === 'ADMIN') {
      this.userForm.restrictedAccess = false;
      this.userForm.allowedApplicationIds = [];
      this.userForm.applicationGrants = [];
    }
  }

  hasAccess(id: number) {
    return this.userForm.applicationGrants.some((grant) => grant.applicationId === id);
  }

  grantFor(id: number): AppAccessGrant | undefined {
    return this.userForm.applicationGrants.find((grant) => grant.applicationId === id);
  }

  toggleAccess(id: number) {
    if (this.hasAccess(id)) {
      this.userForm.applicationGrants = this.userForm.applicationGrants.filter((grant) => grant.applicationId !== id);
    } else {
      this.userForm.applicationGrants = [
        ...this.userForm.applicationGrants,
        this.defaultGrant(id)
      ];
    }
    this.syncAllowedIds();
  }

  toggleDocument(id: number, field: keyof Pick<AppAccessGrant, 'canViewDocumentation' | 'canViewTechnicalSheet' | 'canViewUserGuide'>) {
    const grant = this.grantFor(id);
    if (!grant) {
      return;
    }
    grant[field] = !grant[field];
    this.userForm.applicationGrants = [...this.userForm.applicationGrants];
  }

  selectAllApps() {
    this.userForm.applicationGrants = this.apps().map((app) => this.defaultGrant(app.id, true));
    this.syncAllowedIds();
  }

  clearApps() {
    this.userForm.applicationGrants = [];
    this.syncAllowedIds();
  }

  cancelUser() {
    this.editingUserId = null;
    this.userForm = this.emptyUser();
  }

  selectedUser(): User | undefined {
    return this.users().find((user) => user.id === this.editingUserId);
  }

  saveUser() {
    if (this.auth.isManager()) {
      if (!this.editingUserId) {
        this.flash('Choisissez un collaborateur déjà présent dans Bird pour lui donner des accès.', true);
        return;
      }
      this.portal
        .updateAccess(this.editingUserId, {
          restrictedAccess: this.userForm.restrictedAccess,
          allowedApplicationIds: this.userForm.applicationGrants.map((grant) => grant.applicationId),
          applicationGrants: this.userForm.applicationGrants
        })
        .subscribe({
          next: () => {
            this.flash('Accès enregistrés');
            this.cancelUser();
            this.reload();
          },
          error: (err) => this.flash(this.apiError(err, 'Enregistrement impossible'), true)
        });
      return;
    }

    if (!this.userForm.firstName?.trim() || !this.userForm.lastName?.trim() || !this.userForm.email?.trim()) {
      this.flash('Prénom, nom et email sont obligatoires', true);
      return;
    }
    if (!this.userForm.department) {
      this.flash('Choisissez une direction', true);
      return;
    }
    if (!this.editingUserId && !this.userForm.password) {
      this.flash('Le mot de passe est obligatoire pour un nouvel utilisateur', true);
      return;
    }

    const request = this.editingUserId
      ? this.portal.updateUser(this.editingUserId, this.userForm)
      : this.portal.createUser(this.userForm);
    request.subscribe({
      next: () => {
        this.flash('Utilisateur et accès enregistrés');
        this.userForm = this.emptyUser();
        this.editingUserId = null;
        this.reload();
      },
      error: (err) => this.flash(this.apiError(err, 'Enregistrement impossible'), true)
    });
  }

  private openGeneratedFile(response: { body: Blob | null; headers: { get(name: string): string | null } }, fallback: string) {
    const blob = response.body;
    if (!blob) {
      this.flash('Fichier vide', true);
      return;
    }
    const filename = this.filenameFrom(response.headers.get('content-disposition'), fallback);
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    if (filename.endsWith('.html')) {
      window.open(url, '_blank', 'noopener');
    } else {
      link.click();
    }
    setTimeout(() => URL.revokeObjectURL(url), 2000);
    this.flash(`Document généré : ${filename}`);
  }

  private filenameFrom(header: string | null, fallback: string) {
    const match = header?.match(/filename="?([^"]+)"?/i);
    return match?.[1] || fallback;
  }

  private flash(text: string, error = false) {
    this.message.set(text);
    this.messageError.set(error);
  }

  private apiError(err: { status?: number; error?: { message?: string; error?: string } }, fallback: string) {
    if (err?.status === 0) {
      return 'API injoignable. Vérifiez que le backend est démarré.';
    }
    if (err?.status === 401) {
      return 'Session expirée. Reconnectez-vous.';
    }
    return err?.error?.message || err?.error?.error || fallback;
  }

  private toOrder(value: unknown) {
    const order = Number(value);
    return Number.isFinite(order) ? order : 0;
  }

  private normalizeColor(value: string) {
    const color = (value || '#C81E1E').trim();
    return /^#?[0-9a-fA-F]{6}$/.test(color) ? (color.startsWith('#') ? color : `#${color}`) : '#C81E1E';
  }

  private emptyApp(): ApplicationPayload {
    return {
      name: '',
      description: '',
      url: 'https://',
      icon: 'monitor',
      logoUrl: '',
      categoryId: this.categories()[0]?.id ?? 1,
      ownerDepartment: this.departments()[0]?.name ?? 'DSI',
      status: 'ACTIVE',
      featured: false,
      longDescription: '',
      modop: '',
      technicalSheetContent: '',
      userGuideContent: '',
      documentationUrl: '',
      documentationStoredFile: '',
      documentationFileName: '',
      documentationContentType: '',
      technicalSheetUrl: '',
      technicalSheetStoredFile: '',
      technicalSheetFileName: '',
      technicalSheetContentType: '',
      userGuideUrl: '',
      userGuideStoredFile: '',
      userGuideFileName: '',
      userGuideContentType: '',
      supportUrl: 'https://helpdesk.auchan.sn',
      supportContact: 'Service Desk DSI — helpdesk@auchan.sn',
      version: '2026.1',
      audience: ''
    };
  }

  private applyPendingAppEdit() {
    if (!this.pendingEditAppId) {
      return;
    }
    const app = this.apps().find((item) => item.id === this.pendingEditAppId);
    if (!app) {
      return;
    }
    this.pendingEditAppId = null;
    this.editApp(app);
    void this.router.navigate([], { relativeTo: this.route, queryParams: {}, replaceUrl: true });
  }

  private moveAppTo(from: number, to: number) {
    const list = [...this.apps()];
    if (from === to || from < 0 || to < 0 || from >= list.length || to >= list.length) {
      return;
    }
    const [moved] = list.splice(from, 1);
    list.splice(to, 0, moved);
    this.apps.set(list);
    this.persistAppOrder();
  }

  private persistAppOrder() {
    const seq = ++this.appOrderSeq;
    const ids = this.apps().map((app) => app.id);
    this.portal.reorderApplications(ids).subscribe({
      next: (apps) => {
        if (seq !== this.appOrderSeq) {
          return;
        }
        this.apps.set(apps.filter((app) => !!app.category));
        this.flash('Ordre des applications enregistré');
      },
      error: (err) => {
        if (seq !== this.appOrderSeq) {
          return;
        }
        this.flash(this.apiError(err, 'Impossible d’enregistrer l’ordre'), true);
        this.reload();
      }
    });
  }

  private moveCategoryTo(from: number, to: number) {
    const list = [...this.categories()];
    if (from === to || from < 0 || to < 0 || from >= list.length || to >= list.length) {
      return;
    }
    const [moved] = list.splice(from, 1);
    list.splice(to, 0, moved);
    this.categories.set(list);
    this.persistCategoryOrder();
  }

  private persistCategoryOrder() {
    const seq = ++this.categoryOrderSeq;
    const ids = this.categories().map((category) => category.id);
    this.portal.reorderCategories(ids).subscribe({
      next: (categories) => {
        if (seq !== this.categoryOrderSeq) {
          return;
        }
        this.categories.set(categories);
        this.flash('Ordre des catégories enregistré');
      },
      error: (err) => {
        if (seq !== this.categoryOrderSeq) {
          return;
        }
        this.flash(this.apiError(err, 'Impossible d’enregistrer l’ordre'), true);
        this.reload();
      }
    });
  }

  private emptyCategory(): CategoryPayload {
    return {
      name: '',
      slug: '',
      icon: 'folder',
      color: '#C81E1E'
    };
  }

  private emptyDepartment(): DepartmentPayload {
    return {
      name: '',
      code: '',
      description: '',
      sortOrder: this.departments().length + 1
    };
  }

  private static blankUser(): UserPayload {
    return {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      role: 'USER',
      department: '',
      active: true,
      managerId: null,
      restrictedAccess: false,
      allowedApplicationIds: [],
      applicationGrants: []
    };
  }

  private emptyUser(): UserPayload {
    return {
      ...Admin.blankUser(),
      department: this.auth?.user()?.department ?? this.departments()[0]?.name ?? ''
    };
  }

  private defaultGrant(applicationId: number, allDocuments = false): AppAccessGrant {
    return {
      applicationId,
      canViewDocumentation: true,
      canViewTechnicalSheet: allDocuments,
      canViewUserGuide: true
    };
  }

  private syncAllowedIds() {
    this.userForm.allowedApplicationIds = this.userForm.applicationGrants.map((grant) => grant.applicationId);
  }
}
