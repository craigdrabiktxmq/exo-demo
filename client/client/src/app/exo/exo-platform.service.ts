import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { ExoConfigurationService } from './exo-configuration.service';
import { ExoConfig } from './exo-config';
import { Subscription } from 'rxjs/Subscription';
import { ReplaySubject } from 'rxjs/ReplaySubject';
import { ExoDistributedEndpointService } from './exo-distributed-endpoint.service';

/**
 * ExoPlatformService is responsible for initializing the Exo platform and serving as a hub
 * for application code to access Exo resources.
 * 
 * The main purpose of this class from an application developer's perspective is the platformReady
 * member, which applications subscribe to in order to be notified that the platform is ready to 
 * use.  At that point, API calls using ExoDistributedService can execute against a running Hashgraph.
 */
@Injectable()
export class ExoPlatformService {

  private _platformReady:Subject<boolean>;
  /**
   * An observable that application code can subscribe to in order to be notified that
   * the Exo platform has been initialized.
   * @returns Subject An observable that notifies code when the platform has been initialized.
   */
  public get platformReady():Subject<boolean> {
    return this._platformReady;
  }

  /**
   * Class constructor.  Creates the platformReady Subject instance and receives dependencies through DI.
   * @param  {ExoConfigurationService} configService
   * @param  {ExoDistributedEndpointService} endpointService
   */
  constructor(  private configService:ExoConfigurationService, 
                private endpointService:ExoDistributedEndpointService) {
    this._platformReady = new ReplaySubject<boolean>(1);
   }

  /**
   * This method is called by the module when the module is loaded.  It handles the 
   * initialization procedure for the framework.  Application code should not call 
   * this method directly.  Application code should subscribe to platformReady to be 
   * notified when the framework has initialized.
   * 
   * @param  {ExoConfig} configuration  Configuration parameters that define how the 
   *                                    application should operate.  @see ExoConfig
   * @returns void
   */
  public init(configuration:ExoConfig):void {
    this.configService.init(configuration).subscribe(_ => 
      this.endpointService.init(this.configService.config).subscribe(_ => this._platformReady.next(true))
    );
  }
}
