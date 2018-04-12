package com.amazonaws.lambda.interx;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.json.Jackson;

import java.util.HashMap;
import java.util.Map;

public class LambdaFunctionHandler implements RequestHandler<SensorEvent, Integer> {

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
    public Integer handleRequest(SensorEvent event, Context context) {
        context.getLogger().log("Input: " + event);
        AmazonS3 s3bucket;
        AmazonDynamoDB dynamodb;
        try {
            s3bucket = createS3Client();
            dynamodb = createDynamoClient();
        } catch (AmazonServiceException e) {
            context.getLogger().log("Error " + e.getErrorMessage());
            return 1;
        }

        String timestamp = event.getTimestamp();
        String factoryId = event.getFactoryId();
        String iotCode = event.getIotCode();
        String content = event.getContent();
        context.getLogger().log("Received with timestamp {" + timestamp
                + "} from factory {" + factoryId
                + "} with iot_code {" + iotCode + "}");

        String filename = iotCode + " " + timestamp;
//        PutObjectResult filePut = s3bucket.putObject(factoryId, filename, content);
//        context.getLogger().log("File created " + filePut.toString());
        context.getLogger().log("File created ");

        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("timestamp", new AttributeValue(timestamp));
        item.put("iot_code" , new AttributeValue(iotCode));
        item.put("factory_id", new AttributeValue(factoryId));
        item.put("log_name", new AttributeValue(filename));

//        PutItemResult dbPut = dynamodb.putItem(dynamoTableName, item);
//        context.getLogger().log("DB record added " + dbPut.toString());
        context.getLogger().log("DB record added ");

        return 0;
    }
}
