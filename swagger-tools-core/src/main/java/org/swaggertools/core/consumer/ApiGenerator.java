//package org.swaggertools.core.consumer;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.Operation;
//import io.swagger.v3.oas.models.media.MediaType;
//import io.swagger.v3.oas.models.media.Schema;
//import io.swagger.v3.oas.models.parameters.Parameter;
//import io.swagger.v3.oas.models.parameters.RequestBody;
//import io.swagger.v3.oas.models.responses.ApiResponse;
//import io.swagger.v3.oas.models.responses.ApiResponses;
//import org.swaggertools.core.model.ApiDefinition;
//import org.swaggertools.core.model.HttpStatus;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Consumer;
//
//import static org.swaggertools.core.util.AssertUtils.notEmpty;
//import static org.swaggertools.core.util.AssertUtils.notNull;
//
//
//public abstract class ApiGenerator extends JavaGenerator implements Consumer<ApiDefinition> {
//    private static final String JSON = "application/json";
//    private static final String X_IGNORE = "x-ignore";
//
//    protected OperationInfo getOperationInfo(Operation operation) {
//        if (operation.getExtensions() != null && operation.getExtensions().get(X_IGNORE) != null) {
//            return null;
//        }
//
//        notNull(operation.getOperationId(), "operationId is not set");
//        notEmpty(operation.getTags(), "tag is not set");
//        OperationInfo info = new OperationInfo();
//        info.tag = operation.getTags().get(0);
//        if (operation.getParameters() != null) {
//            operation.getParameters().forEach(it -> info.parameters.add(getParameterInfo(it)));
//        }
//        ParameterInfo body = getRequestBodyInfo(operation.getRequestBody());
//        if (body != null) {
//            info.parameters.add(body);
//        }
//        addResponse(info, operation.getResponses());
//        return info;
//    }
//
//    private ParameterInfo getParameterInfo(Parameter parameter) {
//        ParameterInfo info = new ParameterInfo();
//        info.name = parameter.getName();
//        info.kind = "path".equals(parameter.getIn()) ? ParameterKind.PATH : ParameterKind.QUERY;
//        info.required = parameter.getRequired() == null ? false : parameter.getRequired();
//        info.schema = parameter.getSchema();
//        return info;
//    }
//
//    private ParameterInfo getRequestBodyInfo(RequestBody requestBody) {
//        //TODO: support other media types
//        if (requestBody != null && requestBody.getContent() != null) {
//            MediaType mediaType = requestBody.getContent().get(JSON);
//            if (mediaType != null && mediaType.getSchema() != null) {
//                ParameterInfo info = new ParameterInfo();
//                info.name = "requestBody";
//                info.kind = ParameterKind.BODY;
//                info.required = true;
//                info.schema = mediaType.getSchema();
//                return info;
//            }
//        }
//        return null;
//    }
//
//    private void addResponse(OperationInfo info, ApiResponses responses) {
//        for (Map.Entry<String, ApiResponse> e : responses.entrySet()) {
//            if (!"default".equals(e.getKey())) {
//                int statusCode = Integer.valueOf(e.getKey());
//                if (statusCode >= 200 && statusCode <= 299) {
//                    info.responseStatus = HttpStatus.valueOf(statusCode);
//                    if (e.getValue().getContent() != null) {
//                        //TODO: support other media types
//                        MediaType mediaType = e.getValue().getContent().get(JSON);
//                        if (mediaType != null) {
//                            info.responseSchema = mediaType.getSchema();
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//    }
//
//    protected static class OperationInfo {
//        public String tag;
//        public String path;
//        public String method;
//        public String name;
//        public final List<ParameterInfo> parameters = new LinkedList<>();
//        public Schema responseSchema;
//        public HttpStatus responseStatus;
//    }
//
//    protected static class ParameterInfo {
//        public String name;
//        public Schema schema;
//        public ParameterKind kind;
//        public boolean required;
//    }
//
//    protected static enum ParameterKind {
//        PATH, QUERY, BODY
//    }
//}
