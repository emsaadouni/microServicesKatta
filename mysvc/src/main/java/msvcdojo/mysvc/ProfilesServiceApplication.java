package msvcdojo.mysvc;

import org.springframework.core.io.AbstractResource;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.util.Assert;
import javax.persistence.Id;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
    /**
     * @return a new {@link InputStream} containing photo data as a JPEG. The caller is
     * responsible for closing the stream.
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException;
}
@RepositoryRestResource
interface ProfilesRepository extends MongoRepository<Profile, String> {
    @Query("{ '_id' : ?0 }")
    Profile findByKey(@Param("key") String key);

    Profile findByFullName(@Param("address") String address);

}