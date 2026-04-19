# SonarQube

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
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->

[Juergen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)

Für eine statische Codeanalyse durch _SonarQube_ wird der SonarQube-Server mit
_Docker Compose_ als Docker-Container gestartet werden:

```powershell
    cd extras\compose\sonarqube
    docker compose up
```

Wenn der Server zum ersten Mal gestartet wird, ruft man in einem Webbrowser die
URL `http://localhost:9000` auf. In der Startseite muss man sich einloggen und
verwendet dazu als Loginname `admin` und ebenso als Password `admin`. Danach
wird man weitergeleitet, um das initiale Passwort zu ändern.

Nun wähle man in der Webseite rechts oben das Profil aus und klickt auf den
Karteireiter _Security_. Im Abschnitt _Generate Tokens_ macht nun die folgende
Eingaben:

* _Name_: z.B. Softwarearchitektur
* _Type_: _Global Analysis Token_ auswählen
* _Expires in_: z.B. _90 days_ auswählen

Abschließend klickt man auf den Button _Generate_ und trägt den generierten
Token in `gradle.properties` bei der Property `sonarToken` ein bzw. in
`${env:USERPROFILE}\.m2\settings.xml` beim XML-Tag `<sonar.token>`.
