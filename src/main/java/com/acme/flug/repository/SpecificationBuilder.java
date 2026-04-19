package com.acme.flug.repository;

import com.acme.flug.entity.Flug;
import com.acme.flug.entity.Flug_;
import com.acme.flug.entity.Flugzeug_;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/// Singleton-Klasse, um Specifications für Queries in Spring Data JPA zu bauen.
@Component
public class SpecificationBuilder {
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit `package private` für _Spring_.
    SpecificationBuilder() {
    }

    /// Specification für eine Query mit Spring Data bauen.
    ///
    /// @param suchparameter als MultiValueMap-ähnliche Struktur
    /// @return Specification für eine Query mit Spring Data
    @Nullable
    public Specification<Flug> build(final Map<String, ? extends List<String>> suchparameter) {
        getLogger().debug("build: suchparameter={}", suchparameter);

        if (suchparameter.isEmpty()) {
            return null;
        }

        final var specs = suchparameter
            .entrySet()
            .stream()
            .map(this::toPredicateSpecification)
            .toList();

        if (specs.isEmpty() || specs.contains(null)) {
            return null;
        }

        return Specification.where(PredicateSpecification.allOf(specs));
    }

    @Nullable
    @SuppressWarnings({"CyclomaticComplexity", "PMD.AvoidLiteralsInIfCondition"})
    private PredicateSpecification<Flug> toPredicateSpecification(
        final Map.Entry<String, ? extends List<String>> entry
    ) {
        getLogger().trace("toSpec: entry={}", entry);

        final var key = entry.getKey();
        final var values = entry.getValue();

        // Beispiel für Multi-Value wie bei "ausstattung" im Flug-Projekt:
        // ?zielOrt=Berlin&zielOrt=Paris
        if ("zielOrt".contentEquals(key)) {
            return toSpecificationZielOrte(values);
        }

        if (values.size() != 1) {
            return null;
        }

        final var value = values.getFirst();
        return switch (key) {
            case "startOrt" -> startOrt(value);
            case "zielOrtEinzeln" -> zielOrt(value);
            case "flugzeug.hersteller" -> flugzeugHersteller(value);
            case "flugzeug.modell" -> flugzeugModell(value);
            default -> null;
        };
    }

    @Nullable
    private PredicateSpecification<Flug> toSpecificationZielOrte(final Collection<String> zielOrte) {
        getLogger().trace("build: zielOrte={}", zielOrte);
        if (zielOrte == null || zielOrte.isEmpty()) {
            return null;
        }

        final var specsImmutable = zielOrte.stream()
            .map(this::zielOrt)
            .toList();

        if (specsImmutable.isEmpty() || specsImmutable.contains(null)) {
            return null;
        }

        final SequencedCollection<PredicateSpecification<Flug>> specs = new ArrayList<>(specsImmutable);
        final var first = specs.removeFirst();

        // Wie beim Kumpel: AND-Verknüpfung
        // Hinweis: AND über mehrere zielOrt-LIKEs ist oft logisch "zu streng".
        // Wenn du lieber OR willst, sag kurz Bescheid, dann ändern wir es.
        return specs.stream().reduce(first, PredicateSpecification::and);
    }

    private PredicateSpecification<Flug> startOrt(final String teil) {
        return (root, builder) -> builder.like(
            builder.lower(root.get(Flug_.startOrt)),
            builder.lower(builder.literal("%" + teil + '%'))
        );
    }

    private PredicateSpecification<Flug> zielOrt(final String teil) {
        return (root, builder) -> builder.like(
            builder.lower(root.get(Flug_.zielOrt)),
            builder.lower(builder.literal("%" + teil + '%'))
        );
    }

    private PredicateSpecification<Flug> flugzeugHersteller(final String teil) {
        return (root, builder) -> builder.like(
            builder.lower(root.get(Flug_.flugzeug).get(Flugzeug_.hersteller)),
            builder.lower(builder.literal("%" + teil + '%'))
        );
    }

    private PredicateSpecification<Flug> flugzeugModell(final String teil) {
        return (root, builder) -> builder.like(
            builder.lower(root.get(Flug_.flugzeug).get(Flugzeug_.modell)),
            builder.lower(builder.literal("%" + teil + '%'))
        );
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(SpecificationBuilder.class));
    }
}
