/*
 * Copyright (C) 2025 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.flug.controller;

import java.util.Collections;
import java.util.List;
import org.springframework.data.web.PagedModel.PageMetadata;

public record StringPage(List<String> content, PageMetadata page) {
    // https://stackoverflow.com/questions/77359073/how-to-solve-ei-expose-rep-in-records-for-lists
    public StringPage(final List<String> content, final PageMetadata page) {
        this.content = Collections.unmodifiableList(content);
        this.page = page;
    }

    @Override
    public List<String> content() {
        return Collections.unmodifiableList(content);
    }
}
