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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

    // Add batch of W2s:
    public List<W2Dto> addListW2s(List<W2Dto> newW2s) {
        return w2Repository.saveAll(newW2s.stream().map(W2Dto::mapToEntity).toList()).stream().map(W2Dto::new).toList();
    }

    // Find W2 by ID:
    @PostAuthorize("returnObject.userId == #userId")
    public W2Dto findById(int id, int userId) {
        return new W2Dto(w2Repository.findById(id)
                .orElseThrow(() -> new NotFoundException(environment.getProperty("w2.not.found") + " " + id)));
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
            throw new NotFoundException(environment.getProperty("w2.not.found") + " " + id);
        }
        updatedW2.setId(id);
        return new W2Dto(w2Repository.saveAndFlush(updatedW2.mapToEntity()));
    }

    // Update all W2s by TaxReturnId:
    @Transactional
    public List<W2Dto> updateAllByTaxReturnId(int taxReturnId, List<W2Dto> updatedW2s) {

        // We need to account for the fact that some previously submitted W2s may have been removed:
        w2Repository.deleteAllByTaxReturnId(taxReturnId);

        // Side effect: Our updated W2s will have new IDs. Unavoidable without a more complex solution.
        return w2Repository.saveAll(updatedW2s.stream().map(W2Dto::mapToEntity).toList()).stream().map(W2Dto::new).toList();
    }

    // Delete W2 by Id:
    public void deleteById(int id) {
        // Verify W2 exists:
        if(!w2Repository.existsById(id)) {
            throw new NotFoundException(environment.getProperty("w2.not.found") + " " + id);
        }
        w2Repository.deleteById(id);
    }

    // Upload image to S3:
    public String uploadImage(int id, byte[] image, String contentType, int userId) {
        W2 w2 =findById(id, userId).mapToEntity();
        String imageKey = defineKey(w2, contentType);
        s3Service.uploadFile(imageKey, image);
        w2.setImageKey(imageKey);
        w2Repository.saveAndFlush(w2);
        return imageKey;
    }

    // Utility method to build an image key based on the W2.
    // May need to update the key format in order to store image prior to
    // saving the W2 entity because the W2 entity may not have an ID yet:
    private String defineKey(W2 w2, String contentType) {
        StringBuilder sb = new StringBuilder();
        sb.append("w2s/");
        sb.append(w2.getUserId());
        sb.append("/");
        sb.append(w2.getId());
        sb.append(".");
        sb.append(contentType.split("/")[1]);

        return sb.toString();
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
            throw new UnableToReadStreamException("stream.read.unable");
        }

        return new ByteArrayResource(byteArray);
    }
}
