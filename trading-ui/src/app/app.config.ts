import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';

import { routes } from './app.routes';
import { JwtInterceptor } from './core/interceptors/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),

    provideRouter(routes),

    // 🔥 REQUIRED FOR TOASTR
    provideAnimations(),

    // ✅ EXECUTION STYLE TOAST CONFIG
    provideToastr({
      positionClass: 'toast-top-center',
      timeOut: 2000,
      progressBar: true,
      preventDuplicates: true,
      newestOnTop: true,
      closeButton: false
    }),

    // 🔑 Enable DI-based HTTP interceptors
    provideHttpClient(withInterceptorsFromDi()),

    // 🔐 JWT Interceptor
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true
    }
  ]
};
