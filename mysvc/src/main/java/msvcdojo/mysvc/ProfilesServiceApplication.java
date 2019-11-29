package msvcdojo.mysvc;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.AbstractResource;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.Id;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class ProfilesServiceApplication {
    @Document(collection = "profiles")
    class Profile {
        public Profile() {
        }

        @Id
        private String id;
        @Indexed
        private String fullName;
        private List<String> photos;

        public void setId(String id) {
            this.id = id;
        }

        public void setKey(String key) {
            this.id = key;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public void addPhotoReference(String photoId) {
            this.photosList().add(photoId);
        }

        public String getKey() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public Integer getPhotoCount() {
            return this.photosList().size();
        }

        public List<String> photosList() {
            if (this.photos == null)
                this.photos = new ArrayList<>();
            return this.photos;
        }

        @Override
        public String toString() {
            return String.format(
                    "Customer[id=%s, fullName='%s']",
                    id, fullName);
        }
    }

    class PhotoResource extends AbstractResource {

        private final Photo photo;

        public PhotoResource(Photo photo) {
            Assert.notNull(photo, "Photo must not be null");
            this.photo = photo;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.photo.getInputStream();
        }

        @Override
        public long contentLength() throws IOException {
            return -1;
        }
    }

    interface Photo {
        public InputStream getInputStream() throws IOException;

    }

    @RepositoryRestResource
    interface ProfilesRepository extends MongoRepository<Profile, String> {

        @Query("{ '_id' : ?0 }")
        Profile findByKey(@Param("key") String key);

        Profile findByFullName(@Param("address") String address);

    }

    @RestController
    @RequestMapping(value = "/profiles/{key}/photos", produces = MediaType.APPLICATION_JSON_VALUE)
    class ProfilePhotoController {
        @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
        ResponseEntity<Resource<Profile>> insertPhoto(@PathVariable String key,
                                                      @RequestParam MultipartFile file) throws IOException{

            Photo photo = file::getInputStream;
            GridFS fs = new GridFS(null);
            ProfilesRepository profilesRepository = null;
            Profile profile = profilesRepository.findOne(key);
            String id = key + profile.getPhotoCount();
            try (InputStream inputStream = photo.getInputStream()) {
                fs.createFile(inputStream);
            }
            profile.addPhotoReference(id);
            profilesRepository.save(profile);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}/photo").buildAndExpand(id).toUri();
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(uri);
            return new ResponseEntity<>( headers, HttpStatus.CREATED);
        }

    }
    @Bean
    CommandLineRunner init(ProfilesRepository profilesRepository) {
        return a -> profilesRepository.deleteAll();
    }
}
