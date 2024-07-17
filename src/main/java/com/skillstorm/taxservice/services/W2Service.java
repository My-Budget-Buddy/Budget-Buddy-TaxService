package com.skillstorm.taxservice.services;

import com.skillstorm.taxservice.dtos.W2Dto;
import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.exceptions.UnableToReadStreamException;
import com.skillstorm.taxservice.models.W2;
import com.skillstorm.taxservice.repositories.W2Repository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static software.amazon.awssdk.utils.IoUtils.toByteArray;

@Service
@PropertySource("classpath:SystemMessages.properties")
public class W2Service {

    private final W2Repository w2Repository;
    private final S3Service s3Service;
    private final Environment environment;

    @Autowired
    public W2Service(W2Repository w2Repository, S3Service s3Service, Environment environment) {
        this.w2Repository = w2Repository;
        this.s3Service = s3Service;
        this.environment = environment;
    }

    // Add new W2 by UserId and Year:
    public W2Dto addW2(W2Dto newW2) {
        return new W2Dto(w2Repository.saveAndFlush(newW2.mapToEntity()));
    }

    // Find W2 by ID:
    @PostAuthorize("returnObject.userId == #userId")
    public W2Dto findById(int id, int userId) {
        return new W2Dto(w2Repository.findById(id)
                .orElseThrow(() -> new NotFoundException(environment.getProperty("w2.not.found"), id)));
    }

    // Find all W2s by UserId:
    public List<W2Dto> findAllByUserId(int userId) {
        return w2Repository.findAllByUserId(userId).stream().map(W2Dto::new).toList();
    }

    // Find all W2s by UserId and Year:
    public List<W2Dto> findAllByUserIdAndYear(int userId, int year) {
        return w2Repository.findAllByUserIdAndYear(userId, year).stream().map(W2Dto::new).toList();
    }

    // Find all W2s by TaxReturnId:
    @PostAuthorize("returnObject.get(0).userId == #userId")
    public List<W2Dto> findAllByTaxReturnId(int taxReturnId, int userId) {
        return w2Repository.findAllByTaxReturnId(taxReturnId).stream().map(W2Dto::new).toList();
    }

    // Update W2 by ID:
    public W2Dto updateById(int id, W2Dto updatedW2) {
        // Verify W2 exists:
        if(!w2Repository.existsById(id)) {
            throw new NotFoundException(environment.getProperty("w2.not.found"), id);
        }
        updatedW2.setId(id);
        return new W2Dto(w2Repository.saveAndFlush(updatedW2.mapToEntity()));
    }

    // Delete W2 by Id:
    public void deleteById(int id) {
        // Verify W2 exists:
        if(!w2Repository.existsById(id)) {
            throw new NotFoundException(environment.getProperty("w2.not.found"), id);
        }
        w2Repository.deleteById(id);
    }

    // Upload image to S3:
    // TODO: Add content type as S3 meta info instead of using Tika
    public String uploadImage(int id, byte[] image, String contentType, int userId) {
        W2 w2 =findById(id, userId).mapToEntity();
        String imageKey = UUID.nameUUIDFromBytes(image).toString() + "." + contentType.split("/")[1];
        s3Service.uploadFile(imageKey, image);
        w2.setImageKey(imageKey);
        w2Repository.saveAndFlush(w2);
        return imageKey;
    }

    // Download image from S3:
    @SneakyThrows
    public Resource downloadImage(int id, int userId) {
        W2 w2 = findById(id, userId).mapToEntity();
        InputStream inputStream = s3Service.getObject(w2.getImageKey());
        byte[] byteArray;
        try {
            byteArray = toByteArray(inputStream);
        } catch (IOException e) {
            throw new UnableToReadStreamException(environment.getProperty("stream.read.unable"));
        }

        return new ByteArrayResource(byteArray);
    }
}
