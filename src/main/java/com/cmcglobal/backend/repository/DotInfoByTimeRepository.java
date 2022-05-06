package com.cmcglobal.backend.repository;

import com.cmcglobal.backend.constant.Constant;
import com.cmcglobal.backend.entity.Dot;
import com.cmcglobal.backend.entity.DotInfoByTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Transactional
public interface DotInfoByTimeRepository extends JpaRepository<DotInfoByTime, Integer> {
    @Modifying
    @Query(value = "UPDATE dot_info_by_time SET owner = :username, member = :username, status = :status, from_date = :fromDate, to_date = :toDate WHERE dot_id IN (:dotIds)", nativeQuery = true)
    void updateOwnerUserAndStatus(@Param("username") String owner, @Param("status") String status, @Param("dotIds") List<Integer> dotIds, @Param("fromDate") String fromDate, @Param("toDate") String toDate);

    @Modifying
    @Query(value = "DELETE FROM dot_info_by_time WHERE id IN (:dotInfoIds)", nativeQuery = true)
    void deleteDotInfoByTimeByListId(@Param("dotInfoIds") List<Integer> dotInfoIds);

    @Modifying
    @Query(value = "UPDATE dot_info_by_time SET status = 'allocated', member = owner WHERE id IN (:dotInfoIds)", nativeQuery = true)
    void updateDotInfoByTimeByListId(@Param("dotInfoIds") List<Integer> dotInfoIds);

    @Modifying
    @Query(value = "UPDATE dot_info_by_time SET member = :member, status = :status WHERE id = :id", nativeQuery = true)
    void updateUserAndStatus(@Param("member") String member, @Param("status") String status, @Param("id") Integer id);

    @Modifying
    @Query(value = "SELECT * FROM dot_info_by_time" +
            " WHERE (dot_id = :dotId)" +
            " AND ((from_date <= :fromDate AND to_date >= :fromDate) OR (from_date <= :toDate AND to_date >= :toDate) OR (from_date >= :fromDate AND to_date <= :toDate))", nativeQuery = true)
    List<DotInfoByTime> findAllByDotIdInTimeRange(@Param("fromDate") String fromDate, @Param("toDate") String toDate, @Param("dotId") Integer dotId);

    @Modifying
    @Query(value = "SELECT * FROM dot_info_by_time" +
            " WHERE (dot_id = :dotId)" +
            " AND (:date IS NULL OR (dot_info_by_time.from_date <= :date AND dot_info_by_time.to_date >= :date))", nativeQuery = true)
    List<DotInfoByTime> findAllByDotIdByDate(@Param("date") String date, @Param("dotId") Integer dotId);

    @Modifying
    @Query(value = "SELECT * FROM dot_info_by_time" +
            " WHERE (member IN (:usernames))" +
            " AND status = :status" +
            " AND (:date IS NULL OR (dot_info_by_time.from_date <= :date AND dot_info_by_time.to_date >= :date))", nativeQuery = true)
    List<DotInfoByTime> findAllByMemberInAndStatusIsAndByDate(List<String> usernames, String status, String date);

    @Modifying
    @Query(value = "SELECT * FROM dot_info_by_time" +
            " WHERE (dot_id IN (:listDotIds)) " +
            " AND ((from_date <= :fromDate AND to_date >= :fromDate) OR (from_date <= :toDate AND to_date >= :toDate) OR (from_date >= :fromDate AND to_date <= :toDate))", nativeQuery = true)
    List<DotInfoByTime> findAllByDotsAndTimeRange(List<Integer> listDotIds, String fromDate, String toDate);

    DotInfoByTime findByFromDateIsAndToDateIs(String fromDate, String toDate);

    DotInfoByTime findByDot(Dot dot);

    DotInfoByTime findByDotAndFromDateAndToDate(Dot dot, LocalDate fromDate, LocalDate toDate);

    DotInfoByTime findByDotAndStatusAndFromDateAndToDate(Dot dot, String status, LocalDate fromDate, LocalDate toDate);

    boolean existsByMemberAndFromDateAndToDateAndIdIsNot(String member, LocalDate fromDate, LocalDate toDate, Integer id);

    boolean existsByMemberAndFromDateAndToDateAndStatusIs(String member, LocalDate fromDate, LocalDate toDate, String status);
}
