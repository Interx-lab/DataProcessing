package com.amazonaws.lambda.interx;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class LambdaFunctionHandler implements RequestHandler<KinesisEvent, Integer> {

    private final AmazonDynamoDBClientBuilder dynamoBuilder =
            AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.AP_NORTHEAST_2)
                    .withCredentials(new ProfileCredentialsProvider("myProfile"));

    private AmazonDynamoDB createDynamoClient() {
        return dynamoBuilder.build();
    }

    private final AmazonS3ClientBuilder s3Builder =
            AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.AP_NORTHEAST_2)
                    .withCredentials(new ProfileCredentialsProvider("myProfile"));

    private AmazonS3 createS3Client() {
        return s3Builder.build();
    }

    private final String dynamoTableName = "myTable";

    @Override
    public Integer handleRequest(KinesisEvent event, Context context) {
        context.getLogger().log("Input: " + event);
        AmazonS3 s3bucket;
        AmazonDynamoDB dynamodb;
        try {
            s3bucket = createS3Client();
            dynamodb = createDynamoClient();
        } catch (AmazonServiceException e) {
            context.getLogger().log("Error " + e.getErrorMessage());
            return event.getRecords().size();
        }

        for (KinesisEventRecord record : event.getRecords()) {
            String data = new String(record.getKinesis().getData().array());
            SensorRequest request = Jackson.fromJsonString(data, SensorRequest.class);
            String timestamp = request.getTimestamp();
            String iotCode = request.getIotCode();
            String filename = iotCode + " " + timestamp;
            String content = request.getContent();
            String factoryId = request.getTimestamp();
            context.getLogger().log("Received with timestamp {" + timestamp
                    + "} from factor {" + factoryId
                    + "} with iot_code {" + iotCode + "}");

            PutObjectResult filePut = s3bucket.putObject(factoryId, filename, content);
            context.getLogger().log("File created " + filePut.toString());

            Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
            item.put("timestamp", new AttributeValue(timestamp));
            item.put("iot_code" , new AttributeValue(iotCode));
            item.put("factory_id", new AttributeValue(factoryId));
            item.put("log_name", new AttributeValue(filename));

            PutItemResult dbPut = dynamodb.putItem(dynamoTableName, item);
            context.getLogger().log("DB record added " + dbPut.toString());
        }

        return event.getRecords().size();
    }
}
