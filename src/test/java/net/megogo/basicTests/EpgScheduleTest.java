package net.megogo.basicTests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class EpgScheduleTest {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://epg.megogo.net";
    }

    @DataProvider(name = "videoIdsProvider")
    public Object[][] videoIdsProvider() {
        return new Object[][] {
                {"1639111"},
                {"1585681"},
                {"1639231"}
        };
    }

    @Test(dataProvider = "videoIdsProvider")
    public void programsSortedByStartTime(String videoId) {
        Response response = given()
                .queryParam("video_ids", videoId)
                .when()
                .get("/channel")
                .then()
                .statusCode(200)
                .extract()
                .response();
        List<Map<String, Object>> programs = response.jsonPath().getList("data.programs[0]");

        if (programs != null && !programs.isEmpty()) {
            for (int i = 0; i < programs.size() - 1; i++) {
                Long currentStartTimestamp = Long.valueOf(programs.get(i).get("start_timestamp").toString());
                Long nextStartTimestamp = Long.valueOf(programs.get(i + 1).get("start_timestamp").toString());

                Assert.assertTrue(currentStartTimestamp <= nextStartTimestamp,
                        "Programs are not sorted by start_timestamp for video_id: " + videoId);
            }
        } else {
            Assert.fail("No programs found for video_id: " + videoId);
        }
    }

    @Test(dataProvider = "videoIdsProvider")
    public void currentProgramExists(String videoId) {
        Response response = given()
                .queryParam("video_ids", videoId)
                .when()
                .get("/channel")
                .then()
                .statusCode(200)
                .extract()
                .response();
        List<Map<String, Object>> programs = response.jsonPath().getList("data.programs[0]");

        long currentTime = Instant.now().getEpochSecond();
        boolean currentProgramExists = programs.stream().anyMatch(program -> {
            long startTimestamp = ((Number) program.get("start_timestamp")).longValue();
            long endTimestamp = ((Number) program.get("end_timestamp")).longValue();
            return currentTime >= startTimestamp && currentTime <= endTimestamp;
        });
        Assert.assertTrue(currentProgramExists, "No program is active at the current time");
        }

    @Test(dataProvider = "videoIdsProvider")
    public void noPastOrFuturePrograms(String videoId) {
        Response response = given()
                .queryParam("video_ids", videoId)
                .when()
                .get("/channel")
                .then()
                .statusCode(200)
                .extract()
                .response();
        List<Map<String, Object>> programs = response.jsonPath().getList("data.programs[0]");

        long currentTime = Instant.now().getEpochSecond();
        long twentyFourHoursAhead = currentTime + 86400;
        for (Map<String, Object> program : programs) {
            long startTimestamp = ((Number) program.get("start_timestamp")).longValue();
            long endTimestamp = ((Number) program.get("end_timestamp")).longValue();
            Assert.assertTrue(endTimestamp >= currentTime, "There is a program from the past");
            Assert.assertTrue(startTimestamp <= twentyFourHoursAhead, "There is a program scheduled more than 24 hours ahead");
        }
    }
}
