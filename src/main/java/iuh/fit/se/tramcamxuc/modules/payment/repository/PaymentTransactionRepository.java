package iuh.fit.se.tramcamxuc.modules.payment.repository;

import iuh.fit.se.tramcamxuc.modules.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByOrderCode(Long orderCode);
}