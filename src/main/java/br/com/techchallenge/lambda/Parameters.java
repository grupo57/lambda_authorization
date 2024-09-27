package br.com.techchallenge.lambda;

public class Parameters {
    private static final String AWS_REGION_VALUE_KEY = "aws_region";
    private static final String ISSUER_ALLOWED_VALUE_KEY = "issuer_allowed";


    public static String getAwsRegion() {
        return System.getenv(AWS_REGION_VALUE_KEY);
    }

    public static String getIssuerAllowed() {
        return System.getenv(ISSUER_ALLOWED_VALUE_KEY);
    }

}
