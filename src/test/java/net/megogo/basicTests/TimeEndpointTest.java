package net.megogo.basicTests;

import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;

public class TimeEndpointTest {

    @Test
    public void serverReturnsCurrentTime() {

        long serverTime = RestAssured.get("https://epg.megogo.net/time")
                .jsonPath()
                .getLong("data.timestamp");

        long currentTime = Instant.now().getEpochSecond();
        Assert.assertTrue(Math.abs(currentTime - serverTime) <= 5, "Server time is incorrect");
    }

}
