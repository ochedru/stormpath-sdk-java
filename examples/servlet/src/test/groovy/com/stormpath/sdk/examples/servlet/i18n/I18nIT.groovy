package com.stormpath.sdk.examples.servlet.i18n

import org.testng.annotations.Test

import static com.jayway.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

/**
 * @since 1.0.RC9
 */
class I18nIT {

    @Test(enabled = false)
    void testLoginPageDefaultLocale() {
        given()
            .header("Accept", "text/html")
        .when()
            .get("http://localhost:8080/login")
        .then()
            .body(
            "html.head.title", equalTo("Login"),
            "**.findAll { it.@class == 'header' }.span.a", equalTo("Create Account"),
            "**.findAll { it.@type == 'submit' }", equalTo("Login"),
            "**.findAll { it.@class == 'to-login' }", equalTo("Forgot Password?")
        )
    }

    @Test(enabled = false)
    void testRegisterPageDefaultLocale() {
        given()
                .header("Accept", "text/html")
            .when()
                .get("http://localhost:8080/register")
            .then()
                .body(
                    "html.head.title", equalTo("Create Account"),
                    "**.findAll { it.@type == 'submit' }", equalTo("Create Account"),
                    "**.findAll { it.@class == 'to-login' }", equalTo("Back to Login")
                )
    }

    @Test(enabled = false)
    void testLoginPage() {
        given()
                .header("Accept-Language", "es")
                .header("Accept", "text/html")
            .when()
                .get("http://localhost:8080/login")
            .then()
                .body(
                    "html.head.title", equalTo("Iniciar Sesión"),
                    "**.findAll { it.@class == 'header' }.span.a", equalTo("Crear Cuenta"),
                    "**.findAll { it.@type == 'submit' }[0]", equalTo("Iniciar Sesión"),
                    "**.findAll { it.@class == 'to-login' }", equalTo("Se te olvidó tu contraseña?")
                )
    }

    @Test(enabled = false)
    void testRegisterPage() {
        given()
                .header("Accept-Language", "es")
                .header("Accept", "text/html")
            .when()
                .get("http://localhost:8080/register")
            .then()
                .body(
                    "html.head.title", equalTo("Crear Cuenta"),
                    "**.findAll { it.@type == 'submit' }", equalTo("Crear Cuenta"),
                    "**.findAll { it.@class == 'to-login' }", equalTo("Regresar a Iniciar Sesión")
                )
    }
}
