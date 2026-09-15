import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AppIcon } from '../../shared/app-icon';
import { AppMark } from '../../shared/app-mark';
import { BusinessApp, Category } from '../../core/models';
import { PortalService } from '../../core/portal.service';

@Component({
  selector: 'app-portal',
  imports: [FormsModule, AppIcon, AppMark, RouterLink],
  templateUrl: './portal.html',
  styleUrl: './portal.scss'
})
export class Portal implements OnInit {
  readonly query = signal('');
  selectedCategory = signal<number | null>(null);
  onlyFavorites = signal(false);
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
  }

  selectCategory(id: number | null) {
    this.selectedCategory.set(this.selectedCategory() === id ? null : id);
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
