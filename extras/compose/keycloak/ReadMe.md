# Hinweise zu Keycloak als "Authorization Server"

<!--
  Copyright (C) 2024 - present Juergen Zimmermann, Hochschule Karlsruhe

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
-->

[Juergen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)

## Inhalt

- [JWT](#jwt)
- [Installation](#installation)
- [Konfiguration](#konfiguration)
- [Client Secret](#client-secret)
- [Initial Access Token](#initial-access-token)
- [Token über die REST-Schnittstelle von Keycloak](#token-über-die-rest-schnittstelle-von-keycloak)
- [Ergänzung des eigenen Server-Projekts](#ergänzung-des-eigenen-server-projekts)
- [Inspektion der H2-Datenbank von Keycloak](#inspektion-der-h2-datenbank-von-keycloak)


## JWT

Ein _JWT_ (= JSON Web Token) ist ein codiertes JSON-Objekt, das Informationen zu
einem authentifizierten Benutzer enthält. Ein JWT kann verfiziert werden, da er
digital signiert ist. Mit der URL `https://jwt.io` kann ein JWT in seine Bestandteile
decodiert werden:

- Algorithm
- Payload
- Signature

## Installation

_Keycloak_ wird als Docker Container gestartet, wofür das Verzeichnis
`C:\Zimmermann\volumes\keycloak` vorhanden sein und ggf. angelegt werden muss:

```powershell
    cd extras\compose\keycloak
    docker compose up
```

In `compose.yml` sind unterhalb von `environment:` der temporäre Administrator
mit Benutzername und Passwort konfiguriert, und zwar Benutzername `tmp` und
Passwort `p`.

Außerdem sind die Umgebungsvariablen für die beiden Dateien für den privatem
Schlüssel und das Zertifikat gesetzt, so dass Keycloak wahlweise über
`https://localhost:8843` oder `http://localhost:8880` aufgerufen werden kann.

## Konfiguration

Nachdem Keycloak als Container gestartet ist, sind folgende umfangreiche
Konfigurationsschritte _sorgfältig_ durchzuführen, nachdem man in einem
Webbrowser `https://localhost:8843` oder `http://localhost:8880` aufgerufen hat:

```text
    Username    tmp
    Password    p
        siehe .env in extras\compose\keycloak

    Menüpunkt "Users"
        Button <Add user> anklicken
            Username    admin
            Email       admin@acme.com
            First name  Keycloak
            Last name   Admin
            <Create> anklicken
        Tab "Credentials" anklicken
            Button <Set password> anklicken
                Password                p
                Password confirmation   p
                Temporary               Off
                Button <Save> anklicken
                Button <Save password> anklicken
        Tab "Role mapping" anklicken
            Button <Assign role> anklicken
                Drop-Down-Menü "Filter by realm roles" auswählen
                Checkbox "admin" anklicken
                Button <Assign> anklicken
        Drop-Down-Menü in der rechten oberen Ecke
            "Sign-Out" anklicken

    Einloggen
        Username    admin
        Password    p

    Menüpunkt "Manage realms" anklicken
        Button <Create realm> anklicken
            Realm name      spring
            <Create> anklicken

    Menüpunkt "Clients"
        <Create client> anklicken
        Client ID   spring-client
        Name        Spring Client
        <Next>
            "Capability config"
                Client authentication       On
                Authorization               Off
                Authentication Flow         Standard flow                   Haken setzen
                                            Direct access grants            Haken setzen
        <Next>
            Root URL                https://localhost:8443
            Valid redirect URIs     *
        <Save>

        spring-client
            Tab "Roles"
                <Create Role> anklicken
                Role name       ADMIN
                <Save> anklicken
            Breadcrumb "Client details" anklicken
            Tab "Roles"
                <Create Role> anklicken
                Role name       USER
                <Save> anklicken

    # https://www.keycloak.org/docs/latest/server_admin/index.html#assigning-permissions-using-roles-and-groups
    Menüpunkt "Users"
        <Add user>
            Required User Actions:      Überprüfen, dass nichts ausgewählt ist
            Username                    admin
            Email                       admin@acme.com
            First name                  Spring
            Last name                   Admin
            <Create> anklicken
            Tab "Credentials"
                <Set password> anklicken
                    "p" eingeben und wiederholen
                    "Temporary" auf "Off" setzen
                    <Save> anklicken
                    <Save password> anklicken
            Tab "Role Mapping"
                <Assign Role> anklicken
                    "Filter by clients" auswählen
                        "ADMIN"         Haken setzen     (ggf. blättern)
                        <Assign> anklicken
            Tab "Details"
                Required user actions       Überprüfen, dass nichts ausgewählt ist
                <Save> anklicken
    Menüpunkt "Users"
        <Add user>
            Required User Actions:      Überprüfen, dass nichts ausgewählt ist
            Username                    user
            Email                       user@acme.com
            First name                  Spring
            Last name                   User
            <Create> anklicken
            Tab "Credentials"
                <Set password> anklicken
                    "p" eingeben und wiederholen
                    "Temporary" auf "Off" setzen
                    <Save> anklicken
                    <Save password> anklicken
            Tab "Role Mapping"
                <Assign Role> anklicken
                    "Filter by clients" auswählen
                        "USER"          Haken setzen     (ggf. blättern)
                        <Assign> anklicken
            Tab "Details"
                Required user actions       Überprüfen, dass nichts ausgewählt ist
                <Save> anklicken
        Breadcrumb "Users" anklicken
            WICHTIG: "admin" und "user" mit der jeweiligen Emailadresse sind aufgelistet

    Menüpunkt "Realm settings"
        Tab "Sessions"
            # Refresh Token: siehe https://stackoverflow.com/questions/52040265/how-to-specify-refresh-tokens-lifespan-in-keycloak
            SSO Session Idle                                1 Hours
            <Save> anklicken
        Tab "Tokens"
            Access Tokens
                Access Token Lifespan                       30 Minutes
                Access Token Lifespan For Implicit Flow     30 Minutes
                <Save> anklicken
```

Mit der URL `https://localhost:8843/realms/spring/.well-known/openid-configuration`
kann man in einem Webbrowser die Konfiguration als JSON-Datensatz erhalten.

Die zugehörige Basis-URL `https://localhost:8443/realms/spring` wird für Gradle in
`gradle.properties` beim Schlüssel `app.keycloak.issuer-uri` bzw. für Maven in
`settings.xml` beim Tag `app.keycloak.issuer-uri` eingetragen, damit Spring Boot
für den Microservice die Client-Konfiguration für OAuth2 erstellen kann, wozu
der Endpunkt `.well-known/openid-configuration` verwendet wird.

## Client Secret

Im Wurzelverzeichnis des Projekts in der Datei `.env` muss man die
Umgebungsvariable `CLIENT_SECRET` auf folgenden Wert aus _Keycloak_ setzen:

- Menüpunkt `Clients`
- `spring-client` aus der Liste beim voreingestellten Tab `Clients list` auswählen
- Tab `Credentials` anklicken
- Die Zeichenkette beim Label `Client Secret` kopieren.

Diese Zeichenkette muss man für Gradle in `gradle.properties` beim Schlüssel
`app.keycloak.client-secret` bzw. für Maven in `settings.xml` beim Tag
`app.keycloak.client-secret` eintragen.

Auch in Postman ist der Wert für die dortige Umgebungsvariable `client_secret` einzutragen.

## Initial Access Token

Ein _Initial Access Token_ für z.B. _Postman_ wurde bei der obigen Konfiguration
für _Keycloak_ folgendermaßen erzeugt:

- Menüpunkt `Clients`
- Tab `Initial access token` anklicken
- Button `Create` anklicken und eine hinreichend lange Gültigkeitsdauer einstellen.

## Token über die REST-Schnittstelle von Keycloak

Ein _JWT_ (= JSON Web Token) ist ein codiertes JSON-Objekt, das Informationen zu
einem authentifizierten Benutzer enthält (s.u.). Mit Postman kann man einen _JWT_
von Keycloak anfordern. Dazu konfiguriert man folgendermaßen einen Request in Postman:

- _POST_ als HTTP-Methode einstellen
- Als URI `https://localhost:8843/realms/spring/protocol/openid-connect/token` verwenden
- Beim Tab _Body_ den Radio-Button _x-www-form-urlencoded_ für den entsprechenden
  Content-Type anklicken.
- Im Body folgende Schlüssel und die zugehörigen Werte eingeben:
  - `username` mit z.B. `admin`
  - `password` mit z.B. `p`
  - `grant_type` mit dem Wert `password`
  - `client_id` mit z.B. `spring-client` (s.o.)
  - `client_secret` mit dem Wert aus dem obigen Abschnitt [Client Secret](#client-secret)

Wenn man nun den Request abgeschickt hat, kann man aus dem Response-Body den Wert
zu `access_token` verwenden und bei `https://jwt.io` in seine Bestandteile
decodieren lassen:

- Algorithm, i.a. RS256
- Payload bzw. Nutzdaten
- Signature zur Verifikation

## Ergänzung des eigenen Server-Projekts

### Gradle

Für _Gradle_ muss man in `gradle.properties` im Abschnitt zu den Spring Modulen
den Eintrag für `security` auskommentieren (oder auf `true`) setzen. Außerdem muss
man im Abschnitt zu Keycloak folgende Properties setzen:

- `app.keycloak.client-secret=`<s.o. bei [Client Secret](#client-secret)>
- `app.keycloak.host=localhost`
- `app.keycloak.port=8880`
- `app.keycloak.issuer-uri=http://localhost:8880/realms/spring`

### Maven

Für _Maven_ muss man in `~\.m2\settings.xml` das Tag `activeProfile` mit dem Wert
`security` aktivieren bzw. den Kommentar entfernen. Außerdem muss man beim _Profile_
`security` folgende Tags innerhalb der Properties setzen:

```
    <app.keycloak.client-secret>s.o. bei "Client Secret"</app.keycloak.client-secret>
    <app.keycloak.host>localhost</app.keycloak.host>
    <app.keycloak.port>8880</app.keycloak.port>
    <app.keycloak.issuer-uri>http://localhost:8880/realms/spring</app.keycloak.issuer-uri>
```

### application.yaml für Spring

Die Properties für `com.c4-soft.springaddons.oidc` müssen gesetzt sein, d.h.

- `ops` und
- `resourceserver.permit-all`

Außerdem muss bei `security.oauth2.resourceserver.jwt` die untergeordnete Property
`issuer-uri` auf `${app.keycloak.issuer-uri}` gesetzt sein (s.o. bei Gradle bzw.
Maven).

### Java

Im Record `KeycloakProps` sind Properties mit Defaultwerten für den eigenen Server
bereitgestellt. Diese Properties werden berücksichtigt, wenn in der Klasse `Application`
im Wurzel-Package bei der Annotation `@EnableConfigurationProperties` auch diese
Klasse `KeycloakProps` angegeben wird. Die Prperties in `KeycloakProps` können
durch die "Systemproperties" aus `gradle.properties` bzw. `settings.xml` (s.o.)
gesetzt oder überschrieben werden.

Im Interface `KeycloakRepository` im Package `com.acme.flug.security` sind die Methoden
(-köpfe) deklariert, mit denen man

- die Konfiguration von OIDC (= OpenID Connect) bei Spring per GET-Request abfragen kann und
- einen Token zu gegebenem Benutzernamen und Passwort abfragen kann, die als String
  `username=...&password=...&grant_type=password&client_id=spring-client&client_secret=...`
  übergeben werden müssen.

In der Klasse `SecurityConfig` sind die Beans bzw. Factory-Methoden für

- den programmatischen Zugriff auf die REST-Schnittstelle von Keycloak durch
  das Interface `KeycloakRepository`,
- die Absicherung von URIs aus Bibliotheken wie z.B. Swagger und Actuator und
- ggf. die Überprüfung, ob ein neu anzulegender User ein bereits "gehacktes"
  Passwort verwendet.

In der Klasse `AuthController` ist eine REST-Schnittstelle implementiert, mit
der man durch einen POST-Request mit dem Pfad `/auth/token` einen _Access Token_
und einen _Refresh Token_ direkt vom eigenen Server anfordern kann, falls im
Request-Body ein JSON-Datensatz mit den Properties `username` und `password`
mitgeschickt wird. Der JSON-Datensatz im Response-Body wird durch Jackson
ausgehend von einem Rückgabe-Objekt des records `TokenDTO` geliefert.

Nun kann man eigene Methoden in einer Controller- oder einer Service-Klasse schützen,
indem man z.B. die Annotation `@PreAuthorized(("hasRole('ADMIN')"))` oder
`@PreAuthorize("hasAnyRole('ADMIN', 'USER')")` verwenden. Zur Vereinfachung kann
man aber auch `@RoleAdmin` bzw. `@RoleAdminOrUser` verwenden, was in den gleichnamigen
Dateien implementiert ist.

## Postman

Wenn man mit Postman auf einen Endpunkt zugreifen will, wofür ein JWT für OAuth 2.0
benötigt wird, muss man folgendermaßen vorgehen:

- Beim Request, Folder oder Collection wählt man den Karteireiter _Authorization_ aus.
- Im Drop-Down-Menü für _Auth Type_ wählt man den Eintrag _OAuth 2.0_ aus.
- Bei _Auth URL_ trägt man `https://localhost:8843/realms/spring/protocol/openid-connect/auth` ein.
  Das bei einem GET-Request mit `https://localhost:8843/realms/spring/.well-known/openid-configuration`
  der Wert beim Schlüssel `authorization_endpoint`.
- Bei _Access Token URL_ trägt man `https://localhost:8843/realms/spring/protocol/openid-connect/token` ein.
  Das bei einem GET-Request mit `https://localhost:8843/realms/spring/.well-known/openid-configuration`
  der Wert beim Schlüssel `token_endpoint`.
- Bei _Client ID_ trägt man die Postman-Umgebungsvariable `{{client_id}}` oder `spring-client` ein.
- Bei _Client Secret_ trägt man die Postman-Umgebungsvariable `{{client_secret}}` ein oder den
  Wert bei Keycloak: Clients > spring-client > Tab Credentials > Client Secret
- Nun klickt man ganz unten auf den Button _Get New Access Token_.
- Jetzt erscheint ein Login-Fenster für Keycloak für den dortigen Benutzernamen sowie das Passwort,
  z.B. `admin` und `p`.
- Dann klickt man auf den Button _Sign In_, um sich bei Keycloak einzuloggen.
- Abschließend klickt man der Reihe nach auf die Buttons _Proceed_ und _Use Token_.

## Inspektion der H2-Datenbank von Keycloak

Im Development-Modus verwaltet Keycloak seine Daten in einer H2-Datenbank. Um
die _H2 Console_ als DB-Browser zu starten, lädt man zunächst die JAR-Datei
von `https://repo.maven.apache.org/maven2/com/h2database/h2/2.3.230/h2-2.3.230.jar`.
herunter und speichert sie z.B. im Verzeichnis `extras\compose\keycloak`.

Mit dem Kommando `java -jar h2-2.3.230.jar` startet man nun die H2 Console, wobei
ein Webbrowser gestartet wird. Dort gibt man folgende Werte ein:

- JDBC URL: `jdbc:h2:tcp://localhost/C:/Zimmermann/volumes/keycloak/data/h2/keycloakdb`
- Benutzername: ``
- Passwort: ``

Danach kann man z.B. die Tabellen `USER_ENTITY` und `USER_ROLE_MAPPING` inspizieren.

**VORSICHT: AUF KEINEN FALL IRGENDEINE TABELLE EDITIEREN, WEIL MAN SONST
KEYCLOAK NEU AUFSETZEN MUSS!**
