<h1>IoT-App Backend</h1>

Dies ist die Doku der IoT App. Sie beinhaltet die Dokumentation der API, sowie die Dokumentation des Projektes.

# Inhalt

---

<!-- TOC -->
* [Inhalt](#inhalt)
* [Einleitung](#einleitung)
  * [Projektbeschreibung](#projektbeschreibung)
    * [Zielsetzung](#zielsetzung)
  * [Anforderungsanalyse](#anforderungsanalyse)
  * [Nutzwertanalyse](#nutzwertanalyse)
* [Umsetzung](#umsetzung)
  * [Authentication-API](#authentication-api)
  * [Project Structure](#project-structure)
* [Progress](#progress)
  * [Version 1.0](#version-10)
  * [Version 2.0](#version-20)
    * [now working:](#now-working)
    * [TODO:](#todo)
    * [Cookies](#cookies)
* [Issues](#issues)
  * [JWT is not meant for authentication](#jwt-is-not-meant-for-authentication)
  * [Login Umleitung](#login-umleitung)
* [Anleitungen](#anleitungen)
  * [Spring Security](#spring-security)
  * [Spring MVC](#spring-mvc)
  * [React](#react)
<!-- TOC -->


# Einleitung

---

Dieses Projekt ist Teil der Leistungsbeurteilung des Modules 133. 


## Projektbeschreibung

### Zielsetzung

Die Applikation sollte Datenbestand, Formularüberprüfung, Usability, Login für Benutzer-Accounts, 
Registrierung und Session handling beinhalten. Des Weiteren soll sie eine klare 3-Tier Architektur aufweisen, bestehend aus Presentations, Business und Data Layer.

- Der Presentation-Layer sollte gut strukturiert und mittels CSS formatiert sein. Ausserdem muss eine Überprüfung der User Eingaben implementiert sein.

- Der Business-Layer zeigt eine sinnvolle Struktur auf und wird nach den Standards der Programmierung implementiert und dokumentiert.

- Der Data-Layer muss eine Datenbank aufweisen, die nach rationalen Richtlinien definiert und implementiert wird.

Ausserdem beinhaltet die Applikation ein konzeptionelles GUI Design, welches responsiv reagieren soll.

## Anforderungsanalyse

Aufgrund der vorgegebenen Eckdaten haben wir folgende Anforderungen abgeleitet:

1. **Bestandteile des Webauftrittes:** Der Webauftritt besteht aus mehreren einzelnen Websiten.
Eine dieser ist die Startseite, welche für alle Benutzer zugänglich ist. Dabei spielt es keine Rolle, ob bereits ein Useraccount vorhanden ist oder nicht.
Die zweite Seite ist nur für den eingeloggten User ersichtlich und zugänglich.
Für die Registrierung und das Einloggen wird ebanfalls eine eigene Website benötigt, welche sich aber lediglich auf diese Funktionen beschränkt sind und ansonnsten keinerlei Content beinhalten.
2. **Registrierung neuer User:** Es kann ein neuer User im Registrierungsfenster erstellt werden, dies geschieht mittels Eingabe eines Usernamens, einer Mailadresse und eines Passwortes.
Bevor die Registrierung abgeschlossen werden kann, wird einerseits überprüft, ob all diese Felder ausgefüllt sind, andererseits aber ach wie sei aisgefüllt sind. 
Es wird mit den bereits vorhandenen Datenbankeinträgen abgeglichen, ob der eingegebene Username und die Mailadresse bereits erfasst wurden. 
Die Mailadresse wird zudem auf ihre Richtigkeit überprüft, sprich ob sie der Norm einer solchen entspricht.
Ist dies der Fall, wird im Registrierungsfenster eine Fehlermeldung angegeben.
Ein unausgefülltes Feld, dies impliziert diesmal auch das Passwort, wird ebenfalle eine Fehlermeldung zurückgegeben.
Sobald eine gültige Eingabe abgesendet ist, wird ein User erstellt und der Benutzer gelange direkt in den Login Screen.
3. **Login eines registrierten Users:** Im Loginfenster kann ein User via Usernamen und Passwort angemeldet werden.
Hier gibt es ebanfalls eine Überprüfung der eingegebenen Werte. Einerseits, ob die Felder überhaupt ausgefüllt sind, andererseits aber auch ob in der Datenbank ein solches Wertepaar vorhanden ist.
Ist alles korrekt vorhanden wird der User angemeldet und gelangt direkt in sein Userprofil. Des weiteren wird ab dem Moment des erfolgreichen Loggins, ein Logout Button ersichtlich für ein simples Logout aus dem Userprofil.
4. **Sessionhandling:** Das Sessionhandling wird mittels JWT (JSON Web Token) gehandhabt. Sobald ein User seine Loginrequest ansendet, wird serverseitig ein JWT erstellt und zurückgesendet.
Dieser Token wird zurück an den Client gesendet und von ihm gespeichert.
Sobald der User eine neue Request an den Server sendet, bspw. beim Wechsel von seinem Userprofil zur Startseite, wird nun dieser JWT mitgegeben.
Anschliessend wird dieser vom Server validiert und die Respons wird zurück an den Client gesendet.

## Nutzwertanalyse

# Umsetzung

Die Applikation ist als 4 Tier Architektur aufgebaut. Sie besteht aus einer Client-, Presentation-, Application- und Data-Storage-Schicht.

**Client-Schicht** (Client Layer):</br>
Der Browser, der die React-Anwendung ausführt und auf Port 3000 läuft. Diese Schicht ist für die Darstellung der Benutzeroberfläche und die Interaktion mit dem Benutzer verantwortlich.

**Präsentationsschicht** (Presentation Layer):</br>
Die React-Anwendung selbst, die API-Aufrufe an das Spring Boot-Programm macht. Sie dient als Frontend und stellt die Benutzeroberfläche bereit.

**Anwendungsschicht** (Application Layer):</br>
Das Spring Boot-Programm auf Port 8080. Diese Schicht handhabt die Geschäftslogik und koordiniert die Daten zwischen der MariaDB und der InfluxDB.

**Datenspeicherschicht** (Data Storage Layer):</br>
Hier haben Sie zwei Datenbanken - MariaDB auf Port 3306 für die Speicherung von Anmeldedaten und eine externe InfluxDB für andere Daten. Beide werden über die Spring Boot-Anwendung abgerufen.

## Authentication-API

[API documentation](documentation/authentication-api.yaml ':include :type=markdown')

```
+--------+                                           +---------------+
|        |--(A)------- Authorization Grant --------->|               |
|        |                                           |               |
|        |<-(B)----------- Access Token -------------|               |
|        |               & Refresh Token             |               |
|        |                                           |               |
|        |                            +----------+   |               |
|        |--(C)---- Access Token ---->|          |   |               |
|        |                            |          |   |               |
|        |<-(D)- Protected Resource --| Resource |   | Authorization |
| Client |                            |  Server  |   |     Server    |
|        |--(E)---- Access Token ---->|          |   |               |
|        |                            |          |   |               |
|        |<-(F)- Invalid Token Error -|          |   |               |
|        |                            +----------+   |               |
|        |                                           |               |
|        |--(G)----------- Refresh Token ----------->|               |
|        |                                           |               |
|        |<-(H)----------- Access Token -------------|               |
+--------+           & Optional Refresh Token        +---------------+
```
(A) The client requests an access token by authenticating with the authorization server and presenting an authorization grant.

(B) The authorization server authenticates the client and validates the authorization grant, and if valid, issues an access token and a refresh token.

(C) The client makes a protected resource request to the resource server by presenting the access token.

(D) The resource server validates the access token, and if valid, serves the request.

(E) Steps (C) and (D) repeat until the access token expires. If the client knows the access token expired, it skips to step (G); otherwise, it makes another protected resource request.

(F) Since the access token is invalid, the resource server returns an invalid token error.

(G) The client requests a new access token by authenticating with the authorization server and presenting the refresh token. The client authentication requirements are based on the client type and on the authorization server policies.

(H) The authorization server authenticates the client and validates the refresh token, and if valid, issues a new access token (and, optionally, a new refresh token).

### Password Security

Version 2y is the most secure

Strength 10 is standard and means 2^10 rounds

`SecureRandom` is used to generate a salt for the hash

Custom RNG: By providing your own SecureRandom instance, you have control over the random number generator (RNG) 
used for salt generation. This can be useful if you have specific requirements for the RNG, such as using a
hardware-based RNG or a specific algorithm.

Peppers won't be implemented as it is not recommended for todays algorithms
[source](https://stackoverflow.com/questions/16891729/best-practices-salting-peppering-passwords)

BCrypt hash string will look like: $2<a/b/x/y>$[strength]$[22 character salt][31 character hash]



```java
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 10, new SecureRandom());
    }
```

## Project Structure

Aufbau gemäss [Best Practices](https://medium.com/the-resonant-web/spring-boot-2-0-project-structure-and-best-practices-part-2-7137bdcba7d3)</br>

# Progress

## Version 1.0

JWT authentication without refresh token

sources:
- [Concept](https://www.bezkoder.com/spring-boot-react-jwt-auth/#Spring_Boot_038_Spring_Security_for_Back-end)
- [Backend](https://www.bezkoder.com/spring-boot-jwt-authentication/)
- [Frontend](https://www.bezkoder.com/react-hooks-jwt-auth/#Setup_Reactjs_Project)

## Version 2.0

JWT authentication with refresh token and cookie ready

**Backend**: Spring Boot</br>
- [GitHub Pull Request](https://github.com/doemefu/iotApp/pull/3)
- [Tutorial](https://www.bezkoder.com/spring-boot-refresh-token-jwt/)

[Repo]: <> (https://github.com/bezkoder/spring-boot-refresh-token-jwt/tree/master)

**Frontend**: React
- [GitHub Pull Request](https://github.com/doemefu/frontIotApp/pull/3)
- [Tutorial](https://www.bezkoder.com/react-refresh-token/)

[Repo]: <> (https://github.com/bezkoder/react-refresh-token-hooks/tree/master)

### now working:

- [x] Register
- [x] Login
- [x] Logout
- [x] (Semi) stateless Sessionhandling
- [x] Role based authenticated endpoints
- [x] Influx data connection

### TODO:

- [ ] Forgot password
- [ ] Admin userhandling
- [ ] Delete account
- [ ] Responsive
- [ ] MQTT connection
- [ ] User status handling

### Cookies

Cookies did not work out. They didn't get stored in the browser even though they were correctly sent and received on the client. After quite some research it turns out, browsers have a hard time handling self-signed certificates, cors http(s) requests and especially setting cookies on localhost. Once those issues get resolved we might start a new attempt.
For the time being, the functions and methods are just commented out, and localstorage of the browser is used despite the security issues. There just isn't a different option.

# Issues

## JWT is not meant for authentication

https://stackoverflow.com/questions/39909419/why-use-jwt-for-authentication

## Login Umleitung

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
- [outdated but schematic tutorial (***Ehrenbre***)](https://www.bezkoder.com/spring-boot-react-jwt-auth/)

## Spring Security

See class `WebSecurityConfig` for the configuration of the security filter chain.

- [The Security Filter Chain](https://kasunprageethdissanayake.medium.com/spring-security-the-security-filter-chain-2e399a1cb8e3)
- [Request Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html) 
- https://docs.spring.io/spring-security/site/docs/5.5.x/guides/form-javaconfig.html


## Spring MVC

- [Request Mapping](https://www.baeldung.com/spring-requestmapping)
- [Redirect after Login or Registration](https://www.baeldung.com/spring-redirect-after-login)

## React

- [From validation](https://www.bezkoder.com/react-form-validation-hooks/)