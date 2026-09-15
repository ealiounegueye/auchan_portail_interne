import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-icon',
  template: `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      @switch (name) {
        @case ('users') {
          <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
        }
        @case ('wallet') {
          <path d="M21 12V7H3v12a2 2 0 0 0 2 2h14" />
          <path d="M21 12a3 3 0 0 0-3 3h6a3 3 0 0 0-3-3Z" />
          <path d="M3 7l9-4 9 4" />
        }
        @case ('truck') {
          <path d="M14 18V6H2v12h12Z" />
          <path d="M14 9h5l3 4v5h-8" />
          <circle cx="6.5" cy="18.5" r="1.5" />
          <circle cx="18.5" cy="18.5" r="1.5" />
        }
        @case ('store') {
          <path d="M3 9 5 3h14l2 6" />
          <path d="M4 9h16v12H4z" />
          <path d="M9 21v-8h6v8" />
        }
        @case ('megaphone') {
          <path d="M3 11v2a4 4 0 0 0 4 4h1" />
          <path d="M11 6l10-3v18l-10-3V6Z" />
          <path d="M11 8v8" />
        }
        @case ('monitor') {
          <rect x="2" y="3" width="20" height="14" rx="2" />
          <path d="M8 21h8M12 17v4" />
        }
        @case ('badge') {
          <path d="M12 15a7 7 0 1 0-7-7 7 7 0 0 0 7 7Z" />
          <path d="M8.2 13.4 7 22l5-3 5 3-1.2-8.6" />
        }
        @case ('receipt') {
          <path d="M6 3h12v18l-2-1-2 1-2-1-2 1-2-1-2 1Z" />
          <path d="M9 8h6M9 12h6M9 16h3" />
        }
        @case ('graduation') {
          <path d="M22 10 12 5 2 10l10 5 10-5Z" />
          <path d="M6 12v5c3 2 9 2 12 0v-5" />
        }
        @case ('calculator') {
          <rect x="4" y="2" width="16" height="20" rx="2" />
          <path d="M8 6h8M8 12h.01M12 12h.01M16 12h.01M8 16h.01M12 16h.01M16 16h.01" />
        }
        @case ('chart') {
          <path d="M4 19V5M4 19h16" />
          <path d="M8 16v-6M12 16V8M16 16v-3" />
        }
        @case ('card') {
          <rect x="2" y="5" width="20" height="14" rx="2" />
          <path d="M2 10h20" />
        }
        @case ('warehouse') {
          <path d="M3 21V10l9-7 9 7v11" />
          <path d="M7 21v-8h10v8" />
        }
        @case ('boxes') {
          <path d="M21 8H3l9-5 9 5Z" />
          <path d="M12 3v18" />
          <path d="M3 8v11h18V8" />
        }
        @case ('cart') {
          <circle cx="8" cy="20" r="1.5" />
          <circle cx="18" cy="20" r="1.5" />
          <path d="M3 4h2l2.4 11.2a2 2 0 0 0 2 1.6h8.4a2 2 0 0 0 2-1.5L21 8H6" />
        }
        @case ('cash') {
          <rect x="2" y="6" width="20" height="12" rx="2" />
          <circle cx="12" cy="12" r="2.5" />
        }
        @case ('calendar') {
          <rect x="3" y="4" width="18" height="18" rx="2" />
          <path d="M16 2v4M8 2v4M3 10h18" />
        }
        @case ('package') {
          <path d="M21 8 12 3 3 8v8l9 5 9-5V8Z" />
          <path d="M12 13V3M3 8l9 5 9-5" />
        }
        @case ('car') {
          <path d="M3 13h18l-2-5H5l-2 5Z" />
          <path d="M5 13v5h14v-5" />
          <circle cx="7.5" cy="18.5" r="1.5" />
          <circle cx="16.5" cy="18.5" r="1.5" />
        }
        @case ('heart') {
          <path d="M20 8.5c0 5-8 11.5-8 11.5S4 13.5 4 8.5A4.5 4.5 0 0 1 12 7a4.5 4.5 0 0 1 8 1.5Z" />
        }
        @case ('globe') {
          <circle cx="12" cy="12" r="9" />
          <path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18" />
        }
        @case ('pie') {
          <path d="M12 3a9 9 0 1 0 9 9h-9V3Z" />
          <path d="M12 3a9 9 0 0 1 9 9" />
        }
        @case ('headset') {
          <path d="M4 13v4a2 2 0 0 0 2 2h1v-8H6a2 2 0 0 0-2 2Z" />
          <path d="M20 13v4a2 2 0 0 1-2 2h-1v-8h1a2 2 0 0 1 2 2Z" />
          <path d="M4 13a8 8 0 0 1 16 0" />
        }
        @case ('mail') {
          <rect x="3" y="5" width="18" height="14" rx="2" />
          <path d="m3 7 9 6 9-6" />
        }
        @case ('building') {
          <path d="M4 21V6l8-3 8 3v15" />
          <path d="M9 21v-6h6v6M9 9h.01M15 9h.01M9 13h.01M15 13h.01" />
        }
        @case ('book') {
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
          <path d="M6.5 2H20v15H6.5A2.5 2.5 0 0 0 4 19.5V4.5A2.5 2.5 0 0 1 6.5 2Z" />
        }
        @case ('search') {
          <circle cx="11" cy="11" r="7" />
          <path d="m20 20-3.5-3.5" />
        }
        @case ('star') {
          <path d="m12 3 2.7 5.5 6.1.9-4.4 4.3 1 6.1L12 17.3 6.6 19.8l1-6.1L3.2 9.4l6.1-.9L12 3Z" />
        }
        @default {
          <rect x="4" y="4" width="16" height="16" rx="4" />
        }
      }
    </svg>
  `,
  styles: [`
    :host { display: inline-flex; }
    svg { width: 1em; height: 1em; }
  `]
})
export class AppIcon {
  @Input({ required: true }) name = 'monitor';
}
