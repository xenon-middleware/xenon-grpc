package nl.esciencecenter.xenon.grpc.filesystems;

import static nl.esciencecenter.xenon.grpc.MapUtils.mapPropertyDescriptions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.AttributeNotSupportedException;
import nl.esciencecenter.xenon.filesystems.CopyMode;
import nl.esciencecenter.xenon.filesystems.CopyStatus;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;
import nl.esciencecenter.xenon.grpc.XenonProto;

import io.grpc.Status;
import io.grpc.StatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    MapUtils to convert Xenon objects to gRPC response fields
 */
class MapUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapUtils.class);

    private MapUtils() {
    }

    static Set<PosixFilePermission> parsePermissions(List<XenonProto.PosixFilePermission> permissionsValueList) throws StatusException {
        Set<PosixFilePermission> permissions = new HashSet<>();
        for (XenonProto.PosixFilePermission permission:  permissionsValueList) {
            switch (permission) {
                case NONE:
                    // Do nothing with NONE, this allows for a file with no permission
                    break;
                case OWNER_READ:
                    permissions.add(PosixFilePermission.OWNER_READ);
                    break;
                case OWNER_WRITE:
                    permissions.add(PosixFilePermission.OWNER_WRITE);
                    break;
                case OWNER_EXECUTE:
                    permissions.add(PosixFilePermission.OWNER_EXECUTE);
                    break;
                case GROUP_READ:
                    permissions.add(PosixFilePermission.GROUP_READ);
                    break;
                case GROUP_WRITE:
                    permissions.add(PosixFilePermission.GROUP_WRITE);
                    break;
                case GROUP_EXECUTE:
                    permissions.add(PosixFilePermission.GROUP_EXECUTE);
                    break;
                case OTHERS_READ:
                    permissions.add(PosixFilePermission.OTHERS_READ);
                    break;
                case OTHERS_WRITE:
                    permissions.add(PosixFilePermission.OTHERS_WRITE);
                    break;
                case OTHERS_EXECUTE:
                    permissions.add(PosixFilePermission.OTHERS_EXECUTE);
                    break;
                case UNRECOGNIZED:
                    throw Status.INVALID_ARGUMENT.withDescription("Unrecognized posix file permission").asException();
            }
        }
        return permissions;
    }

    static CopyMode mapCopyMode(XenonProto.CopyMode mode) {
        switch (mode) {
            case CREATE:
                return CopyMode.CREATE;
            case REPLACE:
                return CopyMode.REPLACE;
            case IGNORE:
                return CopyMode.IGNORE;
        }
        // the default
        return CopyMode.CREATE;
    }

    static XenonProto.PathAttributes writeFileAttributes(XenonProto.FileSystem filesystem, PathAttributes a) {
        XenonProto.PathAttributes.Builder builder = XenonProto.PathAttributes.newBuilder()
            .setPath(writePath(a.getPath(), filesystem))
            .setCreationTime(a.getCreationTime())
            .setIsDirectory(a.isDirectory())
            .setIsExecutable(a.isExecutable())
            .setIsHidden(a.isHidden())
            .setIsOther(a.isOther())
            .setIsReadable(a.isReadable())
            .setIsRegularFile(a.isRegular())
            .setIsSymbolicLink(a.isSymbolicLink())
            .setIsWritable(a.isWritable())
            .setLastAccessTime(a.getLastAccessTime())
            .setLastModifiedTime(a.getLastModifiedTime())
            .setSize(a.getSize());

        try {
            if (a.getPermissions() != null) {
                builder.addAllPermissions(writePermissions(a.getPermissions()));
            }
        } catch (AttributeNotSupportedException e) {
            LOGGER.warn("Skipping permissions, not supported", e);
        }
        try {
            if (a.getOwner() != null) {
                builder.setOwner(a.getOwner());
            }
        } catch (AttributeNotSupportedException e) {
            LOGGER.warn("Skipping owner, not supported", e);
        }
        try {
            if (a.getGroup() != null) {
                builder.setGroup(a.getGroup());
            }
        } catch (AttributeNotSupportedException e) {
            LOGGER.warn("Skipping group, not supported", e);
        }
        return builder.build();
    }

    private static Set<XenonProto.PosixFilePermission> writePermissions(Set<PosixFilePermission> permissionsx) {
        Set<XenonProto.PosixFilePermission> permissions = new HashSet<>();
        for (PosixFilePermission permission : permissionsx) {
            switch (permission) {
                case GROUP_EXECUTE:
                    permissions.add(XenonProto.PosixFilePermission.GROUP_EXECUTE);
                    break;
                case GROUP_READ:
                    permissions.add(XenonProto.PosixFilePermission.GROUP_READ);
                    break;
                case GROUP_WRITE:
                    permissions.add(XenonProto.PosixFilePermission.GROUP_WRITE);
                    break;
                case OTHERS_EXECUTE:
                    permissions.add(XenonProto.PosixFilePermission.OTHERS_EXECUTE);
                    break;
                case OTHERS_READ:
                    permissions.add(XenonProto.PosixFilePermission.OTHERS_READ);
                    break;
                case OTHERS_WRITE:
                    permissions.add(XenonProto.PosixFilePermission.OTHERS_WRITE);
                    break;
                case OWNER_EXECUTE:
                    permissions.add(XenonProto.PosixFilePermission.OWNER_EXECUTE);
                    break;
                case OWNER_READ:
                    permissions.add(XenonProto.PosixFilePermission.OWNER_READ);
                    break;
                case OWNER_WRITE:
                    permissions.add(XenonProto.PosixFilePermission.OWNER_WRITE);
                    break;
            }
        }
        return permissions;
    }

    static XenonProto.Path writePath(Path path, XenonProto.FileSystem fs) {
        return XenonProto.Path.newBuilder().setFilesystem(fs).setPath(path.getAbsolutePath()).build();
    }

    static String getFileSystemId(FileSystem fileSystem, String username) {
        return fileSystem.getAdaptorName() + "://" + username + "@" + fileSystem.getLocation();
    }

    private static XenonProto.FileSystem writeFileSystem(FileSystem fs) {
        XenonProto.CreateFileSystemRequest request = XenonProto.CreateFileSystemRequest.newBuilder()
            .setAdaptor(fs.getAdaptorName())
            .setLocation(fs.getLocation())
            .putAllProperties(fs.getProperties())
            .build();
        return XenonProto.FileSystem.newBuilder()
            .setId(getFileSystemId(fs, new DefaultCredential().getUsername()))
            .setRequest(request)
            .build();
    }

    static XenonProto.FileSystems writeFileSystems(FileSystem[] xenonfilesystems) {
        XenonProto.FileSystems.Builder builder = XenonProto.FileSystems.newBuilder();
        for (FileSystem fs : xenonfilesystems) {
            builder.addFilesystems(writeFileSystem(fs));
        }
        return builder.build();
    }

    static XenonProto.CopyStatus mapCopyStatus(CopyStatus status, XenonProto.CopyOperation operation) {
        XenonProto.CopyStatus.Builder builder = XenonProto.CopyStatus.newBuilder()
            .setBytesCopied(status.bytesCopied())
            .setBytesToCopy(status.bytesToCopy())
            .setCopyOperation(operation)
            .setState(status.getState())
            .setDone(status.isDone())
            .setRunning(status.isRunning());
        if (status.hasException()) {
            builder.setError(status.getException().getMessage());
        }
        return builder.build();
    }

    static XenonProto.FileSystemAdaptorDescription mapFileAdaptorDescription(FileSystemAdaptorDescription status) {
        List<XenonProto.PropertyDescription> supportedProperties = mapPropertyDescriptions(status.getSupportedProperties());
        return XenonProto.FileSystemAdaptorDescription.newBuilder()
            .setName(status.getName())
            .setDescription(status.getDescription())
            .addAllSupportedLocations(Arrays.asList(status.getSupportedLocations()))
            .addAllSupportedProperties(supportedProperties)
            .build();
    }
}