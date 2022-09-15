package ir.moke.yaja;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiveUtils {
    public static void zipFile(Path targetFile, List<Path> filePaths) throws Exception {
        FileOutputStream fos = new FileOutputStream(targetFile.toFile().getAbsolutePath());
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (Path path : filePaths) {
            File fileToZip = path.toFile();
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
    }
}
