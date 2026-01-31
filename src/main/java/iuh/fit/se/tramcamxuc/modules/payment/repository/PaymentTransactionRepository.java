package iuh.fit.se.tramcamxuc.modules.payment.repository;

import iuh.fit.se.tramcamxuc.modules.payment.entity.PaymentTransaction;
import iuh.fit.se.tramcamxuc.modules.payment.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
            "GROUP BY p.plan.name " +
            "ORDER BY SUM(p.amount) DESC")
    List<Object[]> getRevenueByPlan();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p " +
            "WHERE p.status = 'PAID' AND p.createdAt >= :startDate AND p.createdAt <= :endDate")
    Long getRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Long countByStatus(PaymentStatus status);
}