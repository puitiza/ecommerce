package com.ecommerce.authservice.model.templateAPI;

/**
 * Swagger api documentation requires these classes to display json models of the responses.
 * Each class's javadoc is what Swagger will translate them into on the ui.
 */
public class AuthResponseTemplate {

    public static final String OK = """
            {
              "data": {
                "id": 1,
                "name": "xiaomi",
                "price": 10,
                "manufacturingDate": null,
                "weight": 20
              },
              "success": true,
              "exception": null
            }
            """;
    public static final String NOT_FOUND = """
            {
                "data": null,
                "success": false,
                "exception": {
                    "status": 404,
                    "timestamp": "28-06-2022 10:37:06 PET",
                    "errorCode": "PC-0001",
                    "message": "Item with id {0} not found",
                    "stackTrace": [
                        "com.anthony.product.service.ProductService.lambda$getProduct$0(ProductService.java:24)",
                        "java.base/java.util.Optional.orElseThrow(Optional.java:403)",
                        "com.anthony.product.service.ProductService.getProduct(ProductService.java:22)",
                        "com.anthony.product.controller.ProductController.getProductById(ProductController.java:19)"
                    ],
                    "errors": null,
                    "debugMessage": null
                }
            }
            """;
}
