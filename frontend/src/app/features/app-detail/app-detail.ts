import { Component, OnInit, computed, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AppIcon } from '../../shared/app-icon';
import { AppMark } from '../../shared/app-mark';
import { BusinessApp, DocumentFormat, DocumentKind } from '../../core/models';
import { AuthService } from '../../core/auth.service';
import { PortalService } from '../../core/portal.service';

@Component({
  selector: 'app-detail',
  imports: [RouterLink, AppIcon, AppMark],
  templateUrl: './app-detail.html',
  styleUrl: './app-detail.scss'
})
export class AppDetail implements OnInit {
  readonly app = signal<BusinessApp | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly downloadMessage = signal('');

  readonly resources = computed(() => {
    const item = this.app();
    if (!item) {
      return [];
    }
    return [
      {
        kind: 'documentation' as DocumentKind,
        name: 'Mode opératoire (MODOP)',
        hint: item.documentationFileName
          ? `Fichier importé : ${item.documentationFileName}`
          : 'Document généré à partir de la fiche application',
        icon: 'book',
        externalUrl: item.documentationUrl,
        fileName: item.documentationFileName || null,
        allowed: item.canViewDocumentation !== false
      },
      {
        kind: 'fiche-technique' as DocumentKind,
        name: 'Fiche technique',
        hint: item.technicalSheetFileName
          ? `Fichier importé : ${item.technicalSheetFileName}`
          : 'Document généré à partir de la fiche application',
        icon: 'receipt',
        externalUrl: item.technicalSheetUrl,
        fileName: item.technicalSheetFileName || null,
        allowed: item.canViewTechnicalSheet !== false
      },
      {
        kind: 'guide' as DocumentKind,
        name: 'Guide utilisateur',
        hint: item.userGuideFileName
          ? `Fichier importé : ${item.userGuideFileName}`
          : 'Document généré à partir de la fiche application',
        icon: 'graduation',
        externalUrl: item.userGuideUrl,
        fileName: item.userGuideFileName || null,
        allowed: item.canViewUserGuide !== false
      }
    ].filter((resource) => resource.allowed);
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly portal: PortalService,
    readonly auth: AuthService
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.loading.set(false);
      this.error.set('Application introuvable');
      return;
    }
    this.portal.application(id).subscribe({
      next: (app) => {
        this.app.set(app);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'Impossible d’afficher cette application');
      }
    });
  }

  toggleFavorite() {
    const item = this.app();
    if (!item) {
      return;
    }
    this.portal.toggleFavorite(item.id).subscribe(() => {
      this.app.set({ ...item, favorite: !item.favorite });
    });
  }

  download(kind: DocumentKind, format: DocumentFormat) {
    const item = this.app();
    if (!item) {
      return;
    }
    this.downloadMessage.set('');
    this.portal.downloadDocument(item.id, kind, format).subscribe({
      next: (response) => {
        const blob = response.body;
        if (!blob) {
          this.downloadMessage.set('Fichier vide');
          return;
        }
        const header = response.headers.get('content-disposition');
        const match = header?.match(/filename="?([^"]+)"?/i);
        const filename = match?.[1] || `${kind}.${format}`;
        const url = URL.createObjectURL(blob);
        if (format === 'html') {
          window.open(url, '_blank', 'noopener');
        } else {
          const link = document.createElement('a');
          link.href = url;
          link.download = filename;
          link.click();
        }
        setTimeout(() => URL.revokeObjectURL(url), 2000);
        this.downloadMessage.set(`${filename} généré`);
      },
      error: () => this.downloadMessage.set('Accès refusé ou génération impossible')
    });
  }

  downloadFile(kind: DocumentKind) {
    const item = this.app();
    if (!item) {
      return;
    }
    this.downloadMessage.set('');
    this.portal.downloadUploadedDocument(item.id, kind).subscribe({
      next: (response) => {
        const blob = response.body;
        if (!blob) {
          this.downloadMessage.set('Fichier vide');
          return;
        }
        const header = response.headers.get('content-disposition');
        const utf = header?.match(/filename\*=UTF-8''([^;]+)/i);
        const plain = header?.match(/filename="?([^"]+)"?/i);
        const filename = utf ? decodeURIComponent(utf[1]) : plain?.[1] || 'document';
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        link.click();
        setTimeout(() => URL.revokeObjectURL(url), 2000);
        this.downloadMessage.set(`${filename} téléchargé`);
      },
      error: () => this.downloadMessage.set('Accès refusé ou fichier introuvable')
    });
  }
}
