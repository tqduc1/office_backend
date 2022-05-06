package com.cmcglobal.backend.entity.immutable;

import lombok.*;
import org.springframework.data.annotation.Immutable;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dots_management_report")
@Entity
@Immutable
public class DotsManagementReport{
    @EmbeddedId
    private ReportCompositeKey id;

    @Column(name = "owner", insertable = false, updatable = false)
    private String owner;

    @Column(name = "floor_id", insertable = false, updatable = false)
    private Integer floorId;

    @Column(name = "allocated_dots", insertable = false, updatable = false)
    @PositiveOrZero
    private Integer numberOfAllocatedDots;

    @Column(name = "occupied_dots", insertable = false, updatable = false)
    @PositiveOrZero
    private Integer numberOfOccupiedDots;
}
