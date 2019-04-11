import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: 'home/0', pathMatch: 'full' },
  { path: 'home/:id', loadChildren: './pages/home/home.module#HomePageModule' },
  { path: 'about', loadChildren: './pages/about/about.module#AboutPageModule' },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })
  ],
  exports: [RouterModule]
})

export class AppRoutingModule { }
