package com.couturix.user;

import com.couturix.domain.enums.RoleCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT DISTINCT u FROM User u
            JOIN u.roles r
            WHERE u.atelier.id = :atelierId
              AND r.code = :role
            """)
    List<User> findAdminsByAtelierId(@Param("atelierId") Long atelierId, @Param("role") RoleCode role);

    long countDistinctByRoles_Code(RoleCode code);
}
