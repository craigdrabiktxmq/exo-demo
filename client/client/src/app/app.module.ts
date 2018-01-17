import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import { MatCardModule, MatFormFieldModule, MatSelectModule, MatInputModule, MatButtonModule, MatRadioModule } from '@angular/material';
import { FlexLayoutModule } from '@angular/flex-layout';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { ApiModule, DefaultService } from '../api/index';
import { FormsModule } from '@angular/forms';
import { ExoModule } from './exo/exo.module';
import { ExoConfig } from './exo/exo-config';

const appRoutes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: '**', redirectTo: '/home' }
];

/*
const exoConfig:ExoConfig = {
  loadConfigFrom: undefined,
  defaultNodes: ['http://localhost:52204', 'http://localhost:52207'],
  apiPath: '/HashgraphZoo/1.0.0',
  endpointsServicePath: '/endpoints'
} as ExoConfig;
*/
const exoConfig:ExoConfig = {
  loadConfigFrom: '/assets/exo-config.json',
  defaultNodes: ['http://localhost:52206'],
  apiPath: null,
  endpointsServicePath: null
} as ExoConfig;
@NgModule({
  declarations: [
    AppComponent,
    HomeComponent
  ],
  imports: [
    RouterModule.forRoot(appRoutes),
    BrowserModule,
    ApiModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatInputModule,
    MatCardModule,
    MatSelectModule,
    MatFormFieldModule,
    MatRadioModule,
    FormsModule,
    FlexLayoutModule,
    ExoModule.forRoot(exoConfig)
  ],
  providers: [DefaultService],
  bootstrap: [AppComponent]
})
export class AppModule { }
