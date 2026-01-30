package iuh.fit.se.tramcamxuc.modules.payment.repository;

import iuh.fit.se.tramcamxuc.modules.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByOrderCode(Long orderCode);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.status = 'PAID'")
    Long getTotalRevenue();

    @Query("SELECT p.plan.name, COALESCE(SUM(p.amount), 0) " +
            "FROM PaymentTransaction p " +
            "WHERE p.status = 'PAID' " +
            "GROUP BY p.plan.name")
    List<Object[]> getRevenueByPlan();
}