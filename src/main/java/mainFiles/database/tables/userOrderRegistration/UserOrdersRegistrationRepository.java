package mainFiles.database.tables.userOrderRegistration;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository("userOrdersRegistrationRepository")
public interface UserOrdersRegistrationRepository extends CrudRepository<UserOrderRegistration, Integer> {
    List<UserOrderRegistration> findByChatId(Long chatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserOrderRegistration u WHERE u.chatId = :chatId")
    void deleteByChatId(Long chatId);
}
