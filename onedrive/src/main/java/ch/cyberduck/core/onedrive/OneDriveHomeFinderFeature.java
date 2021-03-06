package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveDrive;

import java.io.IOException;
import java.util.EnumSet;

public class OneDriveHomeFinderFeature extends DefaultHomeFinderService {

    private final OneDriveSession session;

    public OneDriveHomeFinderFeature(final OneDriveSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        final Path home = super.find();
        if(home == DEFAULT_HOME) {
            try {
                final OneDriveDrive.Metadata metadata = OneDriveDrive.getDefaultDrive(session.getClient()).getMetadata();
                final Path drive = new Path(metadata.getId(), EnumSet.of(Path.Type.volume, Path.Type.directory));
                switch(metadata.getDriveType()) {
                    case personal:
                        drive.attributes().setDisplayname("OneDrive Personal");
                        break;
                    case business:
                        drive.attributes().setDisplayname("OneDrive Business");
                        break;
                    case documentLibrary:
                        drive.attributes().setDisplayname("Document Library");
                        break;
                }
                return drive;
            }
            catch(OneDriveAPIException e) {
                throw new OneDriveExceptionMappingService().map("Failure to read attributes of {0}", e, home);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, home);
            }
        }
        return home;
    }
}
