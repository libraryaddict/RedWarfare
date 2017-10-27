package me.libraryaddict.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.List;

import me.libraryaddict.core.ServerType;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class UtilFile
{
    private static File _updateFolder;

    static
    {
        _updateFolder = new File("").getAbsoluteFile();

        for (int i = 0; i < 4; i++)
        {
            if (new File(_updateFolder, "ServerFiles").exists())
                break;

            _updateFolder = _updateFolder.getParentFile();
        }

        _updateFolder = new File(_updateFolder, "ServerFiles");

        if (!_updateFolder.exists())
        {
            System.out.println("Cannot find 'ServerFiles'");
        }
    }

    public static boolean addZipFile(File zip, String fileName, File copyFrom)
    {
        try
        {
            ZipFile zipFile = new ZipFile(zip);

            copyFrom = copyFrom.getAbsoluteFile();

            ZipParameters param = new ZipParameters();
            param.setFileNameInZip(fileName);
            param.setIncludeRootFolder(false);
            param.setSourceExternalStream(true);

            zipFile.removeFile(fileName);

            zipFile.addFile(copyFrom, param);
            return true;
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        return false;
    }

    private static long calculateTotalWork(List fileHeaders) throws ZipException
    {
        if (fileHeaders == null)
        {
            throw new ZipException("fileHeaders is null, cannot calculate total work");
        }

        long totalWork = 0;

        for (int i = 0; i < fileHeaders.size(); i++)
        {
            FileHeader fileHeader = (FileHeader) fileHeaders.get(i);

            if (fileHeader.getZip64ExtendedInfo() != null && fileHeader.getZip64ExtendedInfo().getUnCompressedSize() > 0)
            {
                totalWork += fileHeader.getZip64ExtendedInfo().getCompressedSize();
            }
            else
            {
                totalWork += fileHeader.getCompressedSize();
            }
        }

        return totalWork;
    }

    public static void copyFile(File source, File dest)
    {
        System.out.println("Copying '" + source.getAbsolutePath() + "' to '" + dest.getAbsolutePath() + "'");

        try
        {
            Files.walkFileTree(source.toPath(), new SimpleFileVisitor<Path>()
            {
                private FileVisitResult copy(Path fileOrDir) throws IOException
                {
                    Path destination = dest.toPath().resolve(source.toPath().relativize(fileOrDir));

                    File file = destination.toFile().getAbsoluteFile();

                    if (file.exists() && file.isDirectory())
                    {
                        return FileVisitResult.CONTINUE;
                    }

                    if (!file.getParentFile().exists())
                    {
                        file.getParentFile().mkdirs();
                    }

                    Files.copy(fileOrDir, destination, StandardCopyOption.REPLACE_EXISTING);

                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
                {
                    return copy(dir);
                }

                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    return copy(file);
                }
            });
        }
        catch (Exception e)
        {
            UtilError.handle(e);
        }
    }

    public static void createZip(File source, File destination)
    {
        try
        {
            destination = destination.getAbsoluteFile();

            if (!destination.getParentFile().exists())
            {
                destination.getParentFile().mkdirs();
            }

            ZipFile zipFile = new ZipFile(destination);
            ZipParameters zipParam = new ZipParameters();
            zipParam.setIncludeRootFolder(false);

            zipParam.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zipParam.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            zipFile.addFolder(source, zipParam);
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public static void delete(File file)
    {
        if (file.isDirectory())
        {
            for (File f : file.listFiles())
            {
                delete(f);
            }
        }

        file.delete();
    }

    public static void extractZip(File zip, File destination)
    {
        extractZip(zip, destination, false);
    }

    public static void extractZip(File zip, File destination, boolean enforceLimit)
    {
        try
        {
            if (!zip.exists())
                throw new IllegalArgumentException(zip + " does not exist!");

            ZipFile zipFile = new ZipFile(zip);

            if (!zipFile.isValidZipFile())
                return;

            if (!destination.exists())
            {
                destination.mkdirs();
            }

            if (enforceLimit && calculateTotalWork(zipFile.getFileHeaders()) > 1e+8)
                return;

            zipFile.extractAll(destination.getAbsolutePath());
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public static boolean extractZipFile(File zip, String fileName, File destination)
    {
        try
        {
            ZipFile zipFile = new ZipFile(zip);

            destination = destination.getAbsoluteFile();

            if (!destination.getParentFile().exists())
            {
                destination.getParentFile().mkdirs();
            }

            FileHeader header = zipFile.getFileHeader(fileName);

            if (header == null)
                return false;

            zipFile.extractFile(header, destination.getParent(), new UnzipParameters(), destination.getName());
            return true;
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        return false;
    }

    public static File[] getMaps(ServerType game)
    {
        File file = new File(getUpdateFolder(), "Maps/" + game.getName());

        if (!file.exists())
        {
            file.mkdirs();
        }

        System.out.println("Looking for maps in " + file.getAbsolutePath());

        return file.listFiles();
    }

    public static String getSha(final File file)
    {
        try
        {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA1");

            try (InputStream is = new BufferedInputStream(new FileInputStream(file)))
            {
                final byte[] buffer = new byte[1024];

                for (int read = 0; (read = is.read(buffer)) != -1;)
                {
                    messageDigest.update(buffer, 0, read);
                }
            }

            // Convert the byte to hex format
            try (Formatter formatter = new Formatter())
            {
                for (final byte b : messageDigest.digest())
                {
                    formatter.format("%02x", b);
                }

                return formatter.toString();
            }
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        return null;
    }

    public static File getUpdateFolder()
    {
        return _updateFolder;
    }

    public static void grabDownload(String url, File dest)
    {
        try
        {
            URL website = new URL(url);

            try (ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    FileOutputStream fos = new FileOutputStream(dest))
            {
                fos.getChannel().transferFrom(rbc, 0, (long) 3e+7);
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }
        }
        catch (Exception ex)
        {
           // UtilError.handle(ex);
        }
    }

    public static boolean isZipFile(File file)
    {
        try
        {
            return file.exists() && !file.isDirectory() && new ZipFile(file).isValidZipFile();
        }
        catch (Exception ex)
        {
        }

        return false;
    }

    public static void moveFile(File source, File dest)
    {
        System.out.println("Moving '" + source.getAbsolutePath() + "' to '" + dest.getAbsolutePath() + "'");

        try
        {
            Files.walkFileTree(source.toPath(), new SimpleFileVisitor<Path>()
            {
                private FileVisitResult move(Path fileOrDir) throws IOException
                {
                    Path destination = dest.toPath().resolve(source.toPath().relativize(fileOrDir));

                    File file = destination.toFile().getAbsoluteFile();

                    if (file.exists() && file.isDirectory())
                    {
                        return FileVisitResult.CONTINUE;
                    }

                    if (!file.getParentFile().exists())
                    {
                        file.getParentFile().mkdirs();
                    }

                    Files.move(fileOrDir, destination, StandardCopyOption.REPLACE_EXISTING);

                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
                {
                    return move(dir);
                }

                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    return move(file);
                }
            });
        }
        catch (Exception e)
        {
            UtilError.handle(e);
        }
    }

    public static ZipInputStream readInputFromZip(File zip, String configName)
    {
        try
        {
            ZipFile zipFile = new ZipFile(zip);

            FileHeader header = zipFile.getFileHeader(configName);

            if (header == null)
                return null;

            return zipFile.getInputStream(header);
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        return null;
    }

    public static boolean removeZipFile(File zip, String fileName)
    {
        try
        {
            ZipFile zipFile = new ZipFile(zip);

            zipFile.removeFile(fileName);
            return true;
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        return false;
    }
}
