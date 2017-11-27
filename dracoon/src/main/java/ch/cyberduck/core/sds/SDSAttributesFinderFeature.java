package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;

import org.apache.commons.lang3.StringUtils;

public class SDSAttributesFinderFeature implements AttributesFinder {

    public static final String KEY_CNT_DOWNLOADSHARES = "count_downloadshares";
    public static final String KEY_CNT_UPLOADSHARES = "count_uploadshares";

    private final SDSSession session;

    public SDSAttributesFinderFeature(final SDSSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        try {
            final Node node = new NodesApi(session.getClient()).getFsNode(StringUtils.EMPTY,
                Long.parseLong(new SDSNodeIdProvider(session).getFileid(file, new DisabledListProgressListener())), null);
            return this.toAttributes(node);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    public PathAttributes toAttributes(final Node node) throws BackgroundException {
        final PathAttributes attributes = new PathAttributes();
        attributes.setVersionId(String.valueOf(node.getId()));
        attributes.setChecksum(Checksum.parse(node.getHash()));
        attributes.setCreationDate(node.getCreatedAt().getMillis());
        attributes.setModificationDate(node.getUpdatedAt().getMillis());
        attributes.setSize(node.getSize());
        attributes.setPermission(this.toPermission(node));
        attributes.setAcl(this.toAcl(node));
        return attributes;
    }

    private Permission toPermission(final Node node) {
        final Permission permission = new Permission(Permission.Action.read, Permission.Action.none, Permission.Action.none);
        switch(node.getType()) {
            case "ROOM":
            case "FOLDER":
                permission.setUser(permission.getUser().or(Permission.Action.execute));
        }
        if(node.getPermissions().getChange()) {
            permission.setUser(permission.getUser().or(Permission.Action.write));
        }
        return permission;
    }

    private Acl toAcl(final Node node) throws BackgroundException {
        final Acl acl = new Acl();
        final Acl.User user = new Acl.CanonicalUser(String.valueOf(session.userAccount().getId()));
        if(node.getPermissions().getManage()) {
            acl.addAll(user, SDSPermissionsFeature.MANAGE_ROLE);
        }
        if(node.getPermissions().getRead()) {
            acl.addAll(user, SDSPermissionsFeature.READ_ROLE);
        }
        if(node.getPermissions().getCreate()) {
            acl.addAll(user, SDSPermissionsFeature.CREATE_ROLE);
        }
        if(node.getPermissions().getChange()) {
            acl.addAll(user, SDSPermissionsFeature.CHANGE_ROLE);
        }
        if(node.getPermissions().getDelete()) {
            acl.addAll(user, SDSPermissionsFeature.DELETE_ROLE);
        }
        if(node.getPermissions().getManageDownloadShare()) {
            acl.addAll(user, SDSPermissionsFeature.DOWNLOAD_SHARE_ROLE);
        }
        if(node.getPermissions().getManageUploadShare()) {
            acl.addAll(user, SDSPermissionsFeature.UPLOAD_SHARE_ROLE);
        }
        return acl;
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        return this;
    }
}
