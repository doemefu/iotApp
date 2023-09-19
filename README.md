<h1>IoT-App Backend</h1>

# Table of Contents

# Project Structure

Gemäss [Best Practices](https://medium.com/the-resonant-web/spring-boot-2-0-project-structure-and-best-practices-part-2-7137bdcba7d3)


# Progress

## Issues

### Login Umleitung

The `.formLogin(withDefaults())` method in Spring Security's configuration chain is used for configuring form-based authentication. When you include `.formLogin(withDefaults())`, Spring Security automatically provides a default form-based login page for authentication, which is found under localhost:8080/login.
Contrary to that my own loginPage is found under localhost:3000/login as it's a React component.

Here's what it does, essentially:

1. **Default Login Page**: When you access a secured endpoint without being authenticated, Spring Security redirects you to a default login page.

2. **Authentication Mechanism**: It sets up the authentication mechanism to validate the username and password submitted through the form against the configured `UserDetailsService`.

3. **Redirect Strategy**: After a successful login, it redirects the user to the originally requested URL or a default URL if none was originally requested.

By including `.formLogin(withDefaults())`, you're essentially telling Spring Security to handle authentication via a form-based login mechanism. This mechanism might interfere with your RESTful API endpoints, specifically the `/login` POST endpoint.

When your front-end sends a POST request to `/login`, Spring Security intercepts it to perform the form-based login, instead of letting it go through to your REST controller. Since your API expects a JSON payload while the form login expects form parameters, this conflict arises.

If you don't want Spring Security to generate a default login page and handle form-based login, it's better to comment out or remove the `.formLogin()` line as you did, especially when you are developing RESTful services where you would like to handle login via JSON payloads and custom logic.

So, in the context of a RESTful service, it's common to disable the form-based login and handle security through other mechanisms like JWT, OAuth2, or simple Basic Authentication depending on your needs.

For further information see: [spring documentation](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html)

# Anleitungen

- [Full tutorial](https://spring.io/guides/tutorials/react-and-spring-data-rest/)
- [outdated but schematic tutorial](https://www.bezkoder.com/spring-boot-react-jwt-auth/)

## Spring Security

See class `WebSecurityConfig` for the configuration of the security filter chain.

- [The Security Filter Chain](https://kasunprageethdissanayake.medium.com/spring-security-the-security-filter-chain-2e399a1cb8e3)
- [Request Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html) 
- https://docs.spring.io/spring-security/site/docs/5.5.x/guides/form-javaconfig.html


## Spring MVC

- [Request Mapping](https://www.baeldung.com/spring-requestmapping)
- [Redirect after Login or Registration](https://www.baeldung.com/spring-redirect-after-login)

