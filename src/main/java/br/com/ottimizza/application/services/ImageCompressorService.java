package br.com.ottimizza.application.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import br.com.ottimizza.application.utils.ImageUtilities;

@Service
public class ImageCompressorService {

    public Resource compress(MultipartFile image, int size, boolean removeTransparency, boolean higherQuality) throws Exception {
        ImageUtilities imageUtilities = new ImageUtilities();

        String filename = StringUtils.cleanPath(image.getOriginalFilename());
        String extension = filename.substring(filename.lastIndexOf(".") + 1);

        InputStream imageInputStream = image.getInputStream();

        File imageTemporaryFile = File.createTempFile(UUID.randomUUID().toString(), "." + extension);
        Path imageTemporaryPath = Paths.get(imageTemporaryFile.getAbsolutePath());

        write(imageTemporaryPath, imageInputStream);

        BufferedImage bi = imageUtilities.compress(imageTemporaryFile, size, removeTransparency, higherQuality);

        imageUtilities.writeFile(imageTemporaryFile, bi);

        return loadFileAsResource(imageTemporaryFile);
    }

    private void write(Path path, InputStream is) throws IOException {
        Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
    }

    public Resource loadFileAsResource(File file) throws Exception {
        try {
            Path filePath = Paths.get(file.getAbsolutePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new Exception("File not found " + file.getName());
            }
        } catch (MalformedURLException ex) {
            throw new Exception("File not found " + file.getAbsolutePath(), ex);
        }
    }

}
