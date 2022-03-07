import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.lambda.powertools.parameters.ParamManager;
import software.amazon.lambda.powertools.parameters.SSMProvider;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static software.amazon.awssdk.regions.Region.EU_WEST_2;

public class Testy implements RequestHandler<Map<String,String>, String> {
    public static void main(String[] args) {
        System.out.println("Testing basic client:");

        List<Long> basicDurations = new ArrayList<>();
        SsmClient ssmClient = SsmClient.builder().region(EU_WEST_2).httpClient(UrlConnectionHttpClient.create()).build();

        for(int i = 0; i < 100; i++) {
            Instant beforeBasic = Instant.now();

            GetParametersByPathResponse response = ssmClient.getParametersByPath(buildRequestWithNextToken(null));
            List<Parameter> fetchedParameters = new ArrayList<>(response.parameters());

            while (response.nextToken() != null){
                response = ssmClient.getParametersByPath(buildRequestWithNextToken(response.nextToken()));
                fetchedParameters.addAll(response.parameters());
            }

            Instant afterBasic = Instant.now();

            long beforeLong = (beforeBasic.getEpochSecond() * 1000) + beforeBasic.getLong(ChronoField.MILLI_OF_SECOND);
            long afterLong = (afterBasic.getEpochSecond() * 1000) + afterBasic.getLong(ChronoField.MILLI_OF_SECOND);
            basicDurations.add(afterLong - beforeLong);
        }
        System.out.printf("Initial request (ms): %d%n", basicDurations.get(0));
        Collections.sort(basicDurations);
        System.out.printf("Median request(ms): %d%n", basicDurations.get(basicDurations.size()/2) + basicDurations.get(basicDurations.size()/2 - 1)/2);

        System.out.println("\n\nTesting powertools client:");

        List<Long> powerDurations = new ArrayList<>();
        SSMProvider ssmProvider = ParamManager.getSsmProvider(
                SsmClient.builder()
                        .httpClient(UrlConnectionHttpClient.create())
                        .region(EU_WEST_2)
                        .build());

        for(int i = 0; i < 100; i++) {
            Instant beforePower = Instant.now();

            Map<String, String> powerParams =
                    ssmProvider
                            .recursive()
                            .getMultiple("/dev/core/credentialIssuers");


            Instant afterPower = Instant.now();

            long beforePowerLong = (beforePower.getEpochSecond() * 1000) + beforePower.getLong(ChronoField.MILLI_OF_SECOND);
            long afterPowerLong = (afterPower.getEpochSecond() * 1000) + afterPower.getLong(ChronoField.MILLI_OF_SECOND);
            powerDurations.add(afterPowerLong - beforePowerLong);
        }

        System.out.printf("Initial request (ms): %d%n", powerDurations.get(0));
        Collections.sort(powerDurations);
        System.out.printf("Median request(ms): %d%n", powerDurations.get(powerDurations.size()/2) + powerDurations.get(powerDurations.size()/2 - 1)/2);
    }

    private static GetParametersByPathRequest buildRequestWithNextToken(String nextToken) {
        return GetParametersByPathRequest.builder()
                .path("/dev/core/credentialIssuers")
                .recursive(true)
                .nextToken(nextToken)
                .build();
    }

    @Override
    public String handleRequest(Map<String, String> input, Context context) {
        main(null);
        return null;
    }
}
