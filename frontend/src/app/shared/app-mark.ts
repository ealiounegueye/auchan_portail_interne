import { Component, Input } from '@angular/core';
import { AppIcon } from './app-icon';

@Component({
  selector: 'app-mark',
  imports: [AppIcon],
  template: `
    @if (imageSrc()) {
      <img [src]="imageSrc()" [alt]="alt" />
    } @else {
      <app-icon [name]="icon || 'monitor'" />
    }
  `,
  styles: `
    :host {
      display: grid;
      place-items: center;
      width: 100%;
      height: 100%;
    }
    img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
      display: block;
    }
  `
})
export class AppMark {
  @Input() icon = 'monitor';
  @Input() logoUrl: string | null | undefined;
  @Input() alt = '';

  imageSrc() {
    const value = (this.logoUrl || this.icon || '').trim();
    if (!value) {
      return '';
    }
    if (/^(https?:\/\/|\/|data:image)/i.test(value) || /\.(png|jpe?g|gif|webp|svg)(\?.*)?$/i.test(value)) {
      return value;
    }
    return '';
  }
}
