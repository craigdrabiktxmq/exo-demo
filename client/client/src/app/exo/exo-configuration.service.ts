import { Injectable } from '@angular/core';
import { ExoConfig } from './exo-config';
import { HttpClient } from '@angular/common/http';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import "rxjs/add/observable/from";
import { Observer } from 'rxjs/Observer';
import { ReplaySubject } from 'rxjs/ReplaySubject';

/**
 * This class is responsible for loading and managing configuration 
 * parameters for the framework.  Generally applications won't have 
 * to use this provider directly, but they can retrieve Exo's 
 * configuration using the config() getter.
 */
@Injectable()
export class ExoConfigurationService {

  private _config:ExoConfig;
  
  /**
   * Used to retrieve the current configuration object.  This method 
   * returns a shallow copy of the configuration object to protect 
   * against accidental modification of properties.  Code should not 
   * modify the configuration object, and it should be considered 
   * read-only.  Modifying values during execution _may_ cause the 
   * application's behavior to change on the fly, but is not supported.
   * 
   * @returns ExoConfig The current configuration for the framework.
   */
  public get config():ExoConfig {
      return Object.assign({}, this._config);
  }
  /**
   * Constructor.
   * @param  {HttpClient} privatehttpClient
   */
  constructor(private httpClient:HttpClient) { }
  
  /**
   * This method is called by the platform during initialization.  
   * It attempts to load Exo's configuration from the URL identified 
   * bythe "loadConfigFrom" property of the config object.  Any values 
   * defined on the configuration passed to init (e.g. defined in your 
   * app's modeule and passed into Exo via forRoot()) will overwrite 
   * any values loaded from the JSON file.
   * 
   * It is possible to completely configure Exo in code, and not use a
   * JSON config file.  This is not recommended, as you would have to 
   * recompile and redeploy your application every time you want to 
   * change for example the default node list.
   * 
   * By default, if you don't change anything in exo.module, Exo will 
   * try to load its configuration from /assets/exo-config.json.  It's 
   * perfectly acceptable (and probably best) to just place a JSON file 
   * at that location and not have to pass anything in via forRoot().
   * 
   * @param  {ExoConfig} config Initial settings object.  
   * @returns Subject
   */
  public init(config:ExoConfig):Subject<boolean> {
    let configReady:Subject<boolean> = new Subject();
    if (config) {
      //Something has been passed in from the application.  Values defined on the
      //instance we were given should override those loaded from the JSON file.
      this._config = config;
      if (config.loadConfigFrom) {
        //Fetch the config JSON file from the location defined in the 
        //passed-in config instance.
        this.httpClient.get(config.loadConfigFrom).subscribe(
          result => {
            //Use the loaded config, and override those values 
            //with any that were passed into the init method
            let newConfig:ExoConfig = result as ExoConfig;
            for (let property in this._config) {
              //Yes, I mean != and not !==
              if (this._config.hasOwnProperty(property) && this._config[property] != undefined) {
                newConfig[property] = this._config[property];
              }
            }
            this._config = newConfig;

            //Notify calling code that we've finished
            configReady.next(true);
          },
          error => {
            //We failed to load a configuration file from the URL in the 
            //config we were passed.  Try again from the default location.
            this.loadConfigFromAssets(configReady);
          }
        );
      } else {
        //A config was passed in, but no value for loadConfigFrom was set.
        //Assume that the application wants to hard-code its configuration.
        configReady.next(true);
      }
    } else {
      //Atempt to load the config file from the assets folder
      this.loadConfigFromAssets(configReady);
    }

    return configReady;
  }

  /**
   * This method attempts to load the configuration file from
   * the default location (/assets/exo-config.json).
   * @param configReady 
   */
  private loadConfigFromAssets(configReady:Subject<boolean>):void {
    this.httpClient.get('/assets/exo-config.json').subscribe(
      result => {
        //success, use the JSON values as our configuration.
        this._config = result as ExoConfig;
        configReady.next(true);
      },
      error => {
        //Failure - there's no config file at the default location.  Assume 
        //that <app root>/api is the API root so execution can continue.
        this._config = new ExoConfig();
        this._config.defaultNodes = [window.location.origin];
        this._config.apiPath = '/api';
        configReady.next(true);
      }
    )
  }
}
