import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;

import java.time.Instant;
import java.time.temporal.ChronoField;

import static software.amazon.awssdk.regions.Region.EU_WEST_2;

public class Testy {
    public static void main(String[] args) {
        SsmClient ssmClient = SsmClient.builder().region(EU_WEST_2).build();

        Instant before = Instant.now();

        GetParametersByPathRequest request = GetParametersByPathRequest.builder().path("/dev/core/credentialIssuers").recursive(true).build();
        GetParametersByPathResponse response = ssmClient.getParametersByPath(request);

        Instant after = Instant.now();

        System.out.printf("Parameters retrieved: %d%n", response.parameters().size());


        long beforeLong = (before.getEpochSecond() * 1000) + before.getLong(ChronoField.MILLI_OF_SECOND);
        long afterLong = (after.getEpochSecond() * 1000) + after.getLong(ChronoField.MILLI_OF_SECOND);
        System.out.printf("Duration of call to param store (ms): %d%n", afterLong - beforeLong );
    }
}
