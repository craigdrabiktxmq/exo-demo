import { ModuleWithProviders, NgModule, Optional } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExoConfig } from './exo-config';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import "rxjs/add/observable/of";
import { ExoConfigurationService } from './exo-configuration.service';
import { ExoPlatformService } from './exo-platform.service';
import { ExoDistributedEndpointService } from './exo-distributed-endpoint.service';

@NgModule({
  imports: [
    CommonModule,    
  ],
  providers: [
    ExoConfigurationService, 
    ExoPlatformService, 
    ExoDistributedEndpointService
  ]
})
/**
 * Enclosing module for the Exo framework
 */
export class ExoModule { 

  /**
   * Holds the configuration instance passed into the 
   * module via forRoot(), if there is one.
   */
  private static config:ExoConfig;

  /**
   * Called during bootstrapping by an application when the application 
   * wants to define some or all configuration properties in code.
   * @param  {ExoConfig} config A configuration object.  @see ExoConfig for more 
   *                            information on how the framework behaves with 
   *                            various combinationsof settings.
   * 
   * @returns ModuleWithProviders
   */
  public static forRoot(@Optional() config:ExoConfig): ModuleWithProviders {
    if (config) {
      ExoModule.config = config;
    } else {
      ExoModule.config = new ExoConfig();
      ExoModule.config.loadConfigFrom = "/assets/exo-config.json";
    }

    return {
      ngModule: ExoModule,
      providers: [
        
      ]
    };
  }

  /**
   * Constructor.  Causes the platform service to bootstrap the framework when instantiated.
   * Application developers should take care to only add Exo as a dependency at the root of
   * their application to avoid creating multiple copes of Exo services and incurring the
   * overhead of bootstrapping multiple times.  
   * @param platformService 
   */
  constructor(platformService:ExoPlatformService) {
    platformService.init(ExoModule.config);
  }
}
