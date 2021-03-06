package ch.cyberduck.core.udt;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Scheme;

public class UDTProtocol extends AbstractProtocol {
    @Override
    public String getIdentifier() {
        return "udt";
    }

    @Override
    public String getDescription() {
        return "UDP-based Data Transfer Protocol";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.udt;
    }

    @Override
    public String getPrefix() {
        return null;
    }
}
