
/**
 * This class defines configuration parameters for the Exo framework.
 * Exo will attempt to load its configuration from /assets/exp-config.json
 * if a configuration file loaction isn't specified in loadConfiguration.
 * 
 * Any settings in code, e.g. when you instantiate an ExoConfig and pass
 * it to to the module's forRoot() method will take priority over what's
 * defined in exo-config.json.
 * 
 * If Exo can't load a config file, it will attempt to continue as best it can.
 * Eventually, it will explode :)
 * 
 * exo-config.json is basically a JSON representation of this class.
 */
export class ExoConfig extends Object {
    /**
     * Exo will try to load its configuration via HTTP from this URL.
     * If a URL is not provided, Exo will assume that you want to 
     * configure the framework in code, using the configuration instance
     * defined in exo.module.ts.
     */
    public loadConfigFrom:string;

    /**
     * Lists the default set of nodes that are known to exist.  
     * This does not have to be a complete list of all nodes in
     * the Hashgraph network.  Exo will try to get an updated list
     * from one of these nodes during initialization.  
     */
    public defaultNodes: Array<string>;

    /**
     * Base path to the api.  For example, if a node exposes REST
     * endpoints at http://node.company.com:54321/apis/myapi/v1, set this
     * value to '/apis/myapi/v1;
     */
    public apiPath: string;

    /**
     * Path to the service that exposes a list of active node endpoints.
     * When Exo initializes, it will request an updated list of active 
     * endpoints from a URL constructed like:
     * <defaultNodes[n]><apiPath><endpointServicePath>
     * 
     * If the defaultNode entry is "http://node.company.com:54321" and 
     * the apiPath is "apis/myapi/v1" and the endpointsServicePath is 
     * "/endpoints", then Exo will make a request to 
     * "http://node.company.com/54321/apis/myapi/v1/endpoints"
     */
    public endpointsServicePath: string = '/endpoints';
}