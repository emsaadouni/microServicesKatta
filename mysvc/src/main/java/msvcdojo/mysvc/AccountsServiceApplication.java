package msvcdojo.mysvc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;


public class AccountsServiceApplication {

    @Entity
    class Account {

        public String getUsername() {
            return username;
        }

        public Long getId() {
            return id;
        }

        @Id
        @GeneratedValue
        private Long id;

        private String username;

        private String role;

        Account() { // JPA only
        }

        public Account(String username, String role) {
            this.username = username;
            this.role = role;
        }
        public String getRole() { return role; }
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("User{");
            sb.append("id=").append(id);
            sb.append(", username='").append(username).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
    @RepositoryRestResource
    interface AccountRepository extends JpaRepository<Account, Long> {
        List<Account> findByUsername(@Param("username") String username);
        List<Account> findByRole(@Param("role") String role);
    }

    @RestController
    class HomeController {

        @Value("${name}")
        private String name;

        @RequestMapping("/")
        String home() {
            return "Hello, " + name + "!";
        }
    }
}
