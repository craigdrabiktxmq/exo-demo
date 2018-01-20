import { Injectable } from '@angular/core';
import { ExoConfigurationService } from './exo-configuration.service';
import { HttpClient } from '@angular/common/http';
import { ExoConfig } from './exo-config';
import { Subject } from 'rxjs/Subject';

/**
 * DistributedEndpointService handles the management of available API URLs.
 * During framework initialization, we query an updated list of endpoints
 * from a known node in the Hashgraph.  That node will return a list of 
 * nodes that are currently running and exposing endpoints.
 * 
 * Services that request data from the hashgraph will inject this service
 * and use it to get a URL to one of the nodes.
 * 
 * TODO:  Make the endpoint selection mechanism pluggable.  The intention 
 * is to allow for a number of different selection schemes.  At first these 
 * schemes would be relatively simple:  fixed, round robin, and random.  The 
 * goal is to make the mechanism extensible so that more complex schemes can 
 * be implemented, such as schemes that select endpoints based on performance
 * metrics.
 * 
 * TODO:  Implement a mechanism for updating the node list periodically.
 */
@Injectable()
export class ExoDistributedEndpointService {

  /**
   * Holds the list of endpoints with available APIs
   */
  private endpoints:Array<string>;

  /**
   * Local copy of the framework configuration
   */
  private configuration:ExoConfig;

  /**
   * Constructor
   * @param  {HttpClient} privatehttpClient
   */
  constructor(private httpClient:HttpClient) { }

  /**
   * This method is called by the platform during initialization.  
   * It queries an updated list of API endpoints from a list of 
   * known network nodes.  The platform subscribes to the returned
   * observable to be notified that the list has been retrieved.
   * 
   * Applications should not directly call or subscribe to the 
   * result of the init call.  Instead, subscribe to platformReady 
   * on the ExoPlatform service.
   * 
   * @see ExoPlatform
   * 
   * @param  {ExoConfig} configuration Platform configuration
   * @returns Subject
   */
  public init(configuration:ExoConfig):Subject<boolean> {
    this.configuration = configuration;

    let endpointsReady:Subject<boolean> = new Subject<boolean>();
    this.endpoints = configuration.defaultNodes.map(endpoint => endpoint);
    
    //Fetch an updated list of nodes from one of the known-good nodes in the config.
    //
    //TODO:  Iterate through the default nodes looking for 
    //one that's up.  If one fails, move on to the next.
    this.httpClient
      .get(this.endpoints[0] + configuration.apiPath + configuration.endpointsServicePath)
      .subscribe( 
        result => { 
          this.endpoints = result as Array<string>; 
          endpointsReady.next(true);
        },
        error => {
          endpointsReady.next(true);
        }
      );
    return endpointsReady;
  }

  //TODO:  Modularize URL strategy
  private currentEndpointPointer:number = 0;

  /**
   * Services implemented in application code should use this method to get a base
   * URL for an API exposed by a running Hashgraph node.
   * 
   * Currently they will receive a node from the list via round-robin selection.
   * In the future, node selection will be modularized and support different 
   * selection mechanisms.  
   */
  public getBaseUrl():string {
    //For testing purposes, let's just implement a round-robin approach
    this.currentEndpointPointer = (this.currentEndpointPointer + 1) % this.endpoints.length;
    return this.endpoints[this.currentEndpointPointer] + this.configuration.apiPath;
  }
}
