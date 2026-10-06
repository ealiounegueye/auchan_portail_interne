import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AppIcon } from '../../shared/app-icon';
import { AppMark } from '../../shared/app-mark';
import { Pager } from '../../shared/pager';
import { BusinessApp, Category } from '../../core/models';
import { PortalService } from '../../core/portal.service';

@Component({
  selector: 'app-portal',
  imports: [FormsModule, AppIcon, AppMark, Pager, RouterLink],
  templateUrl: './portal.html',
  styleUrl: './portal.scss'
})
export class Portal implements OnInit {
  readonly query = signal('');
  selectedCategory = signal<number | null>(null);
  onlyFavorites = signal(false);
  readonly page = signal(1);
  readonly pageSize = 12;
  readonly categories = signal<Category[]>([]);
  readonly catalog = signal<BusinessApp[]>([]);
  readonly loading = signal(true);

  readonly visibleApps = computed(() => {
    const categoryId = this.selectedCategory();
    const q = this.query().trim().toLowerCase();
    let list = this.catalog();
    if (categoryId) {
      list = list.filter((app) => app.category.id === categoryId);
    }
    if (this.onlyFavorites()) {
      list = list.filter((app) => app.favorite);
    }
    if (q) {
      list = list.filter(
        (app) =>
          app.name.toLowerCase().includes(q) ||
          app.description.toLowerCase().includes(q) ||
          app.category.name.toLowerCase().includes(q) ||
          app.ownerDepartment.toLowerCase().includes(q) ||
          (app.longDescription ?? '').toLowerCase().includes(q)
      );
    }
    return list;
  });

  readonly pageCount = computed(() => Math.max(1, Math.ceil(this.visibleApps().length / this.pageSize)));

  readonly currentPage = computed(() => Math.min(this.page(), this.pageCount()));

  readonly pagedApps = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.visibleApps().slice(start, start + this.pageSize);
  });

  readonly rangeStart = computed(() => (this.visibleApps().length === 0 ? 0 : (this.currentPage() - 1) * this.pageSize + 1));

  readonly rangeEnd = computed(() => Math.min(this.currentPage() * this.pageSize, this.visibleApps().length));

  constructor(private readonly portal: PortalService) {}

  ngOnInit() {
    this.portal.categories().subscribe((categories) => this.categories.set(categories));
    this.reload();
  }

  reload() {
    this.loading.set(true);
    this.portal.applications().subscribe({
      next: (apps) => {
        this.catalog.set(apps);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onQueryChange(value: string) {
    this.query.set(value);
    this.page.set(1);
  }

  selectCategory(id: number | null) {
    this.onlyFavorites.set(false);
    this.selectedCategory.set(this.selectedCategory() === id ? null : id);
    this.page.set(1);
  }

  showFavorites() {
    this.page.set(1);
    if (this.onlyFavorites()) {
      this.onlyFavorites.set(false);
      return;
    }
    this.selectedCategory.set(null);
    this.onlyFavorites.set(true);
  }

  search() {
    // Filtrage immédiat à la saisie.
  }

  emptyMessage() {
    if (this.onlyFavorites()) {
      return 'Aucun favori pour le moment.';
    }
    if (this.selectedCategory()) {
      return 'Aucune application dans cette catégorie.';
    }
    if (this.query().trim()) {
      return 'Aucune application ne correspond à votre recherche.';
    }
    return 'Aucune application n’est encore autorisée pour votre compte. Contactez votre responsable.';
  }

  toggleFavorite(app: BusinessApp, event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.portal.toggleFavorite(app.id).subscribe(() => {
      this.catalog.update((apps) =>
        apps.map((item) => (item.id === app.id ? { ...item, favorite: !item.favorite } : item))
      );
    });
  }
}
