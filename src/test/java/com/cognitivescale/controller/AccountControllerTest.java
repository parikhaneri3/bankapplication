package com.cognitivescale.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cognitivescale.util.AccountUtils;
import com.cognitivescale.util.TestConstants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {
	private static final Logger LOG = LoggerFactory.getLogger(AccountControllerTest.class);

	@Before
	public void setUpConfig() {
		RestAssured.baseURI = "http://localhost:5554";
		RestAssured.defaultParser = Parser.JSON;
	}

	@Test
	public void accountsAPIs() {

		Map<String, String> accountMap = AccountUtils.buildAccountModel();

		// creates account
		createAccount(accountMap);

		// login with username and password
		Integer accountNumber = loginWithUsernameAndPassword(accountMap);

		// get balance info by account number
		getBalanceInfo(accountNumber);

		// caculate interest by account number
		calculateInterest(accountNumber);
	}

	public static void createAccount(Map<String, String> accountMap) {
		Response response = RestAssured.given().contentType("application/json").accept("application/json")
				.body(new Gson().toJson(accountMap)).when().post("/account/create_account");
		String accountMessage = AccountUtils.buildResponse(response);
		assertThat(accountMessage).isEqualTo(TestConstants.ACCOUNT_CREATED);
		LOG.info("/account/create_account response ::: " + response.asString());
	}

	public static Integer loginWithUsernameAndPassword(Map<String, String> accountMap) {
		Response loggedInUser = RestAssured.given().contentType("application/json").accept("application/json")
				.body(new Gson().toJson(accountMap)).when().post("/account/login");
		JsonObject loginObject = (JsonObject) new JsonParser().parse(loggedInUser.asString());
		String loginMessage = loginObject.get("message").getAsString();
		String loginStatus = loginObject.get("status").getAsString();
		JsonObject loginDataJson = loginObject.get("data").getAsJsonObject();
		Integer accountNumber = loginDataJson.get("accountNumber").getAsInt();
		assertEquals(true, loggedInUser.getStatusCode() == 200);
		assertThat(loginStatus).isEqualTo(TestConstants.STATUS_SUCCESS);
		assertThat(loginMessage).isEqualTo(TestConstants.ACCOUNT_IS_REGISTERED);
		LOG.info("/account/login response ::: " + loggedInUser.asString());
		return accountNumber;
	}

	private void getBalanceInfo(Integer accountNumber) {
		Response response = RestAssured.given().param("accountNumber", accountNumber).when().get("/account/balance");
		String accountMessage = AccountUtils.buildResponse(response);
		assertThat(accountMessage).isEqualTo(TestConstants.ACCOUNT_BALANCE);
		LOG.info("/account/balance response ::: " + response.asString());
	}

	private void calculateInterest(Integer accountNumber) {
		Response response = RestAssured.given().param("accountNumber", accountNumber).param("date", "2050-08-30").when()
				.get("/account/calculate_interest");
		String accountMessage = AccountUtils.buildResponse(response);
		assertThat(accountMessage).isEqualTo(TestConstants.ACCOUNT_CALCULATED_BALANCE);
		LOG.info("/account/calculate_interest response ::: " + response.asString());
	}
}
