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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.flug.mail;

import com.acme.flug.entity.Flug;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/// Mail-Client.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
@Service
@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class MailService {
    private final StableValue<Logger> logger = StableValue.of();

    /// Objekt für _Jakarta Mail_, um Emails zu verschicken
    private final JavaMailSender mailSender;

    /// Injizierte Properties für _Spring Mail_.
    private final MailConfig mailConfig;

    /// Mailserver
    @Value("${spring.mail.host}")
    @SuppressWarnings("NullAway.Init")
    private String mailhost;

    /// Konstruktor mit `package private` für _Constructor Injection_ bei _Spring_.
    ///
    /// @param mailSender Injiziertes Objekt für _Spring Mail_.
    /// @param mailConfig Injiziertes Property-Objekt für _Spring Mail_.
    MailService(final JavaMailSender mailSender, final MailConfig mailConfig) {
        this.mailSender = mailSender;
        this.mailConfig = mailConfig;
    }

    /// Email senden, dass es einen neuen Flug gibt.
    ///
    /// @param neuerFlug Das Objekt des neuen Flugs.
    @Async
    public void send(final Flug neuerFlug) {
        final var mimeMessage = mailSender.createMimeMessage();

        try {
            final var mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(mailConfig.from());
            mimeMessageHelper.setTo(mailConfig.sales());
            mimeMessageHelper.setSubject("Neuer Flug " + neuerFlug.getId());
            final var plainText = "Neuer Flug angelegt\n" +
                "Von: " + neuerFlug.getStartOrt() + "\n" +
                "Nach: " + neuerFlug.getZielOrt() + "\n" +
                "Abflug: " + neuerFlug.getAbflugZeit();
            final var htmlText = "<strong>Neuer Flug</strong><br>" +
                "Von: <em>" + neuerFlug.getStartOrt() + "</em><br>" +
                "Nach: <em>" + neuerFlug.getZielOrt() + "</em><br>" +
                "Abflug: " + neuerFlug.getAbflugZeit();
            mimeMessageHelper.setText(plainText, htmlText);

            mailSender.send(mimeMessage);
            getLogger().trace("send: Thread-ID={}, mailConfig={}, flug={}",
                Thread.currentThread().threadId(), mailhost, neuerFlug);
        } catch (MailException | MessagingException _) {
            getLogger().warn("Email nicht gesendet: Ist der Mailserver {} erreichbar?", mailhost);
        }
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(MailService.class));
    }
}
