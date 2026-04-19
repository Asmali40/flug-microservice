# Hinweise zum Programmierbeispiel

<!--
  Copyright (C) 2020 - present Juergen Zimmermann, Hochschule Karlsruhe

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

> Bevor man mit der Projektarbeit an der 2. Abgabe beginnt, sichert man sich
> die 1. Abgabe, u.a. weil für die 2. Abgabe auch die Original-Implementierung
> aus der 1. Abgabe benötigt wird.

Inhalt

- [Eigener Namespace in Kubernetes](#eigener-namespace-in-kubernetes)
- [Relationale Datenbanksysteme](#relationale-datenbanksysteme)
- [Übersetzung und lokale Ausführung](#übersetzung-und-lokale-ausführung)
  - [Ausführung in IntelliJ IDEA](#ausführung-in-intellij-idea)
  - [Start und Stop des Servers in der Kommandozeile](#start-und-stop-des-servers-in-der-kommandozeile)
  - [Image erstellen](#image-erstellen)
- [Postman](#postman)
- [OpenAPI mit Swagger](#openapi-mit-swagger)
- [Unit Tests und Integrationstests](#unit-tests-und-integrationstests)
- [Rechnername in der Datei hosts](#rechnername-in-der-datei-hosts)
- [Kubernetes, Helm und Terraform](#kubernetes-helm-und-terraform)
  - [WICHTIG: Schreibrechte für die Logdatei](#wichtig-schreibrechte-für-die-logdatei)
  - [Helm als Package Manager für Kubernetes](#helm-als-package-manager-für-kubernetes)
  - [Bereitstellung mit Terraform und Port Forwarding](#bereitstellung-mit-terraform-und-port-forwarding)
  - [Bereitstellung mit Pulumi und Port Forwarding](#bereitstellung-mit-pulumi-und-port-forwarding)
  - [kubectl top](#kubectl-top)
  - [Validierung der Installation](#validierung-der-installation)
- [Statische Codeanalyse](#statische-codeanalyse)
  - [Checkstyle und SpotBugs](#checkstyle-und-spotbugs)
  - [SonarQube](#sonarqube)
- [Analyse von Sicherheitslücken](#analyse-von-sicherheitslücken)
  - [OWASP Security Check](#owasp-security-check)
  - [Docker Scout](#docker-scout)
  - [Trivy von Aquasec](#trivy-von-aquasec)
- [Dokumentation](#dokumentation)
  - [Dokumentation durch AsciiDoctor und PlantUML](#dokumentation)
  - [API Dokumentation durch javadoc](#api-dokumentation-durch-javadoc)

---

## Eigener Namespace in Kubernetes

Genauso wie in Datenbanksystemen gibt es in Kubernetes _keine_ untergeordneten
Namespaces. Vor allem ist es in Kubernetes empfehlenswert für die eigene
Software einen _neuen_ Namespace anzulegen und __NICHT__ den Default-Namespace
zu benutzen. Das wurde bei der Installation von Kubernetes durch den eigenen
Namespace `acme` bereits erledigt. Außerdem wurde aus Sicherheitsgründen beim
defaultmäßigen Service-Account das Feature _Automounting_ deaktiviert und der
Kubernetes-Cluster wurde intern defaultmäßig so abgesichert, dass

- über das Ingress-Gateway keine Requests von anderen Kubernetes-Services zulässig sind
- über das Egress-Gateway keine Requests an andere Kubernetes-Services zulässig sind.

---

## Relationale Datenbanksysteme

### Docker Compose für PostgreSQL und pgadmin

Wenn man den eigenen Microservice direkt mit Windows - nicht mit Kubernetes -
laufen lässt, kann man PostgreSQL und das Administrationswerkzeug pgadmin
einfach mit _Docker Compose_ starten und später auch herunterfahren.

> ❗ Vor dem 1. Start von PostgreSQL muss man das Skript `create-db-flug.sql`
> aus dem Verzeichnis `extras\db\postgres\sql` nach
> `C:\Zimmermann\volumes\postgres\sql` kopieren und die Anleitung ausführen.
> Für die Windows-Verzeichnisse `C:\Zimmermann\volumes\postgres\data`,
> `C:\Zimmermann\volumes\postgres\tablespace` und
> `C:\Zimmermann\volumes\postgres\tablespace\kunde` muss außerdem Vollzugriff
> gewährt werden, was über das Kontextmenü mit _Eigenschaften_ und den
> Karteireiter _Sicherheit_ für die Windows-Gruppe _Benutzer_ eingerichtet
> werden kann.
> Übrigens ist das Emoji für das Ausrufezeichen von https://emojipedia.org.

Der Name des Docker-Containers lautet `postgres` und ebenso lautet der
_virtuelle Rechnername_ `postgres`. Der virtuelle Rechnername `postgres`
wird später auch als Service-Name für PostgreSQL in Kubernetes verwendet.
Der neue Datenbank-User `kunde` wurde zum Owner der Datenbank `kunde`.

Statt eine PowerShell zu verwenden, kann man Docker Compose auch direkt in
IntelliJ aufrufen, indem man mit der rechten Maustaste `compose.yml`
anklickt und den Menüpunkt `Run postgres/compose.yml...` auswählt.

Jetzt läuft der PostgreSQL- bzw. DB-Server. Die Datenbank-URL für den eigenen
Microservice als DB-Client lautet: `postgresql://localhost/kunde`, dabei ist
`localhost` aus Windows-Sicht der Rechnername, der Port defaultmäßig `5432`
und der Datenbankname `kunde`.

Außerdem kann _pgadmin_ zur Administration verwendet werden. pgadmin läuft
ebenfalls als Docker-Container und ist über ein virtuelles Netzwerk mit dem
Docker-Container des DB-Servers verbunden. Deshalb muss beim Verbinden mit dem
DB-Server auch der virtuelle Rechnername `postgres` statt `localhost` verwendet
werden. pgadmin kann man mit einem Webbrowser und der URL `http://localhost:8888`
aufrufen. Die Emailadresse `pgadmin@acme.com` und das Passwort `p` sind voreingestellt.
Da pgadmin ist übrigens mit Chromium implementiert ist.

Beim 1. Einloggen konfiguriert man einen Server-Eintrag mit z.B. dem Namen
`localhost` und verwendet folgende Werte:

- Host: `postgres` (virtueller Rechnername des DB-Servers im Docker-Netzwerk.
  __BEACHTE__: `localhost` ist im virtuellen NXetzwerk der Name des
  pgadmin-Containers selbst !!!)
- Port: `5432` (Defaultwert)
- Username: `postgres` (Superuser beim DB-Server)
- Password: `p`

Es empfiehlt sich, das Passwort abzuspeichern, damit man es künftig nicht jedes
Mal beim Einloggen eingeben muss.

### Terraform für PostgreSQL und pgadmin

In `extras\terraform\main.tf` wird das Helm-Chart für PostgreSQL verwendet, damit
in Kubernetes auch der PostgreSQL-Server gestartet wird

---

## Übersetzung und lokale Ausführung

### Maven-Profile in settings.xml aktivieren

In der Datei `settings.xml` im Verzeichnis `${env:USERPROFILE}\.m2` müssen die Profile
`persistence`, ggf. `postgres` sowie `mail` aktiviert werden, d.h. die Kommentare bei
den entsprechenden Tags `<activeProfile>` müssen entfernt werden.

### Ausführung in IntelliJ IDEA

Bei Gradle: Am rechten Rand auf den Button _Gradle_ klicken und in _Tasks_ > _application_
durch einen Doppelklick auf _bootRun_ starten.

Bei Maven: Am rechten Rand auf den Button _Maven_ klicken und innerhalb vom Projekt
_Plugins_ > _spring-boot_ durch einen Doppelklick auf _spring-boot:run_ starten.

Danach gibt es bei Gradle in der Titelleiste am oberen Rand den Eintrag _flug [bootRun]_
im Auswahlmenü und man kann von nun an den Server auch damit (neu-) starten,
stoppen und ggf. debuggen.

---

### Start und Stop des Servers in der Kommandozeile

Nachdem der DB-Server gestartet wurde, kann man in einer Powershell den Server
mit dem Profil `dev` starten.

```powershell
    # Gradle:
    .\gradlew bootRun

    # Maven:
    .\mvnw spring-boot:run
```

Mit `<Strg>C` kann man den Server herunterfahren, weil in `application.yml`
auch die Property für _graceful shutdown_ konfiguriert ist.

Außerdem kann man in `application.yml`

* die Property `server.port` auf z.B. `8443` setzen, um den Default-Port
  `8080` umzukonfigurieren,
* die Property `server.ssl.bundle` auskommentieren, um den Server ohne TLS laufen zu lassen.

---

### Image erstellen

Bei Verwendung der Buildpacks werden ggf. einige Archive von Github heruntergeladen,
wofür es leider kein Caching gibt. Ein solches Image kann mit dem Linux-User `cnb`
gestartet werden. Mit der Task bootBuildImage kann man im Verzeichnis für das
Projekt "bestellung" ebenfalls ein Docker-Image erstellen.

```powershell
    # Gradle und Buildpacks mit Bellsoft Liberica
    .\gradlew bootBuildImage

    # Maven und Buildpacks mit Bellsoft Liberica
    .\mvnw spring-boot:build-image -D'maven.test.skip=true'

    # Eclipse Temurin mit Ubuntu Noble (2024.04) als Basis-Image
    docker build --tag=juergenzimmermann/flug:2024.04.2-eclipse .
    # Azul Zulu mit Ubuntu Jammy (2022.04) als Basis-Image
    docker build --tag=juergenzimmermann/flug:2024.04.2-azul --file=Dockerfile.azul .
```

Statt der diversen Optionen für `docker build` kann man auch eine Konfigurationsdatei
für Docker `Bake` erstellen, z.B. docker-bake.hcl (im Format HCL = HashiCorp Configuration Language).
Dann lauten die entsprechenden einfacheren Aufrufe für die obigen Beispiele:

```
    docker buildx bake
    docker buildx bake alpine
    docker buildx bake azul
```

Mit dem Unterkommando `inspect` von docker kann man die Metadaten, z.B. Labels,
zu einem Image inspizieren:

```powershell
    docker inspect juergenzimmermann/flug:2024.04.2-buildpacks-bellsoft
```

Mit _history_ kann man dann ein Docker-Image und die einzelnen Layer inspizieren:

```powershell
    docker history juergenzimmermann/flug:2024.04.2-buildpacks-bellsoft
```

Mit der PowerShell kann man Docker-Images folgendermaßen auflisten und löschen,
wobei das Unterkommando `rmi` die Kurzform für `image rm` ist:

```powershell
    docker images | sort
    docker rmi myImage:myTag
```

Im Laufe der Zeit kann es immer wieder Images geben, bei denen der Name
und/oder das Tag `<none>` ist, sodass das Image nicht mehr verwendbar und
deshalb nutzlos ist. Solche Images kann man mit dem nachfolgenden Kommando
herausfiltern und dann unter Verwendung ihrer Image-ID, z.B. `9dd7541706f0`
löschen:

```powershell
    docker rmi 9dd7541706f0
```

### Docker Compose für einen Container mit dem eigenen Server

Wenn das Image gebaut ist, kann man durch _Docker Compose_ die Services für
den DB-Server, den DB-Browser und den eigenen Microservice auf einmal starten.
Dabei ist der Service _flug_ so konfiguriert, dass er erst dann gestartet wird,
wenn der "healthcheck" des DB-Servers "ready" meldet.

```powershell
    cd extras\compose\flug

    # PowerShell fuer flug einschl. DB-Server und Mailserver
    # Image mit Cloud-Native Buildpacks und z.B. Bellsoft Liberica (siehe compose.yml)
    docker compose up

    # Nur zur Fehlersuche: weitere PowerShell für bash
    # Bei Buildpacks den Builder "paketobuildpacks/builder-jammy-base:latest" verwenden (s. gradle.properties)
    cd extras\compose\flug
    docker compose exec flug bash
        id
        ps -ef
        env
        ls -l /layers
        ls -l /layers/paketo-buildpacks_bellsoft-liberica/jre
        #ls -l /layers/paketo-buildpacks_adoptium/jre
        #ls -l /layers/paketo-buildpacks_azul-zulu/jre
        pwd
        hostname
        cat /etc/os-release
        exit

    # Fehlersuche im Netzwerk:
    docker compose -f compose.busybox.yml up
    docker compose exec busybox sh
        nslookup postgres
        exit

    # 2. Powershell: flug einschl. DB-Server und Mailserver herunterfahren
    docker compose down
```

Der eigene Server wird mit `docker compose down` einschließlich DB-Server
heruntergefahren, weil beide über ein virtuelles Netz verbunden sein müssen
und deshalb die YAML-Datei für den DB-Server in `compose.yml` inkludiert
wird. Will man nur den Service _flug_ ohne den DB-Server herunterfahren, so
lautet das Kommando: `docker compose down kunde`.

## Postman

Im Verzeichnis `extras\postman` gibt es Dateien für den Import in Postman.
Zuerst importiert man die Datei `*_environment.json`, um Umgebungsvariable
anzulegen, und danach die Dateien `*_collection.json`, um Collections für Requests
anzulegen.

---

## OpenAPI mit Swagger

Mit der URL `https://localhost:8443/swagger-ui.html` kann man in einem
Webbrowser den RESTful Web Service über eine Weboberfläche nutzen, die
von _Swagger_ auf der Basis von der Spezifikation _OpenAPI_ generiert wurde.
Die _Swagger JSON Datei_ kann man mit `https://localhost:8443/v3/api-docs`
abrufen.

## Unit Tests und Integrationstests

Wenn der DB-Server erfolgreich gestartet ist, können auch die Unit- und
Integrationstests gestartet werden.

```powershell
    # Gradle
    .\gradlew test

    # Maven
    .\mvnw test jacoco:report
```

__WICHTIGER__ Hinweis zu den Tests für den zweiten Microservice, der den ersten
Microservice aufruft: Da die Tests direkt mit Windows laufen, muss Port-Forwarding
für den aufzurufenden, ersten Microservice gestartet sein, falls dieser in Kubernetes
läuft.

Um das Testergebnis mit _Allure_ zu inspizieren, ruft man in Gradle einmalig
`.\gradlew downloadAllure` auf. Fortan kann man den generierten Webauftritt mit
den Testergebnissen folgendermaßen aufrufen:

```powershell
    # Gradle
    .\gradlew allureServe

    # Maven (leerer Report!):
    mkdir target\allure-results
    mvn allure:serve
```

---

## Rechnername in der Datei hosts

Wenn man mit Kubernetes arbeitet, bedeutet das auch, dass man i.a. über TCP
kommuniziert. Deshalb sollte man überprüfen, ob in der Datei
`C:\Windows\System32\drivers\etc\hosts` der eigene Rechnername mit seiner
IP-Adresse eingetragen ist. Zum Editieren dieser Datei sind Administrator-Rechte
notwendig.

---

## Kubernetes, Helm und Terraform

### WICHTIG: Schreibrechte für die Logdatei

Wenn die Anwendung in Kubernetes läuft, ist die Log-Datei `application.log` im
Verzeichnis `C:\Zimmermann\volumes\kunde-v2`. Das bedeutet auch zwangsläufig,
dass diese Datei durch den _Linux-User_ vom (Kubernetes-) Pod angelegt und
geschrieben wird, wozu die erforderlichen Berechtigungen in Windows gegeben
sein müssen.

Wenn man z.B. die Anwendung zuerst mittels _Cloud Native Buildpacks_ laufen
lässt, dann wird `application.log` vom Linux-User `cnb` erstellt.

### Helm als Package Manager für Kubernetes

_Helm_ ist ein _Package Manager_ für Kubernetes mit einem _Template_-Mechanismus
auf der Basis von _Go_.

Zunächst muss man z.B. mit dem Gradle- oder Maven-Plugin von Spring Boot ein
Docker-Image erstellen ([s.o.](#image-erstellen)).

Die Konfiguration für Helm ist im Unterverzeichnis `extras\helm\kunde`.
Die Metadaten für das _Helm-Chart_ sind in der Default-Datei `Chart.yaml` und
die einzelnen Manifest-Dateien für das Helm-Chart sind im Unterverzeichis
`templates` im Format YAML. In diesen Dateien gibt es Platzhalter ("templates")
mit der Syntax der Programmiersprache Go. Die Defaultwerte für diese Platzhalter
sind in der Default-Datei `values.yaml` und können beim Installieren mit z.B.
_Terraform_ oder _Pulumi_ durch weitere YAML-Dateien überschrieben werden.

Mit den nachfolgenden Kommandos kann man ein Helm-Chart überprüfen ("lint")
und eine Markdown-Datei zur Dokumentation der Defaultwerte für das Helm-Chart
generieren.

```powershell
    cd extras\helm\kunde

    # Ueberprüfung des Helm-Charts
    helm lint --strict .

    # Markdown-Datei mit den Defaultwerten generieren
    helm-docs
```

### Bereitstellung mit Terraform und Port Forwarding

Siehe `extras\terraform\ReadMe.md`.

### Bereitstellung mit Pulumi und Port Forwarding

Siehe `extras\pulumi\ReadMe.md`.
```

### kubectl top

Mit `kubectl top pods -n acme` kann man sich die CPU- und RAM-Belegung der Pods
anzeigen lassen. Ausgehend von diesen Werten kann man `resources.requests` und
`resources.limits` in `dev.yaml` ggf. anpassen.

Voraussetzung für `kubectl top` ist, dass der `metrics-server` für Kubernetes
im Namespace `kube-system` installiert wurde.
https://kubernetes.io/docs/tasks/debug/debug-cluster/resource-metrics-pipeline

### Validierung der Installation

#### Polaris

Ob _Best Practices_ bei der Installation eingehalten wurden, kann man mit
_Polaris_ überprüfen. Um den Aufruf zu vereinfachen, gibt es im Unterverzeichnis
`extras\kubernetes` das Skript `polaris.ps1`:

```powershell
    cd extras\kubernetes
    .\polaris.ps1
```

Nun kann Polaris in einem Webbrowser mit der URL `http://localhost:8008`
aufgerufen werden.

#### kubescape

Ob _Best Practices_ bei den _Manifest-Dateien_ eingehalten wurden, kann man mit
_kubescape_ überprüfen. Um den Aufruf zu vereinfachen, gibt es im
Unterverzeichnis `extras\kubernetes` das Skript `kubescape.ps1`:

```powershell
    cd extras\kubernetes
    .\kubescape.ps1
```

#### Pluto

Ob _deprecated_ APIs bei den _Manifest-Dateien_ verwendet wurden, kann man mit
_Pluto_ überprüfen. Um den Aufruf zu vereinfachen, gibt es im
Unterverzeichnis `extras\kubernetes` das Skript `pluto.ps1`:

```powershell
    cd extras\kubernetes
    .\pluto.ps1
```

---

## Statische Codeanalyse

### Checkstyle und SpotBugs

Eine statische Codeanalyse ist durch die Werkzeuge _Checkstyle_, _SpotBugs_,
_Spotless_ und _Modernizer_ möglich, indem man die folgenden Tasks aufruft:

```powershell
    # Gradle:
    .\gradlew checkstyleMain spotbugsMain checkstyleTest spotbugsTest spotlessApply modernizer

    # Maven:
    .\mvnw checkstyle:checkstyle spotbugs:check spotless:check modernizer:modernizer jxr:jxr
```

### SonarQube

Für eine statische Codeanalyse durch _SonarQube_ muss zunächst der
SonarQube-Server mit _Docker Compose_ als Docker-Container gestartet werden.
Zur Konfiguration des Servers siehe `extras\compose\sonarqube\ReadMe.md`.

```powershell
    cd extras\compose\sonarqube
    docker compose up
```

Nachdem der Server gestartet ist, wird der SonarQube-Scanner in einer zweiten
PowerShell mit `.\gradlew sonar` bzw. `.\mvnw sonar:sonar` gestartet.
Das Resultat kann dann in der Webseite des zuvor gestarteten Servers über die
URL `http://localhost:9000` inspiziert werden.

Abschließend wird der oben gestartete Server heruntergefahren.

```powershell
    cd extras\compose
    docker compose down
```

---

## Analyse von Sicherheitslücken

### OWASP Security Check

In `build.gradle.kts` bzw. `pom.xml` sind _dependencies_ konfiguriert, um
Java Archive, d.h. .jar-Dateien, von Drittanbietern zu verwenden, z.B. die
JARs für Spring oder für Jackson. Diese Abhängigkeiten lassen sich mit
_OWASP Dependency Check_ analysieren:

```powershell
    # Gradle:
    .\gradlew dependencyCheckAnalyze --info

    # Maven:
    .\mvnw dependency-check:check
```

#### Docker Scout

Mit dem Unterkommando `quickview` von _Scout_ kann man sich zunächst einen
groben Überblick verschaffen, wieviele Sicherheitslücken in den Bibliotheken im
Image enthalten sind:

```powershell
    docker scout quickview juergenzimmermann/kunde:2024.04.2-buildpacks-bellsoft
```

Dabei bedeutet:

* C ritical
* H igh
* M edium
* L ow

Sicherheitslücken sind als _CVE-Records_ (CVE = Common Vulnerabilities and Exposures)
katalogisiert: https://www.cve.org (ursprünglich: https://cve.mitre.org/cve).
Übrigens bedeutet _CPE_ in diesem Zusammenhang _Common Platform Enumeration_.
Die Details zu den CVE-Records im Image kann man durch das Unterkommando `cves`
von _Scout_ auflisten:

```powershell
    # Analyse des Images mit Cloud-Native Buildpacks und Bellsoft Liberica
    docker scout cves juergenzimmermann/kunde:2024.04.2-buildpacks-bellsoft
    docker scout cves --format only-packages juergenzimmermann/kunde:2024.04.2-buildpacks-bellsoft
````

Statt der Kommandozeile kann man auch den Menüpunkt "Docker Scout" im
_Docker Dashboard_ verwenden.

### Trivy von Aquasec

Von Aquasec gibt es _Trivy_, um Docker-Images auf Sicherheitslücken zu analysieren.
Trivy gibt es auch als Docker-Image. In `compose.trivy.yml` ist ein Service für Trivy
so konfiguriert, dass das Image `kunde` analysiert wird:

```powershell
    cd extras\compose\trivy
    # Analyse des Images mit Cloud-Native Buildpacks und z.B. Bellsoft Liberica (siehe compose.yml)
    docker compose up
```

---

## Dokumentation

### Dokumentation durch AsciiDoctor und PlantUML

Eine HTML- und PDF-Dokumentation aus AsciiDoctor-Dateien, die ggf. UML-Diagramme
mit PlantUML enthalten, wird durch folgende Tasks erstellt:

```powershell
    # Gradle:
    .\gradlew asciidoctor asciidoctorPdf

    # Maven:
    .\mvnw asciidoctor:process-asciidoc asciidoctor:process-asciidoc@pdf -Pasciidoctor
```

### API Dokumentation durch javadoc

Eine API-Dokumentation in Form von HTML-Seiten kann man durch das Gradle- bzw.
Maven-Plugin erstellen:

```powershell
    # Gradle:
    .\gradlew javadoc

    # Maven:
    .\mvnw compile javadoc:javadoc
```
