import { JsonrpcRequest } from "../base";

/**
 * Wraps a JSON-RPC Request for a specific Edge-ID.
 * 
 * <pre>
 * {
 *   "jsonrpc": "2.0",
 *   "id": UUID,
 *   "method": "edgeRpc",
 *   "params": {
 *     "edgeId": string,
 *     "payload": JsonrpcRequest
 *   }
 * }
 * </pre>
 */
export class EdgeRpcRequest extends JsonrpcRequest {

    static METHOD: string = "edgeRpc";

    public constructor(
        public readonly edgeId: string,
        public readonly payload: JsonrpcRequest
    ) {
        super(EdgeRpcRequest.METHOD, { edgeId: edgeId, payload: payload });
    }

}