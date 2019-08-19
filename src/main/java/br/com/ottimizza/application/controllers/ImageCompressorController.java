package br.com.ottimizza.application.controllers;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.ottimizza.application.services.ImageCompressorService;

@RestController
@RequestMapping(value = "/api/v1/image_compressor")
public class ImageCompressorController {

    @Inject
    private ImageCompressorService imageCompressorService;

    @PostMapping
    public HttpEntity<?> compress(@RequestParam(value = "size", defaultValue = "800") int size,
            @RequestParam(value = "remove_transparency", defaultValue = "true") boolean removeTransparency,
            @RequestParam(value = "higher_quality", defaultValue = "true") boolean higherQuality,
            @RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        Resource resource = imageCompressorService.compress(file, size, removeTransparency, higherQuality);

        String contentDisposition = getContentDisposition(resource, "attachment");
        String contentType = getContentType(resource, request);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(resource);
    }

    //
    public String getContentType(Resource resource, HttpServletRequest request) throws Exception {
        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        return (contentType == null) ? "application/octet-stream" : contentType;
    }

    public String getContentDisposition(Resource resource, String contentDisposition) throws Exception {
        return String.format("%s;filename=\"%s\"", contentDisposition, resource.getFilename());
    }

    public String getContentDisposition(Resource resource) throws Exception {
        return getContentDisposition(resource, "inline");
    }

}
