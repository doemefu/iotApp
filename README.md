<h1>IoT-App Backend</h1>

Dies ist die Doku der IoT App. Sie beinhaltet die Dokumentation der API, sowie die Dokumentation des Projektes.

# Inhalt

---

- [Table of Contents](#table-of-contents)
- [Documentation](#documentation)
  - [Inhalt](#inhalt)
  - [Einleitung](#einleitung)
    - [Projektbeschreibung](#projektbeschreibung)
    - [Anforderungsanalyse](#anforderungsanalyse)
- [Projektaufbau](#projektaufbau)
  - [Backend](#backend)
    - [Spring Security](#spring-security)
  - [Frontend](#frontend)
  - [Datenbank](#datenbank)
- [GUI](#gui)
  - [UI](#ui)
  - [UX](#ux)
    - [Spring Security](#spring-security-1)
  - [Authentication-API](#authentication-api)
- [Project Structure](#project-structure)
- [Progress](#progress)
  - [Versions](#versions)
  - [Version 1.0](#version-10)
  - [Version 2.0](#version-20)
  - [Issues](#issues)
    - [Login Umleitung](#login-umleitung)
- [Anleitungen](#anleitungen)
  - [Spring Security](#spring-security-2)
  - [Spring MVC](#spring-mvc)
  - [React](#react)


# Einleitung

---

Dieses Projekt ist Teil der Leistungsbeurteilung des Mudules 133. 


## Projektbeschreibung

<h3> Zielsetzung </h3>

Die Applikation sollte Datenbestand, Formularüberprüfung, Usability, Login für Benutzer-Accounts, 
Registrierung und Sessionhandling beinhalten. Des weiteren soll sie eine klare 3-Tier Architektur aufweisen, bestehend aus Presentations, Business und Data Layer.

- Der Presentation Layer sollte gut strukturiert und mittels CSS formatiert sein. Ausserdem muss eine Überprüfung der User Eingaben implementiert sein.

- Der Business Layer zeigt eine sinnvolle Struktur auf und wird nach den Standards der Programmierung implementiert und dokumentiert.

- Der Data Layer muss eine Datenbank aufweisen, die nach rationalen Richtlinien definiert und implementiert wird.

Ausserdem beinhaltet die Applikation ein konzeptionelles GUI Design, welches responsiv reagieren soll.

<h3> Funktionsweise </h3>



## Anforderungsanalyse

Aufgrund der vorgegebenen Eckdaten haben wir folgende Anforderungen abgeleitet:

1. **Bestandteile des Webauftrittes:** Der Webauftritt besteht aus mehreren einzelnen Websiten.
Eine dieser ist die Startseite, welche für alle Benutzer zugänglich ist. Dabei spielt es keine Rolle, ob bereits ein Useraccount vorhanden ist oder nicht.
Die zweite Seite ist nur für den eingeloggten User ersichtlich und zugänglich.
Für die Registrierung und das Einloggen wird ebanfalls eine eigene Website benötigt, welche sich aber lediglich auf diese Funktionen beschränkt sind und ansonnsten keinerlei Content beinhalten.
2. **Registrierung neuer User:** Es kann ein neuer User im Registrierungsfenster erstellt werden, dies geschieht mittels Eingabe eines Usernamens, einer Mailadresse und eines Passwortes.
Bevor die Registrierung abgeschlossen werden kann, wird einerseits überprüft, ob all diese Felder ausgefüllt sind, andererseits aber ach wie sei ausgefüllt sind. 
Es wird mit den bereits vorhandenen Datenbankeinträgen abgeglichen, ob der eingegebene Username und die Mailadresse bereits erfasst wurden. 
Die Mailadresse wird zudem auf ihre Richtigkeit überprüft, sprich ob sie der Norm einer solchen entspricht.
Ist dies der Fall, wird im Registrierungsfenster eine Fehlermeldung angegeben.
Ein unausgefülltes Feld, dies impliziert diesmal auch das Passwort, wird ebenfalle eine Fehlermeldung zurückgegeben.
Sobald ein gültige Eingabe abgesendet ist, wird ein User erstellt und der Benutzer gelange direkt in den Login Screen.
3. **Login eines registrierten Users:** Im Loginfenster kann ein User via Usernamen und Passwort angemeldet werden.
Hier gibt es ebanfalls eine Überprüfung der eingegebenen Werte. Einerseits, ob die Felder überhaupt ausgefüllt sind, andererseits aber auch ob in der Datenbank ein solches Wertepaar vorhanden ist.
Ist alles korrekt vorhanden wird der User angemeldet und gelangt direkt in sein Userprofil. Des weiteren wird ab dem Moment des erfolgreichen Loggins, ein Logout Button ersichtlich für ein simples Logout aus dem Userprofil.
4. **Sessionhandling:** Das Sessionhandling wird mittels JWT (JSON Web Token) gehandhabt. Sobald ein User seine Loginrequest ansendet, wird serverseitig ein JWT erstellt und zurückgesendet.
Dieser Token wird zurück an den Client gesendet und von ihm gespeichert.
Sobald der User eine neue Request an den Server sendet, bspw. beim Wechsel von seinem Userprofil zur Startseite, wird nun dieser JWT mitgegeben.
Anschliessend wird dieser vom Server validiert und die Respons wird zurück an den Client gesendet.

# Projektaufbau

In diesem Projekt sind verschiedenste Elemente verbaut um die von uns festgestellten Anforderungen zu erfüllen.

## Backend

Für den Aufbau des Back-end haben wir uns für eine Kombination aus Spring Boot, Spring Security und JWT entschieden. Diese Frameworks interagieren sehr gut zusammen, da sie aus dem Spring Ökosystem stammen und mit dem JSON Web Token eine Komponente für simples Sessionhandling integriert werden kann.

### Spring Security

Der WebSecurityConfigurerAdapter ider das Herzstück dieser Sicherheitsimplementierung. Er bieter Konfigurationen für HttpSecurity, CORS, CSRF, Session Handling und Regelung für geschützte Ressourcen. Die bereits vorhandenen Standardkonfigurationen können leicht erweitert und angepasst werden. Folgende Elemente sind damit gemeint:

**UserDetailsService:** Dieses Interface verfpgt über eine Methode zum Laden von Benutzern anhand ihres Benutzernamens und gibt ein UserDetails-Objekt zurück, welches zur Authentifizierung und Validierung verwendet werden kann.

**UserDetails:** Dieses Objekt enthält die erforderlichen Informationen, wie beispielsweise Benutzernamen, Passwort und Berechtigungen, um ein Authentifizuerungsobjekt zu erstellen.

**UsernamePasswordAuthentificationToken:** Dieser Objekt enthält Benutzernamen und Passwort aus der Anmeldeanfrage. Es wird vom AuthentificationManager verwendet für die Authentifizierung eines Anmeldekontos.

**AuthentificationManager:** Dieser Manager verfügt über einen DauAuthentificationProvider, unter Verwendung von UserDetailsSercive und PasswordEncoder. Dessen Funktionalität ist ist die Validierung des UsernamePasswordAuthentificationToken-Objektes. Bei einer erfolgreichen Validierung gibt der AuthentificationManager ein vollständig ausgefülltes Authentifizierungsobjekt zurück, einschliesslich gewährter Berechtigungen.

**OncePerRequestFilter:** Dieser Filter führt bei jeder Anforderung an die API eine einzelne Ausführung durch. Er bietet eine doFilterInternal()-Methode, die implementiert wird, um JWT zu analysieren und zu validieren, Benutzerdetails, unter Verwendung von UserDetailsService, zu laden und Berechtigungen zu überprüfen, unter Verwendung von UsernamePasswordAuthenticationToken.

**AuthentificationEntryPoint:** Dieser erfasst unberechtigte Fehler und gibt einen HTTP-Statuscode 401 zurück, wenn Clients auf geschützte Ressourcen ohne Authentifizierung zugreiffen.

Das Repository enthält UserRepository und RoleRepository, um mit der Datenbank zu arbeiten, und wird in den Controller importiert. Dieser Controller empfängt und vararbeiter Anfragen, nachdem sie vom OncePerRequestFilter gefiltert wurden

**AuthController:** Dieser behandelt Anfragen zur Registrierung und Anmeldung.

**TestController:** Der Controller verfügt über Methoden zum Zugriff auf geschützte Ressourcen mit Validierungen basierend auf den Benutzerrollen.

## Frontend

## Datenbank

# GUI

## UI

## UX




### Spring Security

![Spring Security Concept](https://www.bezkoder.com/wp-content/uploads/2019/10/spring-boot-authentication-spring-security-architecture.png)

![Logik der Registration und des Logins von Usern](https://www.bezkoder.com/wp-content/uploads/2020/03/spring-boot-react-authentication-jwt-example-flow.png)


![Logik der Registration und des Logins von Usern mit Refresh Token](https://www.bezkoder.com/wp-content/uploads/2021/04/spring-boot-refresh-token-jwt-example-flow.png)












# Authentication-API

[API documentation](documentation/authentication-api.yaml ':include :type=markdown')

# Project Structure

Gemäss [Best Practices](https://medium.com/the-resonant-web/spring-boot-2-0-project-structure-and-best-practices-part-2-7137bdcba7d3)


# Progress

## Versions

## Version 1.0

- JWT authentication

sources:
[Concept](https://www.bezkoder.com/spring-boot-react-jwt-auth/#Spring_Boot_038_Spring_Security_for_Back-end)
[Backend](https://www.bezkoder.com/spring-boot-jwt-authentication/)
[Frontend](https://www.bezkoder.com/react-hooks-jwt-auth/#Setup_Reactjs_Project)

## Version 2.0

add Refresh Token
https://www.bezkoder.com/react-refresh-accessToken/

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