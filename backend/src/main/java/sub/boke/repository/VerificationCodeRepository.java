package sub.boke.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sub.boke.domain.VerificationCode;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findTopByEmailAndTypeAndIsUsedFalseOrderByCreatedAtDesc(
            String email, VerificationCode.CodeType type);

    @Modifying
    @Query("UPDATE VerificationCode v SET v.isUsed = true WHERE v.email = :email AND v.type = :type")
    void invalidatePreviousCodes(@Param("email") String email, @Param("type") VerificationCode.CodeType type);
}