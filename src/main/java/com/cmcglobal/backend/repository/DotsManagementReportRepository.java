package com.cmcglobal.backend.repository;

import com.cmcglobal.backend.entity.immutable.DotsManagementReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface DotsManagementReportRepository extends JpaRepository<DotsManagementReport, Integer> {

    @Modifying
    @Transactional
    @Query(value = "CREATE OR REPLACE VIEW dots_management_report AS" +
            " SELECT owner, floor_id,\n" +
            " count(case when status = 'allocated' then 1 else null end) AS allocated_dots,\n" +
            " count(case when status = 'occupied' then 1 else null end) AS occupied_dots FROM office.dot" +
            " WHERE (from_date <= :exportDate AND to_date >= :exportDate)" +
            " GROUP BY owner, floor_id", nativeQuery = true)
    void updateExportDateDotsManagementReport(@Param("exportDate") String exportDate);

    @Query(value = "SELECT * from dots_management_report" +
            " WHERE (:#{#floorIds.size()} < 1 OR floor_id IN (:floorIds))" +
            " AND (:#{#userIdsInGroup.size()} < 1 OR owner IN (:userIdsInGroup))", nativeQuery = true)
    Page<DotsManagementReport> displayDotsManagementReport(@Param("floorIds") List<Integer> floorIds, @Param("userIdsInGroup") List<String> userIdsInGroup, Pageable pageable);

    @Query(value = "SELECT * from dots_management_report" +
            " WHERE (:#{#floorIds.size()} < 1 OR floor_id IN (:floorIds))" +
            " AND (:#{#userIdsInGroup.size()} < 1 OR owner IN (:userIdsInGroup))", nativeQuery = true)
    List<DotsManagementReport> exportDotsManagementReport(@Param("floorIds") List<Integer> floorIds, @Param("userIdsInGroup") List<String> userIdsInGroup);

}
