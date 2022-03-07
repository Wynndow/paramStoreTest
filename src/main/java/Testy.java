import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.lambda.powertools.parameters.ParamManager;
import software.amazon.lambda.powertools.parameters.SSMProvider;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Map;

import static software.amazon.awssdk.regions.Region.EU_WEST_2;

public class Testy {
    public static void main(String[] args) {
        System.out.println("Testing basic client:");
        SsmClient ssmClient = SsmClient.builder().region(EU_WEST_2).httpClient(UrlConnectionHttpClient.create()).build();

        Instant beforeBasic = Instant.now();

        GetParametersByPathRequest request = GetParametersByPathRequest.builder().path("/dev/core/credentialIssuers").recursive(true).build();
        GetParametersByPathResponse response = ssmClient.getParametersByPath(request);

        Instant afterBasic = Instant.now();

        System.out.printf("Parameters retrieved: %d%n", response.parameters().size());


        long beforeLong = (beforeBasic.getEpochSecond() * 1000) + beforeBasic.getLong(ChronoField.MILLI_OF_SECOND);
        long afterLong = (afterBasic.getEpochSecond() * 1000) + afterBasic.getLong(ChronoField.MILLI_OF_SECOND);
        System.out.printf("Duration of call to param store (ms): %d%n%n%n%n", afterLong - beforeLong );

        System.out.println("Testing powertools client:");

        SSMProvider ssmProvider = ParamManager.getSsmProvider(
                SsmClient.builder()
                        .httpClient(UrlConnectionHttpClient.create())
                        .region(EU_WEST_2)
                        .build());

        Instant beforePower = Instant.now();

        Map<String, String> powerParams =
                ssmProvider
                        .recursive()
                        .getMultiple("/dev/core/credentialIssuers");


        Instant afterPower = Instant.now();

        System.out.printf("Parameters retrieved: %d%n", powerParams.size());
        
        long beforePowerLong = (beforePower.getEpochSecond() * 1000) + beforePower.getLong(ChronoField.MILLI_OF_SECOND);
        long afterPowerLong = (afterPower.getEpochSecond() * 1000) + afterPower.getLong(ChronoField.MILLI_OF_SECOND);
        System.out.printf("Duration of call to param store (ms): %d%n", afterPowerLong - beforePowerLong );
    }
}
