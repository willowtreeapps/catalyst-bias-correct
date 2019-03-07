package org.catalyst.biascorrect.controllers.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.catalyst.biascorrect.PlatformConstants;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

abstract class BaseController extends Controller {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected WSClient _wsClient;

    BaseController(WSClient wsClient) {
        super();
        _wsClient = wsClient;
    }

    protected JsonNode extractResponseJson(CompletionStage<JsonNode> jsonPromise) {
        JsonNode deJson = null;
        try {
            deJson = jsonPromise.toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return deJson;
    }

    protected JsonNode toJson(Object jsonObject) {
        return OBJECT_MAPPER.convertValue(jsonObject, ObjectNode.class);
    }

    protected Result jsonOk(String key, String value) {
        ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
        objectNode.put(key, value);
        return ok(objectNode);
    }

    protected Result jsonError(String errorMessage) {
        ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
        objectNode.put(PlatformConstants.JSON_ERROR, errorMessage);
        return badRequest(objectNode);
    }

    protected String getValueFromJson(JsonNode parent, String key) {
        String value = null;
        JsonNode valueNode = parent.path(key);
        if (valueNode != null && !valueNode.isNull() && !valueNode.isMissingNode()) {
            value = valueNode.textValue();
        }
        return value;
    }
}
