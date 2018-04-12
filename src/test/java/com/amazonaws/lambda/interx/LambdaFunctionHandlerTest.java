package com.amazonaws.lambda.interx;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class LambdaFunctionHandlerTest {

    private SensorEvent input;

    @Before
    public void createInput() throws IOException {
        input = TestUtils.parse("/sensor-event.json", SensorEvent.class);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    protected String getRandomString(int length) {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < length) {
            int index = (int) (rnd.nextFloat() * CHARS.length());
            stringBuilder.append(CHARS.charAt(index));
        }
        String string = stringBuilder.toString();
        return string;
    }

    protected String getRandomTimestamp() {
        long offset = Timestamp.valueOf("2018-03-01 00:00:00").getTime();
        long end = Timestamp.valueOf("2018-04-01 00:00:00").getTime();
        long diff = end - offset + 1;
        Timestamp rand = new Timestamp(offset + (long)(Math.random() * diff));
        return rand.toString();
    }

    @Test
    public void testLambdaFunctionHandler() {
        LambdaFunctionHandler handler = new LambdaFunctionHandler();
        Context ctx = createContext();

        System.out.println("Timestamp " + input.getTimestamp()
                + "; Factory " + input.getFactoryId()
                + "; iotCode " + input.getIotCode()
                + "; content " + input.getContent());
        Integer output = handler.handleRequest(input, ctx);
        Assert.assertEquals(0, output.intValue());
    }

    @Test
    public void testLambdaFunctionHandlerBig() throws InterruptedException {
        LambdaFunctionHandler handler = new LambdaFunctionHandler();
        Context ctx = createContext();

        for (int i = 0; i < 100; i++) {
            String timestamp = getRandomTimestamp();
            String factoryId = getRandomString(6);
            String iotCode = getRandomString(8);
            String content = getRandomString(10);
            SensorEvent event = new SensorEvent(timestamp, factoryId, iotCode, content);
            Integer output = handler.handleRequest(event, ctx);
            Assert.assertEquals(0, output.intValue());
            Thread.sleep(1000);
        }
    }
}
