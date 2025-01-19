package mainFiles.database.tables.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("usersRepository")
public interface UsersRepository extends CrudRepository<User, Long> {
    @Query(value = "SELECT COUNT(*) FROM users_data WHERE user_name = :userName", nativeQuery = true)
    Integer countByUserName(@Param("userName") String userName);

    default boolean existsByUserName(String userName) {
        return countByUserName(userName) > 0;
    }

    Optional<User> findByUserName(String userName);
}

