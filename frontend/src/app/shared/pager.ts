import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pager',
  template: `
    @if (pages > 1) {
      <nav class="pager" [attr.aria-label]="label">
        <button type="button" [disabled]="safePage <= 1" (click)="goTo(safePage - 1)">Précédent</button>
        <span>Page {{ safePage }} / {{ pages }}</span>
        <button type="button" [disabled]="safePage >= pages" (click)="goTo(safePage + 1)">Suivant</button>
      </nav>
    }
  `,
  styles: `
    .pager {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      margin-top: 16px;
      flex-wrap: wrap;
    }
    button {
      border: 0;
      border-radius: 999px;
      padding: 8px 14px;
      font-weight: 700;
      background: #fff;
      box-shadow: var(--shadow);
    }
    button:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
    span {
      font-weight: 600;
      color: var(--muted);
    }
  `
})
export class Pager {
  @Input() page = 1;
  @Input() pages = 1;
  @Input() label = 'Pagination';
  @Output() go = new EventEmitter<number>();

  get safePage() {
    return Math.min(Math.max(1, this.page), Math.max(1, this.pages));
  }

  goTo(page: number) {
    const next = Math.min(Math.max(1, page), Math.max(1, this.pages));
    if (next !== this.safePage) {
      this.go.emit(next);
    }
  }
}
