/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.flug;

// import module java.base; // NOSONAR

import org.apache.catalina.util.ServerInfo;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Objects;

/// Banner als String-Konstante für den Start des Servers.
@SuppressWarnings({
    "AccessOfSystemProperties",
    "CallToSystemGetenv",
    "UtilityClassCanBeEnum",
    "UtilityClass",
    "ClassUnconnectedToPackage"
})
final class Banner {

    // http://patorjk.com/software/taag/#p=display&f=Slant&t=flug%202025.10.1
    private static final String FIGLET = """
            ______               ___   ____ ___   ______   _______    ___
           / __/ /_  ______ _   |__ \\ / __ \\__ \\ / ____/  <  / __ \\  <  /
          / /_/ / / / / __ `/   __/ // / / /_/ //___ \\    / / / / /  / /\s
         / __/ / /_/ / /_/ /   / __// /_/ / __/____/ / _ / / /_/ /_ / / \s
        /_/ /_/\\__,_/\\__, /   /____/\\____/____/_____/ (_)_/\\____/(_)_/  \s
                    /____/                                              \s
        """;
    private static final String SERVICE_HOST = System.getenv("FLUG_SERVICE_HOST");
    private static final String KUBERNETES = SERVICE_HOST == null
        ? "N/A"
        : "FLUG_SERVICE_HOST=" + SERVICE_HOST + ", FLUG_SERVICE_PORT=" + System.getenv("KUNDE_SERVICE_PORT");
    private static final String CLASSPATH_ORIG = System.getProperty("java.class.path");
    private static final String CLASSPATH = CLASSPATH_ORIG.length() > 120 ? "<very long>" : CLASSPATH_ORIG;

    /// Banner für den Server-Start.
    static final String TEXT = """

        $figlet
        (C) Juergen Zimmermann, Hochschule Karlsruhe
        Version             2025.10.1
        Spring Boot         $springBoot
        Spring Framework    $spring
        Apache Tomcat       $tomcat
        Java                $java
        Betriebssystem      $os
        Rechnername         $rechnername
        IP-Adresse          $ip
        Heap: Size          $heapSize
        Heap: Free          $heapFree
        Kubernetes          $kubernetes
        Username            $username
        JVM Locale          $locale
        OpenAPI             /swagger-ui.html /v3/api-docs.yaml
        CLASSPATH           $classpath
        """
        .replace("$figlet", FIGLET)
        .replace("$springBoot", SpringBootVersion.getVersion())
        .replace("$spring", Objects.requireNonNull(SpringVersion.getVersion()))
        .replace("$tomcat", ServerInfo.getServerInfo())
        .replace("$java", Runtime.version() + " - " + System.getProperty("java.vendor"))
        .replace("$os", System.getProperty("os.name"))
        .replace("$rechnername", getLocalhost().getHostName())
        .replace("$ip", getLocalhost().getHostAddress())
        .replace("$heapSize", Runtime.getRuntime().totalMemory() / (1024L * 1024L) + " MiB")
        .replace("$heapFree", Runtime.getRuntime().freeMemory() / (1024L * 1024L) + " MiB")
        .replace("$kubernetes", KUBERNETES)
        .replace("$username", System.getProperty("user.name"))
        .replace("$locale", Locale.getDefault().toString())
        .replace("$classpath", CLASSPATH);

    @SuppressWarnings("ImplicitCallToSuper")
    private Banner() {
    }

    private static InetAddress getLocalhost() {
        try {
            return InetAddress.getLocalHost();
        } catch (final UnknownHostException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
