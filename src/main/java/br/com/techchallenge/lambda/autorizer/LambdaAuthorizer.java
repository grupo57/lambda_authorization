package br.com.techchallenge.lambda.autorizer;

import br.com.techchallenge.lambda.Parameters;
import br.com.techchallenge.lambda.autorizer.exception.AuthHeaderMalformedException;
import br.com.techchallenge.lambda.autorizer.exception.AuthHeaderMissingException;
import br.com.techchallenge.lambda.autorizer.model.AuthorizerResponse;
import br.com.techchallenge.lambda.autorizer.model.PolicyDocument;
import br.com.techchallenge.lambda.autorizer.model.Statement;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.services.lambda.runtime.events.IamPolicyResponse.ALLOW;
import static com.amazonaws.services.lambda.runtime.events.IamPolicyResponse.DENY;

public class LambdaAuthorizer implements RequestHandler<APIGatewayProxyRequestEvent, AuthorizerResponse> {
    private static final String MISSING_AUTH_HEADER_MESSAGE = "Missing authorization header";
    private static final String INVALID_AUTH_HEADER_MESSAGE = "Authorization header invalid";
    private static final String MESSAGE = "message";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public AuthorizerResponse handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        Map<String, String> headers = request.getHeaders();
        String authHeader = extractAuthHeader(headers);
        System.out.println("Received request header Authorization : " + authHeader);

        Map<String, String> ctx = new HashMap<>();

        APIGatewayProxyRequestEvent.ProxyRequestContext proxyContext = request.getRequestContext();
        String arn = String.format("arn:aws:execute-api:%s:%s:%s/%s/%s/%s",
                Parameters.getAwsRegion(),
                proxyContext.getAccountId(),
                proxyContext.getApiId(),
                proxyContext.getStage(),
                proxyContext.getHttpMethod(),
                "*");

        String effect = ALLOW;
        try {
            if (authHeader == null || authHeader.isEmpty()) {
                throw new AuthHeaderMissingException(MISSING_AUTH_HEADER_MESSAGE);
            }
            if (!authHeader.startsWith("Bearer ")) {
                throw new AuthHeaderMalformedException(INVALID_AUTH_HEADER_MESSAGE);
            }

            String bearerToken = authHeader.replace(BEARER_PREFIX, "");
            if (bearerToken.split("\\.").length != 3) {
                throw new AuthHeaderMalformedException(INVALID_AUTH_HEADER_MESSAGE);
            }

            DecodedJWT decodedJWT = JWT.decode(bearerToken);
            validateIssuer(decodedJWT);
            ctx.put(MESSAGE, "SUCCESS");
        } catch (Exception e) {
            System.err.println(e.getMessage() + e);
            effect = DENY;
            ctx.put(MESSAGE, e.getMessage());
        }

        Statement statement = Statement.builder()
                .resource(arn)
                .effect(effect)
                .build();

        PolicyDocument policyDocument = PolicyDocument.builder()
                .statements(Collections.singletonList(statement))
                .build();

        AuthorizerResponse response = AuthorizerResponse.builder()
                .principalId(proxyContext.getAccountId())
                .policyDocument(policyDocument)
                .context(ctx)
                .build();
        System.out.println(response);
        return response;
    }

    private static String extractAuthHeader(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }

        return headers.entrySet()
                .stream()
                .filter(e -> e.getKey().equalsIgnoreCase(AUTHORIZATION_HEADER))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private static void validateIssuer(DecodedJWT decodedJWT) {

        String issuerAllowed = Parameters.getIssuerAllowed();
        if (issuerAllowed == null || issuerAllowed.isEmpty())
            throw new JWTVerificationException("Default issuer not found");

        String issuer = decodedJWT.getIssuer();
        if (!issuerAllowed.equalsIgnoreCase(issuer)) {
            throw new JWTVerificationException("Provided issuer is wrong " + issuer);
        }
    }

}