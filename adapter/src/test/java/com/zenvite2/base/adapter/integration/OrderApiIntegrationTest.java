package com.zenvite2.base.adapter.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

// TODO: Remove this sample code when implementing the actual service
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderApiIntegrationTest {

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @Test
  void should_create_order_with_draft_status() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
                {"customerName": "Nguyen Van A"}
                """)
        .when()
        .post("/api/v1/orders")
        .then()
        .statusCode(201)
        .body("success", is(true))
        .body("data.customerName", equalTo("Nguyen Van A"))
        .body("data.status", equalTo("DRAFT"))
        .body("data.items", empty());
  }

  @Test
  void should_add_item_to_order() {
    String orderId = createDraftOrder();

    Number totalAmount =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"productName": "Motorcycle Insurance", "quantity": 2, "unitPrice": 50000}
                """)
            .when()
            .post("/api/v1/orders/{id}/items", orderId)
            .then()
            .statusCode(201)
            .body("data.items.size()", equalTo(1))
            .body("data.items[0].productName", equalTo("Motorcycle Insurance"))
            .extract()
            .path("data.totalAmount");

    assertThat(new BigDecimal(totalAmount.toString())).isEqualByComparingTo("100000");
  }

  @Test
  void should_update_item_quantity() {
    String orderId = createDraftOrder();
    String itemId = addItem(orderId, "Product A", 1, 30000);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                {"quantity": 5}
                """)
        .when()
        .put("/api/v1/orders/{id}/items/{itemId}", orderId, itemId)
        .then()
        .statusCode(200)
        .body("data.items[0].quantity", equalTo(5));

    // BigDecimal compared separately — JSON serializes int/float inconsistently
    Number updateTotal =
        given()
            .when()
            .get("/api/v1/orders/{id}", orderId)
            .then()
            .extract()
            .path("data.totalAmount");
    assertThat(new BigDecimal(updateTotal.toString())).isEqualByComparingTo("150000");
  }

  @Test
  void should_remove_item_from_order() {
    String orderId = createDraftOrder();
    String itemId = addItem(orderId, "To Remove", 1, 10000);

    given()
        .when()
        .delete("/api/v1/orders/{id}/items/{itemId}", orderId, itemId)
        .then()
        .statusCode(200)
        .body("data.items", empty());
  }

  @Test
  void should_confirm_order_with_items() {
    String orderId = createDraftOrder();
    addItem(orderId, "Product A", 1, 10000);

    given()
        .when()
        .put("/api/v1/orders/{id}/confirm", orderId)
        .then()
        .statusCode(200)
        .body("data.status", equalTo("CONFIRMED"));
  }

  @Test
  void should_return_422_when_confirm_empty_order() {
    String orderId = createDraftOrder();

    given()
        .when()
        .put("/api/v1/orders/{id}/confirm", orderId)
        .then()
        .statusCode(422)
        .body("success", is(false))
        .body("error.type", equalTo("BUSINESS_RULE_VIOLATION"));
  }

  @Test
  void should_cancel_order() {
    String orderId = createDraftOrder();

    given()
        .when()
        .put("/api/v1/orders/{id}/cancel", orderId)
        .then()
        .statusCode(200)
        .body("data.status", equalTo("CANCELLED"));
  }

  @Test
  void should_return_422_when_add_item_to_confirmed_order() {
    String orderId = createDraftOrder();
    addItem(orderId, "A", 1, 10000);
    confirmOrder(orderId);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                {"productName": "B", "quantity": 1, "unitPrice": 5000}
                """)
        .when()
        .post("/api/v1/orders/{id}/items", orderId)
        .then()
        .statusCode(422)
        .body("error.type", equalTo("BUSINESS_RULE_VIOLATION"));
  }

  @Test
  void should_get_order_by_id() {
    String orderId = createDraftOrder();

    given()
        .when()
        .get("/api/v1/orders/{id}", orderId)
        .then()
        .statusCode(200)
        .body("data.id", equalTo(orderId));
  }

  @Test
  void should_search_orders() {
    createDraftOrder();

    given()
        .queryParam("customerName", "Nguyen")
        .when()
        .get("/api/v1/orders")
        .then()
        .statusCode(200)
        .body("data.content.size()", greaterThanOrEqualTo(1));
  }

  // === Helpers ===

  private String createDraftOrder() {
    return given()
        .contentType(ContentType.JSON)
        .body(
            """
                {"customerName": "Nguyen Van A"}
                """)
        .when()
        .post("/api/v1/orders")
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");
  }

  private String addItem(String orderId, String productName, int quantity, int unitPrice) {
    return given()
        .contentType(ContentType.JSON)
        .body(
            String.format(
                """
                {"productName": "%s", "quantity": %d, "unitPrice": %d}
                """,
                productName, quantity, unitPrice))
        .when()
        .post("/api/v1/orders/{id}/items", orderId)
        .then()
        .statusCode(201)
        .extract()
        .path("data.items[0].id");
  }

  private void confirmOrder(String orderId) {
    given().when().put("/api/v1/orders/{id}/confirm", orderId).then().statusCode(200);
  }
}
