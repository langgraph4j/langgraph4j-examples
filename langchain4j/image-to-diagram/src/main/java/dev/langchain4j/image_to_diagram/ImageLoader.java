package dev.langchain4j.image_to_diagram;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;

import static java.util.Objects.requireNonNull;

/**
 * The `ImageLoader` class provides utility methods for loading and converting images to Base64 format.
 */
public interface ImageLoader {

    static ImageContent loadImageContentFromResource(String imagePath, String mimeType) throws IOException {
        requireNonNull( imagePath, "imagePath cannot be null");
        requireNonNull( mimeType, "mimeType cannot be null");

        try (var inputStream = ImageLoader.class.getResourceAsStream(imagePath)) {
            if (inputStream == null) {
                throw new IOException("image not found: %s".formatted(imagePath));
            }

            byte[] imageBytes = inputStream.readAllBytes();
            var base64String = Base64.getEncoder().encodeToString(imageBytes);

            final var img = Image.builder()
                    .base64Data( base64String )
                    //.url( imagePath )
                    .mimeType(mimeType)
                    .build();
            return ImageContent.from(img, ImageContent.DetailLevel.AUTO);

        }
    }

    static ImageContent loadImageContentFromURI(URI imageURI, String mimeType)  {
        requireNonNull( imageURI, "imageURI cannot be null");
        requireNonNull( mimeType, "mimeType cannot be null");

        final var img = Image.builder()
                .url( imageURI )
                .mimeType(mimeType)
                .build();
        return ImageContent.from(img, ImageContent.DetailLevel.AUTO);

    }



}
