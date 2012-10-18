package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class MultipleBookmarkCollection extends Collection<FolderBookmarkCollection> implements EditableCollection {
    private static Logger log = Logger.getLogger(FolderBookmarkCollection.class);

    private static final MultipleBookmarkCollection EMPTY = new MultipleBookmarkCollection(null) {
        private static final long serialVersionUID = 5741322275377145083L;

        @Override
        public void load() {
            //
        }
    };
    private static final long serialVersionUID = -6318679625583371126L;

    public static MultipleBookmarkCollection empty() {
        return EMPTY;
    }

    private static final MultipleBookmarkCollection DEFAULT_COLLECTION = new MultipleBookmarkCollection(
            LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), "Bookmarks")
    );

    public static MultipleBookmarkCollection defaultCollection() {
        return DEFAULT_COLLECTION;
    }

    private Local folder;

    /**
     * Reading bookmarks from this folder
     *
     * @param f Parent directory to look for bookmarks
     */
    public MultipleBookmarkCollection(Local f) {
        this.folder = f;
        this.folder.mkdir();
        this.load();
    }

    @Override
    public void load() {
        if(log.isInfoEnabled()) {
            log.info("Reloading:" + folder);
        }
        final AttributedList<Local> groups = folder.children(
                new PathFilter<Local>() {
                    @Override
                    public boolean accept(Local file) {
                        return file.attributes().isDirectory();
                    }
                }
        );
        for(Local group : groups) {
            this.add(new FolderBookmarkCollection(group));
        }
        super.load();
    }

    public boolean contains(Host bookmark) {
        for(AbstractHostCollection c : this) {
            if(c.contains(bookmark)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean allowsAdd() {
        for(AbstractHostCollection c : this) {
            if(!c.allowsAdd()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean allowsDelete() {
        for(AbstractHostCollection c : this) {
            if(!c.allowsDelete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean allowsEdit() {
        for(AbstractHostCollection c : this) {
            if(!c.allowsEdit()) {
                return false;
            }
        }
        return true;
    }
}