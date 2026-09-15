import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const authed = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(authed).pipe(
    catchError((err) => {
      if (shouldLogout(req.url, err.status) && !req.url.includes('/api/auth/login')) {
        auth.logout();
      }
      return throwError(() => err);
    })
  );
};

function shouldLogout(url: string, status: number) {
  if (status === 401) {
    return true;
  }
  const path = url.split('?')[0];
  return status === 403 && path.endsWith('/api/applications');
}
