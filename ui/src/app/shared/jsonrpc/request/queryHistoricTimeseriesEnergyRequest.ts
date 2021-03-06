import { format } from 'date-fns';
import { ChannelAddress } from "../../type/channeladdress";
import { JsonrpcRequest } from "../base";
import { JsonRpcUtils } from "../jsonrpcutils";

/**
 * Represents a JSON-RPC Request to query Timeseries Energy data.
 * 
 * <pre>
 * {
 *   "jsonrpc": "2.0",
 *   "id": UUID,
 *   "method": "queryHistoricTimeseriesEnergy",
 *   "params": {
 *     "timezone": Number,
 *     "fromDate": YYYY-MM-DD,
 *     "toDate": YYYY-MM-DD,
 *     "channels": ChannelAddress[]
 *   }
 * }
 * </pre>
 */
export class QueryHistoricTimeseriesEnergyRequest extends JsonrpcRequest {


    static METHOD: string = "queryHistoricTimeseriesEnergy";

    public constructor(
        public readonly fromDate: Date,
        public readonly toDate: Date,
        public readonly channels: ChannelAddress[]
    ) {
        super(QueryHistoricTimeseriesEnergyRequest.METHOD, {
            timezone: new Date().getTimezoneOffset() * 60,
            fromDate: format(fromDate, 'yyyy-MM-dd'),
            toDate: format(toDate, 'yyyy-MM-dd'),
            channels: JsonRpcUtils.channelsToStringArray(channels)
        });
    }

}

