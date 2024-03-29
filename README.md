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
* [Umsetzung](#umsetzung)
  * [Authentication-API](#authentication-api)
    * [Password Security](#password-security)
    * [Email-Verifizierung](#email-verifizierung)
    * [Passwort ändern](#passwort-ändern)
  * [React Refresh Token](#react-refresh-token)
    * [JWT und Axios](#jwt-und-axios)
    * [Authorisierung](#authorisierung)
  * [Spring Security](#spring-security)
    * [HttpServletRequest](#httpservletrequest)
    * [AuthorizationFilter](#authorizationfilter)
    * [Logging](#logging)
  * [Datenbank](#datenbank)
    * [MariaDB](#mariadb)
    * [InfluxDB](#influxdb)
  * [TestCases](#testcases)
* [Security Tests](#security-tests)
  * [Metasploit](#metasploit)
    * [Exploits und Payloads](#exploits-und-payloads)
    * [Modularität](#modularität)
    * [Frameworks und Penetrationtests](#frameworks-und-penetrationtests)
    * [Community und Entwicklung](#community-und-entwicklung)
    * [Meterpreter](#meterpreter)
  * [NMAP](#nmap)
    * [Was ist NMAP?](#was-ist-nmap)
    * [Ping Scan](#ping-scan)
    * [Port Scan](#port-scan)
    * [Service Version Scan](#service-version-scan)
    * [Aggressiver Scan](#aggressiver-scan)
  * [ZAP](#zap)
    * [Was ist ZAP?](#was-ist-zap)
    * [Warum ZAP?](#warum-zap)
    * [Auswertung](#auswertung)
      * [Beschreibung](#beschreibung)
      * [Zusätzliche Informationen](#zusätzliche-informationen)
      * [Lösung](#lösung)
      * [URL](#url)
      * [Beschreibung](#beschreibung-1)
      * [Lösung](#lösung-1)
      * [URL](#url-1)
      * [Beschreibung](#beschreibung-2)
      * [Lösung](#lösung-2)
      * [URL](#url-2)
      * [Beschreibung](#beschreibung-3)
      * [Lösung](#lösung-3)
      * [URL](#url-3)
      * [Beschreibung](#beschreibung-4)
      * [Lösung](#lösung-4)
      * [URL](#url-4)
      * [Beschreibung](#beschreibung-5)
      * [Zusätzliche Informationen](#zusätzliche-informationen-1)
      * [Lösung](#lösung-5)
      * [URL](#url-5)
* [Issues](#issues)
  * [JWT und Sessionhandling](#jwt-und-sessionhandling)
  * [Bootstrap und CSS-Formatierung](#bootstrap-und-css-formatierung)
* [Work in Progress Notizen](#work-in-progress-notizen)
  * [Project Structure](#project-structure)
  * [Version 1.0](#version-10)
  * [Version 2.0](#version-20)
    * [now working:](#now-working)
    * [TODO:](#todo)
    * [Cookies](#cookies)
  * [JWT is not meant for authentication](#jwt-is-not-meant-for-authentication)
  * [Login Umleitung](#login-umleitung)
  * [Anleitungen](#anleitungen)
  * [Spring MVC](#spring-mvc)
  * [React](#react)
<!-- TOC -->


# Einleitung

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

**Bestandteile des Webauftrittes:** Der Webauftritt besteht aus mehreren einzelnen Websiten.
Eine dieser ist die Startseite, welche für alle Benutzer zugänglich ist. Dabei spielt es keine Rolle, ob bereits ein Useraccount vorhanden ist oder nicht.
Die zweite Seite ist nur für den eingeloggten User ersichtlich und zugänglich.
Für die Registrierung und das Einloggen wird ebanfalls eine eigene Website benötigt, welche sich aber lediglich auf diese Funktionen beschränkt sind und ansonnsten keinerlei Content beinhalten.

**Registrierung neuer User:** Es kann ein neuer User im Registrierungsfenster erstellt werden, dies geschieht mittels Eingabe eines Usernamens, einer Mailadresse und eines Passwortes.
Bevor die Registrierung abgeschlossen werden kann, wird einerseits überprüft, ob all diese Felder ausgefüllt sind, andererseits aber ach wie sei aisgefüllt sind.
Es wird mit den bereits vorhandenen Datenbankeinträgen abgeglichen, ob der eingegebene Username und die Mailadresse bereits erfasst wurden.
Die Mailadresse wird zudem auf ihre Richtigkeit überprüft, sprich ob sie der Norm einer solchen entspricht.
Ist dies der Fall, wird im Registrierungsfenster eine Fehlermeldung angegeben.
Ein unausgefülltes Feld, dies impliziert diesmal auch das Passwort, wird ebenfalle eine Fehlermeldung zurückgegeben.
Sobald eine gültige Eingabe abgesendet ist, wird ein User erstellt und der Benutzer gelange direkt in den Login Screen.

**Login eines registrierten Users:** Im Loginfenster kann ein User via Usernamen und Passwort angemeldet werden.
Hier gibt es ebanfalls eine Überprüfung der eingegebenen Werte. Einerseits, ob die Felder überhaupt ausgefüllt sind, andererseits aber auch ob in der Datenbank ein solches Wertepaar vorhanden ist.
Ist alles korrekt vorhanden wird der User angemeldet und gelangt direkt in sein Userprofil. Des weiteren wird ab dem Moment des erfolgreichen Loggins, ein Logout Button ersichtlich für ein simples Logout aus dem Userprofil.

**Sessionhandling:** Das Sessionhandling wird mittels JWT (JSON Web Token) gehandhabt. Sobald ein User seine Loginrequest ansendet, wird serverseitig ein JWT erstellt und zurückgesendet.
Dieser Token wird zurück an den Client gesendet und von ihm gespeichert.
Sobald der User eine neue Request an den Server sendet, bspw. beim Wechsel von seinem Userprofil zur Startseite, wird nun dieser JWT mitgegeben.
Anschliessend wird dieser vom Server validiert und die Respons wird zurück an den Client gesendet.

# Improvements

## Datenbank

Im Rahmen des Moduls xxx wurden diverse Verbesserung der Datenbankperformance und -sicherheit umgesetzt. Dies umfasst
unter anderem die folgenden Änderungen:

### System-Versioned Tables

[MariaDB Doku](https://mariadb.com/kb/en/system-versioned-tables/)
```sql
alter table user_accounts add system versioning;
alter table user_role add system versioning;
```

Dann kam aber das raus:
```bash
MariaDB [mydatabase]> Select * from user_accounts for system_time AS OF TIMESTAMP'2024-03-27 15:00:00';
+----+----------------------------+----------------------------+--------------------------+--------------------------------------------------------------+-------------+-----------+
| id | changed_at                 | created_at                 | email                    | password                                                     | username    | status_id |
+----+----------------------------+----------------------------+--------------------------+--------------------------------------------------------------+-------------+-----------+
|  1 | 2023-12-15 20:50:46.858000 | 2023-12-15 20:50:31.010000 | dominicfur@gmail.com     | $2y$10$MWRes8gjSw26CWYmzOkQAuytaqMYG0iOy9PgfdJFd8FXoXy.526Am | dominicfur  |         2 |
|  2 | 2023-12-16 10:30:46.406000 | 2023-12-16 10:30:36.630000 | domifur@hotmail.com      | $2y$10$2O1tpusKDXleiXwLP1J3yu/Vu0NDy10dfln4uyVhmuYupfcZrps9u | dominicfur2 |         2 |
|  3 | 2023-12-20 09:55:28.079000 | 2023-12-20 09:55:28.079000 | david.egeler@edu.tbz.ch  | $2y$10$TgqKfNq0fmZMobp5CN1oRO7u5km.vJmY1UNby5M/wOPTmHnHpVtO2 | david       |         1 |
|  4 | 2023-12-20 10:10:51.247000 | 2023-12-20 10:10:31.560000 | david.egeler@hotmail.com | $2y$10$6oxnN4gESOrUoyxbqKygGuUwVzjZDYPOVd1bkgQUm2I3zB2f3sB/C | david1      |         2 |
|  5 | 2024-03-27 15:11:57.337000 | 2024-03-27 15:11:57.337000 | test@test.test           | $2y$10$ugXAlp1JoSNUmkU23swEfeV4wubPVpJER/Tz/va1NnPoLqGhvj1CC | test        |         1 |
|  6 | 2024-03-27 15:12:40.704000 | 2024-03-27 15:12:40.704000 | test@gmail.com           | $2y$10$s2AoaQXFwdoMGTvScp/qF.z5bb.7D3KPv8qeRxftRFku31cPmeUM. | test123     |         1 |
+----+----------------------------+----------------------------+--------------------------+--------------------------------------------------------------+-------------+-----------+
```
deshalb musste ich:
```sql
SET GLOBAL time_zone = 'Europe/Zurich';
```
und restarten

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

<!---
[API documentation](documentation/authentication-api.yaml ':include :type=markdown')
--->

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
(A) Der Client fordert ein Zugriffstoken an, indem er sich beim Autorisierungsserver authentifiziert und eine Autorisierungszusage vorlegt.

(B) Der Autorisierungsserver authentifiziert den Client und überprüft die Autorisierungszusage. Bei erfolgreicher Validierung stellt er ein Zugriffstoken und ein Auffrischungstoken aus.

(C) Der Client stellt eine geschützte Ressourcenanfrage an den Ressourcenserver, indem er das Zugriffstoken vorlegt.

(D) Der Ressourcenserver validiert das Zugriffstoken und, wenn es gültig ist, bedient er die Anfrage.

(E) Die Schritte (C) und (D) wiederholen sich, bis das Zugriffstoken abläuft. Wenn der Client weiss, dass das Zugriffstoken abgelaufen ist, überspringt er Schritt (G); andernfalls stellt er eine weitere geschützte Ressourcenanfrage.

(F) Da das Zugriffstoken ungültig ist, gibt der Ressourcenserver einen Fehler für ungültiges Token zurück.

(G) Der Client fordert ein neues Zugriffstoken an, indem er sich beim Autorisierungsserver authentifiziert und das Auffrischungstoken vorlegt. Die Authentifizierungsanforderungen des Clients basieren auf dem Clienttyp und den Richtlinien des Autorisierungsservers.

(H) Der Autorisierungsserver authentifiziert den Client und überprüft das Auffrischungstoken. Bei erfolgreicher Validierung stellt er ein neues Zugriffstoken aus (und optional ein neues Auffrischungstoken).

### Password Security

`SecureRandom` wird verwendet, um ein Salz für den Hash zu generieren.

Benutzerdefinierte Zufallszahlengeneratoren (RNG): Durch Bereitstellen Ihrer eigenen SecureRandom-Instanz haben Sie die Kontrolle über den Zufallszahlengenerator (RNG), der für die Salzgenerierung verwendet wird. Dies kann nützlich sein, wenn Sie spezielle Anforderungen an den RNG haben, wie die Verwendung eines hardwarebasierten RNG oder eines bestimmten Algorithmus.

Peppers (Zusatzsalze) werden nicht implementiert, da sie für moderne Algorithmen nicht empfohlen werden.

Ein BCrypt-Hash-String sieht folgendermassen aus: $2<a/b/x/y>$[strenght]$[22 character salt][31 character hash]

Störke heisst in diesem Fall, wie viele Runden des Hashingprozesses durchgeführt werden. In unserer Applikation haben wir den normwert von Stärke 10 verwendet was bedeutet, dass 10^2 Runden durchlaufen werden.

```java
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 10, new SecureRandom());
    }
```
### Email-Verifizierung

Der `EmailTokenService` handhabt die Verwaltung des Email-Verifizierungstokens. Sie bietet Methoden zur Erstellung, Validierung und Löschung dieser Email-Verifizierungstoken. Mit der Methode `createEmailTokenForUser` wird ein Verifizierungstoken für einen bestimmten User erstellt und speichert diesen in der Datenbank. Der Token beinhaltet ein Ablaufdatum, welches normalerweise 24 stunden nach der Erstellung als "nicht verwendet" markiert wird.

Mit `validateEmailToken` wird der Validierungstoken validiert, indem der empfangene Token mit dem in der DB gespeicherten verglichen wird. Hierbei wird überprüft, ob der Token abgelaufen idt oder bereits verwendet wurde. Wenn dieser gültig ist, gibt die Methode den Benutzer zurück welcher ihm zugehört.

`deleteToken` und `deleteTokenByValue` löschen den Validierungstoken aus der Datebbank, entweder durch die direkte Übergabe des Tokens oder durch den Token-Wert.

Sobald eine Email-Verifizierung initialisiert ist, beispielsweise durch den Registrierungsprozess, wird eine automatisch generierte Email an die angegebene Usermail gesendet. in `EmailConfiguration` wird diese Mail angefertigt, inklusive einem Link zur Verifizierung. Dieser führt zurück auf die Website, wo eine Erfolgsmeldung beim erfolgreichen Abschluss erscheint.

### Passwort ändern

Die Passwortänderung kann in zwei verschiedenen Fällen auftreten. Einerseits als bewusste Aktion des bereits angemeldeten Userd, andererseits auch als Möglichkeit das vergessene Passwort eines unangemeldeten Users zurückzusetzen.

**Angemeldeter User**<br>
In `/user` kann über einen Button die Passwortänderung initialisiert werden. Der User gelangt auf `ResetPassword.js`, wo er ein Inputfeld für das alte und eines für das neue Passwort vorfindet. Stimmt das alte Passwort überein, so wird das neue Passwort als gültig anerkannt und in die Datenbank geschrieben, somit ist der Reset-Prozess abgeschlossen.

**Unauthorisierter User**<br>
In `/login` kann ebenfalls über einen Button die Passwortänderung initialisiert werden. Der User gelangt auf `ForgotPassword.js`. Auf dieser Seite findet der User ein Eingabefeld für die Mailadresse seines Accounts vor. Sofern die Mailadresse eines bekannten Users eingegeben wurde, wird an diese ein Link für das Resetten versendet. Mit diesem Link gelangt der User zurück auf `ForgotPassword.js` und kann das neue Passwort eingeben und den Prozess abschliessen. Ist dies gemacht, wird der gesendete Link invalid und kann nicht mehr verwendet werden, das dient als Absicherung gegen ungewolltes Entwenden des Links und präventiert das ungewollte Verwenden durch Drittpersonen.

## React Refresh Token

– Ein Refreshtoken wird dem Benutzer beim Einloggen zur Verfügung gestellt.

– Ein gültiges JWT muss dem HTTP-Header hinzugefügt werden, wenn der Client auf geschützte Ressourcen zugreift.

– Mit Hilfe von Axios-Interceptoren kann die React-App überprüfen, ob der Zugriffstoken (JWT) abgelaufen ist (Statuscode 401). In diesem Fall sendet sie eine Anfrage zum Erhalten eines neuen Zugriffstokens (/refreshToken) und verwendet diesen für neue Ressourcenanfragen.

### JWT und Axios

In React ist Axios ein häufig verwendetes JavaScript-Modul, das für das Senden von HTTP-Anfragen an Server verwendet wird. Axios Interceptoren sind Funktionen, die von Axios bereitgestellt werden und es ermöglichen, Anfragen und Antworten zu überwachen und zu bearbeiten, bevor sie an den Server gesendet bzw. von ihm empfangen werden. Sie sind äuerssst nützlich, um bestimmte Aufgaben und Anforderungen an HTTP-Anfragen und -Antworten zu implementieren, insbesondere wenn mit Authentifizierung, Autorisierung und Fehlerbehandlung umgegangen werden muss.

Die Kombination aus Axios Interceptoren und React ermöglicht eine zentrale und effiziente Steuerung der Netzwerkkommunikation. Dadurch können wiederkehrende Aufgaben automatisiert werden. Interzeptoren sind hierbei besonders nützlich fpr die Handhabung von Authentifizierung und Autorisierung.

**Authentifizierung und Token-Handling:** Wir verwenden einen Request-Interceptor, um sicherzustellen, dass ein Authentifizierungs-Token (JWT) zu jeder Anfrage hinzugefügt wird, um auf geschützte Ressourcen zuzugreifen. Dies geschieht im `instance.interceptors.request.use-Block`, wo wir den Token dem Anforderungs-Header hinzufügen.

**Fehlerbehandlung:** Wir verwenden Response-Interceptoren, um Fehler global zu behandeln. Wenn ein Statuscode 401 (unautorisiert) empfangen wird, versuchen wir im Response-Interceptor automatisch, ein neues Authentifizierungstoken zu erhalten und die ursprüngliche Anfrage erneut auszuführen. Dies hilft bei der Behandlung von abgelaufenen Tokens oder ungültigen Authentifizierungen.

Auf Grund dessen ist in `api.js` eine API für die Handhabung der Authentifizierung der User enthalten. Diese überprüft, ob ein User eingeloggt werden kann oder nicht. Des weiteren könne neue Tokens für eine weiterführende Authentifizierung ausgestellt werden.

Im Front-end werden innerhalb der beiden Klassen `UserService`und `AuthService` die Antworten der API gehandhabt. Auf der einen Seite kann unterschieden werden um welche Aktion des Users es sich handelt, beispielsweise ein Login oder ein Logout. Andererseits kann aber auch bestummen werden, um welche Art Usere es sich handelt und desswegen die korrekte Userseite anzeigen, je nach Klassifizierung des Users.

![spring-security-refresh-token-jwt-spring-boot-flow.png](documentation%2Fspring-security-refresh-token-jwt-spring-boot-flow.png)

### Authorisierung

In `AuthEntryPointJwt` befindet sich der `AuthentificationEntryPoint`, welcher unbefugte Anfroderungen von Usern oder anderen behandelt. Will ein User ohne gültigen JWT auf geschützte Ressourcen zugreiffen wirdd `AuthEntryPointJwt` aufgerufen, um die Antwort zu konfigurieren und eine Fehlermeldung zurückzugeben.

`AuthTokenFilter` ist ein `OncePerRequestFilter`, der jede eingehendeAnforderung überprüft, ob ein gültiger JWT im Header vorhanden ist. Ist ein gültiger Token vorhanden, wird der User authentifizuert und seine Identität wird im Spring Security-Kontext gespeichert. Die Filterkette konfiguriert die Zugriffsberechtigungen für die jeweiligen Endpunkte, beispielsweise können Seiten wie 
`WebSecurityConfig` definiert die Sicherheitsrichtlinien für unsere Anwendungen. In diesem Zusammenhang konfiguriert sie den `AuthEntryPointJwt` als Eintrittspunkt für die unbefugten Anforderungen. 

Im Header werden Metadaten über den Token selbst gesichert, beispielsweise den Signaturalgorithmus. In unserer Applikation beinhalter er

## Spring Security

Mittels Spring Security kann die Form von Authentifizierung gehandhabt werden. Standardmässig verlangt Spring Security eine Authentifizierung für jede einzelne Request. Desswegen wird bei jeder Verwendung von `HttpSecurity` vorausgesetzt, dass gewisse Authentifizierungsrichtlinien angewendet werden.

![spring-boot-authentication-spring-security-architecture.png](documentation%2Fspring-boot-authentication-spring-security-architecture.png)

### HttpServletRequest

```java
http
.authorizeHttpRequests((authorize) -> authorize
.anyRequest().authenticated()
)
```

Dies setzt voraus, dass bei jedem Endpoint in der Applikation im Punkt Sicherheit mindestens eine Authentifizierung benötigt wird. Jedoch wird in der Applikation auch zwischen verscheidenen Stufen bzw. Arten von Authentifizierung unterschieden.

### AuthorizationFilter

Der AuthorizationFilter befindet sich standardmässig am Ende der Spring Security-Filterkette. Das bedeutet, dass die Authentifizierungsfilter, Schutzmechanismen gegen Exploits und andere Filterintegrationen von Spring Security keine zusätzliche Autorisierung erfordern. Wenn Sie eigene Filter vor dem Autorisierungsfilter hinzufügen, erfordern diese ebenfalls keine separate Autorisierung. Andernfalls benötigen sie diese.

Ein Ort, an dem dies in der Regel wichtig wird, ist die Hinzufügung von Spring MVC-Endpunkten. Da diese von der DispatcherServlet ausgeführt werden und dies nach dem Autorisierungsfilter erfolgt, müssen die Endpunkte in `authorizeHttpRequests` aufgenommen werden, um zugelassen zu werden.

`authorizeHttpRequests` ist der zentrale Punkt, an dem die Anforderungen für die Autorisierung der HTTP-Anfragen definiert werden. Hier wird angegeben, welche Pfade (Endpunkte) von welchen Benutzern aufgerufen werden dürfen. Einige Endpunkte, wie "/api/auth/login", "/api/auth/register" usw., werden für alle Benutzer (permitAll) zugänglich gemacht, während anyRequest().authenticated() sicherstellt, dass alle anderen Anfragen eine Authentifizierung erfordern.

### Logging

Logging ist ein nützliches Instrument, um die Leistung und das Verhalten Ihrer Anwendung zu überwachen, Fehler zu diagnostizieren und zu beheben sowie Sicherheitsbedenken anzugehen.

`LoggingAspect` ist ein Aspekt, der die Protokollierung von Methodenaufrufen ermöglicht. Dieser Aspekt verwendet Spring AOP und definiert einen "Advice", der nach einem erfolgreichen Methodenaufruf in den Controllern ausgeführt wird. Dieser Advice ruft `logMethodCall` auf für die Informationserfassung. Diese Informationen werden dann in der Datenbank gespeichert, indem ein Eintrag in `LogEntry` erstellt und über das `LogEntryRepository` persistiert wird.

`LogEntry` wird verwendet, um die Informationen für jeden Protokolleintrag darzustellen. Sie beinhaltet Attribute wie den Benutzernamen, den Endpunkt, den Zeitstempel und den Namen der aufgerufenen Methode.

`LogEntryRepository` ist ein Spring Data JPA-Repository, das die Kommunikation mit der Datenbank für Log-Einträge erleichtert. Mit diesem Repositories können leicht Log-Einträge erstellt, abgerufen, aktualisiert und gelöscht.

## Datenbank

In dieser Applikation sind zwei verschiedene Datenbanken angebunden. Zum einen eine für die Sicherung der Anmeldedaten, welche die Website betrifft, anderereits eine extern angebundene, welche unabhängig Daten speichert. Beide Datenbanken werden mittels der Spring Boot-Anwendung angerufen.

### MariaDB

Wie bereits erwähnt beinhalter diese Datenbank die Anmeldedaten, welche erforderlich für die Funktionalität der Website sind. Sie steht im ständigen Austausch mit den Anfragen der Website. Standardmässig sind alle Besucher der Website automatisch teilsberechtigt für gewisse Aktionen, wie beispielsweise der zugriff auf die Home Seite.

Bei der Anmeldung oder Regitstierung eines Users wird die DB angefragt. Bei der Anmeldung wird überprüft, ob ein User mit den eingegebenen Parametern existiert und falls dies eintrifft, wird dieser eingeloggt. Es werden im gleichen Zug aber noch weitere Daten zurückgegeben, wie beispielsweise die Berechtigungen des Users bzw. welchen Rang dieser besitzt. Des Weiteren wird ebenfalls der AccessToken des Users gesendet. Diese Daten sind unter dem persönlichen Profil des Users ersichtlich, welches erst ersichtlich wird, sofern ein User vorhandne ist.

![website_user_profile.jpeg](documentation%2Fwebsite_user_profile.jpeg)

### InfluxDB

Diese Datenbank ist, wie bereits erwähnt, extern angeschlossen. Sie agiert sälbstständig und die gespeicherten Daten werden ebenfalls eigenständig erhoben und gewartet. Sprich diese DB kann nicht in oder von der Website verändert werden. Sie dient lediglich als Darstellung dieser Daten.
In diesem Fall handelt es sich um einen Datensatz von verschiedenen Zeitpunkten, welche die Temperatur und die Feuchtigkeit einer Messstelle beinhalten. Mittels Formatierung können diese Daten in einem Graphen ersichtlich dargestellet werden. Dies geschieht auf der Seite Data View, welche ebenfalls nur durch einen authentifizierten User zugänglich ist.

![website_data_view.jpeg](documentation%2Fwebsite_data_view.jpeg)

## TestCases

Um sicher zu stellen, dass die Applikation auch wirklich die korrekt funktioniert ist ein Pool aus verschiedenen Test unersetzlich. Anstelle einer Testauflistung in dieser Dokumentation, haben wir uns dafür entschieden, Tests in der Applikation selbst uz schreiben und hier lediglich die Verlinkung zu diesen Tests und Auswertungen anzugeben.

Hier sind die Dokumentationen für die einzelnen API-Endpunkte:

[DefaultApi.md](src%2Fmain%2Fgen%2Fdocs%2FDefaultApi.md)<br>
[FluxRecord.md](src%2Fmain%2Fgen%2Fdocs%2FFluxRecord.md)<br>
[ForgotPasswordRequest.md](src%2Fmain%2Fgen%2Fdocs%2FForgotPasswordRequest.md)<br>
[LoginRequest.md](src%2Fmain%2Fgen%2Fdocs%2FLoginRequest.md)<br>
[RegisterRequest.md](src%2Fmain%2Fgen%2Fdocs%2FRegisterRequest.md)<br>
[ResetPasswordRequest.md](src%2Fmain%2Fgen%2Fdocs%2FResetPasswordRequest.md)<br>
[Role.md](src%2Fmain%2Fgen%2Fdocs%2FRole.md)<br>
[TokenRefreshRequest.md](src%2Fmain%2Fgen%2Fdocs%2FTokenRefreshRequest.md)<br>
[User.md](src%2Fmain%2Fgen%2Fdocs%2FUser.md)<br>
[UserStatus.md](src%2Fmain%2Fgen%2Fdocs%2FUserStatus.md)<br>
[VerifyRequest.md](src%2Fmain%2Fgen%2Fdocs%2FVerifyRequest.md)<br>

Hier sind die Tests für die einzelnen API-Endpunkte:

[FluxRecordTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FFluxRecordTest.java)<br>
[ForgotPasswordRequestTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FForgotPasswordRequestTest.java)<br>
[LoginRequestTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FLoginRequestTest.java)<br>
[RegisterRequestTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FRegisterRequestTest.java)<br>
[ResetPasswordRequestTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FResetPasswordRequestTest.java)<br>
[RoleTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FRoleTest.java)<br>
[TokenRefreshRequestTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FTokenRefreshRequestTest.java)<br>
[UserStatusTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FUserStatusTest.java)<br>
[UserTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FUserTest.java)<br>
[VerifyRequestTest.java](src%2Fmain%2Fgen%2Fsrc%2Ftest%2Fjava%2Forg%2Fopenapitools%2Fclient%2Fmodel%2FVerifyRequestTest.java)<br>

# Security Tests

## Metasploit

Metasploit ist ein weit verbreitetes Open-Source-Framework für Penetrationtests, Sicherheitsforschung und - entwicklung. Es bietet eine Platform, mit der Sicherheitsfachleute Schwachstellen in Computersystemen identifizieren, ausnutzen und patchen können.

### Exploits und Payloads

Metasploit bietet eine grosse Sammlung von Exploits für verschiedene Schwachstellen in Softwareanwendungen. Ein Exploit ist ein Programm oder eine Technik, die eine Schwachstelle in einem Computersystem ausnutzt.
Payloads sind Nutzlasten, die nach einer erfolgreichen Ausnutzung einer Schwachstelle ausgeführt werden, um einen Angriff auf das System zu gewähren.

### Modularität

Metasploit ist modular aufgebaut, was bedeutet, dass es in einzelne Module unterteilt ist, welche unabhängig voneinander arbeiten können.

### Frameworks und Penetrationtests

Metasploit wird häufig in Penetrationtests und Sicherheitsaudits eingesetzt, um die Sicherheit von Systemen zu bewerten. Sicherheitsfachleute können es verwenden, um Schwachstellen zu identifizieren, Sicherheitslpcken auszunutzen und Systeme auf ihre Resistenz gegenüber Angriffen zu prüfen.

### Community und Entwicklung

Die Community von Metasploit spielt eie eine wichtige Rolle bei der Weiterentwicklung des Frameworks. Durch die Beteiligung von Sicherheitsforschern und Entwicklern weltweit wird Metasploit kontinuierlich aktualisiert, um die neusten Sicherheitsanforderungen zu erfüllen.

### Meterpreter

Der Meterpreter ist ein Payload, der oft in Verbindung mit Metasploit-Exploits verwendet wird. Es ermöglichst den Angreiffern, eine Shell auf dem Zielrechner zu starten und verschiedene Aktionen auszuführen. Beispielsweise können Dateien komprimiert, Screenshots gemacht oder die Kontrolle über das System übernommen werden.


=======
## NMAP
### Was ist NMAP?
Nmap (Network Mapper) ist ein Open-Source-Tool für die Netzwerkerkennung und Sicherheitsprüfung. Es kann verwendet werden, um Hosts und Dienste in einem Computernetzwerk zu entdecken, sowie Informationen über diese zu sammeln. Nmap bietet verschiedene Scan-Techniken, einschließlich Ping-Scans, Port-Scans und Service-Version-Scans, um umfassende Informationen über ein Netzwerk zu erhalten.

### Ping Scan
**Befehl: 'nmap -sn furchert.ch'**

**Ergebnis:**
```
 Starting Nmap 7.94 ( https://nmap.org ) at 2023-12-20 10:33 CET
 Nmap scan report for furchert.ch (84.74.80.106)
 Host is up (0.028s latency).
 rDNS record for 84.74.80.106: 84-74-80-106.dclient.hispeed.ch
 Nmap done: 1 IP address (1 host up) scanned in 6.58 seconds
```
Der Ping-Scan zeigt an, dass das Host "furchert.ch" erreichbar ist, mit einer Latenz von 0.028 Sekunden. Die IP-Adresse des Hosts ist 84.74.80.106.

### Port Scan
**Befehl: 'nmap furchert.ch'**

**Ergebnis:**
```
 Starting Nmap 7.94 ( https://nmap.org ) at 2023-12-20 10:34 CET
 Nmap scan report for furchert.ch (84.74.80.106)
 Host is up (0.018s latency).
 rDNS record for 84.74.80.106: 84-74-80-106.dclient.hispeed.ch
 Not shown: 967 filtered tcp ports (no-response)
 PORT      STATE  SERVICE
 21/tcp    open   ftp
 80/tcp    open   http
 113/tcp   closed ident
 443/tcp   open   https
 8008/tcp  open   http
 15000/tcp open   hydap
 ... (weitere offene Ports)
 Nmap done: 1 IP address (1 host up) scanned in 73.90 seconds
```
Der Port-Scan zeigt, dass verschiedene Ports auf dem Host geöffnet sind, darunter FTP (21/tcp), HTTP (80/tcp), HTTPS (443/tcp), und mehr. Einige Ports zeigen den Service "tcpwrapped" an, was darauf hindeutet, dass Nmap den genauen Dienst nicht identifizieren kann.

### Service Version Scan
**Befehl: 'nmap -sV furchert.ch'**

**Ergebnis:**
```
 Nmap scan report for furchert.ch (84.74.80.106)
 Host is up (0.017s latency).
 Not shown: 968 filtered tcp ports (no-response)
 PORT      STATE SERVICE    VERSION
 21/tcp    open  ftp-proxy  McAfee Web Gateway ftp proxy 11.2.5 - build: 42905
 80/tcp    open  rtsp
 443/tcp   open  ssl/http   nginx 1.25.3
 8008/tcp  open  http
 15000/tcp open  tcpwrapped
 ... (weitere Service-Versionen)
 2 services unrecognized despite returning data.
```
Der Service-Version-Scan identifiziert bestimmte Dienste und ihre Versionen auf offenen Ports. Zum Beispiel wird der Port 21/tcp als "ftp-proxy" mit der Version "McAfee Web Gateway ftp proxy 11.2.5 - build: 42905" identifiziert. Der Port 80/tcp zeigt einen HTTP-Dienst an, der von Nginx (Version 1.25.3) gehostet wird.

### Aggressiver Scan
**Befehl: 'nmap -A furchert.ch'**

**Ergebnis:**
```
 Nmap scan report for furchert.ch (84.74.80.106)
 Host is up (0.013s latency).
 Not shown: 967 filtered tcp ports (no-response)
 PORT      STATE  SERVICE    VERSION
 21/tcp    open   ftp-proxy  McAfee Web Gateway ftp proxy 11.2.5 - build: 42905
 80/tcp    open   rtsp
 113/tcp   closed ident
 443/tcp   open   ssl/http   nginx 1.25.3
 8008/tcp  open   http
 15000/tcp open   tcpwrapped
 ... (weitere Ergebnisse)
 2 services unrecognized despite returning data.
```
Der aggressive Scan führt eine umfassendere Untersuchung durch und versucht, zusätzliche Informationen wie Betriebssystemdetails und Traceroute-Ergebnisse zu liefern. Es werden bestimmte Dienste und ihre Versionen identifiziert, darunter HTTP (Nginx 1.25.3), und es gibt Hinweise auf mögliche Sicherheitsmechanismen wie X-Frame-Options.

## ZAP
### Was ist ZAP?
ZAP (Zed Attack Proxy) ist ein Open-Source-Sicherheitswerkzeug, das von der OWASP-Community entwickelt wurde. Es ist darauf ausgerichtet, Webanwendungen auf Sicherheitslücken zu überprüfen und bietet Funktionen für automatisierte Sicherheitsanalysen sowie für manuelle Tests.

### Warum ZAP?
Die Verwendung von Zed Attack Proxy (ZAP) für unsere Applikation ermöglicht automatisierte Sicherheitsscans, um potenzielle Schwachstellen wie SQL-Injektionen und Cross-Site Scripting zu identifizieren. Die interaktive Proxy-Funktionalität erlaubt auch manuelle Sicherheitstests, während die REST-API die Integration von automatisierten Tests in CI/CD-Pipelines erleichtert. ZAP bietet detaillierte Berichte über gefundenen Schwachstellen, unterstützt die Dokumentation von Sicherheitsprüfungen und trägt zu einem proaktiven Sicherheitsansatz bei. Die Open-Source-Natur von ZAP fördert die kontinuierliche Verbesserung und würde uns theoretisch eine kostengünstige Implementierung ermöglichen.

### Auswertung
**Cloud Metadata Potentially Exposed**
#### Beschreibung
The Cloud Metadata Attack attempts to abuse a misconfigured NGINX server in order to access the instance metadata maintained by cloud service providers such as AWS, GCP and Azure. All of these providers provide metadata via an internal unroutable IP address '169.254.169.254' - this can be exposed by incorrectly configured NGINX servers and accessed by using this IP address in the Host header field.

#### Zusätzliche Informationen
Based on the successful response status code cloud metadata may have been returned in the response. Check the response data to see if any cloud metadata has been returned. The meta data returned can include information that would allow an attacker to completely compromise the system.

#### Lösung
Do not trust any user data in NGINX configs. In this case it is probably the use of the $host variable which is set from the 'Host' header and can be controlled by an attacker.

#### URL
```
https://furchert.ch/latest/meta-data/
```
---
**Content Security Policy (CSP) Header not set**
#### Beschreibung
Content Security Policy (CSP) is an added layer of security that helps to detect and mitigate certain types of attacks, including Cross Site Scripting (XSS) and data injection attacks. These attacks are used for everything from data theft to site defacement or distribution of malware. CSP provides a set of standard HTTP headers that allow website owners to declare approved sources of content that browsers should be allowed to load on that page — covered types are JavaScript, CSS, HTML frames, fonts, images and embeddable objects such as Java applets, ActiveX, audio and video files.

#### Lösung
Ensure that your web server, application server, load balancer, etc. is configured to set the Content-Security-Policy header.

#### URL
```
https://furchert.ch/sitemap.xml
https://furchert.ch
```
---
**Missing Anti-clickjacking Header**
#### Beschreibung
The response does not include either Content-Security-Policy with 'frame-ancestors' directive or X-Frame-Options to protect against 'ClickJacking' attacks.

#### Lösung
Modern Web browsers support the Content-Security-Policy and X-Frame-Options HTTP headers. Ensure one of them is set on all web pages returned by your site/app.
If you expect the page to be framed only by pages on your server (e.g. it's part of a FRAMESET) then you'll want to use SAMEORIGIN, otherwise if you never expect the page to be framed, you should use DENY. Alternatively consider implementing Content Security Policy's "frame-ancestors" directive.

#### URL
```
https://furchert.ch
https://furchert.ch/sitemap.xml
```
---
**Server Leaks Version Information via "Server" HTTP Response Header Field**
#### Beschreibung
The web/application server is leaking version information via the "Server" HTTP response header. Access to such information may facilitate attackers identifying other vulnerabilities your web/application server is subject to.

#### Lösung
Ensure that your web server, application server, load balancer, etc. is configured to suppress the "Server" header or provide generic details.

#### URL
```
https://furchert.ch
https://furchert.ch/api
https://furchert.ch/api/
https://furchert.ch/favicon.ico
https://furchert.ch/logo192.png
https://furchert.ch/manifest.json
https://furchert.ch/robots.txt
https://furchert.ch/sitemap.xml
https://furchert.ch/static/css/main.29cfba1e.css
https://furchert.ch/static/js/main.062e310a.js
```
---
**Strict-Transport-Security Header Not Set**

#### Beschreibung
HTTP Strict Transport Security (HSTS) is a web security policy mechanism whereby a web server declares that complying user agents (such as a web browser) are to interact with it using only secure HTTPS connections (i.e. HTTP layered over TLS/SSL). HSTS is an IETF standards track protocol and is specified in RFC 6797.

#### Lösung
Ensure that your web server, application server, load balancer, etc. is configured to enforce Strict-Transport-Security.

#### URL
```
https://furchert.ch
https://furchert.ch/favicon.ico
https://furchert.ch/logo192.png
https://furchert.ch/manifest.json
https://furchert.ch/robots.txt
https://furchert.ch/sitemap.xml
https://furchert.ch/static/css/main.29cfba1e.css
https://furchert.ch/static/js/main.062e310a.js
```
---
**X-Content-Type-Options Header Missing**
#### Beschreibung
The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), rather than performing MIME-sniffing.

#### Zusätzliche Informationen
This issue still applies to error type pages (401, 403, 500, etc.) as those pages are often still affected by injection issues, in which case there is still concern for browsers sniffing pages away from their actual content type.
At "High" threshold this scan rule will not alert on client or server error responses.

#### Lösung
Ensure that the application/web server sets the Content-Type header appropriately, and that it sets the X-Content-Type-Options header to 'nosniff' for all web pages.
If possible, ensure that the end user uses a standards-compliant and modern web browser that does not perform MIME-sniffing at all, or that can be directed by the web application/web server to not perform MIME-sniffing.

#### URL
```
https://furchert.ch
https://furchert.ch/favicon.ico
https://furchert.ch/logo192.png
https://furchert.ch/manifest.json
https://furchert.ch/robots.txt
https://furchert.ch/sitemap.xml
https://furchert.ch/static/css/main.29cfba1e.css
https://furchert.ch/static/js/main.062e310a.js
```
---

# Issues

## JWT und Sessionhandling

In unserer Applikation haben wir den JWT für das Sessionhandling verwendet. Obwohl das Programm damit reibungslos funktioniert, ist dies nicht die optimale Lösung. Der JWT kann von der Applikation nämlich als Baerer Token angesehen werden. Desswegen entspricht er nicht den spezifischen Anforderungen gemäss den Spezifikationen von OAuth 2.0. Dies legt fest, dass Baerer Token im Authorization-HTTP-Header mit dem Baerer-Authentifizierungsschema verwendet werden sollten.

Die Verwendung des JSON Web Token zur Verhinderung von Cross-Site Request Forgery ist ohne genaue Details schwer zu bewerten. Um einen geeigneten Schutz gegen CSRFs wäre besser ein OAuth-Token zusammen mit OAuth 2.0 verwendet worden. Aufgrund mangelnder Zeit und eine komplette Umstrukturierung des Sessionhandlings zu vermeinden, haben wir uns gegen eine Anpassung auf diesen Standard entschieden.

![jwt-flowchart.png](documentation%2Fjwt-flowchart.png)

## Bootstrap und CSS-Formatierung

In unserer Applikation haben wir das Responsiv-Webdesign anfänglich mit Bootstrap aufziehen wollen, sind dabei aber auf Probleme gestossen. Bootstrap verwendet sehr spezifische CSS-Selektoren, um seinen eigenen Stiel zu definieren. Diese Selektoren überschreiben häuffig den allgemeinen CSS-Code, der von uns geschrieben wurde. Um dies zu verhindern, hätten wir die sehr spezifischen Bootstrap-Selektoren verwenden müssen um Bootstrap selbst zu überschreiben.

Diese manuellen Anpassungen in Bootsrtap würden ebenfalls zu weiteren Problemen führen. Ist beispielsweise eine neue Version von Bootstrap vorhanden und wird eingebungen, wären die manuellen Anpassungen eventuell nicht mehr mit der neuen Version kompatibel und müssten angepasst werden, sofern diese dann überhaupt noch zu verwenden sind.

---

# Work in Progress Notizen

## Project Structure

Aufbau gemäss [Best Practices](https://medium.com/the-resonant-web/spring-boot-2-0-project-structure-and-best-practices-part-2-7137bdcba7d3)</br>,

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

## Anleitungen

- [Full tutorial](https://spring.io/guides/tutorials/react-and-spring-data-rest/)
- [outdated but schematic tutorial (***Ehrenbre***)](https://www.bezkoder.com/spring-boot-react-jwt-auth/)




See class `WebSecurityConfig` for the configuration of the security filter chain.

- [The Security Filter Chain](https://kasunprageethdissanayake.medium.com/spring-security-the-security-filter-chain-2e399a1cb8e3)
- [Request Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
- https://docs.spring.io/spring-security/site/docs/5.5.x/guides/form-javaconfig.html


## Spring MVC

- [Request Mapping](https://www.baeldung.com/spring-requestmapping)
- [Redirect after Login or Registration](https://www.baeldung.com/spring-redirect-after-login)

## React

- [From validation](https://www.bezkoder.com/react-form-validation-hooks/)
