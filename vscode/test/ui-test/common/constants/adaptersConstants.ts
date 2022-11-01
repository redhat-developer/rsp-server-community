/**
 * @author Oleksii Korniienko <olkornii@redhat.com>
 */

 export class AdaptersConstants {

    // RSP extensions properties
    public static readonly RSP_EXTENSTION_NAME = 'Community Server Connectors';

    // RSP server provider constants
    public static readonly RSP_SERVER_PROVIDER_NAME = 'Community Server Connector';
    public static readonly RSP_SERVER_PROVIDER_CREATE_NEW_SERVER = 'Create New Server...';
    public static readonly RSP_SERVER_PROVIDER_START = 'Start / Connect to RSP Provider';
    public static readonly RSP_SERVER_PROVIDER_STOP = 'Stop RSP Provider';
    public static readonly RSP_SERVER_PROVIDER_DISCONNECT = 'Disconnect from RSP Provider';
    public static readonly RSP_SERVER_PROVIDER_TERMINATE = 'Terminate RSP Provider';

    // Server state constants
    public static readonly SERVER_CONNECTED = 'Connected';
    public static readonly SERVER_STARTED = 'Started';
    public static readonly SERVER_STARTING = 'Starting';
    public static readonly SERVER_STOPPING = 'Stopping';
    public static readonly SERVER_STOPPED = 'Stopped';
    public static readonly SERVER_UNKNOWN = 'Unknown';
}